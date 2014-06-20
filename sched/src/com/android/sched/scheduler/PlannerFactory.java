/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.sched.util.codec.InputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.DefaultFactoryPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.InputStreamFile;

import javax.annotation.Nonnull;

/**
 * Factory class to manage {@link Planner}
 */
@HasKeyId
public class PlannerFactory {

  @SuppressWarnings("unchecked")
  @Nonnull
  private static final DefaultFactoryPropertyId<Planner<? extends Component>> PLANNER_PROVIDER =
      (DefaultFactoryPropertyId<
          Planner<? extends Component>>) (Object) DefaultFactoryPropertyId.create(
          "sched.planner", "Define which planner to use to compute plan", Planner.class)
          .bypassAccessibility().addDefaultValue("manual");

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Nonnull
  public static final PropertyId<InputStreamFile> PLANNER_FILE = PropertyId.create(
      "sched.planner.file", "The file to read the plan from",
      new InputStreamCodec()).
      requiredIf(((DefaultFactoryPropertyId<Planner>) (Object) PLANNER_PROVIDER).
          getClazz().isImplementedBy(DeserializerPlanner.class));

  public static Planner<? extends Component> createPlanner() {
    return ThreadConfig.get(PLANNER_PROVIDER).create();
  }

  private PlannerFactory() {}
}
