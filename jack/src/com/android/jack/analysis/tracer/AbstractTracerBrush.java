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

package com.android.jack.analysis.tracer;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.sched.marker.Marker;

import javax.annotation.Nonnull;

/**
 * Default base class for {@link TracerBrush} implementations.
 */
public abstract class AbstractTracerBrush<M extends BaseTracerMarker> implements TracerBrush {

  private final boolean traceEnclosingMethod;

  @Nonnull
  private final Class<M> markerClass;

  @Nonnull
  private final Class<? extends Marker> seedMarkerClass;

  public AbstractTracerBrush(boolean traceEnclosingMethod,
      @Nonnull Class<M> markerClass,
      @Nonnull Class<? extends Marker> seedMarkerClass) {
    this.traceEnclosingMethod = traceEnclosingMethod;
    this.markerClass = markerClass;
    this.seedMarkerClass = seedMarkerClass;
  }

  @Override
  public boolean startTrace(@Nonnull JDefinedClassOrInterface type) {
    return markIfNecessary(type);
  }

  @Override
  public void endTrace(@Nonnull JDefinedClassOrInterface type) {
  }

  @Override
  public boolean startTrace(@Nonnull JMethod type) {
    return markIfNecessary(type);
  }

  @Override
  public void endTrace(@Nonnull JMethod type) {
  }

  @Override
  public boolean startTrace(@Nonnull JField type) {
    return markIfNecessary(type);
  }

  @Override
  public void endTrace(@Nonnull JField type) {
  }

  @Override
  public boolean startTraceSeed(@Nonnull JDefinedClassOrInterface type) {
    return isSeed(type);
  }

  @Override
  public void endTraceSeed(@Nonnull JDefinedClassOrInterface type) {
  }

  @Override
  public boolean startTraceSeed(@Nonnull JMethod method) {
    return isSeed(method);
  }

  @Override
  public void endTraceSeed(@Nonnull JMethod method) {
  }

  @Override
  public boolean startTraceSeed(@Nonnull JField field) {
    return isSeed(field);
  }

  @Override
  public void endTraceSeed(@Nonnull JField field) {
  }

  @Override
  public boolean startTraceOverridingMethod(@Nonnull JMethod method) {
    return traceMarked(method) && mustTraceOverridingMethod(method);
  }

  @Override
  public void endTraceOverridingMethod(@Nonnull JMethod method) {
  }

  @Override
  public boolean traceMarked(@Nonnull JNode node) {
    return isMarked(node);
  }

  @Override
  public void endTraceMarked(@Nonnull JNode node) {
  }

  protected boolean markIfNecessary(@Nonnull JNode node) {
    return (node.addMarkerIfAbsent(createMarkerFor(node)) == null);
  }

  @Nonnull
  protected abstract M createMarkerFor(@Nonnull JNode node);

  protected boolean isMarked(@Nonnull JNode node) {
    return node.containsMarker(markerClass);
  }

  protected boolean isSeed(@Nonnull JNode node) {
    return node.containsMarker(seedMarkerClass);
  }

  protected boolean mustTraceOverridingMethod(@Nonnull JMethod method) {
    BaseTracerMarker marker = method.getMarker(markerClass);
    if (marker != null) {
      return marker.mustTraceOverridingMethods();
    }
    return false;
  }

  @Override
  public void setMustTraceOverridingMethods(@Nonnull JMethod method) {
    BaseTracerMarker marker = method.getMarker(markerClass);
    assert marker != null;
    marker.setMustTraceOverridingMethods(true);
  }

  @Override
  public boolean startTraceEnclosingMethod() {
    return traceEnclosingMethod;
  }

  @Override
  public void endTraceEnclosingMethod() {
  }

}
