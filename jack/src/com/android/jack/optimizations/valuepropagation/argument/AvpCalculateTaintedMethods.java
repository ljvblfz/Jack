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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.MethodKind;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/** Calculate tainted methods on types to be emitted */
@Description("Argument value propagation, calculate tainted methods on types to be emitted")
@Transform(add = TaintedVirtualMethodsMarker.class)
@Name("ArgumentValuePropagation: AvpCalculateTaintedMethods")
public class AvpCalculateTaintedMethods extends AvpSchedulable
    implements RunnableSchedulable<JDefinedClassOrInterface> {

  /** Calculate new tainted marker for a type */
  @Nonnull
  private TaintedVirtualMethodsMarker
  calculateTaintedVirtualMethods(@Nonnull JDefinedClassOrInterface type) {
    // Compute the marker, note that this code may be executed in parallel by
    // many threads, so we introduce checks between big chunks of calculation
    // to quickly recheck if other thread succeeded by this type and abort
    // current calculation.

    TaintedVirtualMethodsMarker marker =
        TaintedVirtualMethodsMarker.getMarker(type);
    if (marker != null) {
      return marker;
    }

    TaintedVirtualMethodsMarker.Builder builder =
        new TaintedVirtualMethodsMarker.Builder(type);

    // Process implements clause
    for (JInterface impl : type.getImplements()) {
      marker = TaintedVirtualMethodsMarker.getMarker(type);
      if (marker != null) {
        return marker;
      }

      if (impl instanceof JPhantomClassOrInterface) {
        return builder.createAndAddAsAllTainted();
      }
      TaintedVirtualMethodsMarker other =
          getOrCreateMarker((JDefinedClassOrInterface) impl);
      if (other.allMethodsAreTainted()) {
        return builder.createAndAddAsAllTainted();
      }
      builder.mergeWith(other);
    }

    // Process optional super class
    JClass superClass = type.getSuperClass();
    if (superClass != null) {
      marker = TaintedVirtualMethodsMarker.getMarker(type);
      if (marker != null) {
        return marker;
      }

      if (superClass instanceof JPhantomClassOrInterface) {
        return builder.createAndAddAsAllTainted();
      }
      TaintedVirtualMethodsMarker other =
          getOrCreateMarker((JDefinedClassOrInterface) superClass);
      if (other.allMethodsAreTainted()) {
        return builder.createAndAddAsAllTainted();
      }
      builder.mergeWith(other);
    }

    marker = TaintedVirtualMethodsMarker.getMarker(type);
    if (marker != null) {
      return marker;
    }

    // If this is NOT a type to be emitted, add its methods as well
    if (!type.isToEmit()) {
      for (JMethod method : type.getMethods()) {
        JMethodIdWide idWide = method.getMethodIdWide();
        // Tainted method should be a virtual method with at
        // least one parameter.
        if (idWide.getKind() == MethodKind.INSTANCE_VIRTUAL &&
            idWide.getParamTypes().size() > 0) {
          builder.addTaintedMethod(getMethodSignature(method));
        }
      }
    }

    return builder.createAndAdd();
  }

  @Nonnull
  private TaintedVirtualMethodsMarker getOrCreateMarker(
      @Nonnull JDefinedClassOrInterface type) {
    TaintedVirtualMethodsMarker marker =
        TaintedVirtualMethodsMarker.getMarker(type);
    if (marker != null) {
      return marker;
    }

    // Compute the marker
    return calculateTaintedVirtualMethods(type);
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    getOrCreateMarker(type);
  }
}
