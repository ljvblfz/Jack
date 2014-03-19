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
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldNameLiteral;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.reflection.MemberFinder;
import com.android.jack.reflection.MultipleFieldsFoundException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine string parameter of call to newUpdater method of AtomicIntegerFieldUpdater or
 * AtomicLongFieldUpdater
 */
@Constraint(need = OriginalNames.class)
@Transform(add = JFieldNameLiteral.class)
public class AtomicLongIntUpdaterParameterRefiner extends CommonStringParameterRefiner implements
    StringParameterRefiner {

  @Nonnull
  private final JClassOrInterface atomicIntegerFieldUpdater =
      (JClassOrInterface) Jack.getProgram().getPhantomLookup()
          .getType(CommonTypes.JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICINTEGERFIELDUPDATER);

  @Nonnull
  private final JClassOrInterface atomicLongFieldUpdater =
      (JClassOrInterface) Jack.getProgram().getPhantomLookup()
          .getType(CommonTypes.JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICLONGFIELDUPDATER);

  @Override
  public boolean isApplicable(@Nonnull JMethodCall call) {
    JClassOrInterface receiverType = call.getReceiverType();
    if ((isOrIsSubClassOf(receiverType, atomicIntegerFieldUpdater)
        || isOrIsSubClassOf(receiverType, atomicLongFieldUpdater))
        && call.getMethodName().equals(NEWUPDATER_METHOD_NAME)) {
      return true;
    }
    return false;
  }

  @Override
  @CheckForNull
  public JStringLiteral getExpressionToRefine(@Nonnull JMethodCall call) {
    assert call.getArgs().size() == 2;
    return (getExpressionToRefine(call, 1)); // Second parameter
  }

  @Override
  @CheckForNull
  public JAbstractStringLiteral getRefinedExpression(@Nonnull JMethodCall call,
      @Nonnull JStringLiteral paramToRefine) {
    JFieldNameLiteral strFieldLiteral = null;
    JDefinedClassOrInterface type = getTypeFromClassLiteralExpression(call.getArgs().get(0));

    if (type != null) {
      String fieldName = paramToRefine.getValue();

      JField foundField = null;
      try {
        foundField = MemberFinder.getDirectField(type, fieldName);
      } catch (MultipleFieldsFoundException e) {
        // String will not be refined due to multiple fields
      }

      if (foundField != null) {
        strFieldLiteral = new JFieldNameLiteral(paramToRefine.getSourceInfo(), foundField);
        assert fieldName.equals(strFieldLiteral.getValue());
      }
    }

    return strFieldLiteral;
  }
}
