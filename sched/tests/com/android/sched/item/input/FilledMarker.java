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

import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.item.TagOrMarker;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.marker.MarkedA;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

@Name(FilledMarker.NAME)
@Description(FilledMarker.DESCRIPTION)
@ValidOn(MarkedA.class)
@OnlyFor(SchedTest.class)
public class FilledMarker implements Marker {
  public static final String NAME = "FilledMarker";
  public static final String DESCRIPTION = "FilledMarker description";

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  @Name("FilledMarker.ComposedTag")
  @Description("FilledMarker.ComposedTag description")
  @OnlyFor(SchedTest.class)
  @ComposedOf({FilledMarker.class, SubElement1.class, SubElement2.class})
  public static class Composed implements TagOrMarker {
  }

  @Name("FilledMarker.SubElement1")
  @Description("FilledMarker.SubElement1 description")
  @OnlyFor(SchedTest.class)
  public static class SubElement1 implements Tag {
  }

  @Name("FilledMarker.SubElement2")
  @Description("FilledMarker.SubElement2 description")
  @OnlyFor(SchedTest.class)
  public static class SubElement2 implements Tag {
  }

}
