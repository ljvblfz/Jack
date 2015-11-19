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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Look for code triggering a Dalvik bug and warn about it.
 *
 * The dalvik bug can be triggered by code in a class 'C' containing a check-cast, instance-of,
 * *new-array and const-class targeting an array of a protected class 'I' inner of a class 'D'. The
 * bug happens when 'C' is in the hierarchy of 'D', 'C' and 'D' are not in the same package and not
 * in the same dex. The bug causes the VM to throw unexpected IllegalAccessError depending on the
 * classes partitioning between dex files.</p>
 * </p>
 * Here is a sample code triggering the bug:</p><code>
 * package p1;</p>
 * </p>
 * public class A {</p>
 * protected static class Inner{}</p>
 * }</p>
 * </p>
 * </p>
 * package p2;</p>
 * </p>
 * public class B extends p1.A {</p>
 *   Object field = new Inner[0];</p>
 * }</p>
 * </code>
 */
@Description("Check triggers of Dalvik bug about usage of arrays of protected inner classes.")
@Support(DalvikProtectedInnerChecker.DalvikProtectedInnerCheck.class)
public class DalvikProtectedInnerChecker implements RunnableSchedulable<JMethod> {

  /**
   * Feature indicating that classes should be checked for code triggering a Dalvik bug about usage
   * of arrays of protected inner classes
   */
  @Name("DalvikProtectedInnerCheck")
  @Description("Check triggers of Dalvik bug about usage of arrays of protected inner classes")
  public static class DalvikProtectedInnerCheck implements Feature {
  }

  private static class RiskyAccessToArrayOfInner implements Reportable, HasSourceInfo {
    @Nonnull
    private final JNode node;
    @Nonnull
    private final JDefinedClassOrInterface inner;

    private RiskyAccessToArrayOfInner(@Nonnull JDefinedClassOrInterface inner, @Nonnull JNode node)
    {
      this.node = node;
      this.inner = inner;
    }

    @Nonnull
    @Override
    public SourceInfo getSourceInfo() {
      return node.getSourceInfo();
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Usage of array of '" + Jack.getUserFriendlyFormatter().getName(inner)
          + "' may trigger an Android KitKat bug"
          + " throwing an IllegalAccessError. As a workaround you may change the inner class"
          + " visibility to public or remove its usages as array";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }

  }

  private static class TypeAccessVisitor extends JVisitor {

    @Nonnull
    private final JDefinedClassOrInterface currentType;

    public TypeAccessVisitor(@Nonnull JDefinedClassOrInterface currentType) {
      this.currentType = currentType;
    }

    @Override
    public void endVisit(@Nonnull JDynamicCastOperation cast) {
      check(cast.getType(), cast);
    }

    @Override
    public void endVisit(@Nonnull JClassLiteral constClass) {
      check(constClass.getRefType(), constClass);
    }

    @Override
    public void endVisit(@Nonnull JInstanceOf instanceOf) {
      check(instanceOf.getTestType(), instanceOf);
    }

    @Override
    public void endVisit(@Nonnull JNewArray newArray) {
      check(newArray.getArrayType(), newArray);
    }

    private void check(@Nonnull JType type, @Nonnull JNode node) {
      if (type instanceof JArrayType) {
        JType leafType = ((JArrayType) type).getLeafType();
        if (leafType instanceof JDefinedClassOrInterface) {
          JDefinedClassOrInterface definedLeafType = (JDefinedClassOrInterface) leafType;
          if (definedLeafType.isProtected()
              && !definedLeafType.getEnclosingPackage().equals(currentType.getEnclosingPackage())) {
            Jack.getSession().getReporter().report(Severity.NON_FATAL,
                new RiskyAccessToArrayOfInner(definedLeafType, node));
          }
        }
      }
    }
  }

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  /**
   * Checked bug was fixed in Android API version 20.
   */
  private final boolean needCheck =
      ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL).intValue() < 20;

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if ((!needCheck) || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TypeAccessVisitor typeAccessVisitor = new TypeAccessVisitor(method.getEnclosingType());
    typeAccessVisitor.accept(method);
  }

}
