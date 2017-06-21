/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.test.runner;

import com.google.common.base.Joiner;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.FileListingService;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.jack.test.TestConfigurationException;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This runner is used to execute tests on a device.
 */
public abstract class DeviceRunner extends AbstractRuntimeRunner {

  @Nonnegative
  private static final long ADB_CONNECTION_TIMEOUT = 5000;
  @Nonnegative
  private static final long ADB_WAIT_STEP = ADB_CONNECTION_TIMEOUT / 10;

  @Nonnegative
  private static final int MAX_NB_CLASSES = 10;

  @Nonnull
  private static final String TEST_SCRIPT_NAME = "test-exit-status.sh";
  @Nonnull
  private static final File TEST_SCRIPT_FILE =
      new File(TestsProperties.getJackRootDir(), "jack-tests/etc/" + TEST_SCRIPT_NAME);

  @Nonnull
  protected static final char PATH_SEPARATOR_CHAR = ':';

  @Nonnull
  MyShellOuputReceiver hostOutput = new MyShellOuputReceiver();

  private static class MyShellOuputReceiver implements IShellOutputReceiver {

    @Nonnull
    private final PrintStream out;
    @Nonnull
    private final PrintStream err;

    public MyShellOuputReceiver() {
      this.out = System.out;
      this.err = System.err;
    }

    public MyShellOuputReceiver(@Nonnull OutputStream out, @Nonnull OutputStream err) {
      this.out = new PrintStream(out);
      this.err = new PrintStream(err);
    }

    @Override
    public void addOutput(@Nonnull byte[] data, int offset, int length) {
      out.print(new String(Arrays.copyOfRange(data, offset, offset + length)));
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isCancelled() {
      return false;
    }
  }

  public DeviceRunner() {
    try {
      AndroidDebugBridge.init(/* clientSupport */ false);
    } catch (IllegalStateException ex) {
      // ADB was already initialized, we're fine, so just ignore.
    }
  }

  private static class ShellOutputToStringReceiver implements IShellOutputReceiver {

    @Nonnull
    StringBuffer outBuffer = new StringBuffer();

    @Override
    public void addOutput(@Nonnull byte[] data, int offset, int length) {
      outBuffer.append(new String(Arrays.copyOfRange(data, offset, offset + length)));
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Nonnull
    public String getOutput() {
      return outBuffer.toString();
    }
  }

