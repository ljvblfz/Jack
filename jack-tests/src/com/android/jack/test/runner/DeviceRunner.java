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
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;
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
    for (IDevice device : connectedDevices) {

      checkDeviceRuntime(device);

      if (isVerbose) {
        System.out.println("Running on device: " + device.getName());
      }

      ensureAdbRoot(device);

      //Remove trailing '\n' returned by emulator
      File testsRootDirFile =
          new File(device.getMountPoint(IDevice.MNT_DATA).replace("\n", ""), "jack-tests");
      String testsRootDir = convertToTargetPath(testsRootDirFile);

      String testScriptPathOnTarget =
          convertToTargetPath(new File(testsRootDirFile, "TEST_SCRIPT_NAME"));

      String[] desFilePaths = new String[classpathFiles.length];
      try {
        if (isVerbose) {
          System.out.println("adb shell -s " + device.getSerialNumber() + " mkdir "
              + testsRootDir);
        }
        device.executeShellCommand("mkdir " + testsRootDir, hostOutput);

        if (isVerbose) {
          System.out.println("adb shell -s " + device.getSerialNumber() + " rm "
              + testsRootDir + FileListingService.FILE_SEPARATOR + "*");
        }
        device.executeShellCommand("rm " + testsRootDir + FileListingService.FILE_SEPARATOR + "*",
            hostOutput);

        if (isVerbose) {
          System.out.println("adb -s " + device.getSerialNumber() + " push  "
              + TEST_SCRIPT_FILE.getAbsolutePath() + " "
              + testScriptPathOnTarget);
        }
        device.pushFile(TEST_SCRIPT_FILE.getAbsolutePath(),
            testScriptPathOnTarget);

        if (isVerbose) {
          System.out.println("adb -s " + device.getSerialNumber() + " shell chmod 777 "
              + testScriptPathOnTarget);
        }
        device.executeShellCommand(
            "chmod 777 " + testScriptPathOnTarget, hostOutput);

        int i = 0;
        for (File f : classpathFiles) {
          desFilePaths[i] =
              convertToTargetPath(new File(testsRootDirFile,  "f" + i + "_" + f.getName()));

          if (isVerbose) {
            System.out.println("adb -s " + device.getSerialNumber() + " push "
                + f.getAbsolutePath() + " " + desFilePaths[i]);
          }
          device.pushFile(f.getAbsolutePath(), desFilePaths[i]);
          i++;
        }
      } catch (TimeoutException e) {
        throw new RuntimeRunnerException(e);
      } catch (AdbCommandRejectedException e) {
        throw new RuntimeRunnerException(e);
      } catch (ShellCommandUnresponsiveException e) {
        throw new RuntimeRunnerException(e);
      } catch (IOException e) {
        throw new RuntimeRunnerException(e);
      } catch (SyncException e) {
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
          if (isVerbose) {
            System.out.println("adb -s " + device.getSerialNumber() + " shell "
                + testScriptPathOnTarget + ' ' + args);
          }
          device.executeShellCommand(
              testScriptPathOnTarget + ' ' + args,
              new MyShellOuputReceiver(outRedirectStream, errRedirectStream),
              /* maxTimeToOutputResponse = */ 10000);

          File exitStatusFile = AbstractTestTools.createTempFile("exitStatus", "");
          if (isVerbose) {
            System.out.println("adb -s " + device.getSerialNumber() + " pull "
                + testsRootDir + "/exitStatus "
                + exitStatusFile.getAbsolutePath());
          }
          device.pullFile(testsRootDir + "/exitStatus",
              exitStatusFile.getAbsolutePath());

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
      } catch (TimeoutException e) {
        throw new RuntimeRunnerException(e);
      } catch (AdbCommandRejectedException e) {
        throw new RuntimeRunnerException(e);
      } catch (ShellCommandUnresponsiveException e) {
        throw new RuntimeRunnerException(e);
      } catch (CannotChangePermissionException | CannotCreateFileException | IOException e) {
        throw new RuntimeRunnerException(e);
      } catch (SyncException e) {
        throw new RuntimeRunnerException(e);
      } finally {
        try {
          for (String pushedFile : desFilePaths) {
            if (isVerbose) {
              System.out.println(
                  "adb -s " + device.getSerialNumber() + " rm " + pushedFile);
            }
            device.executeShellCommand("rm " + pushedFile, hostOutput);
          }
        } catch (IOException e) {
          throw new RuntimeRunnerException(e);
        } catch (TimeoutException e) {
          throw new RuntimeRunnerException(e);
        } catch (AdbCommandRejectedException e) {
          throw new RuntimeRunnerException(e);
        } catch (ShellCommandUnresponsiveException e) {
          throw new RuntimeRunnerException(e);
        }
      }
    }

    return exitStatus;
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
    ShellOutputToStringReceiver outputToString = new ShellOutputToStringReceiver();
    try {
      device.executeShellCommand("id", outputToString);

      if (!outputToString.getOutput().contains("uid=0(root)")) {
        ExecuteFile ef;
        ef = new ExecuteFile(getAdbLocation() + " -s " + device.getSerialNumber() + " root");
        ef.inheritEnvironment();
        ef.setOut(System.out);
        ef.setErr(System.err);
        ef.setVerbose(isVerbose);
        ef.run();

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          Thread.currentThread().interrupt();
        }
      }
    } catch (TimeoutException e1) {
      throw new RuntimeRunnerException(e1);
    } catch (AdbCommandRejectedException e1) {
      throw new RuntimeRunnerException(e1);
    } catch (ShellCommandUnresponsiveException e1) {
      throw new RuntimeRunnerException(e1);
    } catch (IOException e1) {
      throw new RuntimeRunnerException(e1);
    } catch (ExecFileException e) {
      throw new RuntimeRunnerException("Error while executing 'adb root'", e);
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
