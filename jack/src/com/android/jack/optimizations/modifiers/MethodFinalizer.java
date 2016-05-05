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

package com.android.jack.optimizations.modifiers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.common.DirectlyDerivedClassesMarker;
import com.android.jack.optimizations.common.TypeToBeEmittedMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/** Make methods final when possible */
@Description("Tighten 'final' modifiers on methods.")
@Constraint(need = DirectlyDerivedClassesMarker.class)
@Support(Optimizations.MethodFinalizer.class)
@Transform(add = EffectivelyFinalMethodMarker.class)
public class MethodFinalizer
    implements RunnableSchedulable<JDefinedClassOrInterface> {

  private final boolean preserveReflections =
      ThreadConfig.get(Optimizations.MethodFinalizer.PRESERVE_REFLECTIONS).booleanValue();

  @Nonnull
  public static final StatisticId<Counter> METHODS_FINALIZED = new StatisticId<>(
      "jack.optimization.method-finalizer", "Methods made final",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final TypePackageAndMethodFormatter formatter = Jack.getLookupFormatter();

  @Override public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    // We want to analyze class hierarchies (trees) consisting from the classes
    // that are going to be emitted
    JClass superClass = type.getSuperClass();
    if (!(type instanceof JDefinedClass) ||
        superClass == null ||
        TypeToBeEmittedMarker.isToBeEmitted(superClass)) {
      // It is either not a class or a class which base class
      // is also a type to be emitted, skip it.
      return;
    }

    // Calculate effectively final methods
    State state = new State();
    trackOverridingChains((JDefinedClass) type, state);

    // Process effectively final methods
    for (JMethod method : state.effectivelyFinalMethods) {
      if (!method.isAbstract() && !method.isFinal()) {
        assert !method.isStatic() && !method.isPrivate() && !(method instanceof JConstructor);
        // Mark as effectively final
        EffectivelyFinalMethodMarker.markAsEffectivelyFinal(method);

        if (!preserveReflections) {
          // Mark as actually final, if strict reflections are not enforced
          method.setFinal();
          tracer.getStatistic(METHODS_FINALIZED).incValue();
        }
      }
    }
  }

  private void trackOverridingChains(@Nonnull JDefinedClass currentClass, @Nonnull State state) {
    // Before visiting derived classes we update the list of 'current'
    // virtual methods coming from base class with this class' methods
    for (JMethod method : currentClass.getMethods()) {
      if (method.getMethodIdWide().getKind() == MethodKind.INSTANCE_VIRTUAL) {
        String signature = getSignatureId(method);

        if (!state.currentMethods.containsKey(signature)) {
          // this is a method introducing new virtual method family
          state.newMethods.add(method);
        }

        // mark the signature as implemented by this method
        state.currentMethods.put(signature, method);
      }
    }

    // Visit derived classes
    for (JDefinedClass derived :
        DirectlyDerivedClassesMarker.getDirectlyDerivedClasses(currentClass)) {
      trackOverridingChains(derived, state);
    }

    // Post process methods after traversing derived classes subtrees
    for (JMethod method : currentClass.getMethods()) {
      if (method.getMethodIdWide().getKind() == MethodKind.INSTANCE_VIRTUAL) {
        String signature = getSignatureId(method);
        assert state.currentMethods.containsKey(signature);

        // The method in currentMethods collection corresponding to this signature must
        // be either this method (if it was not overridden in any of derived classes),
        // or 'null' otherwise.
        JMethod candidate = state.currentMethods.get(signature);
        if (candidate != null) {
          assert candidate == method;
          state.effectivelyFinalMethods.add(method);
        }

        if (state.newMethods.contains(method)) {
          // If this was a method starting a virtual method family,
          // just remove the signature
          state.currentMethods.remove(signature);

        } else {
          // Otherwise, make the signature map to null indicating
          // that we already saw its override
          state.currentMethods.put(signature, null);
        }
      }
    }
  }

  /** Represents state passed to trackOverridingChains(...) */
  private static class State {
    @Nonnull
    /** current virtual methods with their implementation */
    final Map<String, JMethod> currentMethods = Maps.newHashMap();

    @Nonnull
    /** new methods starting virtual method overriding trees */
    final Set<JMethod> newMethods = Sets.newIdentityHashSet();

    @Nonnull
    /** virtual methods not overridden by others */
    final Set<JMethod> effectivelyFinalMethods = Sets.newIdentityHashSet();
  }

  @Nonnull
  private String getSignatureId(@Nonnull JMethod method) {
    return formatter.getName(method);
  }
}
