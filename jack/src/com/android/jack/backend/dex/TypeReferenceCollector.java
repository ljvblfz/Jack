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

package com.android.jack.backend.dex;

import com.android.jack.ir.ast.HasType;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitorWithAnnotation;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;

import javax.annotation.Nonnull;

/**
 * A visitor for collecting types referenced directly by a class or interface.
 */
public abstract class TypeReferenceCollector extends JVisitorWithAnnotation {

  protected abstract void collect(@Nonnull JType type);

  @Override
  public boolean visit(@Nonnull JDefinedClass definedClass) {
    JClass superClass = definedClass.getSuperClass();
    if (superClass != null) {
      collect(superClass);
    }

    for (JInterface interf : definedClass.getImplements()) {
      collect(interf);
    }

    return super.visit(definedClass);
  }

  @Override
  public boolean visit(@Nonnull JMethod jmethod) {
    ThrownExceptionMarker marker = jmethod.getMarker(ThrownExceptionMarker.class);
    if (marker != null) {
      for (JClass exception : marker.getThrownExceptions()) {
        collect(exception);
      }
    }
    return super.visit(jmethod);
  }

  @Override
  public boolean visit(@Nonnull JDefinedInterface definedInterface) {
    for (JInterface interf : definedInterface.getImplements()) {
      collect(interf);
    }
    return super.visit(definedInterface);
  }

  @Override
  public boolean visit(@Nonnull JClassLiteral classLiteral) {
    collect(classLiteral.getRefType());
    return super.visit(classLiteral);
  }

  @Override
  public boolean visit(@Nonnull JInstanceOf instanceofStmt) {
    collect(instanceofStmt.getTestType());
    return super.visit(instanceofStmt);
  }

  @Override
  public boolean visit(@Nonnull JMethodCall methodCall) {
    collect(methodCall.getReceiverType());
    return super.visit(methodCall);
  }

  @Override
  public boolean visit(@Nonnull JFieldRef fieldRef) {
    collect(fieldRef.getReceiverType());
    return super.visit(fieldRef);
  }

  @Override
  public boolean visit(@Nonnull JNode node) {
    if (node instanceof HasType && !(node instanceof JArrayLiteral)) { // JArrayLiteral has no type
      collect(((HasType) node).getType());
    }

    return super.visit(node);
  }


}
