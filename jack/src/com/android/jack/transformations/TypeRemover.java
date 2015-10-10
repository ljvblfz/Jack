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

package com.android.jack.transformations;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.log.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Abstract schedulable removing a selection of types.
 */
@Synchronized
public abstract class TypeRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  private void updateSuperTypeList(@Nonnull JDefinedClassOrInterface type) {
    if (type instanceof JDefinedClass) {
      JClass superClass = type.getSuperClass();
      while (mustBeRemovedInternal(superClass)) {
        assert superClass != null;
        for (JInterface i : ((JDefinedClass) superClass).getImplements()) {
          addImplements(type, i);
        }
        superClass = ((JDefinedClass) superClass).getSuperClass();
      }
      ((JDefinedClass) type).setSuperClass(superClass);
    }
    List<JInterface> implementsCopy = new ArrayList<JInterface>(type.getImplements());
    for (JInterface i : implementsCopy) {
      if (mustBeRemovedInternal(i)) {
        JDefinedInterface jDefinedInterface = (JDefinedInterface) i;
        type.remove(jDefinedInterface);
        for (JInterface subInterface : jDefinedInterface.getImplements()) {
          addImplements(type, subInterface);
        }
      }
    }
  }

  private boolean mustBeRemovedInternal(@CheckForNull JClassOrInterface type) {
    if (type instanceof JDefinedClassOrInterface) {
      return mustBeRemoved((JDefinedClassOrInterface) type);
    }
    return false;
  }

  protected abstract boolean mustBeRemoved(@Nonnull JDefinedClassOrInterface type);

  protected abstract boolean isPlannedForRemoval(@Nonnull JMethod method);

  private void addImplements(@Nonnull JDefinedClassOrInterface type, @Nonnull JInterface i) {
    if (!type.getImplements().contains(i)) {
      if (!mustBeRemovedInternal(i)) {
        type.addImplements(i);
      } else {
        for (JInterface subInterface : ((JDefinedInterface) i).getImplements()) {
          addImplements(type, subInterface);
        }
      }
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    boolean toRemove = mustBeRemoved(type);
    if (toRemove) {
      TransformationRequest request = new TransformationRequest(type);
      request.append(new Remove(type));
      logger.log(Level.INFO, "Removed type {0}", Jack.getUserFriendlyFormatter().getName(type));
      JClassOrInterface enclosing = type.getEnclosingType();
      if (enclosing instanceof JDefinedClassOrInterface) {
        JDefinedClassOrInterface enclosingType = (JDefinedClassOrInterface) enclosing;
        enclosingType.removeMemberType(type);
      }
      Jack.getSession().removeTypeToEmit(type);
      request.commit();
    } else {
      updateSuperTypeList(type);
      updateEnclosingType(type);
      if (type instanceof JDefinedClass) {
        updateEnclosingMethod((JDefinedClass) type);
      }
    }
  }

  private void updateEnclosingType(@Nonnull JDefinedClassOrInterface type) {
    JClassOrInterface enclosingType = type.getEnclosingType();
    while (enclosingType instanceof JDefinedClassOrInterface) {
      if (!mustBeRemovedInternal(enclosingType) || enclosingType.isExternal()) {
        break;
      }
      enclosingType = ((JDefinedClassOrInterface) enclosingType).getEnclosingType();
    }
    type.setEnclosingType(enclosingType);
  }

  private void updateEnclosingMethod(@Nonnull JDefinedClass type) {
    JMethod enclosingMethod = type.getEnclosingMethod();
    if (enclosingMethod != null && isPlannedForRemoval(enclosingMethod)) {
      assert !enclosingMethod.isExternal();
      type.setEnclosingMethod(null);
    }
  }
}