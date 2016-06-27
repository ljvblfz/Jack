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

import com.android.sched.feature.Feature1;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.marker.Marker3;
import com.android.sched.marker.Marker4;
import com.android.sched.marker.Marker5;
import com.android.sched.production.Production2;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.tag.Tag3;

import javax.annotation.Nonnull;

@Name("Runner 4")
@Description("Runner 4 description")
@OnlyFor(SchedTest.class)
@Constraint(need = {Marker4.class, Marker5.class, Tag3.class})
@Produce(Production2.class)
@Optional(@ToSupport(feature = Feature1.class, add = @Constraint(need = Marker3.class)))
public class Runner4 extends RunnerTest implements RunnableSchedulable<Component0> {
  @Override
  public void run(@Nonnull Component0 v) {
    need(Marker4.class);
    need(Marker5.class);
    need(Tag3.class);
    need(Feature1.class, Marker3.class);
    produce(Production2.class);
  }
}
