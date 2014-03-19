/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.transformations;

import com.android.jack.ir.ast.JProgram;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A schedulable that is only useful as a separation between {@code AssertionTransformer} and
 * {@code FieldInitializer}, so that they don't run in the same "type" subplan.
 */
@Description("A separation between AssertionTransformer and FieldInitializer")
@Transform(remove = AssertionTransformerSchedulingSeparator.SeparatorTag.class)
@Constraint(need = AssertionTransformerSchedulingSeparator.SeparatorTag.class)
public class AssertionTransformerSchedulingSeparator implements RunnableSchedulable<JProgram> {

  @Override
  public void run(@Nonnull JProgram program) throws Exception {
    // do nothing
  }

  /**
   * The tag that is used by {@code AssertionTransformer} and {@code FieldInitializer} to
   * express the need for a separation.
   */
  @Description("Allows to express the need for a separation")
  public static class SeparatorTag implements Tag {
  }

}
