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

import static org.junit.Assert.fail;

import com.android.sched.schedulable.AdapterSchedulable;

import org.junit.Before;
import org.junit.Test;

public class ManagedVisitorTest {

  @Before
  public void setUp() {
    ManagedVisitor.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testConstructor() {
    testVisitorError(VisitorError1.class);
  }

  private void testVisitorError(Class<? extends AdapterSchedulable<?, ?>> sched) {
    try {
      new ManagedVisitor(sched);
      fail();
    } catch (SchedulableNotConformException e) {
      // OK
    }
  }
}
