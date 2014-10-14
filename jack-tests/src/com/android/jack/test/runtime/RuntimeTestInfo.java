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

package com.android.jack.test.runtime;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * This class hold the information needed by the runtime tests framework to execute
 * a test.
 */
public class RuntimeTestInfo {

  @Nonnull
  public File directory;
  @Nonnull
  public String jUnit;

  public RuntimeTestInfo(@Nonnull File directory, @Nonnull String jUnit) {
    this.directory = directory;
    this.jUnit = jUnit;
  }

}