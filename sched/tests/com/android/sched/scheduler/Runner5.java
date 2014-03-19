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
import com.android.sched.feature.Feature2;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.marker.Marker1;
import com.android.sched.marker.Marker2;
import com.android.sched.marker.Marker4;
import com.android.sched.production.Production1;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;

import javax.annotation.Nonnull;

@Name("Runner 5")
@Description("Runner 5 description")
@OnlyFor(SchedTest.class)
@Constraint(need = Marker4.class)
@Produce(Production1.class)
@Optional({@ToSupport(feature = Feature1.class, add = @Constraint(need = Marker1.class)),
    @ToSupport(feature = Feature2.class, add = @Constraint(need = Marker2.class))})
public class Runner5 extends RunnerTest implements RunnableSchedulable<Component0> {
  @Override
  public void run(@Nonnull Component0 v) throws Exception {
    need(Marker4.class);
    need(Feature1.class, Marker1.class);
    need(Feature2.class, Marker2.class);
    produce(Production1.class);
  }
}
