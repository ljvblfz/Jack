/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.experimental.incremental;

import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.HasType;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Find all usages between java files.
 */
@Description("Find all usages between java files")
@Name("UsageFinder")
@Transform(add = CompilerStateMarker.class)
@Synchronized
public class UsageFinder implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private final CompilerStateMarker compilerState;

    @Nonnull
    private final String currentFileName;

    public Visitor(@Nonnull JType currentType, @Nonnull CompilerStateMarker compilerState) {
      this.compilerState = compilerState;
      assert currentType.getSourceInfo() != SourceOrigin.UNKNOWN;
      currentFileName = currentType.getSourceInfo().getFileName();
      compilerState.addCodeUsage(currentFileName, null);
      compilerState.addCstUsage(currentFileName, null);
      compilerState.addStructUsage(currentFileName, null);
    }

    private void addStructUsage(@Nonnull JType usedType) {
      if (usedType.getSourceInfo() == SourceOrigin.UNKNOWN) {
        return;
      }
      String usedTypeFileName = usedType.getSourceInfo().getFileName();
      compilerState.addStructUsage(currentFileName, usedTypeFileName);
    }

    private void addCodeUsage(@Nonnull JType usedType) {
      if (usedType.getSourceInfo() == SourceOrigin.UNKNOWN) {
        return;
      }
      String usedTypeFileName = usedType.getSourceInfo().getFileName();
      compilerState.addCodeUsage(currentFileName, usedTypeFileName);
    }

    @Override
    public boolean visit(@Nonnull JDefinedClass definedClass) {
      JClass superClass = definedClass.getSuperClass();
      if (superClass != null) {
        addStructUsage(superClass);
      }

      for (JInterface interf : definedClass.getImplements()) {
        addStructUsage(interf);
      }

      return super.visit(definedClass);
    }

    @Override
    public boolean visit(@Nonnull JMethod jmethod) {
      for (JClass exception : jmethod.getThrownExceptions()) {
        addCodeUsage(exception);
      }
      return super.visit(jmethod);
    }

    @Override
    public boolean visit(@Nonnull JDefinedInterface definedInterface) {
      for (JInterface interf : definedInterface.getImplements()) {
        addStructUsage(interf);
      }
      return super.visit(definedInterface);
    }

    @Override
    public boolean visit(@Nonnull JInstanceOf instanceofStmt) {
      addCodeUsage(instanceofStmt.getTestType());
      return super.visit(instanceofStmt);
    }

    @Override
    public boolean visit(@Nonnull JMethodCall methodCall) {
      addCodeUsage(methodCall.getReceiverType());
      return super.visit(methodCall);
    }

    @Override
    public boolean visit(@Nonnull JFieldRef fieldRef) {
      addCodeUsage(fieldRef.getReceiverType());
      return super.visit(fieldRef);
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      if (node instanceof HasType && !(node instanceof JArrayLiteral)) {
        addCodeUsage(((HasType) node).getType());
      }

      return super.visit(node);
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    // Ignore external types
    if (declaredType.isExternal()) {
      return;
    }

    JSession program = declaredType.getParent(JSession.class);
    assert program != null;
    CompilerStateMarker csm = program.getMarker(CompilerStateMarker.class);
    if (csm == null) {
      csm = new CompilerStateMarker();
      program.addMarker(csm);
    }

    Visitor v = new Visitor(declaredType, csm);
    v.accept(declaredType);
  }
}
