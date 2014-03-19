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

import com.android.sched.schedulable.RunnableSchedulable;

import org.junit.Before;
import org.junit.Test;

public class ManagedRunnerTest {

  @Before
  public void setUp() {
    ManagedRunnable.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testConstructor() {
    testSchedulableError(RunnerError1.class);
    testSchedulableError(RunnerError4.class);
    testSchedulableError(RunnerError5.class);
    testSchedulableError(RunnerError6.class);
    testSchedulableError(RunnerError7.class);
    testSchedulableError(RunnerError8.class);
    testSchedulableError(RunnerError9.class);
    testSchedulableError(RunnerError10.class);
  }

  private void testSchedulableError(Class<? extends RunnableSchedulable<?>> sched) {
    try {
      new ManagedRunnable(sched);
      fail();
    } catch (SchedulableNotConformException e) {
      // Normal Exception
    }
  }
}
