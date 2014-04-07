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

package com.android.jack.transformations.exceptions;

import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A schedulable that is only useful as a separation between a {@code Schedulable} that adds try
 * statements in new members, and {@code TryCatchRemover}, so that they don't run in the same "type"
 * subplan.
 */
@Description("A separation between a Schedulable and TryCatchRemover")
@Transform(remove = TryStatementSchedulingSeparator.SeparatorTag.class)
@Constraint(need = TryStatementSchedulingSeparator.SeparatorTag.class)
public class TryStatementSchedulingSeparator implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    // do nothing
  }

  /**
   * The tag that is used by a {@code Schedulable} that adds try statements and
   * {@code TryCatchRemover} to express the need for a separation.
   */
  @Description("Allows to express the need for a separation")
  public static class SeparatorTag implements Tag {
  }

}
