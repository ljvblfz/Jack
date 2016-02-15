/*
 * Copyright (C) 2016 The Android Open Source Project
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
import com.android.sched.item.Component;
import com.android.sched.marker.LocalMarkerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An identifier for methods. A {@link JMethodId} instance is shared between {@link JMethod}s
 * that may share an overriding relation considering their return type.
 */
public class JMethodId extends LocalMarkerManager implements Component, HasType {

  @Nonnull
  private final JMethodIdWide methodId;
  @Nonnull
  private final JType returnType;
  @Nonnull
  private final List<JMethod> methods = new ArrayList<JMethod>();

  public JMethodId(@Nonnull JMethodIdWide methodId, @Nonnull JType returnType) {
    this.methodId = methodId;
    this.returnType = returnType;
    methodId.addMethodId(this);
  }

  @Nonnull
  @Override
  public JType getType() {
    return returnType;
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final boolean equals(@CheckForNull Object obj) {
    return obj == this;
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

  public void addMethod(@Nonnull JMethod method) {
    methods.add(method);
    assert canBeResultId();
  }

  @Nonnull
  public JMethodIdWide getMethodIdWide() {
    return methodId;
  }

  @Nonnull
  public Collection<JMethod> getMethods() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(methods);
  }

  private boolean canBeResultId() {
    for (JMethod method : methods) {
      if (!method.getMethodId().equals(this)) {
        return false;
      }
    }
    return true;
  }
}
