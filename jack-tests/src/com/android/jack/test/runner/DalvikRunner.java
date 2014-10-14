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

import javax.annotation.Nonnull;


/**
 * This interface defines options that can be set to a Dalvik runner.
 */
public interface DalvikRunner {

  /**
   * This enum defines various mode Dalvik can be run into.
   */
  enum DalvikMode {
    JIT("-Xint:jit"),
    FAST("-Xint:fast");

    @Nonnull
    String arg;

    DalvikMode(@Nonnull String arg) {
      this.arg = arg;
    }

    @Nonnull
    String getArg() {
      return arg;
    }
  }

  @Nonnull
  DalvikRunner setMode(@Nonnull DalvikMode mode);
}
