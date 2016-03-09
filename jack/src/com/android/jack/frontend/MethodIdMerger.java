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

import com.android.jack.Jack;
import com.android.jack.frontend.MethodIdMerger.MethodIdMerged;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.CommonTypes;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Merges ids of {@link JMethod}s with the same name and arguments in the same hierarchy tree.
 */
@Description("Merges ids of JMethods with the same name and arguments in the same hierarchy tree.")
@Transform(add = {MethodIdMerged.class, VirtualMethodsMarker.class})
public class MethodIdMerger implements RunnableSchedulable<JSession> {

  /**
   * This tag means that JMethodIds were merged when needed.
   */
  @Description("JMethodId were merged")
  @Name("MethodIdMerged")
  public static class MethodIdMerged implements Tag {
  }

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JClass javaLangObject = Jack.getSession().getPhantomLookup()
    .getClass(CommonTypes.JAVA_LANG_OBJECT);

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
    public boolean visit(@Nonnull JLambda lambda) {
      accept(lambda.getBody());
      return super.visit(lambda);
    }

    @Override
    public boolean visit(@Nonnull JPhantomClassOrInterface node) {
      if (node.getMarker(VirtualMethodsMarker.class) != null) {
        return false;
      }
      ensureHierarchyVisited(node);
      return super.visit(node);
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

      JClass zuper = getSuper(node);
      while (zuper instanceof JPhantomClassOrInterface) {
        zuper = getSuper(zuper);
      }

      VirtualMethodsMarker virtualMethods;
      if (zuper != null) {
        VirtualMethodsMarker superMarker = ((JNode) zuper).getMarker(VirtualMethodsMarker.class);
        assert superMarker != null;
        virtualMethods = superMarker.clone();
      } else {
        virtualMethods = new VirtualMethodsMarker();
      }

      for (JInterface interfaze : node.getImplements()) {
        if (interfaze instanceof JDefinedClassOrInterface) {
          addIds(virtualMethods, (JNode) interfaze);
        }
      }

      for (JMethod method : node.getMethods()) {
        if (((!method.isStatic()) && (!method.isPrivate()) && !(method instanceof JConstructor))) {
          addId(virtualMethods, method.getMethodIdWide());
        }
      }

      node.addMarker(virtualMethods);
    }

    private void addIds(@Nonnull VirtualMethodsMarker mergeInto, @Nonnull JNode toMerge) {
      VirtualMethodsMarker methodsToMerge = toMerge.getMarker(VirtualMethodsMarker.class);
      assert methodsToMerge != null;
      for (JMethodIdWide jMethodId : methodsToMerge) {
        addId(mergeInto, jMethodId);
      }
    }

    private void addId(@Nonnull VirtualMethodsMarker virtualMethods, @Nonnull JMethodIdWide toAdd) {
      JMethodIdWide existingMethod = virtualMethods.get(toAdd);
      if (existingMethod != null) {
        mergeId(existingMethod, toAdd);
      } else {
        virtualMethods.add(toAdd);
      }
    }

    private void mergeId(@Nonnull JMethodIdWide keep, @Nonnull JMethodIdWide duplicate) {

      keep = getKeptId(keep);
      duplicate = getKeptId(duplicate);

      if (keep == duplicate) {
        return;
      }

      for (JMethodId duplicateId : duplicate.getMethodIds()) {
        JMethodId keptId = keep.getMethodId(duplicateId.getType());
        if (keptId == null) {
          keptId = new JMethodId(keep, duplicateId.getType());
        }
        for (JMethod method : duplicateId.getMethods()) {
          method.setMethodId(keptId);
        }
      }
    }

      /**
   * During the merge of {@link JMethodIdWide} some are kept and some are dropped and because
   * {@link VirtualMethodsMarker} are not rewritten during merge, they contain dropped
   * {@link JMethodIdWide}. This method retrieves the kept id from an id found in a
   * {@code VirtualMethodsMarker}
   */
  @Nonnull
  private JMethodIdWide getKeptId(@Nonnull JMethodIdWide possiblyDroppedId) {
    Iterator<JMethod> methods1 = possiblyDroppedId.getMethods().iterator();
    assert methods1.hasNext() :
      "Only method id contained in JMethod are considered by this visitor";
    return methods1.next().getMethodIdWide();
  }

    @CheckForNull
    private JClass getSuper(@Nonnull JClassOrInterface node) {
      if (node instanceof JDefinedClass) {
        return ((JDefinedClass) node).getSuperClass();
      }
      if (!node.isSameType(javaLangObject)) {
        return javaLangObject;
      } else {
        return null;
      }
    }
    }

  @Override
  public void run(JSession session) throws Exception {
    new Visitor().accept(session.getTypesToEmit());
  }
}
