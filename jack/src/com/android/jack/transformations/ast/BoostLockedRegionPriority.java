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
import java.util.List;

import javax.annotation.Nonnegative;
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
@Constraint(need = {JLock.class, JUnlock.class},
    no = {JSynchronizedBlock.class})
@Transform(add = {JExpressionStatement.class, JMethodCall.class})
public class BoostLockedRegionPriority implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final JClass[] lockClass;
  @Nonnull
  private final JClass[] requestClass;
  @Nonnull
  private final JClass[] resetClass;
  @Nonnull
  private final JMethodId[] requestMethodId;
  @Nonnull
  private final JMethodId[] resetMethodId;
  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  public BoostLockedRegionPriority() {
    List<String> classNames =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_CLASSNAME);
    List<MethodNameValue> requestMethodNameValues =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD);
    List<MethodNameValue> resetMethodNameValues =
        ThreadConfig.get(BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD);

    // Check the make sure the number of boost / reset methods is same as number of locks.
    int totalLocks = classNames.size();
    if (totalLocks != requestMethodNameValues.size()) {
      Jack.getSession().getReporter().report(Severity.FATAL,
          new BadBoostLockedRegionPriorityMethods(
              BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD.getName(), totalLocks,
              requestMethodNameValues.size()));
    }
    if (totalLocks != resetMethodNameValues.size()) {
      Jack.getSession().getReporter().report(Severity.FATAL,
          new BadBoostLockedRegionPriorityMethods(
              BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD.getName(), totalLocks,
              resetMethodNameValues.size()));
    }

    lockClass = new JClass[totalLocks];
    resetClass = new JClass[totalLocks];
    requestClass = new JClass[totalLocks];
    requestMethodId = new JMethodId[totalLocks];
    resetMethodId = new JMethodId[totalLocks];

    final JNodeLookup lookup = Jack.getSession().getLookup();
    for (int i = 0; i < totalLocks; i++) {
      lockClass[i] =
          getClassOrReportFailure(lookup, NamingTools.getTypeSignatureName(classNames.get(i)),
              BoostLockedRegionPriorityFeature.BOOST_LOCK_CLASSNAME.getName());
      requestClass[i] = getClassOrReportFailure(lookup,
          NamingTools.getTypeSignatureName(requestMethodNameValues.get(i).getClassName()),
          BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD.getName());
      resetClass[i] = getClassOrReportFailure(lookup,
          NamingTools.getTypeSignatureName(resetMethodNameValues.get(i).getClassName()),
          BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD.getName());
      requestMethodId[i] = getStaticMethodOrReportFailure(requestClass[i],
          requestMethodNameValues.get(i).getMethodName(),
          BoostLockedRegionPriorityFeature.BOOST_LOCK_REQUEST_METHOD.getName());
      resetMethodId[i] = getStaticMethodOrReportFailure(resetClass[i],
          resetMethodNameValues.get(i).getMethodName(),
          BoostLockedRegionPriorityFeature.BOOST_LOCK_RESET_METHOD.getName());
    }
  }

  private static JClass getClassOrReportFailure(JNodeLookup lookup, String name, String prop) {
    try {
      return lookup.getClass(name);
    } catch (Throwable e) {
      Jack.getSession().getReporter().report(Severity.FATAL,
          new BadBoostLockedRegionPriorityConfigurationException(prop, e));
      Jack.getSession().abortEventually();
      return null;
    }
  }

  private static JMethodId getStaticMethodOrReportFailure(JClass cls, String name, String prop) {
    try {
      return cls.getMethodId(name, Collections.<JType>emptyList(), MethodKind.STATIC,
          JPrimitiveTypeEnum.VOID.getType());
    } catch (Throwable e) {
      Jack.getSession().getReporter().report(Severity.FATAL,
          new BadBoostLockedRegionPriorityConfigurationException(prop, e));
      Jack.getSession().abortEventually();
      return null;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    if (lockClass.length == 0) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(method, tr);
    visitor.accept(method);
    tr.commit();
  }

  private class Visitor extends JVisitor {
    @Nonnull
    private final JMethod method;
    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull JMethod method, @Nonnull TransformationRequest tr) {
      this.method = method;
      this.tr = tr;
    }

    @Override
    public void endVisit(@Nonnull JLock jLock) {
      assert lockClass != null;
      int lockIndex = -1;
      for (int i = 0; i < lockClass.length; i++) {
        if (jLock.getLockExpr().getType().isSameType(lockClass[i])) {
          lockIndex = i;
          break;
        }
      }

      if (lockIndex == -1) {
        return;
      }

      JStatementList list = (JStatementList) jLock.getParent();
      assert list != null;
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

      tr.append(new PrependStatement(jTry.getTryBlock(),
          makeRequestCall(lockIndex, jLock.getSourceInfo())));
      tr.append(new AppendStatement(finallyBlock, makeResetCall(lockIndex, jLock.getSourceInfo())));
    }

    @Nonnull
    private JExpressionStatement makeRequestCall(int lockIndex, SourceInfo info) {
      assert lockClass[lockIndex] != null && requestClass[lockIndex] != null
          && requestMethodId[lockIndex] != null;
      return new JExpressionStatement(info,
          new JMethodCall(info, null, requestClass[lockIndex], requestMethodId[lockIndex], false));
    }

    @Nonnull
    private JExpressionStatement makeResetCall(int lockIndex, SourceInfo info) {
      assert lockClass[lockIndex] != null && requestClass[lockIndex] != null
          && requestMethodId[lockIndex] != null;
      return new JExpressionStatement(info,
          new JMethodCall(info, null, resetClass[lockIndex], resetMethodId[lockIndex], false));
    }
  }

  /**
   * Used to report bad configuration as the result of not setting the require properties to point
   * to valid classes or methods.
   */
  private static class BadBoostLockedRegionPriorityConfigurationException
      extends ReportableException {
    private static final long serialVersionUID = 1L;
    @Nonnull
    private final String prop;

    public BadBoostLockedRegionPriorityConfigurationException(@Nonnull String prop,
        @Nonnull Throwable cause) {
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

  /**
   * Report to user the number of reset / boost method does not match the number of classes.
   */
  private static class BadBoostLockedRegionPriorityMethods implements Reportable {

    @Nonnull
    private final String methodName;

    @Nonnegative
    private final int numLocks;

    @Nonnegative
    private final int numMethods;

    public BadBoostLockedRegionPriorityMethods(String methodName, int numLocks, int numMethods) {
      this.methodName = methodName;
      this.numLocks = numLocks;
      this.numMethods = numMethods;
    }

    @Override
    public String getMessage() {
      return "Number of methods in " + methodName + " is " + numMethods + " but number of locks is "
          + numLocks;
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
