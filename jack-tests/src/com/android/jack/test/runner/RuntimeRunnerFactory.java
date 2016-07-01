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

import com.android.jack.test.runner.DalvikRunner.DalvikMode;
import com.android.jack.test.toolchain.AbstractTestTools;

import javax.annotation.Nonnull;

/**
 * A factory to build {@link RuntimeRunner}s.
 */
public class RuntimeRunnerFactory {

  private static final char SEPARATOR = '-';

  /**
   * Runtime names are composed as follows:
   * <runtime environment name>-<variant>-<kind>
   * Where runtime environment name is one of Dalvik, ART, ..., variant defines a variant
   * for the selected environment (e.g. jit or fast for dalvik) and finally target is either
   * host or device.
   */
  @Nonnull
  public static RuntimeRunner create(@Nonnull String rtName) throws RuntimeRunnerException {

    int firstIndex = rtName.indexOf(SEPARATOR);
    int lastIndex = rtName.lastIndexOf(SEPARATOR);

    String rtEnvName = null;
    String variant = null;
    String runnerKind = null;

    if (firstIndex > -1) {
      rtEnvName = rtName.substring(0, firstIndex);
      runnerKind = rtName.substring(lastIndex + 1);
    }
    if (firstIndex < lastIndex) {
      variant =
          rtName.substring(firstIndex + 1, lastIndex);
    }

    RuntimeRunner result;
    if ("dalvik".equals(rtEnvName)) {
      if ("device".equals(runnerKind)) {
        result = new DalvikRunnerDevice();
      } else if ("host".equals(runnerKind)) {
        result = new DalvikRunnerHost(AbstractTestTools.getRuntimeEnvironmentRootDir(rtName));
      } else {
        throw new RuntimeRunnerException("Unkown target for Dalvik: '" + rtName + "'");
      }
      if ("jit".equals(variant)) {
        ((DalvikRunner) result).setMode(DalvikMode.JIT);
      } else if ("fast".equals(variant)) {
        ((DalvikRunner) result).setMode(DalvikMode.FAST);
      } else if (variant != null) {
        throw new RuntimeRunnerException("Unkown variant for Dalvik: '" + rtName + "'");
      }
    } else if ("art".equals(rtEnvName)) {
      if ("host".equals(runnerKind)) {
        result = new ArtRunnerHost(AbstractTestTools.getRuntimeEnvironmentRootDir(rtName));
        if ("debug".equals(variant)) {
          ((ArtRunnerHost) result).setDebugMode(/* isDebugMode = */ true);
        } else if (variant != null) {
          throw new RuntimeRunnerException("Unkown target for ART: '" + rtName + "'");
        }

      } else if ("device".equals(runnerKind)) {
        if (variant != null) {
          throw new RuntimeRunnerException("Unkown target for ART: '" + rtName + "'");
        }
        result = new ArtRunnerDevice();
      } else {
        throw new RuntimeRunnerException("Unkown target for ART: '" + rtName + "'");
      }
    } else {
      throw new RuntimeRunnerException("Unkown runtime environment for ART: '" + rtName + "'");
    }

    return result;
  }
}
