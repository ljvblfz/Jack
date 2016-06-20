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

package com.android.jack.optimizations.valuepropagation.argument;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.optimizations.common.LiteralValueListTracker;
import com.android.jack.optimizations.common.SimpleLiteralValueListTracker;
import com.android.jack.optimizations.common.TypeToBeEmittedMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Propagates single-valued arguments */
@Description("Argument value propagation, propagation of single-valued arguments")
@Constraint(need = { TaintedVirtualMethodsMarker.class,
                     TypeMethodCallArgumentsMarker.class,
                     TypeToBeEmittedMarker.class })
@Transform(add = { MethodCallArgumentsMarker.class,
                   AvpSchedulable.TaintedMethodMarker.class })
@Name("ArgumentValuePropagation: ComputeMethodArgumentsValues")
public class AvpComputeMethodArgumentsValues extends AvpSchedulable
    implements RunnableSchedulable<JSession> {

  @Description("Interim: counts a number of downstream references on a type")
  @ValidOn(JDefinedClassOrInterface.class)
  private static class TypeRefCounter implements Marker {
    private int count = 0;

    /** Increments the number of referencing types */
    static void inc(@Nonnull JDefinedClassOrInterface type) {
      TypeRefCounter marker = type.getMarker(TypeRefCounter.class);
      if (marker == null) {
        marker = new TypeRefCounter();
        type.addMarker(marker);
      }
      marker.count++;
    }

    /**
     * Decrements the number of referencing types, when it becomes 0 removes the
     * counter and returns true, otherwise returns false.
     */
    static boolean dec(@Nonnull JDefinedClassOrInterface type) {
      TypeRefCounter marker = type.getMarker(TypeRefCounter.class);
      assert marker != null;
      marker.count--;
      if (marker.count == 0) {
        type.removeMarker(TypeRefCounter.class);
      }
      return marker.count == 0;
    }

    @Override
    @Nonnull
    public Marker cloneIfNeeded() {
      throw new AssertionError();
    }
  }


  @Description("Interim: Tracks cumulative call arguments reaching this type")
  @ValidOn(JDefinedClassOrInterface.class)
  private static class CumulativeArguments implements Marker {
    @Nonnull
    final Map<String, LiteralValueListTracker> values = new HashMap<>();

    private CumulativeArguments(
        @Nonnull JDefinedClassOrInterface type,
        @CheckForNull Map<String, LiteralValueListTracker> callsOnType) {

      // The marker must exist by this time
      TaintedVirtualMethodsMarker tainted = TaintedVirtualMethodsMarker.getMarker(type);
      assert tainted != null;
      // Calculate accumulated call arguments only on non-tainted methods
      mergeMethodTrackers(values, callsOnType, tainted);

      // Add calls from super, if the type exists
      JClass superClass = type.getSuperClass();
      if (superClass != null && TypeToBeEmittedMarker.isToBeEmitted(superClass)) {
        // NOTE: the marker must be computed by this time
        CumulativeArguments marker =
            ((JDefinedClassOrInterface) superClass).getMarker(CumulativeArguments.class);
        assert marker != null;
        mergeMethodTrackers(values, marker.values, tainted);
      }

      // Add calls from implemented interfaces, if any
      for (JInterface impl : type.getImplements()) {
        if (TypeToBeEmittedMarker.isToBeEmitted(impl)) {
          // NOTE: the marker must be computed by this time
          CumulativeArguments marker =
              ((JDefinedClassOrInterface) impl).getMarker(CumulativeArguments.class);
          assert marker != null;
          mergeMethodTrackers(values, marker.values, tainted);
        }
      }
    }

    /** Merge a tracker with new data into a target tracker */
    private static void mergeMethodTrackers(
        @Nonnull Map<String, LiteralValueListTracker> mergeTarget,
        @CheckForNull Map<String, LiteralValueListTracker> newData,
        @Nonnull TaintedVirtualMethodsMarker tainted) {
      if (newData != null) {
        for (Map.Entry<String, LiteralValueListTracker> e : newData.entrySet()) {
          String signature = e.getKey();
          if (tainted.isMethodTainted(signature)) {
            continue; // Skip ignored signature
          }

          LiteralValueListTracker tracker = e.getValue();
          LiteralValueListTracker accumulatedTracker = mergeTarget.get(signature);
          if (accumulatedTracker == null) {
            accumulatedTracker = new SimpleLiteralValueListTracker(tracker.size());
            mergeTarget.put(signature, accumulatedTracker);
          }
          accumulatedTracker.updateWith(tracker);
        }
      }
    }

    @Nonnull
    static CumulativeArguments compute(
        @Nonnull JDefinedClassOrInterface type,
        @CheckForNull Map<String, LiteralValueListTracker> callsOnType) {
      assert !type.containsMarker(CumulativeArguments.class);
      CumulativeArguments marker = new CumulativeArguments(type, callsOnType);
      type.addMarker(marker);
      return marker;
    }

    @Override
    @Nonnull
    public Marker cloneIfNeeded() {
      throw new AssertionError();
    }
  }


  /**
   * The marker contains actual method table (by signature) on a type, in terms
   * or what method will be selected if a DIRECT call is performed on this class
   * with a particular signature. All, not only virtual, the methods are included.
   *
   * NOTE: we are not properly handling default interface methods at this time.
   */
  @Description("Interim: type's 'extended' method table")
  @ValidOn(JDefinedClassOrInterface.class)
  private class TypeMethodTable implements Marker {
    // This collection does not contain tainted methods
    @Nonnull
    private final Map<String, JMethod> methods = new HashMap<>();

    /** Goes through tracked methods and marks the values */
    void markCallArgs(@Nonnull Map<String, LiteralValueListTracker> args) {
      for (Map.Entry<String, JMethod> e : methods.entrySet()) {
        LiteralValueListTracker tracker = args.get(e.getKey());
        if (tracker != null) {
          // Note that since method table propagates methods to the derived
          // classes, this call can be made many times on the same method.
          MethodCallArgumentsMarker.markArgValues(e.getValue(), tracker);
        }
      }
    }

    private TypeMethodTable(@Nonnull JDefinedClassOrInterface type) {
      // Superclass, if exists, is a default content for the method table
      JClass superClass = type.getSuperClass();
      if (superClass != null) {
        mergeWith(superClass);
      }

      // Add type's own methods
      for (JMethod method : type.getMethods()) {
        // If the current is a class, it's method override the actual
        // method even if they are abstract.
        methods.put(getMethodSignature(method), method);
      }

      // Add methods from the interfaces
      for (JInterface impl : type.getImplements()) {
        mergeWith(impl);
      }

      // Mark all the methods as tainted
      TaintedVirtualMethodsMarker tainted =
          TaintedVirtualMethodsMarker.getMarker(type);
      assert tainted != null;

      Iterator<Map.Entry<String, JMethod>> iterator = methods.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, JMethod> next = iterator.next();
        String signature = next.getKey();
        JMethod method = next.getValue();
        if (method.getMethodIdWide().getKind() == MethodKind.INSTANCE_VIRTUAL &&
            tainted.isMethodTainted(signature)) {
          AvpSchedulable.TaintedMethodMarker.markAsTainted(method);
          iterator.remove();
        }
      }
    }

    private void mergeWith(@Nonnull JType type) {
      if (type instanceof JDefinedClassOrInterface) {
        if (TypeToBeEmittedMarker.isToBeEmitted(type)) {
          // This is a type to be emitted, its method table must
          // exist by this time.
          TypeMethodTable other =
              ((JDefinedClassOrInterface) type).getMarker(TypeMethodTable.class);
          assert other != null;

          // We also add new methods, never override existing ones,
          // since if we are building this marker for a class:
          //   a. and 'type' is it's super class, then this.methods
          //      collection is empty
          //   b. otherwise 'type' is an implemented interface, and we assume the
          //      class has all interface method declared or inherited (since
          //      we don't support default interface methods at the moment.)
          //
          // Note that we propagate all methods including private, static methods
          // and initializers, but it does not have impact on the algorithm.
          //
          // Otherwise we are building this marker for an interface, and:
          //   a. super class must be null
          //   b. if 'type' is an extended interface, we don't care which
          //      method will get into a map, again, since we don't support
          //      default interface methods.
          //
          for (Map.Entry<String, JMethod> e : other.methods.entrySet()) {
            JMethod method = e.getValue();
            if (!method.getMethodIdWide().isInit() && !method.isPrivate()) {
              if (!methods.containsKey(e.getKey())) {
                methods.put(e.getKey(), method);
              }
            }
          }
        }
      }
    }

    @Override
    @Nonnull
    public Marker cloneIfNeeded() {
      throw new AssertionError();
    }
  }

  @Nonnull
  private TypeMethodTable computeTypeMethodTable(@Nonnull JDefinedClassOrInterface type) {
    assert !type.containsMarker(TypeMethodTable.class);
    TypeMethodTable marker = new TypeMethodTable(type);
    type.addMarker(marker);
    return marker;
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    // Topologically sort types
    List<JDefinedClassOrInterface> sortedTypes =
        sortTopologically(session.getTypesToEmit());

    // Compute arguments passed to particular not tainted methods. This method
    // computes TypeMethodTable, CumulativeArguments and TaintedVirtualMethods
    // markers if needed.
    computeVirtualCallArguments(sortedTypes);

    // Cleanup heavy markers
    for (JDefinedClassOrInterface type : sortedTypes) {
      type.removeMarker(TypeMethodTable.class);
      type.removeMarker(CumulativeArguments.class);
    }
  }

  /**
   * Collect possible call arguments that may be passed a particular method
   * via both virtual and direct calls.
   *
   * The algorithm assumes that each type has TypeMethodTable marker calculated.
   *
   * We iterate the types from base to derived and accumulate information about
   * possible arguments being passed to the methods via virtual calls, we store
   * this accumulated data in CumulativeArguments marker.
   *
   * NOTE: this method used CallCumulativeArguments marker to store
   * intermediate data on types, but this marker does not leak this method.
   */
  private void computeVirtualCallArguments(
      @Nonnull List<JDefinedClassOrInterface> sortedTypes) {
    for (int i = sortedTypes.size() - 1; i >= 0; i--) {
      JDefinedClassOrInterface type = sortedTypes.get(i);

      // Compute the actual method table on this type, note that the computation
      // uses method tables from extended/implemented types which are already computed.
      TypeMethodTable methodTable = computeTypeMethodTable(type);

      // Mark all accumulated virtual method call arguments
      CumulativeArguments virtualCallArgs = CumulativeArguments.compute(type,
          TypeMethodCallArgumentsMarker
              .getCallsArgsOnType(type, /* virtual: */ true));
      methodTable.markCallArgs(virtualCallArgs.values);

      // Mark all direct method call arguments
      ConcurrentMap<String, LiteralValueListTracker> directCallArgs =
          TypeMethodCallArgumentsMarker.getCallsArgsOnType(type, /* virtual: */ false);
      if (directCallArgs != null) {
        methodTable.markCallArgs(directCallArgs);
      }
    }
  }

  /**
   * Topologically sorts the types such that each type
   * extends/inherits types with bigger indices.
   *
   * NOTE: this method used TypeRefCounter marker to store intermediate
   * data on types, but this marker does not leak this method.
   */
  @Nonnull
  private static List<JDefinedClassOrInterface> sortTopologically(
      @Nonnull Collection<JDefinedClassOrInterface> types) {

    // Count downstream links: super class and interfaces
    for (JDefinedClassOrInterface type : types) {
      JClass superClass = type.getSuperClass();
      if (superClass != null && TypeToBeEmittedMarker.isToBeEmitted(superClass)) {
        TypeRefCounter.inc((JDefinedClassOrInterface) superClass);
      }

      for (JInterface impl : type.getImplements()) {
        if (TypeToBeEmittedMarker.isToBeEmitted(impl)) {
          TypeRefCounter.inc((JDefinedClassOrInterface) impl);
        }
      }
    }

    // Initialize queue with types having no incoming references
    Queue<JDefinedClassOrInterface> queue = new LinkedList<>();
    for (JDefinedClassOrInterface type : types) {
      if (!type.containsMarker(TypeRefCounter.class)) {
        queue.offer(type);
      }
    }

    // Work on the queue
    List<JDefinedClassOrInterface> sortedTypes = new ArrayList<>();
    while (!queue.isEmpty()) {
      JDefinedClassOrInterface type = queue.poll();
      sortedTypes.add(type);

      JClass superClass = type.getSuperClass();
      if (superClass != null && TypeToBeEmittedMarker.isToBeEmitted(superClass)) {
        if (TypeRefCounter.dec((JDefinedClassOrInterface) superClass)) {
          queue.offer((JDefinedClassOrInterface) superClass);
        }
      }

      // Count downstream links: interfaces
      for (JInterface impl : type.getImplements()) {
        if (TypeToBeEmittedMarker.isToBeEmitted(impl)) {
          if (TypeRefCounter.dec((JDefinedClassOrInterface) impl)) {
            queue.offer((JDefinedClassOrInterface) impl);
          }
        }
      }
    }
    return sortedTypes;
  }
}
