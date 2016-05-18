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
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.BoostLockedRegionPriorityFeature;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;

import java.util.ArrayList;
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
 * synchronized(lock) {
 *   work();
 * }
 * </code>
 *
 * could be transformed into:
 *
 * <code>
 *
 * synchronized(lock) {
 *   try {
 *     boostPriority()
 *     work();
 *   } finally {
 *     resetPriority();
 *   }
 * }
 * </code>
 *
 * Try blocks are inserted to make sure that the reset call is always executed even in place of
 * exceptions. This is important for threads that might be reused like worker threads.
 */
@Description("Raise locked region priority for certain types of locks.")
@Transform(
  add = {
    JBlock.class,
    JCatchBlock.class,
    JExpressionStatement.class,
    JMethodCall.class,
    JTryStatement.class
  }
)
public class BoostLockedRegionPriority implements RunnableSchedulable<JMethod> {

  @CheckForNull private final JClass lockClass;
  @CheckForNull private final JClass requestClass;
  @CheckForNull private final JClass resetClass;
  @CheckForNull private final JMethodIdWide requestMethodId;
  @CheckForNull private final JMethodIdWide resetMethodId;
  @Nonnull private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  public BoostLockedRegionPriority() {
    String className = ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_CLASSNAME);
    String requestMethodFullName =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD);
    String resetMethodFullName =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD);

    int requestMethodSeperatorIdx = requestMethodFullName.indexOf('#');
    int resetMethodSeperatorIdx = resetMethodFullName.indexOf('#');

    final JNodeLookup lookup = Jack.getSession().getLookup();
    lockClass =
        getClassOrReportFailure(
            lookup,
            NamingTools.getTypeSignatureName(className),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_CLASSNAME);
    requestClass =
        getClassOrReportFailure(
            lookup,
            NamingTools.getTypeSignatureName(
                requestMethodFullName.substring(0, requestMethodSeperatorIdx)),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD);
    resetClass =
        getClassOrReportFailure(
            lookup,
            NamingTools.getTypeSignatureName(
                resetMethodFullName.substring(0, resetMethodSeperatorIdx)),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD);
    requestMethodId =
        getStaticMethodOrReportFailure(
            requestClass,
            requestMethodFullName.substring(
                requestMethodSeperatorIdx + 1, requestMethodFullName.length()),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD);
    resetMethodId =
        getStaticMethodOrReportFailure(
            resetClass,
            resetMethodFullName.substring(
                resetMethodSeperatorIdx + 1, resetMethodFullName.length()),
            BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD);
  }

  private static JClass getClassOrReportFailure(
      JNodeLookup lookup, String name, PropertyId<String> prop) {
    try {
      return lookup.getClass(name);
    } catch (Throwable e) {
      Jack.getSession()
          .getReporter()
          .report(Severity.FATAL, new BadBoostLockedRegionPriorityConfigurationException(prop, e));
      Jack.getSession().setAbortEventually(true);
      return null;
    }
  }

  private static JMethodIdWide getStaticMethodOrReportFailure(
      JClass cls, String name, PropertyId<String> prop) {
    try {
      return cls.getMethodIdWide(name, Collections.<JType>emptyList(), MethodKind.STATIC);
    } catch (Throwable e) {
      Jack.getSession()
          .getReporter()
          .report(Severity.FATAL, new BadBoostLockedRegionPriorityConfigurationException(prop, e));
      Jack.getSession().setAbortEventually(true);
      return null;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
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
    public void endVisit(@Nonnull JSynchronizedBlock jSyncBock) {
      assert lockClass != null;
      if (!jSyncBock.getLockExpr().getType().isSameType(lockClass)) {
        return;
      }
      tr.append(
          new PrependStatement(
              jSyncBock.getSynchronizedBlock(), makeRequestCall(jSyncBock.getSourceInfo())));
      JTryStatement tryStmt = makeTryStatement(jSyncBock.getSourceInfo(), jSyncBock);
      tr.append(new Replace(jSyncBock, tryStmt));
    }

    private JExpressionStatement makeRequestCall(SourceInfo info) {
      assert requestClass != null && lockClass != null && requestMethodId != null;
      return new JExpressionStatement(
          info,
          new JMethodCall(
              info, null, requestClass, requestMethodId, JPrimitiveTypeEnum.VOID.getType(), false));
    }

    private JExpressionStatement makeResetCall(SourceInfo info) {
      assert resetClass != null && lockClass != null && resetMethodId != null;
      return new JExpressionStatement(
          info,
          new JMethodCall(
              info, null, resetClass, resetMethodId, JPrimitiveTypeEnum.VOID.getType(), false));
    }

    private JTryStatement makeTryStatement(SourceInfo info, JSynchronizedBlock syncBlock) {
      JBlock tryBlock = new JBlock(info);
      tryBlock.addStmt(syncBlock);
      JBlock finallyBlock = new JBlock(info);
      finallyBlock.addStmt(makeResetCall(info));
      JTryStatement tryStmt =
          new JTryStatement(
              info,
              new ArrayList<JStatement>(),
              tryBlock,
              new ArrayList<JCatchBlock>(),
              finallyBlock);
      return tryStmt;
    }
  }

  /**
   * Used to report bad configuration as the result of not setting the require properties to point
   * to valid classes or methods.
   */
  private static class BadBoostLockedRegionPriorityConfigurationException
      extends ReportableException {
    private static final long serialVersionUID = 1L;
    @Nonnull private final PropertyId<String> prop;

    public BadBoostLockedRegionPriorityConfigurationException(
        @Nonnull PropertyId<String> prop, @Nonnull Throwable cause) {
      super(cause);
      this.prop = prop;
    }

    @Override
    public String getMessage() {
      return getCause().getMessage() + " needed by property " + prop.getName();
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }
}
