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
import com.android.jack.load.MethodLoader;
import com.android.jack.util.AnnotationUtils;
import com.android.jack.util.NamingTools;
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
 * A Java method implementation.
 */
@Description("A Java method implementation")
public class JMethod extends JNode implements HasEnclosingType, HasName, HasType, CanBeAbstract,
    CanBeSetFinal, CanBeNative, CanBeStatic, Annotable, HasModifier {

  /**
   * Special serialization treatment.
   */
  @CheckForNull
  private JAbstractMethodBody body = null;
  @CheckForNull
  private JDefinedClassOrInterface enclosingType;
  private int modifier;

  @Nonnull
  private final ArrayList<JParameter> params = new ArrayList<JParameter>();
  @Nonnull
  private final JType returnType;

  @Nonnull
  private final List<JAnnotation> annotations = new ArrayList<JAnnotation>();

  @Nonnull
  private JMethodId methodId;

  @CheckForNull
  private JThis jThis;

  @Nonnull
  private MethodLoader loader;

  public JMethod(@Nonnull SourceInfo info,
      @Nonnull JMethodId methodId,
      @CheckForNull JDefinedClassOrInterface enclosingType,
      @Nonnull JType returnType,
      int modifier) {
    this(info, methodId, enclosingType, returnType, modifier, NopMethodLoader.INSTANCE);
  }

  public JMethod(@Nonnull SourceInfo info,
        @Nonnull JMethodId methodId,
        @CheckForNull JDefinedClassOrInterface enclosingType,
        @Nonnull JType returnType,
        int modifier,
        @Nonnull MethodLoader loader) {
    super(info);
    assert JModifier.isMethodModifier(modifier) : "Wrong method modifier.";
    assert JModifier.isValidMethodModifier(modifier);
    this.enclosingType = enclosingType;
    this.returnType = returnType;
    this.modifier = modifier;
    this.methodId = methodId; // FINDBUGS
    this.loader = loader;
    if (needThis(modifier)) {
      jThis = new JThis(this);
    } else {
      jThis = null;
    }
    setMethodId(methodId);
  }

  /**
   * @return the loader
   */
  @Nonnull
  public MethodLoader getLoader() {
    return loader;
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

  /**
   * Adds a parameter to this method.
   */
  public void addParam(JParameter parameter) {
    params.add(parameter);
  }

  /**
   * Prepends a parameter to this method.
   */
  public void prependParam(JParameter parameter) {
    params.add(0, parameter);
  }

  /**
   * Returns true if this method can participate in virtual dispatch. Returns
   * true for non-private instance methods; false for static methods, private
   * instance methods, and constructors.
   */
  public boolean canBePolymorphic() {
    return !isStatic() && !isPrivate();
  }

  @CheckForNull
  public JAbstractMethodBody getBody() {
    loader.ensureBody(this);
    return body;
  }

  @Nonnull
  @Override
  public JDefinedClassOrInterface getEnclosingType() {
    assert enclosingType != null;
    return enclosingType;
  }

  @Nonnull
  @Override
  public String getName() {
    return methodId.getName();
  }

  /**
   * Returns the parameters of this method.
   */
  @Nonnull
  public List<JParameter> getParams() {
    return params;
  }

  @Nonnull
  @Override
  public JType getType() {
    return returnType;
  }

  @Override
  public boolean isAbstract() {
    return JModifier.isAbstract(modifier);
  }

  public boolean isExternal() {
    return getEnclosingType().isExternal();
  }

  @Override
  public boolean isFinal() {
    return JModifier.isFinal(modifier);
  }

  @Override
  public boolean isNative() {
    return JModifier.isNative(modifier);
  }

  public boolean isPublic() {
    return JModifier.isPublic(modifier);
  }

  public boolean isPrivate() {
    return JModifier.isPrivate(modifier);
  }

  public boolean isProtected() {
    return JModifier.isProtected(modifier);
  }

  @Override
  public boolean isStatic() {
    return JModifier.isStatic(modifier);
  }

  public boolean isSynchronized() {
    return JModifier.isSynchronized(modifier);
  }

  public boolean isSynthetic() {
    return JModifier.isSynthetic(modifier);
  }

  public boolean isStrictfp() {
    return JModifier.isStrictfp(modifier);
  }

  public boolean isVarags() {
    return JModifier.isVarargs(modifier);
  }

  public boolean isBridge() {
    return JModifier.isBridge(modifier);
  }

  public void setAbstract() {
      modifier |= JModifier.ABSTRACT;
  }

  public void setBody(JAbstractMethodBody body) {
    this.body = body;
    if (body != null) {
      body.setMethod(this);
    }
  }

  public void setEnclosingType(@CheckForNull JDefinedClassOrInterface enclosingType) {
    this.enclosingType = enclosingType;
  }

  @Override
  public void setFinal() {
    modifier |= JModifier.FINAL;
  }

  public void setSynthetic() {
    modifier |= JModifier.SYNTHETIC;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitChildren(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    visitChildren(schedule);
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
    return Jack.getUnmodifiableCollections().getUnmodifiableSet(
        AnnotationUtils.getAnnotationTypes(annotations));
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

  protected void visitChildren(@Nonnull JVisitor visitor) {
    if (visitor.needLoading()) {
      loader.ensureBody(this);
      loader.ensureAnnotations(this);
    }
    if (jThis != null) {
      visitor.accept(jThis);
    }
    visitor.accept(params);
    if (body != null) {
      visitor.accept(body);
    }
    visitor.accept(annotations);
  }

  protected void visitChildren(@Nonnull ScheduleInstance<? super Component> schedule)
      throws Exception {
    if (jThis != null) {
      jThis.traverse(schedule);
    }
    for (JParameter param : params) {
      param.traverse(schedule);
    }
    if (body != null) {
      body.traverse(schedule);
    }
    for (JAnnotation annotation : annotations) {
      annotation.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(params, existingNode, (JParameter) newNode, transformation)) {
      if (!transform(annotations, existingNode, (JAnnotation) newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (body == existingNode) {
      body = (JAbstractMethodBody) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (body == existingNode) {
      body = null;
    } else {
      super.removeImpl(existingNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Nonnull
  public JMethodId getMethodId() {
    return methodId;
  }

  public void setMethodId(@Nonnull JMethodId methodId) {
    assert getExpectedMethodKind() == methodId.getKind();
    this.methodId = methodId;
    methodId.addMethod(this);
  }

  private MethodKind getExpectedMethodKind() {
    MethodKind expectedKind;
    if (isStatic()) {
      expectedKind = MethodKind.STATIC;
    } else if (isPrivate() || this instanceof JConstructor) {
      expectedKind = MethodKind.INSTANCE_NON_VIRTUAL;
    } else {
      expectedKind = MethodKind.INSTANCE_VIRTUAL;
    }
    return expectedKind;
  }

  @CheckForNull
  public JThis getThis() {
    return jThis;
  }

  public void setThis(@CheckForNull JThis jThis) {
    this.jThis = jThis;
  }

  public static boolean isClinit(@Nonnull JMethod method) {
    return method.getName().equals(NamingTools.STATIC_INIT_NAME);
  }

  public void removeLoader() {
    loader = NopMethodLoader.INSTANCE;
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JDefinedClassOrInterface) && !(parent instanceof JLambda)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }

  public static boolean needThis(int modifier) {
    if (JModifier.isStatic(modifier) || JModifier.isAbstract(modifier)
        || JModifier.isNative(modifier)) {
      return false;
    }

    return true;
  }
}
