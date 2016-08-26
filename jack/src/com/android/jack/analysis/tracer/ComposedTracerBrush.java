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
import com.android.sched.util.findbugs.SuppressFBWarnings;

import java.util.BitSet;
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * {@link TracerBrush} for running several tracing in the same pass.
 */
public class ComposedTracerBrush implements TracerBrush {

  @Nonnull
  private final TracerBrush[] brushes;

  @Nonnull
  private final Stack<BitSet> composedStatus = new Stack<BitSet>();

  /*
   * Because this is just configuration we don't care to store internally an externally visible
   * mutable array.
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public ComposedTracerBrush(@Nonnull TracerBrush[] brushes) {
    this.brushes = brushes;
    BitSet initialAllEnabled = new BitSet(brushes.length);
    initialAllEnabled.set(0, brushes.length);
    composedStatus.push(initialAllEnabled);
  }

  @Override
  public boolean startTrace(@Nonnull JDefinedClassOrInterface type) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTrace(type);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTrace(@Nonnull JDefinedClassOrInterface type) {
    composedStatus.pop();
    for (TracerBrush config : brushes) {
      config.endTrace(type);
    }
  }

  @Override
  public boolean startTrace(@Nonnull JMethod method) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTrace(method);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTrace(@Nonnull JMethod method) {
    composedStatus.pop();
    for (TracerBrush config : brushes) {
      config.endTrace(method);
    }
  }

  @Override
  public boolean startTrace(@Nonnull JField field) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTrace(field);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTrace(@Nonnull JField field) {
    composedStatus.pop();
    for (TracerBrush config : brushes) {
      config.endTrace(field);
    }
  }

  @Override
  public boolean startTraceOverridingMethod(@Nonnull JMethod method) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTraceOverridingMethod(method);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTraceOverridingMethod(@Nonnull JMethod method) {
    composedStatus.pop();
    for (TracerBrush config : brushes) {
      config.endTraceOverridingMethod(method);
    }
  }

  @Override
  public void setMustTraceOverridingMethods(@Nonnull JMethod method) {
    BitSet currentStatus = composedStatus.peek();
    for (int i = 0; i < brushes.length; i++) {
      if (currentStatus.get(i)) {
        brushes[i].setMustTraceOverridingMethods(method);
      }
    }
  }

  @Override
  public boolean traceMarked(@Nonnull JNode node) {
    boolean isMarked = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].traceMarked(node);
      nextStatus.set(i, b);
      isMarked |= b;
    }
    if (isMarked) {
      composedStatus.push(nextStatus);
    }
    return isMarked;
  }

  @Override
  public boolean startTraceEnclosingMethod() {
    boolean startTrace = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTraceEnclosingMethod();
      nextStatus.set(i, b);
      startTrace |= b;
    }
    if (startTrace) {
      composedStatus.push(nextStatus);
    }
    return startTrace;
  }

  @Override
  public void endTraceEnclosingMethod() {
    composedStatus.pop();
    for (TracerBrush config : brushes) {
      config.endTraceEnclosingMethod();
    }
  }

  @Override
  public void endTraceMarked(@Nonnull JNode node) {
    composedStatus.pop();
    for (TracerBrush config : brushes) {
      config.endTraceMarked(node);
    }
  }

  @Override
  public boolean startTraceSeed(@Nonnull JDefinedClassOrInterface type) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTraceSeed(type);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTraceSeed(@Nonnull JDefinedClassOrInterface type) {
    composedStatus.pop();
  }

  @Override
  public boolean startTraceSeed(@Nonnull JMethod method) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTraceSeed(method);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTraceSeed(@Nonnull JMethod method) {
    composedStatus.pop();
  }

  @Override
  public boolean startTraceSeed(@Nonnull JField field) {
    boolean status = false;
    BitSet currentStatus = composedStatus.peek();
    BitSet nextStatus = new BitSet(brushes.length);
    for (int i = 0; i < brushes.length; i++) {
      boolean b = currentStatus.get(i) && brushes[i].startTraceSeed(field);
      nextStatus.set(i, b);
      status |= b;
    }
    if (status) {
      composedStatus.push(nextStatus);
    }
    return status;
  }

  @Override
  public void endTraceSeed(@Nonnull JField field) {
    composedStatus.pop();
  }
}
