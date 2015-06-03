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

import com.android.jack.Jack;
import com.android.jack.annotations.DisableAccessorsOptimization;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Allow for outer fields and methods access in an inner class. Try to create
 * as little accessors as possible. This is achieved by making private fields
 * package private, and making private methods static package private, when
 * possible.
 */
@Description("Generate accessors for outer fields and methods in an inner class")
@Synchronized
@Transform(add = {ReferencedFromInnerClassMarker.class,
    GetterMarker.class,
    SetterMarker.class,
    WrapperMarker.class,
    JMethodCall.class,
    JNewInstance.class,
    JNullLiteral.class,
    JExpressionStatement.class,
    InnerAccessorSchedulingSeparator.SeparatorTag.class},
    remove = {ThreeAddressCodeForm.class, NewInstanceRemoved.class})
@Constraint(no = {SideEffectOperation.class, JAlloc.class})
public class OptimizedInnerAccessorGenerator extends InnerAccessorGenerator {

  @Nonnull
  private final JAnnotationType annotationType =
    Jack.getSession().getPhantomLookup().getAnnotationType(
    NamingTools.getTypeSignatureName(DisableAccessorsOptimization.class.getName()));

  @Nonnull
  private ReferencedFromInnerClassMarker getOrCreateRFICMarker
    (@Nonnull JDefinedClassOrInterface accessorClass) {
    ReferencedFromInnerClassMarker marker =
        accessorClass.getMarker(ReferencedFromInnerClassMarker.class);
    if (marker == null) {
      marker = new ReferencedFromInnerClassMarker();
      accessorClass.addMarker(marker);
    }
    return marker;
  }

  private void markAsReferenced(@Nonnull JDefinedClassOrInterface accessorClass,
      @Nonnull JField field) {
    ReferencedFromInnerClassMarker marker = getOrCreateRFICMarker(accessorClass);
    marker.addField(field);
  }

  @Override
  protected void handleOuterFieldWrite(@Nonnull TransformationRequest tr,
      @Nonnull JFieldRef fieldRef, @Nonnull JDefinedClassOrInterface accessorClass) {
    JField field = fieldRef.getFieldId().getField();
    assert(field != null);
    if (!field.isPrivate() || !field.getAnnotations(annotationType).isEmpty()
        || !accessorClass.getAnnotations(annotationType).isEmpty()) {
      super.handleOuterFieldWrite(tr, fieldRef, accessorClass);
      return;
    }
    markAsReferenced(accessorClass, field);
  }

  @Override
  protected void handleOuterFieldRead(@Nonnull TransformationRequest tr,
      @Nonnull JFieldRef fieldRef, @Nonnull JDefinedClassOrInterface accessorClass) {
    JField field = fieldRef.getFieldId().getField();
    assert(field != null);
    if (!field.isPrivate() || !field.getAnnotations(annotationType).isEmpty()
        || !accessorClass.getAnnotations(annotationType).isEmpty()) {
      super.handleOuterFieldRead(tr, fieldRef, accessorClass);
      return;
    }
    markAsReferenced(accessorClass, field);
  }

  @Override
  protected void handleOuterMethodCall(@Nonnull TransformationRequest tr,
      @Nonnull JMethodCall methodCall, @Nonnull JMethod method,
      @Nonnull JDefinedClassOrInterface accessorClass, boolean isSuper) {
    if (!method.isPrivate() || method.isNative() || !method.getAnnotations(annotationType).isEmpty()
        || !accessorClass.getAnnotations(annotationType).isEmpty()) {
      super.handleOuterMethodCall(tr, methodCall, method, accessorClass, isSuper);
      return;
    }

    ReferencedFromInnerClassMarker referencedMarker = getOrCreateRFICMarker(accessorClass);
    referencedMarker.addMethod(method);
  }

}
