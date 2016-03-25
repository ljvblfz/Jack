/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.ast.inner;

import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.request.AddModifiers;
import com.android.jack.transformations.request.AppendMethodParam;
import com.android.jack.transformations.request.RemoveModifiers;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Adds accessors for outer fields and methods in an inner class
 */
@Description("Changes collected fields visibility to package private,"
    + "transforms collected methods to their uniquely named, package private versions.")
@Transform(add = {NeedsDispatchAdjustment.class,
    JParameter.class,
    NeedsRethising.class,
    OptimizedInnerAccessorSchedulingSeparator.SeparatorTag.class},
    modify = {JMethod.class},
    remove = {ReferencedFromInnerClassMarker.class})
@Constraint(need = {ReferencedFromInnerClassMarker.class},
    no = {InnerAccessorSchedulingSeparator.SeparatorTag.class})
@Filter(SourceTypeFilter.class)
public class ReferencedOuterFieldsExposer implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    if (type instanceof JDefinedInterface) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(type);
    ReferencedFromInnerClassMarker marker = type.getMarker(ReferencedFromInnerClassMarker.class);

    if (marker != null) {
      for (JField f : marker.getFields()) {
        tr.append(new RemoveModifiers(f, JModifier.PRIVATE));
      }

      for (JMethod method : marker.getMethods()) {
        tr.append(new RemoveModifiers(method, JModifier.PRIVATE));
        if (!(method instanceof JConstructor)) {
          tr.append(new AddModifiers(method, JModifier.STATIC));
          JMethodIdWide id = method.getMethodIdWide();
          assert id.getMethods().size() == 1;
          if (id.getKind() != MethodKind.STATIC) {
            id.setKind(MethodKind.STATIC);
            id.addParam(type);
            id.addMarker(NeedsDispatchAdjustment.INSTANCE);
            JParameter thisParam =
                new JParameter(method.getSourceInfo(), InnerAccessorGenerator.THIS_PARAM_NAME,
                    type, JModifier.FINAL | JModifier.SYNTHETIC, method);
            tr.append(new AppendMethodParam(method, thisParam));
            method.addMarker(NeedsRethising.INSTANCE);
          }
        }
      }
      type.removeMarker(ReferencedFromInnerClassMarker.class);
    }

    tr.commit();
  }

}
