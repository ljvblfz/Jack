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

@Name("Visitor 1")
@Description("Visitor 1 description")
@OnlyFor(SchedTest.class)
public class Visitor1 implements AdapterSchedulable<Component0, Component2> {
  @Override
  @Nonnull
  public Iterator<Component2> adapt(@Nonnull Component0 b) {
    return new SpecificIterator();
  }

  private static class SpecificIterator implements Iterator<Component2> {
    private byte i = 0;

    @Override
    public boolean hasNext() {
      return i < 10;
    }

    @Override
    public Component2 next() {
      return new Component2(i++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}

