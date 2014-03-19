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

import com.android.sched.marker.Marker2;
import com.android.sched.marker.Marker4;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.With;
import com.android.sched.scheduler.Component2;
import com.android.sched.tag.Tag4;

@Protect(add = Marker4.class, remove = Tag4.class, modify = Component2.class,
    unprotect = @With(add = Marker2.class))
public class Class4 {
}
