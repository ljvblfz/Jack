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

import javax.annotation.Nonnull;

/**
 * A customization for piloting the {@link Tracer}.
 */
public interface TracerBrush {

  boolean startTrace(@Nonnull JMethod method);
  void endTrace(@Nonnull JMethod method);

  boolean startTrace(@Nonnull JField field);
  void endTrace(@Nonnull JField field);

  boolean startTrace(@Nonnull JDefinedClassOrInterface type);
  void endTrace(@Nonnull JDefinedClassOrInterface type);

  boolean startTraceSeed(@Nonnull JDefinedClassOrInterface type);
  void endTraceSeed(@Nonnull JDefinedClassOrInterface type);

  boolean startTraceSeed(@Nonnull JMethod method);
  void endTraceSeed(@Nonnull JMethod method);

  boolean startTraceSeed(@Nonnull JField field);
  void endTraceSeed(@Nonnull JField field);

  boolean startTraceOverridingMethod(@Nonnull JMethod method);
  void endTraceOverridingMethod(@Nonnull JMethod method);

  boolean startTraceEnclosingMethod();
  void endTraceEnclosingMethod();

  boolean traceMarked(@Nonnull JNode node);
  void endTraceMarked(@Nonnull JNode node);

  void setMustTraceOverridingMethods(@Nonnull JMethod method);
}
