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

package com.android.jack.load;

import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.sched.marker.Marker;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base class for implementing ClassOrInterfaceLoader
 */
public abstract class AbstractClassOrInterfaceLoader implements ClassOrInterfaceLoader {

  @Override
  public void ensureHierarchy(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureMarkers(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureMarker(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull Class<? extends Marker> cls) {
    ensureMarkers(loaded);
  }

  @Override
  public void ensureEnclosing(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureInners(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureAnnotations(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureAnnotation(
      @Nonnull JDefinedClassOrInterface loaded, @Nonnull JAnnotationType annotationType) {
    ensureAnnotations(loaded);
  }

  @Override
  public void ensureMethods(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureMethod(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull String name, @Nonnull List<? extends JType> args, @Nonnull JType returnType) {
    ensureMethods(loaded);
  }

  @Override
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded, @Nonnull String fieldName) {
    ensureFields(loaded);
  }
  @Override
  public void ensureModifier(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureRetentionPolicy(@Nonnull JDefinedAnnotationType loaded) {
    ensureAll(loaded);
  }

  @Override
  public void ensureSourceInfo(@Nonnull JDefinedClassOrInterface loaded) {
    ensureAll(loaded);
  }

  protected abstract void ensureAll(@Nonnull JDefinedClassOrInterface loaded);
}
