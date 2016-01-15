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

package com.android.jack.backend.dex;

import com.android.jack.Options;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.shrob.shrink.KeepMarker;
import com.android.jack.shrob.shrink.Shrinking;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that marks all classes that must be kept in the main dex.
 */
@Description("Marks all classes that must be be kept in the main dex.")
@Transform(add = MainDexMarker.class)
@Constraint(need = ForceInMainDexMarker.class)
@Support(MultiDex.class)
@Optional(value = {
    @ToSupport(feature = Shrinking.class, add = @Constraint(need = KeepMarker.class)),
    @ToSupport(
        feature = MultiDexLegacy.class,
        add = @Constraint(need = MultiDexLegacyTracerBrush.TracerMarker.class))})
public class MainDexCollector extends TypeReferenceCollector
  implements RunnableSchedulable<JDefinedClassOrInterface> {

  private final boolean isShrinkEnabled = ThreadConfig.get(Options.SHROB_ENABLED).booleanValue()
      && ThreadConfig.get(Options.FLAGS).shrink();

  @Override
  protected void collect(@Nonnull JType type) {
    if (type instanceof JDefinedClassOrInterface) {
      JDefinedClassOrInterface jDefinedClassOrInterface = (JDefinedClassOrInterface) type;
      synchronized (jDefinedClassOrInterface) {
        if (!jDefinedClassOrInterface.containsMarker(MainDexMarker.class)) {
          jDefinedClassOrInterface.addMarker(MainDexMarker.INSTANCE);
        }
      }
    }
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    if (type.containsMarker(MultiDexLegacyTracerBrush.TracerMarker.class)
        && ((!isShrinkEnabled) || type.containsMarker(KeepMarker.class))) {
      collect(type);
      accept(type);
    } else if (type.containsMarker(ForceInMainDexMarker.class)) {
      collect(type);
    }
  }

}
