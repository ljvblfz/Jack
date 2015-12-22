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

package com.android.jack.analysis.dependency.type;

import com.android.jack.Jack;
import com.android.jack.backend.dex.TypeReferenceCollector;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JType;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Collect type dependencies.
 */
@Description("Collect type dependencies")
@Name("TypeDependenciesCollector")
@Transform(add = TypeDependencies.Collected.class)
@Synchronized
public class TypeDependenciesCollector implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final TypeDependencies typeDependencies = Jack.getSession().getTypeDependencies();

  private class Visitor extends TypeReferenceCollector {

    @Nonnull
    private final JType currentType;

    public Visitor(@Nonnull JType currentType) {
      this.currentType = currentType;

      if (currentType instanceof JDefinedClassOrInterface) {
        if (currentType instanceof JDefinedClass) {
          JClass superClass = ((JDefinedClass) currentType).getSuperClass();
          if (superClass != null) {
            typeDependencies.addHierarchyDependency(currentType, superClass);
          }
        }

        for (JInterface interf : ((JDefinedClassOrInterface) currentType).getImplements()) {
          typeDependencies.addHierarchyDependency(currentType, interf);
        }
      }
    }

    @Override
    public void endVisit(@Nonnull JType type) {
      if (type instanceof JDefinedClassOrInterface) {
        typeDependencies.createEmptyDependencyIfNeeded(currentType);
      }
    }

    @Override
    protected void collect(@Nonnull JType usedType) {
      if (usedType instanceof JArrayType) {
        usedType = ((JArrayType) usedType).getLeafType();
      }
      if (!(usedType instanceof JPrimitiveType)) {
        typeDependencies.addCodeDependency(currentType, usedType);
      }
    }
  }

  @Override
  public synchronized void run(JDefinedClassOrInterface declaredType) throws Exception {
    Visitor v = new Visitor(declaredType);
    v.accept(declaredType);
  }

}
