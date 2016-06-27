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

package com.android.jack.tracer;

import com.android.jack.analysis.tracer.TracerBrush;
import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;

import javax.annotation.Nonnull;

public class MultiTracerBrush implements TracerBrush {

  private static final long DISTINCT_FORCED_MARKED_KEY = 17;

  private static final long COMMON_FORCED_MARKED_KEY = 13;

  private static final long DISTINCT_SEED_KEY = 11;

  private static final long COMMON_SEED_KEY = 5;

  private final boolean traceEnclosingMethod;

  private final int id;

  private final int seed;

  public MultiTracerBrush(int id, int seed, boolean traceEnclosingMethod) {
    this.id = id;
    this.seed = seed;
    this.traceEnclosingMethod = traceEnclosingMethod;
  }

  @Override
  public boolean startTrace(@Nonnull JDefinedClassOrInterface type) {
    return markIfNecessary(type);
  }

  @Override
  public void endTrace(@Nonnull JDefinedClassOrInterface type) {
  }

  @Override
  public boolean startTrace(@Nonnull JMethod method) {
    return markIfNecessary(method);
  }

  @Override
  public void endTrace(@Nonnull JMethod method) {
  }

  @Override
  public boolean startTrace(@Nonnull JField field) {
    return markIfNecessary(field);
  }

  @Override
  public void endTrace(@Nonnull JField field) {
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
    if (node instanceof JDefinedClassOrInterface
        && !((JDefinedClassOrInterface) node).isToEmit()) {
      return true;
    } else if (node instanceof JMethod
        && !((JMethod) node).getEnclosingType().isToEmit()) {
      return true;
    } else if (node instanceof JField
        && !((JField) node).getEnclosingType().isToEmit()) {
      return true;
    }
    return isMarked(node);
  }

  @Override
  public void endTraceMarked(@Nonnull JNode node) {
  }


  protected boolean markIfNecessary(@Nonnull JNode node) {
    if (isForcedMark((HasName) node)) {
      return false;
    }
    synchronized (node) {
      MultiTracerMarker marker = node.getMarker(MultiTracerMarker.class);
      if (marker == null) {
        MultiTracerMarker newMarker = new MultiTracerMarker();
        marker = node.addMarkerIfAbsent(newMarker);
        if (marker == null) {
          marker = newMarker;
        }
      }
      if (!marker.isSet(id)) {
        marker.set(id);
        return true;
      }
    }
    return false;
  }

  protected boolean isMarked(@Nonnull JNode node) {
    if (isForcedMark((HasName) node)) {
      return true;
    }
    synchronized (node) {
      MultiTracerMarker marker = node.getMarker(MultiTracerMarker.class);
      if (marker != null) {
        return marker.isSet(id);
      }
    }
    return false;
  }

  protected boolean isSeed(@Nonnull HasName node) {
    return (node.getName().hashCode() + seed) != 0
        && ((((node.getName().hashCode() + seed) % DISTINCT_SEED_KEY) == 0L)
            || (((node.getName().hashCode()) % COMMON_SEED_KEY) == 0L));
  }

  protected boolean isForcedMark(@Nonnull HasName node) {
    return (node.getName().hashCode() + seed) != 0
        && ((((node.getName().hashCode() + seed) % DISTINCT_FORCED_MARKED_KEY) == 0L)
            || (((node.getName().hashCode()) % COMMON_FORCED_MARKED_KEY) == 0L));
  }

  protected boolean mustTraceOverridingMethod(@Nonnull JMethod method) {
    synchronized (method) {
      MultiTracerMarker marker = method.getMarker(MultiTracerMarker.class);
      if (marker != null) {
        return marker.mustTraceOverridingMethods(id);
      }
    }
    return false;
  }

  @Override
  public void setMustTraceOverridingMethods(@Nonnull JMethod method) {
    if ((method.getEnclosingType().isToEmit()) && !isForcedMark(method)) {
      synchronized (method) {
        MultiTracerMarker marker = method.getMarker(MultiTracerMarker.class);
        assert marker != null && marker.isSet(id);
        marker.setMustTraceOverridingMethods(id, true);
      }
    }
  }

  @Override
  public boolean startTraceEnclosingMethod() {
    return traceEnclosingMethod;
  }

  @Override
  public void endTraceEnclosingMethod() {
  }

}
