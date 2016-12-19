/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.transformations.ast.splitnew;

import com.android.jack.Options;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This visitor transforms new instance creation in a two-steps process:
 * object allocation and initialization (call to constructor).
 */
@Description("Splits JSymbolicNewInstance into JAlloc and JSymbolicCall")
@Name("SplitNewInstance")
@Transform(add = { JAlloc.class,
                   JAsgOperation.NonReusedAsg.class,
                   JLocalRef.class,
                   JMultiExpression.class,
                   NewInstanceRemoved.class },
    remove = { JNewInstance.class,
               ThreeAddressCodeForm.class })
@Use(LocalVarCreator.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class SplitNewInstance implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);
  @Nonnull
  private static final String LOCAL_VAR_PREFIX = "sni";

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;
    @Nonnull
    private final LocalVarCreator lvCreator;

    private Visitor(@Nonnull TransformationRequest request, @Nonnull LocalVarCreator lvCreator) {
      this.request = request;
      this.lvCreator = lvCreator;
    }

    @Override
    public boolean visit(@Nonnull JNewInstance newInstance) {
      SourceInfo srcInfos = newInstance.getSourceInfo();
      JExpression[] exprs =
          NewExpressionSplitter.splitNewInstance(newInstance, request, lvCreator);
      JMultiExpression splittedNewInstance = new JMultiExpression(srcInfos, exprs);

      request.append(new Replace(newInstance, splittedNewInstance));

      return super.visit(newInstance);
    }
  }

  /** Utility class for splitting new instance expression into  */
  @Transform(add = { JAlloc.class,
                     JAsgOperation.NonReusedAsg.class,
                     JLocalRef.class })
  @Use(LocalVarCreator.class)
  public static class NewExpressionSplitter {
    @Nonnull
    public static JExpression[] splitNewInstance(
        @Nonnull JNewInstance newInstance,
        @Nonnull TransformationRequest request,
        @Nonnull LocalVarCreator varCreator) {

      SourceInfo srcInfos = newInstance.getSourceInfo();
      JClass type = newInstance.getType();

      // tmp = alloc<type>
      JAlloc alloc = new JAlloc(srcInfos, type);
      JLocal tmp = varCreator.createTempLocal(type, srcInfos, request);
      JAsgOperation assign =
          new JAsgOperation(srcInfos, tmp.makeRef(srcInfos), alloc);

      // tmp.init(args)
      JMethodId methodId = newInstance.getMethodId().getMethodId(JPrimitiveTypeEnum.VOID.getType());
      assert methodId != null;
      JMethodCall initCall = new JMethodCall(srcInfos, tmp.makeRef(srcInfos), type, methodId,
          methodId.getMethodIdWide().canBeVirtual());
      initCall.addArgs(newInstance.getArgs());

      // tmp
      JLocalRef result = tmp.makeRef(srcInfos);

      return new JExpression[] { assign, initCall, result };
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    Visitor visitor = new Visitor(request, new LocalVarCreator(method, LOCAL_VAR_PREFIX));
    visitor.accept(method);
    request.commit();
  }
}
