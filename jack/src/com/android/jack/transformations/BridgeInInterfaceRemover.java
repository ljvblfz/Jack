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

import com.android.jack.Options;
import com.android.jack.backend.dex.EnsureAndroidCompatibility;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.transformations.lambda.DefaultBridgeIntoInterface;
import com.android.jack.transformations.lambda.DefaultBridgeSeparator;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} remove bridges from interface based on the value of the minimum
 * Android API level compatibility.
 */
@Description("Remove bridges from interfaces based on the value of the minumun Android API level")
@Support(EnsureAndroidCompatibility.class)
// This schedulable can be run in parallel on methods belonging to the same type that can lead to
// remove several methods in the same time on the same type and it is not supported, thus this
// schedulable must be synchronized
@Constraint(no = DefaultBridgeSeparator.SeparatorTag.class)
@Transform(remove = DefaultBridgeIntoInterface.class)
@Synchronized
// This schedulable removes some methods
@ExclusiveAccess(JDefinedClassOrInterface.class)
public class BridgeInInterfaceRemover implements RunnableSchedulable<JMethod> {

  private final long androidMinApiLevel =
      ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL).longValue();

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (androidMinApiLevel < AndroidCompatibilityChecker.N_API_LEVEL) {
      if (method.getEnclosingType() instanceof JInterface && method.isBridge()) {
        TransformationRequest tr = new TransformationRequest(method.getEnclosingType());
        tr.append(new Remove(method));
        tr.commit();
      }
    }
  }
}
