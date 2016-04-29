/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.filter;

import com.android.sched.SchedProperties;
import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.scheduler.IllegalRequestException;
import com.android.sched.scheduler.Plan;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.ProcessException;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.AsapConfigBuilder;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.Test;

@ImplementationName(iface = OnlyForType.class, name = "filter-tests")
public class FilterTest implements OnlyForType {
  @Test
  public void testProcess1() throws Exception {
    ThreadConfig.setConfig(new AsapConfigBuilder(/* debug = */ false).set(SchedProperties.ONLY_FOR, FilterTest.class)
        .set(ScheduleInstance.SKIP_ADAPTER, false)
        .setString(ScheduleInstance.DEFAULT_RUNNER, "single-threaded").build());
    Assert.assertEquals(
        "8: [A.2, A.2/B.2, A.2/B.2/C.1, A.2/B.2/C.2, A.2, A.2/D.1, A.2/D.2, A.2/D.2/E.2, A.2/D.3, A.2]",
        runPlan());

    ThreadConfig.setConfig(new AsapConfigBuilder(/* debug = */ false).set(SchedProperties.ONLY_FOR, FilterTest.class)
        .set(ScheduleInstance.SKIP_ADAPTER, true)
        .setString(ScheduleInstance.DEFAULT_RUNNER, "single-threaded").build());
    Assert.assertEquals(
        "6: [A.2, A.2/B.2, A.2/B.2/C.1, A.2/B.2/C.2, A.2, A.2/D.1, A.2/D.2, A.2/D.2/E.2, A.2/D.3, A.2]",
        runPlan());
  }

  private String runPlan() throws IllegalRequestException, ProcessException {
    Request sr = new Scheduler().createScheduleRequest();

    PlanBuilder<C_A> pb_a = sr.getPlanBuilder(C_A.class);
    pb_a.append(R_A.class);

    SubPlanBuilder<C_B> pb_b = pb_a.appendSubPlan(A_AB.class);
    pb_b.append(R_B.class);

    SubPlanBuilder<C_C> pb_c = pb_b.appendSubPlan(A_BC.class);
    pb_c.append(R_C.class);

    pb_a.append(R_A.class);

    SubPlanBuilder<C_D> pb_d = pb_a.appendSubPlan(A_AD.class);
    pb_d.append(R_D.class);

    SubPlanBuilder<C_E> pb_e = pb_d.appendSubPlan(A_DE.class);
    pb_e.append(R_E.class);

    pb_a.append(R_A.class);

    Plan<C_A> plan = pb_a.getPlan();

    R_Common.list.clear();
    R_Common.adapterCount = 0;
    plan.getScheduleInstance().process(new C_A("A.2"));

    return R_Common.adapterCount +": " + R_Common.list.toString();
  }
}
