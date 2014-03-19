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

@Name("Visitor 2")
@Description("Visitor 2 description")
@OnlyFor(SchedTest.class)
public class Visitor2 implements AdapterSchedulable<Component2, Component1> {
  @Override
  @Nonnull
  public Iterator<Component1> adapt(@Nonnull Component2 b)
      throws Exception {
    return new SpecificIterator();
  }

  private static class SpecificIterator implements Iterator<Component1> {
    private long i = 0;

    @Override
    public boolean hasNext() {
      return i < 10;
    }

    @Override
    public Component1 next() {
      return new Component1(i++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
