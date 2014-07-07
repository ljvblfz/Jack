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
import com.android.jack.util.NamingTools;
import com.android.sched.marker.LocalMarkerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An identifier for methods. A JMethodId instance is shared between JMethods
 * that may share an overriding relation.
 */
public class JMethodId extends LocalMarkerManager implements HasName, CanBeRenamed {

  /**
   * Method hierarchy filter.
   */
  public static enum HierarchyFilter {
    SUB_AND_SUPER_TYPES,
    SUPER_TYPES,
    SUB_TYPES,
    THIS_TYPE;
  }

  @Nonnull
  private String name;
  @Nonnull
  private final List<JType> paramTypes = new ArrayList<JType>();
  @Nonnull
  private final List<JMethod> methods = new ArrayList<JMethod>();

  @Nonnull
  private final MethodKind methodKind;

  public JMethodId(@Nonnull String name, @Nonnull MethodKind kind) {
    assert !(name.contains("(") || name.contains(")"));
    assert (!(NamingTools.INIT_NAME.equals(name) || NamingTools.STATIC_INIT_NAME.equals(name)))
        || (kind != MethodKind.INSTANCE_VIRTUAL);
    this.name = name;
    this.methodKind = kind;
  }

  public JMethodId(@Nonnull String name, @Nonnull List<? extends JType> paramTypes,
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
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(methods);
  }


  /**
   * Get methods of this {@link JMethodId} located in the hierarchy of the
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
          if (jMethod.getEnclosingType().equals(reference)) {
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
    assert !(name.contains("(") || name.contains(")"));
    this.name = newName;
  }

  boolean equals(@Nonnull String otherName, @Nonnull List<? extends JType> otherParamTypes) {
    if (!(this.name.equals(otherName) && this.paramTypes.size() == otherParamTypes.size())) {
      return false;
    }

    Iterator<? extends JType> otherParams = otherParamTypes.iterator();
    for (JType param : this.paramTypes) {
      if (!param.equals(otherParams.next())) {
        return false;
      }
    }
    return true;
  }

  public void addMethod(@Nonnull JMethod method) {
    methods.add(method);
    assert canBeResultId();
  }

  @Nonnull
  public MethodKind getKind() {
    return methodKind;
  }

  public boolean canBeVirtual() {
    return methodKind == MethodKind.INSTANCE_VIRTUAL;
  }

  private boolean canBeResultId() {
    for (JMethod method : methods) {
      if (!method.getMethodId().equals(this)) {
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
