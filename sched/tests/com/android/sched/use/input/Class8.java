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

import com.android.sched.feature.Feature1;
import com.android.sched.feature.Feature2;
import com.android.sched.marker.Marker4;
import com.android.sched.marker.Marker5;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Use;

@Optional({@ToSupport(feature = Feature1.class, add = @Constraint(need = Marker5.class)),
  @ToSupport(feature = {Feature1.class, Feature2.class}, add = @Constraint(need = Marker4.class))})
@Use(Class2.class)
public class Class8 {
}
