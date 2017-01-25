/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopParameterLoader;
import com.android.jack.load.ParameterLoader;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java method parameter definition.
 */
@Description("Java method parameter definition")
public class JParameter extends JVariable implements HasEnclosingMethod {

  @CheckForNull
  private final JMethod enclosingMethod;

  @Nonnull
  private ParameterLoader loader;

  public JParameter(
      @Nonnull SourceInfo info,
      @CheckForNull String name,
      @Nonnull JType type,
      int modifier,
      @Nonnull JMethod enclosingMethod) {
    this(info, name, type, modifier, enclosingMethod, NopParameterLoader.INSTANCE);
  }

  public JParameter(
      @Nonnull SourceInfo info,
      @CheckForNull String name,
      @Nonnull JType type,
      int modifier,
      @Nonnull JMethod enclosingMethod,
      @Nonnull ParameterLoader loader) {
    super(info, name, type, modifier);
    assert JModifier.isParameterModifier(modifier);
    this.enclosingMethod = enclosingMethod;
    this.loader = loader;
  }

  JParameter(
      @Nonnull SourceInfo info,
      @CheckForNull String name,
      @Nonnull JType type,
      int modifier) {
    super(info, name, type, modifier);
    enclosingMethod = null;
    this.loader = NopParameterLoader.INSTANCE;
  }

  @Nonnull
  @Override
  public JMethod getEnclosingMethod() {
    assert enclosingMethod != null;
    return enclosingMethod;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      if (visitor.needLoading()) {
        loader.ensureAnnotations(this);
      }
      visitor.accept(annotations);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JAnnotation annotation : annotations) {
      annotation.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  @Nonnull
  public List<JAnnotation> getAnnotations(@Nonnull JAnnotationType annotationType) {
    loader.ensureAnnotation(this, annotationType);
    return super.getAnnotations(annotationType);
  }

  @Override
  @Nonnull
  public Collection<JAnnotation> getAnnotations() {
    loader.ensureAnnotations(this);
    return super.getAnnotations();
  }

  @Override
  @Nonnull
  public Set<JAnnotationType> getAnnotationTypes() {
    loader.ensureAnnotations(this);
    return super.getAnnotationTypes();
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JMethod)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
    if (parent != enclosingMethod) {
      throw new JNodeInternalError(this, "Invalid parent or enclosing method");
    }
  }

  @Override
  @Nonnull
  public JParameterRef makeRef(@Nonnull SourceInfo info) {
    return new JParameterRef(info, this);
  }

  @Override
  @CheckForNull
  public <T extends Marker> T getMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.getMarker(cls);
  }

  @Override
  @Nonnull
  public Collection<Marker> getAllMarkers() {
    loader.ensureMarkers(this);
    return super.getAllMarkers();
  }

  @Override
  public <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.containsMarker(cls);
  }

  @Override
  public <T extends Marker> T removeMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.removeMarker(cls);
  }

  public void removeLoader() {
    loader = NopParameterLoader.INSTANCE;
  }
}
