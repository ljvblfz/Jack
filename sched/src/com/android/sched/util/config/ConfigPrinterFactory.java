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

package com.android.sched.util.config;

import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;

import javax.annotation.Nonnull;

/**
 * Factory class to manage {@link ConfigPrinter}
 */
@HasKeyId
public class ConfigPrinterFactory {
  @Nonnull
  public static final
      ImplementationPropertyId<ConfigPrinter> CONFIG_PRINTER = ImplementationPropertyId.create(
          "config.printer", "Define which config printer to use", ConfigPrinter.class)
          .addDefaultValue("none");

  @Nonnull
  public static final PropertyId<OutputStreamFile> CONFIG_PRINTER_FILE = PropertyId.create(
      "config.printer.file", "The file where to print the config",
      new OutputStreamCodec(Existence.MAY_EXIST).allowStandard())
      .addDefaultValue("-");

  @Nonnull
  public static ConfigPrinter getConfigPrinter() {
    return ThreadConfig.get(CONFIG_PRINTER);
  }
}
