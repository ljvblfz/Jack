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

import com.android.jack.Jack;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.FieldLoader;
import com.android.jack.load.NopFieldLoader;
import com.android.jack.util.AnnotationUtils;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java field definition.
 */
@Description("Java field definition")
public class JField extends JNode implements HasName, HasType, JVisitable, CanBeStatic,
    HasEnclosingType, CanBeSetFinal, Annotable, HasModifier {

  @CheckForNull
  private JFieldInitializer fieldInitializer = null;
  @Nonnull
  private final JFieldId fieldId;
  @Nonnull
  protected final List<JAnnotation> annotations = new ArrayList<JAnnotation>();

  protected int modifier;

  @Nonnull
  private JDefinedClassOrInterface enclosingType;

  @CheckForNull
  private JLiteral initialValue;

  @Nonnull
  private final FieldLoader loader;

  public JField(
      @Nonnull SourceInfo info,
      @Nonnull String name,
      @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull JType type,
      int modifier) {
    this(info, name, enclosingType, type, modifier, NopFieldLoader.INSTANCE);
  }

    public JField(
        @Nonnull SourceInfo info,
        @Nonnull String name,
        @Nonnull JDefinedClassOrInterface enclosingType,
        @Nonnull JType type,
        int modifier,
        @Nonnull FieldLoader loader) {
    super(info);
    assert JModifier.isFieldModifier(modifier) : "Wrong field modifier.";
    assert JModifier.isValidFieldModifier(modifier);
    this.modifier = modifier;
    this.enclosingType = enclosingType;
    this.fieldId = new JFieldId(name, type,
        JModifier.isStatic(modifier) ? FieldKind.STATIC : FieldKind.INSTANCE, this);
    this.loader = loader;
  }

  @CheckForNull
  public JFieldInitializer getFieldInitializer() {
    return fieldInitializer;
  }

  @Nonnull
  @Override
  public JDefinedClassOrInterface getEnclosingType() {
    return enclosingType;
  }

  @CheckForNull
  public JLiteral getInitialValue() {
    return initialValue;
  }

  @CheckForNull
  public JValueLiteral getLiteralInitializer() {
    JExpression initializer = getInitializer();
    if (initializer instanceof JValueLiteral) {
      return (JValueLiteral) initializer;
    }
    return null;
  }

  public boolean isPublic() {
    return (JModifier.isPublic(modifier));
  }

  public boolean isPrivate() {
    return (JModifier.isPrivate(modifier));
  }

  public boolean isProtected() {
    return (JModifier.isProtected(modifier));
  }

  @Override
  public boolean isStatic() {
    return (JModifier.isStatic(modifier));
  }

  public boolean isVolatile() {
    return (JModifier.isVolatile(modifier));
  }

  public boolean isTransient() {
    return (JModifier.isTransient(modifier));
  }

  public boolean isEnum() {
    return (JModifier.isEnum(modifier));
  }

  public boolean isCompileTimeConstant() {
    return (JModifier.isCompileTimeConstant(modifier));
  }

  @Override
  public void setFinal() {
    if (isVolatile()) {
      throw new IllegalStateException("Volatile fields cannot be set final");
    }
    modifier |= JModifier.FINAL;
  }

  public void setFieldInitializer(@CheckForNull JFieldInitializer fieldInitializer) {
    this.fieldInitializer = fieldInitializer;
  }

  public void setInitialValue(@CheckForNull JLiteral constant) {
    initialValue = constant;
  }

  public void setVolatile() {
    if (isFinal()) {
      throw new IllegalStateException("Final fields cannot be set volatile");
    }
    modifier |= JModifier.VOLATILE;
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

  public void setEnclosingType(@Nonnull JDefinedClassOrInterface enclosingType) {
    this.enclosingType = enclosingType;
  }


  /**
   * @return the modifier
   */
  @Override
  public int getModifier() {
    return modifier;
  }

  @Override
  public void setModifier(int modifier) {
    this.modifier = modifier;
  }

  @CheckForNull
  public JLiteral getConstInitializer() {
    JExpression initializer = getInitializer();
    if (isFinal() && initializer instanceof JLiteral) {
      return (JLiteral) initializer;
    }
    return null;
  }

  @CheckForNull
  public JExpression getInitializer() {
    if (fieldInitializer != null) {
      return fieldInitializer.getInitializer();
    }
    return null;
  }

  @Nonnull
  @Override
  public String getName() {
    return fieldId.getName();
  }

  @Override
  @Nonnull
  public JType getType() {
    return fieldId.getType();
  }

  public boolean hasInitializer() {
    return fieldInitializer != null;
  }

  @Override
  public boolean isFinal() {
    return (JModifier.isFinal(modifier));
  }

  public boolean isSynthetic() {
    return (JModifier.isSynthetic(modifier));
  }

  public void setSynthetic() {
    modifier |= JModifier.SYNTHETIC;
  }

  @Nonnull
  public JFieldId getId() {
    return fieldId;
  }

  @Override
  public void addAnnotation(@Nonnull JAnnotation annotation) {
    annotations.add(annotation);
  }

  @Override
  @Nonnull
  public List<JAnnotation> getAnnotations(@Nonnull JAnnotationType annotationType) {
    loader.ensureAnnotation(this, annotationType);
    return Jack.getUnmodifiableCollections().getUnmodifiableList(
        AnnotationUtils.getAnnotation(annotations, annotationType));
  }

  @Override
  @Nonnull
  public Collection<JAnnotation> getAnnotations() {
    loader.ensureAnnotations(this);
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(annotations);
  }

  @Override
  @Nonnull
  public Set<JAnnotationType> getAnnotationTypes() {
    loader.ensureAnnotations(this);
    return Jack.getUnmodifiableCollections().getUnmodifiableSet(
        AnnotationUtils.getAnnotationTypes(annotations));
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(annotations, existingNode, (JAnnotation) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JDefinedClassOrInterface)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
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

  @Nonnull
  @Override
  public <T extends Marker> T getMarkerOrDefault(@Nonnull T defaultMarker) {
    loader.ensureMarker(this, defaultMarker.getClass());
    return super.getMarkerOrDefault(defaultMarker);
  }

  @Override
  @CheckForNull
  public <T extends Marker> T addMarkerIfAbsent(@Nonnull T marker) {
    loader.ensureMarker(this, marker.getClass());
    return super.addMarkerIfAbsent(marker);
  }

  @Override
  public void addAllMarkers(@Nonnull Collection<Marker> collection) {
    loader.ensureMarkers(this);
    super.addAllMarkers(collection);
  }
}
