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

package com.android.jack.frontend;

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JVisitor;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Update methodIds of {@link JMethodCall}s and {@link JNameValuePair}s.
 */
public class MethodIdDuplicateRemover extends JVisitor {

  public MethodIdDuplicateRemover() {
    super(false /* needLoading */);
  }

  @Nonnull
  private JMethodId getResolvedMethodId(
      @Nonnull JClassOrInterface receiverType, @Nonnull JMethodId id) {
    Collection<JMethod> methods = id.getMethods();
    if (!methods.isEmpty()) {
      JMethod method = methods.iterator().next();
      return method.getMethodId();
    } else {
      return receiverType.getOrCreateMethodId(id.getName(), id.getParamTypes(), id.getKind());
    }
  }

  @Override
  public boolean visit(@Nonnull JMethodCall call) {
    JMethodId id = getResolvedMethodId(call.getReceiverType(), call.getMethodId());
    call.resolveMethodId(id);
    return super.visit(call);
  }

  @Override
  public boolean visit(@Nonnull JAnnotation annotation) {
    for (JNameValuePair pair : annotation.getNameValuePairs()) {
      JMethodId id = getResolvedMethodId(annotation.getType(), pair.getMethodId());
      pair.resolveMethodId(id);
    }
    return super.visit(annotation);
  }
}
