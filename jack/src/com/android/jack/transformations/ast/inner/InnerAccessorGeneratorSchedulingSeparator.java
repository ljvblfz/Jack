/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.ast.inner;

import com.android.jack.ir.ast.JSession;
import com.android.jack.transformations.ast.ConcatRemover;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A schedulable that is only useful as a separation between {@link InnerAccessorGenerator} on one
 * side, {@link SwitchEnumSupport} and {@link ConcatRemover} on the other side, so that they don't
 * run in the same "type" subplan.
 */
@Description("A separation between InnerAccessorGenerator on one side, SwitchEnumSupport "
    + "and ConcatRemover on the other side")
@Transform(remove = {
    InnerAccessorGeneratorSchedulingSeparator.SeparatorConcatRemoverTag.class,
    InnerAccessorGeneratorSchedulingSeparator.SeparatorSwitchEnumSupportTag.class})
@Constraint(need = {
    InnerAccessorGeneratorSchedulingSeparator.SeparatorConcatRemoverTag.class,
    InnerAccessorGeneratorSchedulingSeparator.SeparatorSwitchEnumSupportTag.class})
public class InnerAccessorGeneratorSchedulingSeparator implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    // do nothing
  }

  /**
   * The tag that is used by {@link InnerAccessorGenerator} and {@link ConcatRemover} to express the
   * need for a separation.
   */
  @Description("Allows to express the need for a separation")
  public static class SeparatorConcatRemoverTag implements Tag {
  }

  /**
   * The tag that is used by {@link InnerAccessorGenerator} and {@link SwitchEnumSupport} to express
   * the need for a separation.
   */
  @Description("Allows to express the need for a separation")
  public static class SeparatorSwitchEnumSupportTag implements Tag {
  }

}
