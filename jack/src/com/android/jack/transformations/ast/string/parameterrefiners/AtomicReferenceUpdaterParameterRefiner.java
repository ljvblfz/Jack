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
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine string parameter of call to newUpdater method of AtomicReferenceFieldUpdater
 */
@Constraint(need = OriginalNames.class)
// Access name of called method.
@Access(JSession.class)
public class AtomicReferenceUpdaterParameterRefiner extends AtomicLongIntUpdaterParameterRefiner {

  @Nonnull
  private final JClassOrInterface atomicFieldUpdater =
      (JClassOrInterface) Jack.getSession().getPhantomLookup()
          .getType(CommonTypes.JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICREFERENCEFIELDUPDATER);

  @Override
  public boolean isApplicable(@Nonnull JMethodCall call) {
    JClassOrInterface receiverType = call.getReceiverType();

    if ((isOrIsSubClassOf(receiverType, atomicFieldUpdater))
        && call.getMethodName().equals(NEWUPDATER_METHOD_NAME)) {
      return true;
    }
    return false;
  }

  @Override
  @CheckForNull
  public JStringLiteral getExpressionToRefine(@Nonnull JMethodCall call) {
    assert call.getArgs().size() == 3;
    return (getExpressionToRefine(call, 2)); // Third parameter
  }
}
