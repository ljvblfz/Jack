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

import com.android.sched.item.Component;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.SchedEventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.Nonnull;

/**
 * A structure that allows to build a full {@link Plan} according to a {@link Request}.
 * @param <T> the root <i>data</i> type of the {@code Plan}
 */
public class PlanBuilder<T extends Component> extends SubPlanBuilder<T> {
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();
  @Nonnull
  private final Request request;

  public PlanBuilder(@Nonnull Request request, @Nonnull Class<T> runOn) {
    super(request.getScheduler(), runOn);

    this.request = request;
  }

  @Nonnull
  public Plan<T> getPlan() {
    assert request != null;

    try (Event event = tracer.open(SchedEventType.PLANBUILDER)) {
      plan.initPlan(request, this);
    }

    return plan;
  }

  @Nonnull
  public Request getRequest() {
    return request;
  }
}