  protected int runOnDevice(@Nonnull String[] options, @CheckForNull String jUnitRunnerName,
      @Nonnull String[] classes, @Nonnull File... classpathFiles)
      throws RuntimeRunnerException {

    AndroidDebugBridge adb;

    adb = AndroidDebugBridge.createBridge(getAdbLocation(), false);

    long start = System.currentTimeMillis();

    if (isVerbose) {
      System.out.println("Initializing adb...");
    }

    while (!isAdbInitialized(adb)) {
      long timeLeft = start + ADB_CONNECTION_TIMEOUT - System.currentTimeMillis();
      if (timeLeft <= 0) {
        break;
      }
      try {
        Thread.sleep(ADB_WAIT_STEP);
      } catch (InterruptedException e) {
        throw new RuntimeRunnerException(e);
      }
    }

    if (!isAdbInitialized(adb)) {
      if (AbstractTestTools.getAndroidSdkLocation() != null) {
        throw new RuntimeRunnerException(
            "Adb not found. Check SDK location '"
                + AbstractTestTools.getAndroidSdkLocation()
                + "'");
      } else {
        throw new RuntimeRunnerException(
            "Adb not found. Set either PATH or 'android.sdk' property in configuration file");
      }
    }

    if (isVerbose) {
      System.out.println("Done");
    }

    IDevice[] connectedDevices = adb.getDevices();

    if (connectedDevices.length == 0) {
      throw new RuntimeRunnerException("No device found");
    }

    int exitStatus = -1;
    List<String> failingDevices = new ArrayList<>(0);
    for (IDevice device : connectedDevices) {

      try {

        checkDeviceRuntime(device);

        if (isVerbose) {
          System.out.println("Running on device: " + device.getName());
        }

        ensureAdbRoot(device);

        String uuid = java.util.UUID.randomUUID().toString();

        //Remove trailing '\n' returned by emulator
        File testsRootDirFile =
            new File(
                device.getMountPoint(IDevice.MNT_DATA).replace("\n", ""), "jack-tests-" + uuid);
        String testsRootDir = convertToTargetPath(testsRootDirFile);

        String testScriptPathOnTarget =
            convertToTargetPath(
                new File(testsRootDirFile, TEST_SCRIPT_NAME));

        String[] desFilePaths = new String[classpathFiles.length];
        try {

          executeShellCommand("mkdir " + testsRootDir, device);

          executeShellCommand(
              "rm " + testsRootDir + FileListingService.FILE_SEPARATOR + "*", device);

          executePushCommand(TEST_SCRIPT_FILE.getAbsolutePath(), testScriptPathOnTarget, device);
          executeShellCommand("chmod 777 " + testScriptPathOnTarget, device);

          int i = 0;
          for (File f : classpathFiles) {
            desFilePaths[i] =
                convertToTargetPath(new File(testsRootDirFile,  "f" + i + "_" + f.getName()));

            executePushCommand(f.getAbsolutePath(), desFilePaths[i], device);

            i++;
          }
        } catch (TimeoutException
            | AdbCommandRejectedException
            | ShellCommandUnresponsiveException
            | IOException
            | SyncException e) {
          deleteTestFiles(device, testsRootDirFile);
          throw new RuntimeRunnerException(e);
        }

        // Split command line to have at most MAX_NB_CLASSES jUnit classes per invocation
        List<List<String>> splittedMainClasses = new ArrayList<List<String>>();
        int currentChunk = 0;
        for (String classToRun : classes) {
          if (splittedMainClasses.size() == currentChunk) {
            splittedMainClasses.add(new ArrayList<String>(MAX_NB_CLASSES));
            if (jUnitRunnerName != null) {
              splittedMainClasses.get(currentChunk).add(jUnitRunnerName);
            }
          }

          splittedMainClasses.get(currentChunk).add(classToRun);

          if (splittedMainClasses.get(currentChunk).size() == MAX_NB_CLASSES) {
            currentChunk++;
          }
        }
        List<String> cmdLines = new ArrayList<String>(splittedMainClasses.size());
        File rootDir = new File(device.getMountPoint(IDevice.MNT_ROOT).replace("\n", ""));
        for (List<String> classList : splittedMainClasses) {
          cmdLines.add(Joiner.on(' ').join(buildCommandLine(rootDir, options,
              classList.toArray(new String[classList.size()]), desFilePaths)));
        }

        try {
          // Bug : exit code return by adb shell is wrong (always 0)
          // https://code.google.com/p/android/issues/detail?id=3254
          // Use go team hack to work this around
          // https://code.google.com/p/go/source/browse/misc/arm/a

          for (String args : cmdLines) {
            executeShellCommand(
                testScriptPathOnTarget + ' ' + uuid + ' ' + args,
                device,
                new MyShellOuputReceiver(outRedirectStream, errRedirectStream),
                /* maxTimeToOutputResponse = */ 10000);

            File exitStatusFile = AbstractTestTools.createTempFile("exitStatus", "");
            executePullCommand(
                testsRootDir + "/exitStatus", exitStatusFile.getAbsolutePath(), device);

            BufferedReader br = new BufferedReader(new FileReader(exitStatusFile));
            try {
              String readLine = br.readLine();
              if (readLine == null) {
                throw new RuntimeRunnerException("Exit status not found");
              }
              exitStatus = Integer.parseInt(readLine);
            } finally {
              br.close();
            }

            if (isVerbose) {
              System.out.println("Exit status: " + exitStatus);
            }

            if (exitStatus != 0) {
              System.err.println("Execution failed on device '" + device.getName() + "'");
              break;
            }
          }
        } catch (TimeoutException
            | AdbCommandRejectedException
            | ShellCommandUnresponsiveException
            | CannotChangePermissionException
            | CannotCreateFileException
            | IOException
            | SyncException e) {
          throw new RuntimeRunnerException(e);
        } finally {
          deleteTestFiles(device, testsRootDirFile);
        }

      } catch (RuntimeRunnerException e) {
        System.err.println("Error with device '" + device.getName() + "': " + e.getMessage());
        e.printStackTrace();
        failingDevices.add(device.getName());
      }

    }

    if (failingDevices.size() > 0) {
      String device = failingDevices.size() == 1 ? "device" : "devices";
      throw new RuntimeRunnerException(
          "Error with "
              + device
              + ": "
              + Joiner.on(',').join(failingDevices)
              + ". See log for details");
    }

    return exitStatus;
  }

  private void deleteTestFiles(@Nonnegative IDevice device, @Nonnull File testDir)
      throws RuntimeRunnerException {
    String testDirName = testDir.getName();
    try {
      executeShellCommand("rm -rf " + convertToTargetPath(testDir), device);
      executeShellCommand("find /data/dalvik-cache -name '*" + testDirName + "*' -exec rm -rf {} +"
          , device);
    } catch (TimeoutException
        | AdbCommandRejectedException
        | ShellCommandUnresponsiveException
        | IOException e) {
      throw new RuntimeRunnerException(e);
    }
  }

