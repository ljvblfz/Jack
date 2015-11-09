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

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
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
 * This marker indicates that a field has an associated getter.
 */
@ValidOn(JDefinedClass.class)
@Description("This marker indicates that a field has an associated getter.")
public class GetterMarker implements Marker {

  @Nonnull
  private static final String GETTER_PREFIX = NamingTools.getNonSourceConflictingName("get");

  @Nonnull
  private final HashMap<JField, JMethod> getters = new HashMap<JField, JMethod>();

  @Nonnull
  Collection<JMethod> getAllGetters() {
    return getters.values();
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError("Not yet supported");
  }

  @Name("InnerAccessorGetter")
  @Description("All JNodes created for a getter allowing to access an inner field.")
  @ComposedOf({JMethodCall.class,
      JParameter.class,
      JParameterRef.class,
      JFieldRef.class,
      JReturnStatement.class,
      JMethodBody.class,
      JBlock.class})
  static class InnerAccessorGetter implements AbstractComponent {
  }

  @Nonnull
  // TODO(delphinemartin): Warning: this is not thread-safe
  JMethod getOrCreateGetter(@Nonnull JField field,
      @Nonnull JDefinedClass accessorClass) {
    // $get<id>($this) {
    //   return $this.field;
    // }
    JMethod getter = getters.get(field);
    if (getter == null) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;
      String getterName = GETTER_PREFIX;
      // It is a temporary deterministic name that will be replace by an index into
      // InnerAccessorAdder
      getterName += field.getName();
      JMethodId id = new JMethodId(getterName, MethodKind.STATIC);
      getter = new JMethod(sourceInfo, id, accessorClass, field.getType(),
          JModifier.SYNTHETIC | JModifier.STATIC);

      JExpression instance = null;
      if (!field.isStatic()) {
        JParameter thisParam = new JParameter(
            sourceInfo, InnerAccessorGenerator.THIS_PARAM_NAME, accessorClass, JModifier.SYNTHETIC,
            getter);
        getter.addParam(thisParam);
        id.addParam(accessorClass);
        instance = thisParam.makeRef(sourceInfo);
      }

      JFieldRef returnedRef = new JFieldRef(sourceInfo, instance, field.getId(), accessorClass);

      JReturnStatement returnSt = new JReturnStatement(sourceInfo, returnedRef);
      JBlock bodyBlock = new JBlock(sourceInfo);
      JMethodBody body = new JMethodBody(sourceInfo, bodyBlock);
      bodyBlock.addStmt(returnSt);
      getter.setBody(body);
      assert !getters.containsKey(field);
      getters.put(field, getter);
    }
    return getter;
  }
}
