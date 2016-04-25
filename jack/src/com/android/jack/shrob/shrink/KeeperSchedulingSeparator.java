/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that is only useful as a separation between the {@link Keeper} and
 * the shrinkers, so that they don't run in the same "type" subplan.
 *
 * The Keeper puts markers on the IR outside of the class or interface it runs on, which
 * necessitates for the shrinkers to wait for the Keeper to finish visiting all types before using
 * the markers.
 */
@Description("A separation between the Keeper and the Shrinkers")
@Transform(remove = KeeperSchedulingSeparator.SeparatorTag.class)
@Constraint(need = KeeperSchedulingSeparator.SeparatorTag.class)
@Support(Shrinking.class)
public class KeeperSchedulingSeparator implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    // do nothing
  }

  /**
   * The tag that is used by the {@link Keeper} and the Shrinkers to
   * express the need for a separation.
   */
  @Description("Allows to express the need for a separation")
  public static class SeparatorTag implements Tag {
  }

}
