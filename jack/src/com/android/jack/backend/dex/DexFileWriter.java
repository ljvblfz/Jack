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

import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.OutputVFS;

import javax.annotation.Nonnull;

/**
 * Write dex to a file.
 */
@HasKeyId
@Description("Write dex into a file")
@Name("DexFileWriter")
@Constraint(need = {ClassDefItemMarker.Complete.class})
@Produce(DexFileProduct.class)
public class DexFileWriter extends DexWriter implements RunnableSchedulable<JSession> {

  /**
   * File name prefix of a {@code .dex} file automatically loaded in an archive.
   */
  static final String DEX_PREFIX = "classes";

  @Nonnull
  public static final String DEX_FILENAME = DEX_PREFIX + FileType.DEX.getFileExtension();

  @Nonnull
  public static final ImplementationPropertyId<DexWritingTool> DEX_WRITING_POLICY =
      ImplementationPropertyId.create("jack.dex.output.policy",
          "Define which policy will be used to emit dex files", DexWritingTool.class)
          .addDefaultValue("single-dex");

  @Nonnull
  private final OutputVFS outputVDir;

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
  public void run(@Nonnull JSession session) {

    DexWritingTool writingTool = ThreadConfig.get(DEX_WRITING_POLICY);
    try {
      writingTool.write(outputVDir);
    } catch (DexWritingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }
  }

}
