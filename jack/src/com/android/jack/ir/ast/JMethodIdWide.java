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

package com.android.jack.ir.ast;

import com.android.jack.Jack;
import com.android.jack.ir.HierarchyFilter;
import com.android.jack.ir.StringInterner;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Component;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An identifier for methods. A JMethodId instance is shared between JMethods
 * that may share an overriding relation.
 */
@Deprecated
public class JMethodIdWide extends LocalMarkerManager implements HasName, CanBeRenamed, Component {

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private String name;
  @Nonnull
  private final List<JType> paramTypes = new ArrayList<JType>();
  @Nonnull
  private final Map<JType, JMethodId> methodIds =
    new HashMap<JType, JMethodId>();

  @Nonnull
  private MethodKind methodKind;

  public JMethodIdWide(@Nonnull String name, @Nonnull MethodKind kind) {
    assert !(name.contains("(") || name.contains(")"));
    assert (!(NamingTools.INIT_NAME.equals(name) || NamingTools.STATIC_INIT_NAME.equals(name)))
        || (kind != MethodKind.INSTANCE_VIRTUAL);
    this.name = StringInterner.get().intern(name);
    this.methodKind = kind;
  }

  public JMethodIdWide(@Nonnull String name, @Nonnull List<? extends JType> paramTypes,
      @Nonnull MethodKind methodKind) {
    this(name, methodKind);
    this.paramTypes.addAll(paramTypes);
  }

  /**
   * Adds a parameter type to this method id.
   */
  public void addParam(@Nonnull JType x) {
    paramTypes.add(x);
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  public Collection<JMethod> getMethods() {
    ArrayList<JMethod> methods = new ArrayList<JMethod>();
    for (JMethodId id : methodIds.values()) {
      methods.addAll(id.getMethods());
    }
    return methods;
  }

  @Nonnull
  public Collection<JMethodId> getMethodIds() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(methodIds.values());
  }

  /**
   * Get methods of this {@link JMethodIdWide} located in the hierarchy of the
   * {@link javax.lang.model.type.ReferenceType ReferenceType} argument. The search is inclusive so
   * if a method with the given id is found in {@code reference}, then it will be included in
   * returned collection.
   *
   * @param reference the methods will be searched in the hierarchy of this type.
   * @param filter tells if the hierarchy must be searched in its whole, only super classes and
   *        interfaces or only sub classes and interfaces.
   */
  @Nonnull
  public Collection<JMethod> getMethods(
      @Nonnull JReferenceType reference, @Nonnull HierarchyFilter filter) {
    Collection<JMethod> methods = getMethods();
    List<JMethod> subset = new ArrayList<JMethod>(methods.size());
    switch (filter) {
      case SUPER_TYPES:
        for (JMethod jMethod : methods) {
          if (reference.canBeSafelyUpcast(jMethod.getEnclosingType())) {
            subset.add(jMethod);
          }
        }
        break;
      case SUB_TYPES:
        for (JMethod jMethod : methods) {
          if (jMethod.getEnclosingType().canBeSafelyUpcast(reference)) {
            subset.add(jMethod);
          }
        }
        break;
      case SUB_AND_SUPER_TYPES:
        for (JMethod jMethod : methods) {
          if (reference.canBeSafelyUpcast(jMethod.getEnclosingType())
              || jMethod.getEnclosingType().canBeSafelyUpcast(reference)) {
            subset.add(jMethod);
          }
        }
        break;
      case THIS_TYPE:
        for (JMethod jMethod : methods) {
          if (jMethod.getEnclosingType().isSameType(reference)) {
            subset.add(jMethod);
          }
        }
        break;
      default:
        throw new AssertionError();
    }

    return subset;
  }

  @Nonnull
  public List<JType> getParamTypes() {
    return paramTypes;
  }

  @Override
  public void setName(@Nonnull String newName) {
    assert !(newName.contains("(") || newName.contains(")"));
    this.name = StringInterner.get().intern(newName);
  }

  boolean equals(@Nonnull String otherName, @Nonnull List<? extends JType> otherParamTypes,
      @Nonnull MethodKind kind) {
    if (this.methodKind != kind) {
      return false;
    }

    return equals(otherName, otherParamTypes);
  }

  public boolean equals(@Nonnull String otherName, @Nonnull List<? extends JType> otherParamTypes) {
    if (!(this.name.equals(otherName) && this.paramTypes.size() == otherParamTypes.size())) {
      return false;
    }

    Iterator<? extends JType> otherParams = otherParamTypes.iterator();
    for (JType param : this.paramTypes) {
      if (!param.isSameType(otherParams.next())) {
        return false;
      }
    }

    return true;
  }

  public void addMethodId(@Nonnull JMethodId methodId) {
    methodIds.put(methodId.getType(), methodId);
    assert canBeResultId();
  }

  @CheckForNull
  public JMethodId getMethodId(@Nonnull JType returnType) {
    return methodIds.get(returnType);
  }

  @Nonnull
  public MethodKind getKind() {
    return methodKind;
  }

  @Deprecated
  public void setKind(@Nonnull MethodKind methodKind) {
    assert getMethods().size() == 1;
    assert methodKind != MethodKind.INSTANCE_VIRTUAL;
    assert this.methodKind != MethodKind.INSTANCE_VIRTUAL;
    this.methodKind = methodKind;
  }

  public boolean canBeVirtual() {
    return methodKind == MethodKind.INSTANCE_VIRTUAL;
  }

  private boolean canBeResultId() {
    for (JMethodId id : methodIds.values()) {
      if (!id.getMethodIdWide().equals(this)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final boolean equals(@CheckForNull Object obj) {
    return obj == this;
  }
}
