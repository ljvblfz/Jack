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

import com.android.jack.frontend.MethodIdDuplicateRemover.UniqMethodIds;
import com.android.jack.frontend.MethodIdMerger.MethodIdMerged;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.lookup.JMethodWithReturnLookupException;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Update methodIds of {@link JMethodCall}s and {@link JNameValuePair}s so that they used the merged
 * uniq ids.
 */
@Description("Update methodIds of JMethodCalls and JNameValuePairs so that they used the merged"
    + " uniq ids")
@Constraint(need = MethodIdMerged.class)
@Transform(add = UniqMethodIds.class)
public class MethodIdDuplicateRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  /**
   * This tag means that Jack IR is using uniq JMethodIds.
   */
  @Description("Jack IR is using uniq JMethodIds")
  @Name("UniqMethodIds")
  public static class UniqMethodIds implements Tag {
  }

  private static class Visitor extends JVisitor {

    private Visitor() {
      super(false /* needLoading */);
    }

    @Nonnull
    private JMethodIdWide getResolvedMethodIdWide(
        @Nonnull JClassOrInterface receiverType, @Nonnull JMethodIdWide id) {
      Collection<JMethod> methods = id.getMethods();
      if (!methods.isEmpty()) {
        JMethod method = methods.iterator().next();
        return method.getMethodIdWide();
      } else {
        return receiverType.getOrCreateMethodIdWide(id.getName(), id.getParamTypes(), id.getKind());
      }
    }

    @Override
    public boolean visit(@Nonnull JLambda lambda) {
      JInterface lambdaType = lambda.getType();
      try {
        JMethodId mthIdWithReturnType = lambda.getMethodIdToImplement();
        lambda.resolveMethodId(lambdaType.getMethodId(
            mthIdWithReturnType.getMethodIdWide().getName(),
            mthIdWithReturnType.getMethodIdWide().getParamTypes(),
            MethodKind.INSTANCE_VIRTUAL,
            mthIdWithReturnType.getType()));
      } catch (JMethodWithReturnLookupException e) {
        throw new AssertionError();
      }
      return super.visit(lambda);
    }

    @Override
    public boolean visit(@Nonnull JMethodCall call) {
      JMethodIdWide id = getResolvedMethodIdWide(call.getReceiverType(), call.getMethodId());
      call.resolveMethodId(id);
      return super.visit(call);
    }

    @Override
    public boolean visit(@Nonnull JAnnotation annotation) {
      for (JNameValuePair pair : annotation.getNameValuePairs()) {
        JMethodIdWide id = getResolvedMethodIdWide(annotation.getType(), pair.getMethodId());
        pair.resolveMethodId(id);
      }
      return super.visit(annotation);
    }
  }

  @Override
  public void run(JDefinedClassOrInterface type) throws Exception {
    new Visitor().accept(type);
  }
}
