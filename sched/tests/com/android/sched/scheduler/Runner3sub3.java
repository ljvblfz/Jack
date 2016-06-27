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

package com.android.sched.scheduler;

import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.marker.Marker5;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.tag.Tag2;

import javax.annotation.Nonnull;

@Name("Runner 3.3")
@Description("Runner 3.3 description")
@Constraint(need = Tag2.class)
@Transform(add = Marker5.class)
@OnlyFor(SchedTest.class)
public class Runner3sub3 extends RunnerTest implements RunnableSchedulable<Component3> {
  @Override
  public void run(@Nonnull Component3 i) {
    need(Tag2.class);
    add(Marker5.class);
  }
}
