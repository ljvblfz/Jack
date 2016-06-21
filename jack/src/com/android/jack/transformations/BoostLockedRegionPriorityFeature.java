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

package com.android.jack.transformations;

import com.android.jack.library.DumpInLibrary;
import com.android.jack.transformations.ast.BoostLockedRegionPriority;
import com.android.jack.util.ClassNameCodec;
import com.android.jack.util.MethodNameCodec;
import com.android.jack.util.MethodNameCodec.MethodNameValue;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.item.Name;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.category.Private;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Feature used to indicate if {@link BoostLockedRegionPriority} is turned on.
 */
@HasKeyId
@Name("BoostLockedRegionPriorityFeature")
@Description("Feature turning on BoostLockedRegionPriorityFeature")
public final class BoostLockedRegionPriorityFeature implements Feature {
  @Nonnull
  public static final BooleanPropertyId ENABLE =
      BooleanPropertyId.create(
              "jack.transformations.boost-locked-region-priority",
              "Boost priority of threads acquiring certain locks")
          .addCategory(Private.class)
          .addDefaultValue(Boolean.FALSE)
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final PropertyId<String> BOOST_LOCK_CLASSNAME =
      PropertyId.create(
              "jack.transformations.boost-locked-region-priority.classname",
              "The class signature where acquiring it as a lock should boost a thread's prioirty",
              new ClassNameCodec())
          .requiredIf(BoostLockedRegionPriorityFeature.ENABLE.getValue().isTrue())
          .addCategory(Private.class)
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final PropertyId<MethodNameValue> BOOST_LOCK_REQUEST_METHOD =
      PropertyId.create(
              "jack.transformations.boost-locked-region-priority.request",
              "Static method in the specified class that can boost a thread's prioirty",
              new MethodNameCodec())
          .requiredIf(BoostLockedRegionPriorityFeature.ENABLE.getValue().isTrue())
          .addCategory(Private.class)
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final PropertyId<MethodNameValue> BOOST_LOCK_RESET_METHOD =
      PropertyId.create(
              "jack.transformations.boost-locked-region-priority.reset",
              "Static method in the specified class that can reset a thread's prioirty",
              new MethodNameCodec())
          .requiredIf(BoostLockedRegionPriorityFeature.ENABLE.getValue().isTrue())
          .addCategory(Private.class)
          .addCategory(DumpInLibrary.class);
}
