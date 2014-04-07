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

package com.android.jack.transformations.flow;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A schedulable that is only useful as a separation between {@code FlowNormalizer} and
 * {@code FieldInitMethodCallRemover}, so that they don't run in the same "method" subplan. This
 * separator is needed because {@code FieldInitMethodCallRemover} is cloning the body of the
 * {@code $init} method when running on another method of the class. The separation is ensuring that
 * the FlowNormalizer run of the {@code $init} method before {@code FieldInitMethodCallRemover}
 * tries to clone its body.
 */
@Description("A separation between AssertionTransformer and FieldInitializer")
@Transform(remove = FlowNormalizerSchedulingSeparator.SeparatorTag.class)
@Constraint(need = FlowNormalizerSchedulingSeparator.SeparatorTag.class)
public class FlowNormalizerSchedulingSeparator
  implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface coi) throws Exception {
    // do nothing
  }
  /**
   * The tag that is used by {@code FlowNormalizer} and {@code FieldInitMethodCallRemover} to
   * express the need for a separation.
   */
  @Description("Allows to express the need for a separation")
  public static class SeparatorTag implements Tag {
  }

}
