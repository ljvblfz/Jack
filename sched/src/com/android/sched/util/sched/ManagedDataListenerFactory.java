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

package com.android.sched.util.sched;

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;

import javax.annotation.Nonnull;


/**
 * Factory class to manage {@link ManagedDataListener}
 */
@HasKeyId
public class ManagedDataListenerFactory {
  @Nonnull
  public static final ImplementationPropertyId<ManagedDataListener> DATA_LISTENER =
      ImplementationPropertyId
          .create("sched.data", "Define which data processor to use", ManagedDataListener.class)
          .addDefaultValue("none");

  @Nonnull
  public static ManagedDataListener getManagedDataListener() {
    return ThreadConfig.get(DATA_LISTENER);
  }

  private ManagedDataListenerFactory() {}
}
