/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.frontend;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JVisitor;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Merges ids of {@link JMethod}s with the same name and arguments in the same hierarchy tree.
 */
public class MethodIdMerger extends JVisitor {

  @Nonnull
  private final JClass javaLangObject;

  public MethodIdMerger(@Nonnull JClass javaLangObject) {
    this.javaLangObject = javaLangObject;
  }

  @Override
  public boolean visit(@Nonnull JDefinedClass node) {
    if (node.getMarker(VirtualMethodsMarker.class) != null) {
      return false;
    }
    handleDefinedClassOrInterface(node);
    return super.visit(node);
  }

  @Override
  public boolean visit(@Nonnull JDefinedInterface node) {
    if (node.getMarker(VirtualMethodsMarker.class) != null) {
      return false;
    }
    handleDefinedClassOrInterface(node);
    return super.visit(node);
  }

  @Override
  public boolean visit(@Nonnull JPhantomClassOrInterface node) {
    if (node.getMarker(VirtualMethodsMarker.class) != null) {
      return false;
    }
    ensureHierarchyVisited(node);
    return super.visit(node);
  }
  @Override
  public boolean visit(@Nonnull JMethod x) {
    return false;
  }

  private void ensureHierarchyVisited(@Nonnull JClassOrInterface node) {
    JClass zuper = getSuper(node);
    if (zuper != null) {
      accept(zuper);
    }
    if (node instanceof JDefinedClassOrInterface) {
      for (JClassOrInterface interfaze : ((JDefinedClassOrInterface) node).getImplements()) {
        accept(interfaze);
      }
    }
  }

  private void handleDefinedClassOrInterface(@Nonnull JDefinedClassOrInterface node) {
    ensureHierarchyVisited(node);

    VirtualMethodsMarker virtualMethods = new VirtualMethodsMarker();

    JClass zuper = getSuper(node);
    while (zuper instanceof JPhantomClassOrInterface) {
      zuper = getSuper(zuper);
    }
    if (zuper != null) {
      addIds(virtualMethods, (JNode) zuper);
    }

    for (JInterface interfaze : node.getImplements()) {
      if (interfaze instanceof JDefinedClassOrInterface) {
        addIds(virtualMethods, (JNode) interfaze);
      }
    }

    for (JMethod method : node.getMethods()) {
      if (((!method.isStatic()) && (!method.isPrivate()) && !(method instanceof JConstructor))) {
        addId(virtualMethods, method.getMethodId());
      }
    }
    node.addMarker(virtualMethods);
  }

  private void addIds(@Nonnull VirtualMethodsMarker mergeInto, @Nonnull JNode toMerge) {
    VirtualMethodsMarker methodsToMerge = toMerge.getMarker(VirtualMethodsMarker.class);
    assert methodsToMerge != null;
    for (JMethodId jMethodId : methodsToMerge) {
      addId(mergeInto, jMethodId);
    }
  }

  private void addId(@Nonnull VirtualMethodsMarker virtualMethods, @Nonnull JMethodId toAdd) {
    JMethodId existingMethod = virtualMethods.get(toAdd);
    if (existingMethod != null) {
      mergeId(existingMethod, toAdd);
    } else {
      virtualMethods.add(toAdd);
    }
  }

  private void mergeId(@Nonnull JMethodId keep, @Nonnull JMethodId duplicate) {

    keep = getKeptId(keep);
    duplicate = getKeptId(duplicate);

    if (keep == duplicate) {
      return;
    }

    for (JMethod method : duplicate.getMethods()) {
      method.setMethodId(keep);
    }
  }

  /**
   * During the merge of {@link JMethodId} some are kept and some are dropped and because
   * {@link VirtualMethodsMarker} are not rewritten during merge, they contain dropped
   * {@code JMethodId}. This method retrieves the kept id from an id found in a
   * {@code VirtualMethodsMarker}
   */
  @Nonnull
  private JMethodId getKeptId(@Nonnull JMethodId possiblyDroppedId) {
    Iterator<JMethod> methods1 = possiblyDroppedId.getMethods().iterator();
    assert methods1.hasNext() :
      "Only method id contained in JMethod are considered by this visitor";
    return methods1.next().getMethodId();
  }

  @CheckForNull
  private JClass getSuper(@Nonnull JClassOrInterface node) {
    if (node instanceof JDefinedClass) {
      return ((JDefinedClass) node).getSuperClass();
    }
    if (!node.equals(javaLangObject)) {
      return javaLangObject;
    } else {
      return null;
    }
  }
}
