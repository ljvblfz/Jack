/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.backend.dex;

import com.android.jack.Options;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.OutputVDir;

import javax.annotation.Nonnull;

/**
 * Write dex into a file.
 */
@HasKeyId
@Description("Write dex into a file")
@Name("DexFileWriter")
@Constraint(need = {DexFileMarker.Complete.class})
@Produce(DexFileProduct.class)
public class DexFileWriter extends DexWriter implements RunnableSchedulable<JSession> {

  /**
   * File name prefix of a {@code .dex} file automatically loaded in an
   * archive.
   */
  static final String DEX_PREFIX = "classes";

  @Nonnull
  public static final String DEX_FILENAME = DEX_PREFIX + DEX_FILE_EXTENSION;

  @Nonnull
  public static final BooleanPropertyId MULTIDEX = BooleanPropertyId.create(
      "jack.dex.output.multidex", "Enable MultiDex output")
      .addDefaultValue(false);

  @Nonnull
  public static final BooleanPropertyId MINIMAL_MAIN_DEX = BooleanPropertyId.create(
      "jack.dex.output.multidex.minimalmaindex",
      "Keep main dex file as small as possible in MultiDex mode").addDefaultValue(false);

  @Nonnull
  private final OutputVDir outputVDir;

  {
    assert ThreadConfig.get(Options.GENERATE_DEX_FILE).booleanValue();
    Container container = ThreadConfig.get(Options.DEX_OUTPUT_CONTAINER_TYPE);
    if (container == Container.DIR) {
      outputVDir = ThreadConfig.get(Options.DEX_OUTPUT_DIR);
    } else {
      outputVDir = ThreadConfig.get(Options.DEX_OUTPUT_ZIP);
    }
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {

    DexWritingTool writingTool;

    if (emitOneDexPerType) {
      writingTool = new MergingDexWritingTool(outputVDir);
    } else {
      writingTool = new SimpleDexWritingTool(outputVDir);
    }
    writingTool.write();
  }

}
