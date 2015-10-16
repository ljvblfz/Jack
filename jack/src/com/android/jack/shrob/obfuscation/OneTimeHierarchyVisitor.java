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

package com.android.jack.shrob.obfuscation;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.sched.schedulable.Constraint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A visitor that visits a type hierarchy.
 */
@Constraint(need = SubClassOrInterfaceMarker.class)
public abstract class OneTimeHierarchyVisitor {
  @Nonnull
  private final List<JDefinedClassOrInterface> alreadyVisitedTypes =
      new ArrayList<JDefinedClassOrInterface>();

  public void visitSuperTypes(@Nonnull JDefinedClassOrInterface type) {
    JClass superClass = type.getSuperClass();
    if (superClass instanceof JDefinedClass) {
      JDefinedClass definedSuperClass = (JDefinedClass) superClass;
      visit(definedSuperClass);
      visitSuperTypes(definedSuperClass);
    }
    for (JInterface superInterface : type.getImplements()) {
      if (superInterface instanceof JDefinedInterface) {
        JDefinedInterface definedSuperInterface = (JDefinedInterface) superInterface;
        visit(definedSuperInterface);
        visitSuperTypes(definedSuperInterface);
      }
    }
  }

  public void visitSubTypes(@Nonnull JDefinedClassOrInterface type) {
    SubClassOrInterfaceMarker marker = type.getMarker(SubClassOrInterfaceMarker.class);
    if (marker != null) {
      Iterator<JDefinedClassOrInterface> it = marker.iterator();
      while (it.hasNext()) {
        visit(it.next());
      }
    }
  }

  public boolean visit(@Nonnull JDefinedClassOrInterface type) {
    if (!alreadyVisitedTypes.contains(type)) {
      alreadyVisitedTypes.add(type);
      return (doAction(type));
    }
    return true;
  }

  public abstract boolean doAction(@Nonnull JDefinedClassOrInterface type);
}

