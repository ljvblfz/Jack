/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Set of virtual methods visible in the marked type. For this set, 2 {@link JMethodId}s are
 * considered equals when their name and argument types are equals.
 */
@Description("Set of virtual methods visible in the marked type.")
@ValidOn({JDefinedClassOrInterface.class, JPhantomClassOrInterface.class})
public class VirtualMethodsMarker implements Marker, Iterable<JMethodId>, Cloneable {

  /**
   * A remover for {@link VirtualMethodsMarker}
   */
  public static class Remover extends JVisitor {

    @Nonnull
    private final JClass javaLangObject;

    public Remover(@Nonnull JClass javaLangObject) {
      super(false /* needLoading */);
      this.javaLangObject = javaLangObject;
    }

    @Override
    public boolean visit(@Nonnull JDefinedClass definedClass) {
      if (definedClass.removeMarker(VirtualMethodsMarker.class) != null) {
        ensureHierarchyVisited(definedClass);
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDefinedInterface defineInterface) {
      if (defineInterface.removeMarker(VirtualMethodsMarker.class) != null) {
        ensureHierarchyVisited(defineInterface);
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JPhantomClassOrInterface phantomClassOrInterface) {
      if (phantomClassOrInterface.removeMarker(VirtualMethodsMarker.class) != null) {
        ensureHierarchyVisited(phantomClassOrInterface);
      }
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

  @Nonnull
  private static final Comparator<JMethodId> methodIdComparator = new Comparator<JMethodId>() {

    @Override
    public int compare(JMethodId o1, JMethodId o2) {
      return Jack.getLookupFormatter().getNameWithoutReturnType(o1).compareTo(
          Jack.getLookupFormatter().getNameWithoutReturnType(o2));
    }

  };

  @Nonnull
  private TreeSet<JMethodId> virtualMethods;

  public VirtualMethodsMarker() {
    virtualMethods =
        new TreeSet<JMethodId>(methodIdComparator);
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  @Override
  public VirtualMethodsMarker clone() {
    VirtualMethodsMarker clone;
    try {
      clone = (VirtualMethodsMarker) super.clone();
      clone.virtualMethods = (TreeSet<JMethodId>) virtualMethods.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  @Nonnull
  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  public void add(@Nonnull JMethodId method) {
    virtualMethods.add(method);
  }

  @CheckForNull
  public JMethodId get(@Nonnull JMethodId method) {
    JMethodId found = virtualMethods.ceiling(method);
    if (found != null && (methodIdComparator.compare(method, found) == 0)) {
      return found;
    } else {
      return null;
    }
  }

  @Nonnull
  @Override
  public Iterator<JMethodId> iterator() {
    return virtualMethods.iterator();
  }

}
