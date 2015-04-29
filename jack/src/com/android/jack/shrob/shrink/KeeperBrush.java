/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.shrob.shrink;

import com.android.jack.analysis.tracer.AbstractTracerBrush;
import com.android.jack.analysis.tracer.Tracer;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPhantomClass;
import com.android.jack.ir.ast.JPhantomInterface;
import com.android.jack.shrob.seed.SeedMarker;
import com.android.jack.shrob.spec.KeepModifier;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * A {@link Tracer} configuration that marks all classes and members that will be kept when
 * shrinking.
 */
@Description("Marks all classes and members that will be kept when shrinking.")
@Transform(add = {KeepMarker.class, PartialTypeHierarchy.class})
@Constraint(need = SeedMarker.class)
@HasKeyId
public class KeeperBrush extends AbstractTracerBrush<KeepMarker> {

  @Nonnull
  public static final BooleanPropertyId KEEP_ENCLOSING_METHOD = BooleanPropertyId.create(
  "jack.shrink.keep.enclosing.method",
  "Keep the enclosing method of anonymous classes").addDefaultValue(Boolean.FALSE);

  public KeeperBrush() {
    super(ThreadConfig.get(KeeperBrush.KEEP_ENCLOSING_METHOD).booleanValue(), KeepMarker.class,
        SeedMarker.class);
  }

  @Override
  protected boolean mustTraceOverridingMethod(@Nonnull JMethod method) {
    if (method.getEnclosingType().isExternal()) {
      return true;
    } else {
      return super.mustTraceOverridingMethod(method);
    }
  }

  @Nonnull
  @Override
  protected KeepMarker createMarkerFor(@Nonnull JNode node) {
    return new KeepMarker();
  }

  @Override
  protected boolean isMarked(@Nonnull JNode node) {
    if (node instanceof JDefinedClassOrInterface
        && ((JDefinedClassOrInterface) node).isExternal()) {
      return true;
    } else if (node instanceof JMethod
        && ((JMethod) node).getEnclosingType().isExternal()) {
      return true;
    } else {
      return super.isMarked(node);
    }
  }
  @Override
  public void setMustTraceOverridingMethods(@Nonnull JMethod method) {
    if (!method.getEnclosingType().isExternal()) {
      super.setMustTraceOverridingMethods(method);
    }
  }

  @Override
  protected boolean markIfNecessary(@Nonnull JNode node) {
    if (node instanceof JDefinedClassOrInterface
        && ((JDefinedClassOrInterface) node).isExternal()) {
      return false;
    } else if (node.getParent(JDefinedClassOrInterface.class).isExternal()) {
      return false;
    } else {
      return super.markIfNecessary(node);
    }
  }

  @Override
  public boolean startTraceSeed(@Nonnull JDefinedClassOrInterface type) {
    SeedMarker marker = type.getMarker(SeedMarker.class);
    return marker != null && marker.getModifier() != KeepModifier.ALLOW_SHRINKING;
  }

  @Override
  public boolean startTraceSeed(@Nonnull JMethod method) {
    SeedMarker marker = method.getMarker(SeedMarker.class);
    return marker != null && marker.getModifier() != KeepModifier.ALLOW_SHRINKING;
  }

  @Override
  public boolean startTraceSeed(@Nonnull JField field) {
    SeedMarker marker = field.getMarker(SeedMarker.class);
    return marker != null && marker.getModifier() != KeepModifier.ALLOW_SHRINKING;
  }

  @Override
  public boolean startTrace(@Nonnull JDefinedClassOrInterface type) {
    boolean traceType = markIfNecessary(type);
    if (traceType) {
      if (type instanceof JDefinedClass) {
        verifyHierarchy((JDefinedClass) type);
      } else {
        assert type instanceof JDefinedInterface;
        verifyImplementedInterfaces(type);
      }
    }
    return traceType;
  }

  private void verifyHierarchy(@Nonnull JDefinedClass t) {
    JClass superClass = t.getSuperClass();
    if (superClass instanceof JPhantomClass) {
      t.addMarker(new PartialTypeHierarchy((JPhantomClass) superClass));
    } else if (superClass != null) {
      verifyHierarchy((JDefinedClass) superClass);
    }
    verifyImplementedInterfaces(t);
  }

  private void verifyImplementedInterfaces(@Nonnull JDefinedClassOrInterface t) {
    for (JInterface jInterface : t.getImplements()) {
      if (jInterface instanceof JPhantomInterface) {
        t.addMarker(new PartialTypeHierarchy((JPhantomInterface) jInterface));
      } else {
        assert jInterface instanceof JDefinedInterface;
        verifyImplementedInterfaces((JDefinedInterface) jInterface);
      }
    }
  }
}
