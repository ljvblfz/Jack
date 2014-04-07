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


import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.MethodLoader;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A Java method implementation.
 */
@Description("A Java method implementation")
public class JMethod extends JNode implements HasEnclosingType, HasName, HasType, CanBeAbstract,
    CanBeSetFinal, CanBeNative, CanBeStatic, Annotable {

  /**
   * Special serialization treatment.
   */
  @CheckForNull
  private JAbstractMethodBody body = null;
  @Nonnull
  private JDefinedClassOrInterface enclosingType;
  private int modifier;

  @Nonnull
  private final List<JParameter> params = new ArrayList<JParameter>();
  @Nonnull
  private final JType returnType;

  @Nonnull
  private final AnnotationSet annotations = new AnnotationSet();

  @Nonnull
  private JMethodId methodId;

  @CheckForNull
  private final JThis jThis;

  @Nonnull
  private final MethodLoader loader;

  public JMethod(@Nonnull SourceInfo info,
      @Nonnull JMethodId methodId,
      @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull JType returnType,
      int modifier) {
    this(info, methodId, enclosingType, returnType, modifier, NopMethodLoader.INSTANCE);
  }

  public JMethod(@Nonnull SourceInfo info,
        @Nonnull JMethodId methodId,
        @Nonnull JDefinedClassOrInterface enclosingType,
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
    if ((!JModifier.isStatic(modifier)) && (!JModifier.isAbstract(modifier))
        && (!JModifier.isNative(modifier)) && enclosingType instanceof JDefinedClass) {
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
  public int getModifier() {
    return modifier;
  }

  /**
   * Adds a parameter to this method.
   */
  public void addParam(JParameter parameter) {
    params.add(parameter);
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

  /**
   * Removes the parameter at the specified index.
   */
  public void removeParam(int index) {
    params.remove(index);
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

  public void setEnclosingType(@Nonnull JDefinedClassOrInterface enclosingType) {
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
  public void addAnnotation(@Nonnull JAnnotationLiteral annotation) {
    annotations.addAnnotation(annotation);
  }

  @Override
  @CheckForNull
  public JAnnotationLiteral getAnnotation(@Nonnull JAnnotation annotationType) {
    loader.ensureAnnotation(this, annotationType);
    return annotations.getAnnotation(annotationType);
  }

  @Override
  @Nonnull
  public Collection<JAnnotationLiteral> getAnnotations() {
    loader.ensureAnnotations(this);
    return annotations.getAnnotations();
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
    annotations.traverse(visitor);
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
    annotations.traverse(schedule);
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(params, existingNode, (JParameter) newNode, transformation)) {
      if (!annotations.transform(existingNode, newNode, transformation)) {
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

  public static boolean isClinit(@Nonnull JMethod method) {
    return method.getName().equals(NamingTools.STATIC_INIT_NAME);
  }
}
