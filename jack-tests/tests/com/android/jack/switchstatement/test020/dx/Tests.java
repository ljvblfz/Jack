/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.switchstatement.test020.dx;

import java.lang.Thread.State;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.switchstatement.test020.jack.Switch1;
import com.android.jack.switchstatement.test020.jack.Switch2;

/**
 * Tests about switches. Running instrument code to see if it is executable.
 */
public class Tests {
  @Test
  public void test1() {
    Assert.assertEquals(1, Switch1.switch1(State.NEW));
    Assert.assertEquals(2, Switch1.switch1(State.RUNNABLE));
    Assert.assertEquals(3, Switch1.switch1(State.BLOCKED));
    Assert.assertEquals(4, Switch1.switch1(State.WAITING));
    Assert.assertEquals(5, Switch1.switch1(State.TIMED_WAITING));
    Assert.assertEquals(6, Switch1.switch1(State.TERMINATED));
  }

  @Test
  public void test2() {
    Assert.assertEquals(1, Switch2.switch2(State.NEW));
    Assert.assertEquals(2, Switch2.switch2(State.RUNNABLE));
    Assert.assertEquals(3, Switch2.switch2(State.BLOCKED));
    Assert.assertEquals(4, Switch2.switch2(State.WAITING));
    Assert.assertEquals(5, Switch2.switch2(State.TIMED_WAITING));
    Assert.assertEquals(6, Switch2.switch2(State.TERMINATED));
  }
}
