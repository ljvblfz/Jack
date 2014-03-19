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

import com.android.sched.marker.Marker1;
import com.android.sched.marker.Marker2;
import com.android.sched.marker.Marker3;
import com.android.sched.tag.Tag2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubPlanTest {

  SchedulableSet scs;
  Scheduler scheduler;

  @Before
  public void setUp() throws Exception {
    Scheduler.class.getClassLoader().setDefaultAssertionStatus(true);
    scheduler = Scheduler.getScheduler();

    scs = scheduler.getAllSchedulable();
    scs.remove(Runner8.class);
    scs.remove(Runner9.class);
    scs.remove(Runner10.class);
  }

  @After
  public void tearDown() throws Exception {
    RunnerTest.reset();
  }

  @Test
  public void testProcess1() throws Exception {
    Request sr = scheduler.createScheduleRequest(scheduler.getAllSchedulable());

    try {
      RunnerTest.init(scheduler, sr);
      PlanBuilder<Component0> spb = sr.getPlanBuilder(Component0.class);
      sr.addTargetIncludeTagOrMarker(Marker1.class);
      sr.addTargetIncludeTagOrMarker(Marker2.class);
      sr.addTargetIncludeTagOrMarker(Marker3.class);
      sr.addTargetIncludeTagOrMarker(Tag2.class);
      spb.append(Runner1.class);
      spb.append(Runner2.class);
      spb.append(Runner6.class);
      SubPlanBuilder<Component2> sspb = spb.appendSubPlan(Visitor1.class);
      SubPlanBuilder<Component1> ssspb = sspb.appendSubPlan(Visitor2.class);
      SubPlanBuilder<Component3> sssspb = ssspb.appendSubPlan(Visitor3.class);
      sssspb.append(Runner3sub1.class);
      Plan<Component0> sp = spb.getPlan();
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }
}
