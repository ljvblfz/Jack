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

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.request.AppendMethodParam;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Nonnull;

/**
 * This marker indicates that a field has an associated setter.
 */
@ValidOn(JDefinedClass.class)
@Description("This marker indicates that a field has an associated setter.")
public class SetterMarker implements Marker {

  @Nonnull
  private static final String SETTER_PREFIX = NamingTools.getNonSourceConflictingName("set");

  @Nonnull
  private static final String VALUE_PARAM_NAME = NamingTools.getNonSourceConflictingName("value");

  @Nonnull
  private final HashMap<JField, JMethod> setters = new HashMap<JField, JMethod>();

  @Nonnull
  Collection<JMethod> getAllSetters() {
    return setters.values();
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError("Not yet supported");
  }

  @Name("InnerAccessorSetter")
  @Description("All JNodes created for a setter allowing to access an inner field.")
  @ComposedOf({JMethod.class,
      JMethodBody.class,
      JParameter.class,
      JParameterRef.class,
      JFieldRef.class,
      JAsgOperation.NonReusedAsg.class,
      JReturnStatement.class,
      JBlock.class})
  static class InnerAccessorSetter implements AbstractComponent {
  }


  @Nonnull
  // TODO(delphinemartin): Warning: this is not thread-safe
  JMethod getOrCreateSetter(@Nonnull JField field,
      @Nonnull JDefinedClass accessorClass,
      @Nonnull TransformationRequest tr) {
    // $set<id>($this, $value) {
    //   $this.field = $value;
    //   return $value;
    // }
    JMethod setter = setters.get(field);
    if (setter == null) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;
      String setterName = SETTER_PREFIX;
      // It is a temporary deterministic name that will be replace by an index into
      // InnerAccessorAdder
      setterName += field.getName();
      JMethodIdWide id = new JMethodIdWide(setterName, MethodKind.STATIC);
      JType fieldType = field.getType();
      setter = new JMethod(sourceInfo, new JMethodId(id, fieldType), accessorClass,
          JModifier.SYNTHETIC | JModifier.STATIC);
      JBlock bodyBlock = new JBlock(sourceInfo);
      JMethodBody body = new JMethodBody(sourceInfo, bodyBlock);

      JExpression instance = null;
      if (!field.isStatic()) {
        JParameter thisParam =
            new JParameter(sourceInfo, InnerAccessorGenerator.THIS_PARAM_NAME, accessorClass,
                JModifier.DEFAULT, setter);
        tr.append(new AppendMethodParam(setter, thisParam));
        instance = thisParam.makeRef(sourceInfo);
      }

      JParameter value = new JParameter(sourceInfo, VALUE_PARAM_NAME, fieldType,
          JModifier.DEFAULT, setter);
      tr.append(new AppendMethodParam(setter, value));
      JFieldRef lhs = new JFieldRef(sourceInfo, instance, field.getId(), accessorClass);

      JAsgOperation asgOperation = new JAsgOperation(sourceInfo, lhs, value.makeRef(sourceInfo));
      bodyBlock.addStmt(new JExpressionStatement(sourceInfo, asgOperation));

      JReturnStatement returnSt =
          new JReturnStatement(sourceInfo, value.makeRef(sourceInfo));
      bodyBlock.addStmt(returnSt);
      setter.setBody(body);
      assert !setters.containsKey(field);
      setters.put(field, setter);
    }

    return setter;
  }
}