  private void executeShellCommand(
      @Nonnull String command,
      @Nonnegative IDevice device,
      @Nonnull MyShellOuputReceiver hostOutput,
      int maxTimeToOutputResponse)
      throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
          IOException {
    if (isVerbose) {
      System.out.println("adb -s " + device.getSerialNumber() + " shell " + command);
    }
    if (maxTimeToOutputResponse != -1) {
      device.executeShellCommand(command, hostOutput, maxTimeToOutputResponse);
    } else {
      device.executeShellCommand(command, hostOutput);
    }
  }

  private void executeShellCommand(@Nonnull String command, @Nonnegative IDevice device)
      throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
          IOException {
    executeShellCommand(command, device, hostOutput, -1);
  }

  private void executePushCommand(
      @Nonnull String srcFile, @Nonnull String destFile, @Nonnegative IDevice device)
      throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
    if (isVerbose) {
      System.out.println(
          "adb -s " + device.getSerialNumber() + " push " + srcFile + " " + destFile);
    }
    device.pushFile(srcFile, destFile);
  }

  private void executePullCommand(
      @Nonnull String srcFile, @Nonnull String destFile, @Nonnegative IDevice device)
      throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
    if (isVerbose) {
      System.out.println(
          "adb -s " + device.getSerialNumber() + " pull " + srcFile + " " + destFile);
    }
    device.pullFile(srcFile, destFile);
  }


  @Nonnull
  protected abstract List<String> buildCommandLine(@Nonnull File rootDir, @Nonnull String[] options,
      @Nonnull String[] mainClasses, @Nonnull String... classpathFiles);

  private boolean isAdbInitialized(@Nonnull AndroidDebugBridge adb) {
    return adb.isConnected() && adb.hasInitialDeviceList();
  }

  @Nonnull
  private String getAdbLocation() {
    String adbLocation = "adb";
    File userSpecifiedSdkLocation = AbstractTestTools.getAndroidSdkLocation();
    if (userSpecifiedSdkLocation != null) {
      adbLocation =
          userSpecifiedSdkLocation.getPath()
              + File.separatorChar
              + "platform-tools"
              + File.separatorChar
              + "adb";
    }
    return adbLocation;
  }


  private void ensureAdbRoot(@Nonnull IDevice device) throws RuntimeRunnerException {
    boolean isRoot = false;

    try {

      isRoot = device.isRoot();

    } catch (TimeoutException
        | AdbCommandRejectedException
        | IOException
        | ShellCommandUnresponsiveException e) {

      throw new RuntimeRunnerException(
          "Cannot fetch root status for device '"
              + device.getName()
              + "("
              + device.getSerialNumber()
              + ")"
              + "': "
              + e.getMessage(),
          e);

    }

    int nbTry = 0;
    while (!isRoot && nbTry < 3) {
      try {

        isRoot = device.root();

      } catch (TimeoutException
          | AdbCommandRejectedException
          | IOException
          | ShellCommandUnresponsiveException e1) {
        // root() seems to throw an IOException: EOF, and it tends
        // to make the subsequent call to isRoot() fail with
        // AdbCommandRejectedException: device offline, until adbd is
        // restarted as root.
      } finally {

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          Thread.currentThread().interrupt();
        }

      }

      nbTry++;

    }

    if (!isRoot) {
      throw new RuntimeRunnerException(
          "Cannot switch to root on device '"
              + device.getName()
              + "("
              + device.getSerialNumber()
              + ")"
              + "'");
    }

  }

  @Nonnull
  protected abstract String getRuntimeName();

  private void checkDeviceRuntime(@Nonnull IDevice device) throws RuntimeRunnerException {
    ShellOutputToStringReceiver outputToString = new ShellOutputToStringReceiver();
    try {
      device.executeShellCommand("dalvikvm -showversion", outputToString);
      if (!outputToString.getOutput().contains(getRuntimeName())) {
        throw new TestConfigurationException(
            "The plugged device does not run the required runtime: '" + getRuntimeName() + "'");
      }
    } catch (TimeoutException e) {
      throw new RuntimeRunnerException(e);
    } catch (AdbCommandRejectedException e) {
      throw new RuntimeRunnerException(e);
    } catch (ShellCommandUnresponsiveException e) {
      throw new RuntimeRunnerException(e);
    } catch (IOException e) {
      throw new RuntimeRunnerException(e);
    }
  }

  @Nonnull
  protected String convertToTargetPath(@Nonnull File file) {
    String path = file.getPath();
    Path root = file.toPath().getRoot();
    if (root != null) {
      path = path.replace(root.toString(), FileListingService.FILE_SEPARATOR);
    }
    return path.replace(File.separator, FileListingService.FILE_SEPARATOR);
  }

}
