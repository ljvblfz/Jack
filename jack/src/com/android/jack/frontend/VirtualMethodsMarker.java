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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

  private static class ComparableMethodId {
    private final int hashCode;
    @Nonnull
    private final JMethodId methodId;
    private ComparableMethodId(@Nonnull JMethodId methodId) {
      this.methodId = methodId;
      int code = methodId.getName().hashCode();
      for (JType type : methodId.getParamTypes()) {
        code ^= type.hashCode();
      }
      hashCode = code;
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(@CheckForNull Object obj) {
      if (obj == this) {
        return true;
      }
      ComparableMethodId other;
      int otherHashCode;
      try {
        other = (ComparableMethodId) obj;
        otherHashCode = other.hashCode;
      } catch (ClassCastException e) {
        return false;
      } catch (NullPointerException e) {
        return false;
      }
      if (hashCode != otherHashCode) {
        return false;
      }
      List<JType> thisParams = methodId.getParamTypes();
      List<JType> otherParams = other.methodId.getParamTypes();

      if (thisParams.size() != otherParams.size()
          || !methodId.getName().equals(other.methodId.getName())) {
        return false;
      }
      Iterator<JType> otherIterator = otherParams.iterator();
      for (JType thisParam : thisParams) {
        if (thisParam != otherIterator.next()) {
          return false;
        }
      }
      return true;
    }
  }

  @Nonnull
  private HashMap<ComparableMethodId, ComparableMethodId> virtualMethods;

  public VirtualMethodsMarker() {
    virtualMethods =
        new HashMap<ComparableMethodId, ComparableMethodId>();
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  @Override
  public VirtualMethodsMarker clone() {
    VirtualMethodsMarker clone;
    try {
      clone = (VirtualMethodsMarker) super.clone();
      clone.virtualMethods =
          (HashMap<ComparableMethodId, ComparableMethodId>) virtualMethods.clone();
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
    ComparableMethodId comparable = new ComparableMethodId(method);
    virtualMethods.put(comparable, comparable);
  }

  @CheckForNull
  public JMethodId get(@Nonnull JMethodId method) {
    ComparableMethodId searched = new ComparableMethodId(method);
    ComparableMethodId found = virtualMethods.get(searched);
    if (found != null) {
      return found.methodId;
    } else {
      return null;
    }
  }

  @Nonnull
  @Override
  public Iterator<JMethodId> iterator() {
    return new Iterator<JMethodId>() {

      @Nonnull
      private final Iterator<ComparableMethodId> iterator = virtualMethods.values().iterator();

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Nonnull
      @Override
      public JMethodId next() {
        return iterator.next().methodId;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

}
