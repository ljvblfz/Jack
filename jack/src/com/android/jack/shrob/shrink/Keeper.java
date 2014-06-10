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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.shrob.seed.SeedMarker;
import com.android.jack.shrob.spec.KeepModifier;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that marks all classes and members that will be kept when
 * shrinking.
 */
@Description("Marks all classes and members that will be kept when shrinking.")
@Transform(add = KeepMarker.class)
@Constraint(need = {ExtendingOrImplementingClassMarker.class, SeedMarker.class})
@HasKeyId
public class Keeper implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Visitor extends Tracer {

    private Visitor() {
      super(ThreadConfig.get(KEEP_ENCLOSING_METHOD).booleanValue());
    }

    @Override
    public void trace(@Nonnull JDefinedClassOrInterface type) {
      if (!isMarked(type)) {
        super.trace(type);

        for (JField field : type.getFields()) {
          SeedMarker marker = field.getMarker(SeedMarker.class);
          if (marker != null && marker.getModifier() != KeepModifier.ALLOW_SHRINKING) {
            trace(field);
          }
        }

        for (JMethod method : type.getMethods()) {
          SeedMarker marker = method.getMarker(SeedMarker.class);
          if (marker != null && marker.getModifier() != KeepModifier.ALLOW_SHRINKING) {
            trace(method);
          }
        }
      }

    }

    @Override
    public boolean markIfNecessary(@Nonnull JNode node) {
      synchronized (node) {
        if (!isMarked(node)) {
          node.addMarker(new KeepMarker());
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean isMarked(@Nonnull JNode node) {
      if (node instanceof JDefinedClassOrInterface
          && ((JDefinedClassOrInterface) node).isExternal()) {
        return true;
      } else if (node instanceof JMethod
          && ((JMethod) node).getEnclosingType().isExternal()) {
        return true;
      } else {
        synchronized (node) {
          return node.containsMarker(KeepMarker.class);
        }
      }
    }

    @Override
    protected boolean mustTraceOverridingMethod(@Nonnull JMethod method) {
      if (method.getEnclosingType().isExternal()) {
        return true;
      } else {
        synchronized (method) {
          KeepMarker marker = method.getMarker(KeepMarker.class);
          if (marker != null) {
            return marker.mustTraceOverridingMethods();
          }
        }
      }
      return false;
    }

    @Override
    protected void setMustTraceOverridingMethods(@Nonnull JMethod method) {
      synchronized (method) {
        KeepMarker marker = method.getMarker(KeepMarker.class);
        if (marker != null) {
          marker.setMustTraceOverridingMethods(true);
        } else {
          assert method.getEnclosingType().isExternal();
        }
      }
    }
  }

  public static final BooleanPropertyId KEEP_ENCLOSING_METHOD = BooleanPropertyId.create(
      "jack.shrink.keep.enclosing.method",
      "Keep the enclosing method of annonymous classes").addDefaultValue(Boolean.FALSE);

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    SeedMarker marker = type.getMarker(SeedMarker.class);
    if (marker != null && marker.getModifier() != KeepModifier.ALLOW_SHRINKING) {
      Visitor visitor = new Visitor();
      visitor.trace(type);
    }
  }
}
