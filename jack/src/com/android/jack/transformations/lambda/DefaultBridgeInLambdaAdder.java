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

package com.android.jack.transformations.lambda;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPhantomInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} add bridges into {@link JLambda} that comes from default bridges
 * generated into interfaces when using Jill.
 */
@Description("Add bridges into JLambda that comes from default bridges generated into interfaces")
@Constraint(need = DefaultBridgeIntoInterface.class)
@Transform(
    add = {JLambda.DefaultBridgeAddedInLambda.class, DefaultBridgeSeparator.SeparatorTag.class})
public class DefaultBridgeInLambdaAdder implements RunnableSchedulable<JMethod> {

  private final long androidMinApiLevel =
      ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL).longValue();

  /**
   * Indicates that an interface is unknown and does not belong to classpath.
   */
  private static class LambdaUnknownInterfaceReportable implements Reportable, HasSourceInfo {

    @Nonnull
    private static final Joiner typeNameJoiner = Joiner.on(", ");

    @Nonnull
    private final List<JPhantomInterface> unknownInterfaces;

    @Nonnull
    private final JLambda lambda;

    public LambdaUnknownInterfaceReportable(@Nonnull JLambda lambda,
        List<JPhantomInterface> unknownTypes) {
      this.lambda = lambda;
      this.unknownInterfaces = unknownTypes;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Lambda coming from jar file need their interfaces on the classpath to be compiled, "
          + "unknown interfaces are "
          + typeNameJoiner.join(
              Iterables.transform(unknownInterfaces, new Function<JPhantomInterface, String>() {
                @Override
                public String apply(JPhantomInterface arg0) {
                  return Jack.getUserFriendlyFormatter().getName(arg0);
                }
              }));
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }

    @Override
    public SourceInfo getSourceInfo() {
      return lambda.getSourceInfo();
    }
  }

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final JSession session = Jack.getSession();

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JLambda lambdaExpr) {
      if (lambdaExpr.getMarker(LambdaFromJillMarker.class) != null) {
        List<JPhantomInterface> unknownInterfaces = new ArrayList<>();
        addDefaultBridges(lambdaExpr, lambdaExpr.getType(), unknownInterfaces);
        if (!unknownInterfaces.isEmpty()) {
          session.getReporter().report(Severity.FATAL,
              new LambdaUnknownInterfaceReportable(lambdaExpr, unknownInterfaces));
          session.setAbortEventually(true);
        }
      }
      return false;
    }

    private void addDefaultBridges(@Nonnull JLambda lambdaExpr, @Nonnull JInterface interfaze,
        @Nonnull List<JPhantomInterface> unknownInterfaces) {
      if (interfaze instanceof JPhantomInterface) {
        unknownInterfaces.add((JPhantomInterface) interfaze);
      } else {
        JDefinedInterface definedInterface = (JDefinedInterface) interfaze;
        for (JMethod method : definedInterface.getMethods()) {
          int mthModifier = method.getModifier();
          if (JModifier.isSynthetic(mthModifier) && JModifier.isBridge(mthModifier)) {
            JMethodId mthIdToAdd = method.getMethodId();
            JMethodIdWide mthIdWideToAdd = mthIdToAdd.getMethodIdWide();
            // Add method id only if it does not already exists
            boolean bridgeAlreadyExists = false;
            for (JMethodId bridge : lambdaExpr.getBridgeMethodIds()) {
              if (bridge.getMethodIdWide().equals(mthIdWideToAdd.getName(),
                  mthIdWideToAdd.getParamTypes())
                  && bridge.getType().equals(mthIdToAdd.getType())) {
                bridgeAlreadyExists = true;
                break;
              }
            }
            if (!bridgeAlreadyExists) {
              lambdaExpr.addBridgeMethodId(method.getMethodId());
            }
          }
        }
        for (JInterface superInterface : definedInterface.getImplements()) {
          addDefaultBridges(lambdaExpr, superInterface, unknownInterfaces);
        }
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (androidMinApiLevel >= AndroidCompatibilityChecker.N_API_LEVEL || method.isNative()
        || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor();
    visitor.accept(method);
  }
}
