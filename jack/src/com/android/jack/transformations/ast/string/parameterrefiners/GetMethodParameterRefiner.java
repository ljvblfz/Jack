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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodNameLiteral;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.reflection.MemberFinder;
import com.android.jack.reflection.MultipleMethodsFoundException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.ast.InitInNewArray;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine string parameter of call to getMethod method of java.lang.Class
 */
@Constraint(need = {OriginalNames.class, InitInNewArray.class})
@Transform(add = JMethodNameLiteral.class)
public class GetMethodParameterRefiner extends CommonStringParameterRefiner implements
    StringParameterRefiner {

  @Nonnull
  private static final String GETMETHOD_METHOD_NAME = "getMethod";

  @CheckForNull
  private JMethodId getMethodMethodId;

  @Override
  public boolean isApplicable(@Nonnull JMethodCall call) {
    if (getMethodMethodId == null) {
      List<JType> parameterList = new ArrayList<JType>(2);
      parameterList.add(javaLangString);
      parameterList.add(javaLangClassArray);
      getMethodMethodId = javaLangClass.getMethodId(
          GETMETHOD_METHOD_NAME, parameterList, MethodKind.INSTANCE_VIRTUAL);
    }
    if (call.getReceiverType() == javaLangClass
        && call.getMethodId() == getMethodMethodId) {
      assert formatter.getName(call.getType()).equals(METHOD_CLASS_SIGNATURE);
      return true;
    }
    return false;
  }

  @Override
  @CheckForNull
  public JStringLiteral getExpressionToRefine(@Nonnull JMethodCall call) {
    assert call.getArgs().size() == 2;
    return (getExpressionToRefine(call, 0)); // First parameter
  }

  @Override
  @CheckForNull
  public JAbstractStringLiteral getRefinedExpression(@Nonnull JMethodCall call,
      @Nonnull JStringLiteral paramToRefine) {
    JMethodNameLiteral strMethodLiteral = null;
    String methodName = paramToRefine.getValue();
    JDefinedClassOrInterface type = getTypeFromClassLiteralExpression(call.getInstance());
    String methodSignature = getMethodSignature(call);

    if (type != null && methodSignature != null) {
      JMethod method = lookupMethod(type, methodSignature);

      if (method != null) {
        strMethodLiteral = new JMethodNameLiteral(paramToRefine.getSourceInfo(), method);
        assert methodName.equals(strMethodLiteral.getValue());
      }
    }

    return strMethodLiteral;
  }

  @CheckForNull
  protected JMethod lookupMethod(@Nonnull JDefinedClassOrInterface type,
      @Nonnull String methodSignature) {
    JMethod foundMethod = null;
    try {
      foundMethod = MemberFinder.getMethod(type, methodSignature);
    } catch (MultipleMethodsFoundException e) {
      // String will not be refined due to multiple methods
    }
    return foundMethod;
  }

  @CheckForNull
  private String getMethodSignature(@Nonnull JMethodCall call) {
    JExpression instance = call.getInstance();
    assert instance != null;

    List<JExpression> args = call.getArgs();
    assert args.size() == 2;
    JExpression methodName = args.get(0);
    JExpression parameters = args.get(1);

    if (methodName instanceof JStringLiteral && parameters instanceof JNewArray) {
      StringBuilder sb = new StringBuilder(((JStringLiteral) methodName).getValue());
      sb.append("(");
      for (JExpression param : ((JNewArray) parameters).getInitializers()) {
        if (param instanceof JClassLiteral) {
          sb.append(Jack.getLookupFormatter().getName(((JClassLiteral) param).getRefType()));
        } else {
          return null;
        }
      }

      sb.append(")");
      return sb.toString();
    }

    return null;
  }
}