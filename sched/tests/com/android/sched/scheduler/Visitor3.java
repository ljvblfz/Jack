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
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.Iterator;

import javax.annotation.Nonnull;

@Name("Visitor 3")
@Description("Visitor 3 description")
@OnlyFor(SchedTest.class)
public class Visitor3 implements AdapterSchedulable<Component1, Component3> {
  @Override
  @Nonnull
  public Iterator<Component3> adapt(@Nonnull Component1 b)
      throws Exception {
    return new SpecificIterator();
  }

  private static class SpecificIterator implements Iterator<Component3> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < 10;
    }

    @Override
    public Component3 next() {
      return new Component3(i++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
