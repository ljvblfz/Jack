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

import com.android.sched.feature.Feature1;
import com.android.sched.feature.Feature2;
import com.android.sched.feature.FeatureLowMemory;
import com.android.sched.production.Production1;
import com.android.sched.production.Production2;
import com.android.sched.tag.Tag1;
import com.android.sched.tag.Tag2;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SchedulerTests {

  SchedulableSet scs;
  Scheduler scheduler;

  @Before
  public void setUp() throws Exception {
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

  @Ignore
  @Test
  public void testProcess1() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production1.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess2() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production1.class).addFeature(Feature1.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess3() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production1.class).addFeature(Feature2.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess4() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production1.class).addFeature(Feature1.class).addFeature(Feature2.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess5() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production2.class);
    sr.addFeature(FeatureLowMemory.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess6() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production2.class).addFeature(Feature1.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess7() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    sr.addSchedulables(scs);
    sr.addProduction(Production2.class).addFeature(Feature2.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testProcess10() throws Exception {
    Request sr = scheduler.createScheduleRequest();

    scs.add(Runner8.class);
    scs.add(Runner9.class);
    scs.add(Runner10.class);

    sr.addSchedulables(scs);
    sr.addInitialTagOrMarker(Tag1.class);
    sr.addTargetIncludeTagOrMarker(Tag1.class);
    sr.addTargetIncludeTagOrMarker(Tag2.class);
    sr.addProduction(Production1.class);
    sr.addProduction(Production2.class);
    try {
      RunnerTest.init(scheduler, sr);
      Plan<Component0> sp = sr.buildPlan(Component0.class);
      sp.getScheduleInstance().process(new Component0());
      RunnerTest.done(sr);
    } catch (ScheduleException e) {
      fail();
    }
  }
}
