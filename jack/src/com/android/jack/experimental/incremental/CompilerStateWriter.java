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

package com.android.jack.experimental.incremental;

import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.PathCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * This {@code RunnableSchedulable} write compiler state to the disk.
 */
@Description("Write compiler state to the disk")
@Name("CompilerStateWriter")
@Constraint(need = CompilerStateMarker.class)
@Produce(CompilerStateProduct.class)
@HasKeyId
public class CompilerStateWriter implements RunnableSchedulable<JSession>{

  public static final BooleanPropertyId GENERATE_COMPILER_STATE = BooleanPropertyId.create(
      "jack.experimental.compilerstate.generate", "Generate compiler state").addDefaultValue(
      Boolean.FALSE);

  @Nonnull
  public static final PropertyId<File> COMPILER_STATE_OUTPUT = PropertyId.create(
      "jack.experimental.compilerstate.output", "Compiler state output file", new PathCodec())
      .requiredIf(GENERATE_COMPILER_STATE.getValue().isTrue());

  @Override
  public void run(@Nonnull JSession program) {
    CompilerStateMarker csm = program.getMarker(CompilerStateMarker.class);
    assert csm != null;
    csm.write(ThreadConfig.get(CompilerStateWriter.COMPILER_STATE_OUTPUT));
  }
}