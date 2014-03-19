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

import javax.annotation.Nonnull;

/**
 * A factory that allows to create instances of {@link PlanBuilder} and {@link SubPlanBuilder}.
 * @param <T> the root <i>data</i> type
 */
public interface PlanBuilderFactory<T extends Component> {

  /**
   * Allows to create a new instance of {@link PlanBuilder}.
   * @param request the {@link Request} according to which the {@code Plan} should be built
   * @param runOn the root <i>data</i> type of the {@code Plan}
   */
  @Nonnull
  public PlanBuilder<T> createPlanBuilder(Request request, Class<T> runOn);

  /**
   * Allows to create a new instance of {@link SubPlanBuilder}.
   * @param runOn the root <i>data</i> type of the subplan
   */
  @Nonnull
  public SubPlanBuilder<T> createSubPlanBuilder(Class<T> runOn);
}
