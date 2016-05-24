/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.transformations.ast.string.parameterrefiners;

import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldNameLiteral;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.reflection.MemberFinder;
import com.android.jack.reflection.MultipleFieldsFoundException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine string parameter of call to getField method of java.lang.Class
 */
@Constraint(need = OriginalNames.class)
@Transform(add = JFieldNameLiteral.class)
public class GetFieldParameterRefiner extends CommonStringParameterRefiner implements
    StringParameterRefiner {

  @Nonnull
  private static final String GETFIELD_METHOD_NAME = "getField";

  @Nonnull
  private final JMethodIdWide getFieldMethodId = javaLangClass.getMethodIdWide(
          GETFIELD_METHOD_NAME, Collections.singletonList((JType) javaLangString),
          MethodKind.INSTANCE_VIRTUAL);

  @Override
  public boolean isApplicable(@Nonnull JMethodCall call) throws JMethodLookupException {
    if (call.getReceiverType().isSameType(javaLangClass)
        && call.getMethodId().equals(getFieldMethodId)) {
      assert formatter.getName(call.getType()).equals(FIELD_CLASS_SIGNATURE);
      return true;
    }
    return false;
  }



  @Override
  @CheckForNull
  public JStringLiteral getExpressionToRefine(@Nonnull JMethodCall call) {
    assert call.getArgs().size() == 1;
    return (getExpressionToRefine(call, 0)); // First parameter
  }

  @Override
  @CheckForNull
  public JAbstractStringLiteral getRefinedExpression(@Nonnull JMethodCall call,
      @Nonnull JStringLiteral paramToRefine) {
    JFieldNameLiteral strFieldLiteral = null;
    JDefinedClassOrInterface type = getTypeFromClassLiteralExpression(call.getInstance());

    if (type != null) {
      String fieldName = paramToRefine.getValue();
      JField field = lookupField(type, fieldName);

      if (field != null) {
        strFieldLiteral = new JFieldNameLiteral(paramToRefine.getSourceInfo(), field);
        assert fieldName.equals(strFieldLiteral.getValue());
      }
    }

    return strFieldLiteral;
  }

  @CheckForNull
  protected JField lookupField(@Nonnull JDefinedClassOrInterface type,
      @Nonnull String fieldName) {
    JField foundField = null;
    try {
      foundField = MemberFinder.getField(type, fieldName);
    } catch (MultipleFieldsFoundException e) {
      // String will not be refined due to multiple fields
    }
    return foundField;
  }
}