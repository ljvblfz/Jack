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

package com.android.jack.transformations.ast;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.BoostLockedRegionPriorityFeature;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.MethodNameCodec.MethodNameValue;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Raise locked region priority for certain types of locks.
 *
 * In particular, this allows a project to manually insert a function call after a lock acquisition
 * and another one after lock release.
 *
 * The first can be use to increase the current thread's priority while the other can be used to
 * reset the change.
 *
 * The idea is allow heavily contended lock to be release faster to improve performance.
 *
 * Example:
 *
 * <code>
 *   JLock target
 *   try {
 *    work();
 *   } finally {
 *    JUnlock target
 *   }
 * </code>
 *
 * could be transformed into:
 *
 * <code>
 *   JLock target
 *   boostPriority()
 *   try {
 *     work();
 *   } finally {
 *     JUnlock target
 *     resetPriority();
 *   }
 * </code>
 *
 * Try blocks are inserted to make sure that the reset call is always executed even in place of
 * exceptions. This is important for threads that might be reused like worker threads.
 */
@Description("Raise locked region priority for certain types of locks.")
@Constraint(
  need = {JLock.class, JUnlock.class},
  no = {JSynchronizedBlock.class}
)
@Transform(
  add = {
    JExpressionStatement.class,
    JMethodCall.class,
  }
)
public class BoostLockedRegionPriority implements RunnableSchedulable<JMethod> {

  @CheckForNull private final JClass lockClass;
  @CheckForNull private final JClass requestClass;
  @CheckForNull private final JClass resetClass;
  @CheckForNull private final JMethodId requestMethodId;
  @CheckForNull private final JMethodId resetMethodId;
  @Nonnull private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  public BoostLockedRegionPriority() {
    String className = ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_CLASSNAME);
    MethodNameValue requestMethodNameValue =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD);
    MethodNameValue resetMethodNameValue =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD);

    final JNodeLookup lookup = Jack.getSession().getLookup();
    lockClass =
        getClassOrReportFailure(
            lookup,
            NamingTools.getTypeSignatureName(className),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_CLASSNAME.getName());
    requestClass =
        getClassOrReportFailure(
            lookup,
            NamingTools.getTypeSignatureName(requestMethodNameValue.getClassName()),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD.getName());
    resetClass =
        getClassOrReportFailure(
            lookup,
            NamingTools.getTypeSignatureName(resetMethodNameValue.getClassName()),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD.getName());
    requestMethodId =
        getStaticMethodOrReportFailure(
            requestClass,
            requestMethodNameValue.getMethodName(),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD.getName());
    resetMethodId =
        getStaticMethodOrReportFailure(
            resetClass,
            resetMethodNameValue.getMethodName(),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD.getName());
  }

  private static JClass getClassOrReportFailure(JNodeLookup lookup, String name, String prop) {
    try {
      return lookup.getClass(name);
    } catch (Throwable e) {
      Jack.getSession()
          .getReporter()
          .report(Severity.FATAL, new BadBoostLockedRegionPriorityConfigurationException(prop, e));
      Jack.getSession().abortEventually();
      return null;
    }
  }

  private static JMethodId getStaticMethodOrReportFailure(
      JClass cls, String name, String prop) {
    try {
      return cls.getMethodId(name, Collections.<JType>emptyList(), MethodKind.STATIC,
          JPrimitiveTypeEnum.VOID.getType());
    } catch (Throwable e) {
      Jack.getSession()
          .getReporter()
          .report(Severity.FATAL, new BadBoostLockedRegionPriorityConfigurationException(prop, e));
      Jack.getSession().abortEventually();
      return null;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    if (lockClass == null
        || requestClass == null
        || resetClass == null
        || requestMethodId == null
        || resetMethodId == null) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(method, tr);
    visitor.accept(method);
    tr.commit();
  }

  private class Visitor extends JVisitor {
    @Nonnull private final JMethod method;
    @Nonnull private final TransformationRequest tr;

    public Visitor(@Nonnull JMethod method, @Nonnull TransformationRequest tr) {
      this.method = method;
      this.tr = tr;
    }

    @Override
    public void endVisit(@Nonnull JLock jLock) {
      assert lockClass != null;
      if (!jLock.getLockExpr().getType().isSameType(lockClass)) {
        return;
      }

      JStatementList list = (JStatementList) jLock.getParent();
      int index = list.getStatements().indexOf(jLock) + 1;
      if (index >= list.getStatements().size()) {
        abortPass();
      }

      JStatement next = list.getStatements().get(index);
      if (!(next instanceof JTryStatement)) {
        abortPass();
        return;
      }

      JTryStatement jTry = (JTryStatement) next;
      JBlock finallyBlock = jTry.getFinallyBlock();

      if (finallyBlock == null) {
        return;
      }

      tr.append(new PrependStatement(jTry.getTryBlock(), makeRequestCall(jLock.getSourceInfo())));
      tr.append(new AppendStatement(finallyBlock, makeResetCall(jLock.getSourceInfo())));
    }

    @Nonnull
    private JExpressionStatement makeRequestCall(SourceInfo info) {
      assert lockClass != null && requestClass != null && requestMethodId != null;
      return new JExpressionStatement(
          info,
          new JMethodCall(
              info, null, requestClass, requestMethodId, false));
    }

    @Nonnull
    private JExpressionStatement makeResetCall(SourceInfo info) {
      assert lockClass != null && resetClass != null && resetMethodId != null;
      return new JExpressionStatement(info,
          new JMethodCall(info, null, resetClass, resetMethodId, false));
    }
  }

  /**
   * Used to report bad configuration as the result of not setting the require properties to point
   * to valid classes or methods.
   */
  private static class BadBoostLockedRegionPriorityConfigurationException
      extends ReportableException {
    private static final long serialVersionUID = 1L;
    @Nonnull private final String prop;

    public BadBoostLockedRegionPriorityConfigurationException(
        @Nonnull String prop, @Nonnull Throwable cause) {
      super(cause);
      this.prop = prop;
    }

    @Override
    public String getMessage() {
      return getCause().getMessage() + " needed by property " + prop;
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }

  /**
   * Report to user that we cannot perform this optimization. It is unlikely this will be ever
   * called.
   */
  private static class BadBoostLockedRegionPriorityState implements Reportable {
    @Override
    public String getMessage() {
      return "Cannot perform BoostLockedRegionPriority."
          + " This is likely due to a library coming from a Jar, which is not supported.";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }

  private static void abortPass() {
    Jack.getSession().getReporter().report(Severity.FATAL, new BadBoostLockedRegionPriorityState());
    Jack.getSession().abortEventually();
  }
}
