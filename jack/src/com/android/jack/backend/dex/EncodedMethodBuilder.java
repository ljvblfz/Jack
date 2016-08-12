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

package com.android.jack.backend.dex;

import com.android.jack.Options;
import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.dex.file.EncodedMethod;
import com.android.jack.dx.dex.file.OffsettedItem;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.transformations.EmptyClinit;
import com.android.jack.transformations.ast.removeinit.FieldInitMethod;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Builds an {@code EncodedMethod} instance from a {@code JMethod} and adds it to
 * the {@code ClassDefItem} of its enclosing {@code JDeclaredType}.
 */
@Description("Builds EncodedMethod from JMethod")
@Name("EncodedMethodBuilder")
@Synchronized
@Constraint(need = {ClassDefItemMarker.class, DexCodeMarker.class},
    no = {FieldInitMethod.class, EmptyClinit.class})
@Transform(add = ClassDefItemMarker.Method.class,
    modify = ClassDefItemMarker.class)
@Protect(add = JMethod.class, modify = JMethod.class, remove = JMethod.class)
@Filter(TypeWithoutValidTypePrebuilt.class)
public class EncodedMethodBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  /**
   * Creates an {@code EncodedMethod} for the given {@code JMethod} and adds it
   * to the {@code ClassDefItem} of its {@code JDeclaredType}.
   *
   * <p>This {@code EncodedMethod} is added to the {@code ClassDefItem}'s direct
   * methods set if it is a constructor, a private method or a static method.
   * Otherwise, it is added to the virtual methods set.
   *
   * <p>If this method belongs to an external type, it is ignored. In this case,
   * no {@code EncodedMethod} is created.
   */
  @Override
  public synchronized void run(@Nonnull JMethod method) {
    JDefinedClassOrInterface declaringClass = method.getEnclosingType();

    ClassDefItemMarker classDefItemMarker =
        declaringClass.getMarker(ClassDefItemMarker.class);
    assert classDefItemMarker != null;

    ClassDefItem classDefItem = classDefItemMarker.getClassDefItem();
    assert classDefItem != null;
    EncodedMethod encodedMethod = createEncodedMethod(method);

    if (isDirectMethod(method)) {
      classDefItem.addDirectMethod(encodedMethod);
    } else {
      classDefItem.addVirtualMethod(encodedMethod);
    }
  }

  private static boolean isDirectMethod(@Nonnull JMethod method) {
    return (method.isPrivate() || method.isStatic() || method instanceof JConstructor);
  }

  @Nonnull
  private EncodedMethod createEncodedMethod(@Nonnull JMethod method) {
    CstMethodRef methodRef = RopHelper.createMethodRef(method);
    int accessFlags = getDxAccessFlagsForMethod(method);

    OffsettedItem code = null;

    if (filter.accept(this.getClass(), method) && !method.isAbstract() && !method.isNative()) {
      DexCodeMarker dcm = method.getMarker(DexCodeMarker.class);
      assert dcm != null;
      code = (OffsettedItem) dcm.getCode();
    } else {
      if (!method.isAbstract() && !method.isNative()) {
        accessFlags |= AccessFlags.ACC_ABSTRACT;
      }
    }

    return new EncodedMethod(methodRef, accessFlags, code);
  }

  private static int getDxAccessFlagsForMethod(@Nonnull JMethod method) {
    int accessFlags = method.getModifier();
    accessFlags &= ~JModifier.DEPRECATED;
    if (method instanceof JConstructor) {
      accessFlags |= AccessFlags.ACC_CONSTRUCTOR;
    }
    if (JModifier.isStaticInitializer(accessFlags)) {
      accessFlags &= ~JModifier.STATIC_INIT;
      accessFlags |= AccessFlags.ACC_CONSTRUCTOR;
    }
    if (JModifier.isSynchronized(accessFlags)) {
      assert !JModifier.isBridge(accessFlags);
      accessFlags |= AccessFlags.ACC_DECLARED_SYNCHRONIZED;
      if (!JModifier.isNative(accessFlags)) {
        // Dx requires ACC_SYNCHRONIZED only for native method with synchronized modifier in java
        // source code
        accessFlags &= ~AccessFlags.ACC_SYNCHRONIZED;
      }
    }

    return accessFlags & AccessFlags.METHOD_FLAGS;
  }

}
