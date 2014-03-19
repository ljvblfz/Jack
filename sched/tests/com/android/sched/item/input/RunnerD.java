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

package com.android.sched.item.input;

import com.android.sched.item.Description;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.production.Production1;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.Component0;
import com.android.sched.scheduler.RunnerTest;

import javax.annotation.Nonnull;

@Description("Runner D description")
@OnlyFor(SchedTest.class)
@Constraint(need = FilledMarker.Composed.class)
@Produce(Production1.class)
public class RunnerD extends RunnerTest implements RunnableSchedulable<Component0> {
  @Override
  public void run(@Nonnull Component0 v) throws Exception {
    produce(Production1.class);
  }
}
