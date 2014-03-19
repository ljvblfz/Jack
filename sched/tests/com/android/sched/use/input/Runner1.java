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

package com.android.sched.use.input;

import com.android.sched.feature.Feature2;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.marker.Marker1;
import com.android.sched.marker.Marker3;
import com.android.sched.marker.Marker6;
import com.android.sched.marker.MarkerOk3;
import com.android.sched.marker.MarkerOk4;
import com.android.sched.production.Production2;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.schedulable.With;
import com.android.sched.scheduler.Component0;
import com.android.sched.scheduler.Component1;
import com.android.sched.tag.Tag1;
import com.android.sched.tag.Tag3;

import javax.annotation.Nonnull;

@Name("Runner 1 (use)")
@Description("Runner 1 description")
@OnlyFor(SchedTest.class)
@Use({Class2.class, Class3.class, Class4.class, Class5.class, Class6.class, Class7.class})
@Produce(Production2.class)
@Support(Feature2.class)
@Transform(add = Marker3.class, remove = Tag3.class, modify=Component0.class)
@Constraint(need=MarkerOk3.class, no=MarkerOk4.class)
@Protect(add = Marker1.class, remove = Tag1.class, modify = Component1.class,
unprotect = @With(remove = Marker1.class, add=Marker6.class))
public class Runner1 implements RunnableSchedulable<Component0> {
  @Override
  public void run(@Nonnull Component0 v) throws Exception {
  }
}
