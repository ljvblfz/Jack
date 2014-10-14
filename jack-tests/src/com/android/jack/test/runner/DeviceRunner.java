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
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.TestConfigurationException;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This runner is used to execute tests on a device.
 */
public abstract class DeviceRunner extends AbstractRuntimeRunner {

  @Nonnull
  public static final File ROOT_DIR = new File("/system");
  @Nonnull
  public static final File ANDROID_DATA_DIR = new File("/data");

  private static final long ADB_CONNECTION_TIMEOUT = 5000;
  private static final long ADB_WAIT_STEP = ADB_CONNECTION_TIMEOUT / 10;

  @Nonnull
  private MyShellOuputReceiver shellOutput = new MyShellOuputReceiver();

  private class MyShellOuputReceiver implements IShellOutputReceiver {

    @Override
    public void addOutput(@Nonnull byte[] data, int offset, int length) {
      outRedirectStream.println(new String(Arrays.copyOfRange(data, offset, offset + length)));
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
    super(ROOT_DIR);
    try {
      AndroidDebugBridge.init(/* clientSupport */ false);
    } catch (IllegalStateException ex) {
      // ADB was already initialized, we're fine, so just ignore.
    }
  }

  private class ShellOutputToStringReceiver implements IShellOutputReceiver {

    @Nonnull
    StringBuffer out = new StringBuffer();

    @Override
    public void addOutput(@Nonnull byte[] data, int offset, int length) {
      out.append(new String(Arrays.copyOfRange(data, offset, offset + length)));
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
      return out.toString();
    }
  }

  protected int runOnDevice(@Nonnull String[] options, @Nonnull String[] mainClasses,
      @Nonnull File... classpathFiles)
      throws RuntimeRunnerException {

    // Assumes adb is in PATH
    AndroidDebugBridge adb = AndroidDebugBridge.createBridge("adb", false);

    long start = System.currentTimeMillis();

    if (isVerbose) {
      outRedirectStream.println("Initializing adb...");
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
      throw new RuntimeRunnerException("adb is not initialized");
    }

    if (isVerbose) {
      outRedirectStream.println("Done");
    }

    IDevice[] connectedDevices = adb.getDevices();

    if (connectedDevices.length == 0) {
      throw new RuntimeRunnerException("No device found");
    }

    int exitStatus = -1;
    for (IDevice device : connectedDevices) {

      checkDeviceRuntime(device);

      if (isVerbose) {
        outRedirectStream.println("Running on device: " + device.getName());
      }

      ensureAdbRoot(device);

      File testsRootDir = new File(device.getMountPoint(IDevice.MNT_DATA) + "/jack-tests");
      File[] desFilePaths = new File[classpathFiles.length];
      try {
        if (isVerbose) {
          outRedirectStream.println("adb shell -s " + device.getSerialNumber() + " mkdir "
              + testsRootDir.getAbsolutePath());
        }
        device.executeShellCommand("mkdir " + testsRootDir.getAbsolutePath(), shellOutput);

        if (isVerbose) {
          outRedirectStream.println("adb -s " + device.getSerialNumber() + " push  "
              + System.getProperty("user.dir") + File.separator + "test-exit-status.sh "
              + testsRootDir.getAbsolutePath() + "/test-exit-status.sh");
        }
        device.pushFile(System.getProperty("user.dir") + File.separator + "test-exit-status.sh",
            testsRootDir.getAbsolutePath() + "/test-exit-status.sh");

        if (isVerbose) {
          outRedirectStream.println("adb -s " + device.getSerialNumber() + " shell chmod 777 "
              + testsRootDir.getAbsolutePath() + "/test-exit-status.sh");
        }
        device.executeShellCommand(
            "chmod 777 " + testsRootDir.getAbsolutePath() + "/test-exit-status.sh", shellOutput);

        int i = 0;
        for (File f : classpathFiles) {
          desFilePaths[i] = new File(testsRootDir, "f" + i + "_"  + f.getName());

          if (isVerbose) {
            outRedirectStream.println("adb -s " + device.getSerialNumber() + " push "
                + f.getAbsolutePath() + " " + desFilePaths[i].getAbsolutePath());
          }
          device.pushFile(f.getAbsolutePath(), desFilePaths[i].getAbsolutePath());
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

      String args = Joiner.on(' ').join(buildCommandLine(options, mainClasses, desFilePaths));

      try {
        // Bug : exit code return by adb shell is wrong (always 0)
        // https://code.google.com/p/android/issues/detail?id=3254
        // Use go team hack to work this around
        // https://code.google.com/p/go/source/browse/misc/arm/a

        if (isVerbose) {
          outRedirectStream.println("adb -s " + device.getSerialNumber() + " shell "
              + testsRootDir.getAbsolutePath() + "/test-exit-status.sh " + args);
        }
        device.executeShellCommand(
            testsRootDir.getAbsolutePath() + "/test-exit-status.sh " + args,
            shellOutput);

        File exitStatusFile = AbstractTestTools.createTempFile("exitStatus", "");
        if (isVerbose) {
          outRedirectStream.println("adb -s " + device.getSerialNumber() + " pull "
              + testsRootDir.getAbsolutePath() + "/exitStatus " + exitStatusFile.getAbsolutePath());
        }
        device.pullFile(testsRootDir.getAbsolutePath() + "/exitStatus",
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
          outRedirectStream.println("Exit status: " + exitStatus);
        }

        for (File pushedFile : desFilePaths) {
          if (isVerbose) {
            outRedirectStream.println(
                "adb -s " + device.getSerialNumber() + "rm " + pushedFile.getAbsolutePath());
          }
          device.executeShellCommand("rm " + pushedFile.getAbsolutePath(), shellOutput);
        }

        if (exitStatus != 0) {
          errRedirectStream.println("Execution failed on device '" + device.getName() + "'");
          break;
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
    }

    return exitStatus;
  }

  @Nonnull
  protected abstract List<String> buildCommandLine(@Nonnull String[] options,
      @Nonnull String[] mainClasses, @Nonnull File... classpathFiles);

  private boolean isAdbInitialized(@Nonnull AndroidDebugBridge adb) {
    return adb.isConnected() && adb.hasInitialDeviceList();
  }

  private void ensureAdbRoot(@Nonnull IDevice device) throws RuntimeRunnerException {
    ShellOutputToStringReceiver outputToString = new ShellOutputToStringReceiver();
    try {
      device.executeShellCommand("id", outputToString);

      if (!outputToString.getOutput().contains("uid=0(root)")) {
        ExecuteFile ef;

        ef = new ExecuteFile("adb -s " + device.getSerialNumber() + " root");
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

}
