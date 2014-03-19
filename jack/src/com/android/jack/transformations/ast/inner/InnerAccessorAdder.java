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

package com.android.jack.transformations.ast.inner;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Adds accessors for outer fields and methods in an inner class
 */
@Description("Adds accessors for outer fields and methods in an inner class")
@Transform(add = {GetterMarker.InnerAccessorGetter.class, SetterMarker.InnerAccessorSetter.class,
    WrapperMarker.InnerAccessorWrapper.class, InnerAccessor.class}, remove = {
    GetterMarker.class, SetterMarker.class, WrapperMarker.class, ThreeAddressCodeForm.class})
@Constraint(need = {GetterMarker.class, SetterMarker.class, WrapperMarker.class},
    no = InnerAccessorSchedulingSeparator.SeparatorTag.class)
public class InnerAccessorAdder implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    if (type.isExternal() || type instanceof JDefinedInterface) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(type);

    {
      GetterMarker getterMarker = type.getMarker(GetterMarker.class);
      if (getterMarker != null) {
        for (JMethod m : getterMarker.getAllGetters()) {
          tr.append(new AppendMethod(type, m));
        }
        type.removeMarker(GetterMarker.class);
      }
    }

    {
      SetterMarker setterMarker = type.getMarker(SetterMarker.class);
      if (setterMarker != null) {
        for (JMethod m : setterMarker.getAllSetters()) {
          tr.append(new AppendMethod(type, m));
        }
        type.removeMarker(SetterMarker.class);
      }
    }

    {
      WrapperMarker wrapperMarker = type.getMarker(WrapperMarker.class);
      if (wrapperMarker != null) {
        for (JMethod m : wrapperMarker.getAllWrappers()) {
          tr.append(new AppendMethod(type, m));
        }
        type.removeMarker(WrapperMarker.class);
      }
    }


    tr.commit();
  }

}
