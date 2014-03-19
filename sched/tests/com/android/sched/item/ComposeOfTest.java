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

package com.android.sched.item;

import static org.junit.Assert.fail;

import com.android.sched.item.input.RunnerA;
import com.android.sched.item.input.RunnerB;
import com.android.sched.item.input.RunnerC;
import com.android.sched.item.input.RunnerD;
import com.android.sched.production.Production1;
import com.android.sched.scheduler.Component0;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Runner10;
import com.android.sched.scheduler.Runner8;
import com.android.sched.scheduler.Runner9;
import com.android.sched.scheduler.RunnerTest;
import com.android.sched.scheduler.SchedulableManager;
import com.android.sched.scheduler.SchedulableSet;
import com.android.sched.scheduler.ScheduleException;
import com.android.sched.scheduler.Scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ComposeOfTest {

  SchedulableSet scs;
  Scheduler scheduler;

  @Before
  public void setUp() throws Exception {
    Scheduler.class.getClassLoader().setDefaultAssertionStatus(true);

    scheduler = Scheduler.getScheduler();
    SchedulableManager sm = SchedulableManager.getSchedulableManager();

    scs = sm.getAllSchedulable();
    scs.remove(Runner8.class);
    scs.remove(Runner9.class);
    scs.remove(Runner10.class);
  }

  @After
  public void tearDown() throws Exception {
    RunnerTest.reset();
  }

  @Test
  public void testComposedOf1() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production1.class);

    try {
      RunnerTest.init(scheduler, sr);
      PlanBuilder<Component0> plan = sr.getPlanBuilder(Component0.class);
      plan.append(RunnerA.class);
      plan.append(RunnerB.class);
      plan.append(RunnerC.class);
      plan.append(RunnerD.class);


      plan.getPlan().getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }
}
