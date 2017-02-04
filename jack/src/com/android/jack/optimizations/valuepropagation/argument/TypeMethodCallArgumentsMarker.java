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

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.optimizations.common.ConcurrentLiteralValueListTracker;
import com.android.jack.optimizations.common.LiteralValueListTracker;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents argument trackers for all the methods called on the type.
 * Allows concurrent modification.
 */
@Description("Represents argument trackers for all the methods called on the type.")
@ValidOn(JDefinedClassOrInterface.class)
public class TypeMethodCallArgumentsMarker implements Marker {
  @Nonnull
  private final ConcurrentMap<String, LiteralValueListTracker> virtual =
      new ConcurrentHashMap<>();

  @Nonnull
  private final ConcurrentMap<String, LiteralValueListTracker> direct =
      new ConcurrentHashMap<>();

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  private void markCall(
      @Nonnull String signature, @Nonnull List<JExpression> args,
      @Nonnull JMethodCall.DispatchKind dispatchKind) {
    ConcurrentMap<String, LiteralValueListTracker> map =
        dispatchKind == JMethodCall.DispatchKind.VIRTUAL ? virtual : direct;
    LiteralValueListTracker tracker = map.get(signature);
    if (tracker == null) {
      tracker = new ConcurrentLiteralValueListTracker(args.size());
      LiteralValueListTracker existing = map.putIfAbsent(signature, tracker);
      if (existing != null) {
        tracker = existing;
      }
    }
    tracker.updateWith(args);
  }

  /** Get collected virtual or direct call arguments on the type */
  @CheckForNull
  public static ConcurrentMap<String, LiteralValueListTracker> getCallsArgsOnType(
      @Nonnull JDefinedClassOrInterface type, boolean isVirtual) {
    TypeMethodCallArgumentsMarker marker =
        type.getMarker(TypeMethodCallArgumentsMarker.class);
    return marker == null ? null : (isVirtual ? marker.virtual : marker.direct);
  }

  /** Marks a call on the receiver type, creates the marker if needed */
  public static void markCallOnReceiverType(
      @Nonnull String signature, @Nonnull JMethodCall call) {
    List<JExpression> args = call.getArgs();
    assert !args.isEmpty();

    // Only track calls on types to be emitted
    JClassOrInterface receiverType = call.getReceiverType();
    if (!receiverType.isToEmit()) {
      return;
    }
    assert receiverType instanceof JDefinedClassOrInterface;
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) receiverType;

    // If this is a call to a virtual method, avoid marking
    // it's arguments if the method is tainted at this type.
    TaintedVirtualMethodsMarker taintedMarker =
        TaintedVirtualMethodsMarker.getMarker(type);
    if (call.getMethodIdWide().getKind() == MethodKind.INSTANCE_VIRTUAL &&
        taintedMarker != null && taintedMarker.isMethodTainted(signature)) {
      return;
    }

    // Create marker if needed, mark arguments
    TypeMethodCallArgumentsMarker marker = type
        .getMarker(TypeMethodCallArgumentsMarker.class);
    if (marker == null) {
      marker = new TypeMethodCallArgumentsMarker();
      TypeMethodCallArgumentsMarker existing = type.addMarkerIfAbsent(marker);
      if (existing != null) {
        marker = existing;
      }
    }
    marker.markCall(signature, args, call.getDispatchKind());
  }
}
