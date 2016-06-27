/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.jack.Options;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.request.AppendArgument;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Fixes calls to methods marked by OptimizedInnerAccessorGenerator and, if required,
 * transforms these methods bodies to use their last argument instead of 'this'.
 */
@Description("Fixes calls to methods marked by OptimizedInnerAccessorGenerator and, if required, " +
    "transforms these methods bodies to use their last argument instead of 'this'.")
@Transform(add = {JParameterRef.class},
    modify = {JNewInstance.class, JMethodCall.class},
    remove = {NeedsRethising.class})
@Constraint(need = {NeedsDispatchAdjustment.class, NeedsRethising.class})
@Filter(SourceTypeFilter.class)
public class MethodCallDispatchAdjuster implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  static class Adjuster extends JVisitor {

    @Nonnull
    protected final TransformationRequest tr;

    @Nonnull
    private final JMethod currentMethod;

    public Adjuster(@Nonnull TransformationRequest tr,
        @Nonnull JMethod currentMethod) {
      this.tr = tr;
      this.currentMethod = currentMethod;
    }

    @Override
    public boolean visit(@Nonnull JMethodCall methodCall) {
      JMethodIdWide id = methodCall.getMethodId();
      if (id.containsMarker(NeedsDispatchAdjustment.class)) {
        JExpression instance = methodCall.getInstance();
        if (instance != null) {
          tr.append(new Remove(instance));
          tr.append(new AppendArgument(methodCall, instance));
        }
      }
      return super.visit(methodCall);
    }

  }

  static class RethisingAdjuster extends Adjuster {

    @Nonnull
    private final JParameter thisParam;

    public RethisingAdjuster(@Nonnull TransformationRequest tr,
        @Nonnull JMethod currentMethod) {
      super(tr, currentMethod);
      this.thisParam = currentMethod.getParams().get(currentMethod.getParams().size() - 1);
    }

    @Override
    public boolean visit(@Nonnull JThisRef x) {
      JParameterRef replacement = thisParam.makeRef(x.getSourceInfo());
      replacement.addAllMarkers(x.getAllMarkers());
      tr.append(new Replace(x, replacement));
      return super.visit(x);
    }

  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Adjuster visitor;
    if (method.containsMarker(NeedsRethising.class)) {
      visitor = new RethisingAdjuster(tr, method);
      method.removeMarker(NeedsRethising.class);
    } else {
      visitor = new Adjuster(tr, method);
    }
    visitor.accept(method);
    tr.commit();
  }

}
