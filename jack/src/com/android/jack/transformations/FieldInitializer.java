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

package com.android.jack.transformations;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.transformations.assertion.DynamicAssertionFeature;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} adds a statement to clinit if the marker
 * {@link InitializationExpression} is present.
 */
@Description("Add a statement to clinit if the marker InitializationExpression is present.")
@Constraint(need = {InitializationExpression.class, EmptyClinit.class})
@Transform(remove = {InitializationExpression.class, ThreeAddressCodeForm.class})
@Support(DynamicAssertionFeature.class)
@Filter(TypeWithoutValidTypePrebuilt.class)
 // Modifies <clinit>
@ExclusiveAccess(JDefinedClassOrInterface.class)
public class FieldInitializer implements RunnableSchedulable<JField> {

  @Override
  public void run(@Nonnull JField field) {
    InitializationExpression marker = field.getMarker(InitializationExpression.class);
    if (marker != null) {
      assert field.isStatic() : "Not yet supported";
      TransformationRequest request = new TransformationRequest(field);

      // Lookup for clinit
      JMethod clinit = field.getEnclosingType().getMethod(NamingTools.STATIC_INIT_NAME,
          JPrimitiveTypeEnum.VOID.getType());
      JMethodBody body = (JMethodBody) clinit.getBody();
      assert body != null;

      JStatement toPrepend = marker.getStatement();
      request.append(new PrependStatement(body.getBlock(), toPrepend));
      field.removeMarker(InitializationExpression.class);
      request.commit();
    }
  }
}
