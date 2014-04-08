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
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This {@code RunnableSchedulable} write compiler state to the disk.
 */
@Description("Write compiler state to the disk")
@Name("CompilerStateWriter")
@Constraint(need = CompilerState.Filled.class)
@Produce(CompilerStateProduct.class)
public class CompilerStateWriter implements RunnableSchedulable<JSession>{

  @Override
  public void run(@Nonnull JSession program) {
    JackIncremental.getCompilerState().write(
        ThreadConfig.get(JackIncremental.COMPILER_STATE_OUTPUT));
  }
}