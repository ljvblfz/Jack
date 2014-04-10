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
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeStringLiteral;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.lookup.JLookupException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine string parameter of call to forName method of java.lang.Class
 */
@Constraint(need = OriginalNames.class)
@Transform(add = JTypeStringLiteral.class)
public class ForNameParameterRefiner extends CommonStringParameterRefiner implements
    StringParameterRefiner {

  @Nonnull
  private static final String FORNAME_METHOD_NAME = "forName";

  @CheckForNull
  private JMethodId forNameMethodId;

  @Override
  public boolean isApplicable(@Nonnull JMethodCall call) {
    if (forNameMethodId == null) {
      forNameMethodId = javaLangClass.getMethodId(
          FORNAME_METHOD_NAME, Collections.singletonList((JType) javaLangString),
          MethodKind.STATIC);
    }
    if (call.getReceiverType().equals(javaLangClass) && call.getMethodId() == forNameMethodId) {
      assert call.getType().equals(javaLangClass);
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
    String typeName = paramToRefine.getValue();
    String typeSignature;
    JType type;
    JTypeStringLiteral strTypeLiteral = null;

    if (typeName.contains("/")) {
      return null;
    }

    if (typeName.startsWith("[")) {
      typeSignature = NamingTools.getBinaryName(typeName);
    } else {
      typeSignature = NamingTools.getTypeSignatureName(typeName);
    }

    if (NamingTools.isTypeDescriptor(typeSignature)) {
      try {
        type = lookup.getType(typeSignature);
        strTypeLiteral = new JTypeStringLiteral(paramToRefine.getSourceInfo(),
            type instanceof JArrayType ? Kind.SRC_SIGNATURE : Kind.SRC_QN, type);
        assert typeName.equals(strTypeLiteral.getValue());
      } catch (JLookupException e) {
        // The string was not a valid type, do not replace it.
      }
    }

    return strTypeLiteral;
  }
}
