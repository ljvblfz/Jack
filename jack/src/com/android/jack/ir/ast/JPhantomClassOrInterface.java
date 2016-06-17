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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Phantom class or interface. It is known to exist because references to the type exists, but the
 * type is not supported by a source or a classpath entry.
 */
@Description("Phantom class or interface")
public class JPhantomClassOrInterface extends JReferenceTypeCommon implements JClassOrInterface {

  @Nonnull
  private JPackage enclosingPackage;

  @Nonnull
  private final List<JFieldId> fields = new ArrayList<JFieldId>();

  @Nonnull
  private final List<JMethodIdWide> methodIdsWide = new ArrayList<JMethodIdWide>();

  @Nonnull
  private final List<JMethodId> methodIds = new ArrayList<JMethodId>();

  public JPhantomClassOrInterface(@Nonnull String name, @Nonnull JPackage enclosingPackage) {
    super(SourceInfo.UNKNOWN, name);
    assert NamingTools.isTypeIdentifier(name);
    this.enclosingPackage = enclosingPackage;
  }

  @Override
  @Nonnull
  public JPackage getEnclosingPackage() {
    return enclosingPackage;
  }

  @Override
  public void setEnclosingPackage(@CheckForNull JPackage enclosingPackage) {
    assert enclosingPackage != null;
    this.enclosingPackage = enclosingPackage;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  @CheckForNull
  public JPrimitiveType getWrappedType() {
    return getWrappedType(this);
  }

  @Override
  public boolean isToEmit() {
    return false;
  }

  @Override
  @Nonnull
  public JFieldId getOrCreateFieldId(@Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    synchronized (fields) {
      for (JFieldId field : fields) {
        if (field.equals(name, type, kind)) {
          return field;
        }
      }
      JFieldId newField = new JFieldId(name, type, kind);
      fields.add(newField);
      return newField;
    }
  }

  @Override
  @Nonnull
  public JFieldId getFieldId(
      @Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    return getOrCreateFieldId(name, type, kind);
  }

  @Override
  public boolean canBeSafelyUpcast(@Nonnull JReferenceType castTo) {
    return isTrivialCast(castTo);
  }

  @Nonnull
  @Override
  public JMethodIdWide getMethodIdWide(
      @Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind) {
    synchronized (methodIdsWide) {
      for (JMethodIdWide id : methodIdsWide) {
        if (id.equals(name, argsType, kind)) {
          return id;
        }
      }
      JMethodIdWide newMethod = new JMethodIdWide(name, argsType, kind);
      methodIdsWide.add(newMethod);
      return newMethod;
    }
  }

  @Nonnull
  @Override
  public JMethodIdWide getOrCreateMethodIdWide(@Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind) {
    return getMethodIdWide(name, argsType, kind);
  }

  @Nonnull
  @Override
  public JMethodId getMethodId(
      @Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind,
      @Nonnull JType returnType) {
    synchronized (methodIdsWide) {
      for (JMethodId id : methodIds) {
        if (id.getType().equals(returnType) && id.getMethodIdWide().equals(name, argsType, kind)) {
          return id;
        }
      }

      JMethodId newMethod =
          new JMethodId(getOrCreateMethodIdWide(name, argsType, kind), returnType);
      methodIds.add(newMethod);
      return newMethod;
    }
  }

  @Nonnull
  @Override
  public JMethodId getOrCreateMethodId(@Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind,
      @Nonnull JType returnType) {
    return getMethodId(name, argsType, kind, returnType);
  }

  @Override
  public final boolean isSameType(@Nonnull JType type) {
    if (type instanceof HasEnclosingPackage) {
      return this.getEnclosingPackage() == ((HasEnclosingPackage) type).getEnclosingPackage()
          && name.equals(type.getName());
    } else {
      return false;
    }
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JPackage)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }

  @Override
  public void setName(@Nonnull String name) {
    enclosingPackage.removeItemWithName(this);
    super.setName(name);
  }
}
