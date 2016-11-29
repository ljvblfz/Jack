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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.reflection.MemberFinder;
import com.android.jack.reflection.MultipleMethodsFoundException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine string parameter of call to getDeclaredMethod method of java.lang.Class
 */
@Constraint(need = OriginalNames.class)
// Access type name.
@Access(JSession.class)
public class GetDeclaredMethodParameterRefiner extends GetMethodParameterRefiner {

  @Nonnull
  private static final String GETDECLAREDMETHOD_METHOD_NAME = "getDeclaredMethod";

  @Nonnull
  private final JMethodIdWide getDeclaredMethodMethodId;

  public GetDeclaredMethodParameterRefiner() {
      List<JType> parameterList = new ArrayList<JType>(2);
      parameterList.add(javaLangString);
      parameterList.add(javaLangClassArray);
      getDeclaredMethodMethodId = javaLangClass.getMethodIdWide(
          GETDECLAREDMETHOD_METHOD_NAME, parameterList, MethodKind.INSTANCE_VIRTUAL);
  }

  @Override
  public boolean isApplicable(@Nonnull JMethodCall call) throws JMethodLookupException {
    if (call.getReceiverType().isSameType(javaLangClass)
        && call.getMethodIdWide().equals(getDeclaredMethodMethodId)) {
      assert formatter.getName(call.getType()).equals(METHOD_CLASS_SIGNATURE);
      return true;
    }
    return false;
  }

  @Override
  @CheckForNull
  protected JMethod lookupMethod(@Nonnull JDefinedClassOrInterface type,
      @Nonnull String methodSignature) {
    JMethod foundMethod = null;
    try {
      foundMethod = MemberFinder.getDirectMethod(type, methodSignature);
    } catch (MultipleMethodsFoundException e) {
      // String will not be refined due to multiple methods
    }
    return foundMethod;
  }
}