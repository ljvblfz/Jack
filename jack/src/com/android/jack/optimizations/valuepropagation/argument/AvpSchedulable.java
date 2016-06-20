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

package com.android.jack.optimizations.valuepropagation.argument;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/** Base class for all argument value propagation schedulables */
public class AvpSchedulable {
  @Nonnull
  public static final StatisticId<Counter> ARGUMENT_VALUES_PROPAGATED =
      new StatisticId<>(
          "jack.optimization.argument-value-propagation.propagated",
          "Argument values propagated",
          CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> PARAMETER_IS_WRITTEN_TO =
      new StatisticId<>(
          "jack.optimization.argument-value-propagation.written-to",
          "Parameters with a value to propagate being written to",
          CounterImpl.class, Counter.class);

  @Nonnull
  private final TypePackageAndMethodFormatter formatter =
      Jack.getLookupFormatter();

  @Nonnull
  String getMethodSignature(
      @Nonnull JMethodIdWide method, @Nonnull JType returnType) {
    return formatter.getName(method.getName(), method.getParamTypes(), returnType);
  }

  @Nonnull
  String getMethodSignature(@Nonnull JMethod method) {
    return getMethodSignature(method.getMethodIdWide(), method.getType());
  }


  @Description("Marks tainted methods")
  @ValidOn(value = { JMethod.class })
  enum TaintedMethodMarker implements Marker {
    TAINTED;

    /** Marks a method as tainted */
    static void markAsTainted(@Nonnull JMethod method) {
      if (!method.containsMarker(TaintedMethodMarker.class)) {
        method.addMarker(TAINTED);
      }
    }

    /** Checks if the method is tainted, removed the marker after */
    static boolean checkIfTaintedAndRemoveMarker(@Nonnull JMethod method) {
      return method.removeMarker(TaintedMethodMarker.class) != null;
    }

    @Override
    @Nonnull
    public Marker cloneIfNeeded() {
      return this;
    }
  }
}
