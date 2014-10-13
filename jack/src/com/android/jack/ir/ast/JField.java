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
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.Collection;
import java.util.List;

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
  protected final AnnotationSet annotations = new AnnotationSet();

  protected int modifier;

  @Nonnull
  private JDefinedClassOrInterface enclosingType;

  @CheckForNull
  private JLiteral initialValue;

  public JField(
      @Nonnull SourceInfo info,
      @Nonnull String name,
      @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull JType type,
      int modifier) {
    super(info);
    assert JModifier.isFieldModifier(modifier) : "Wrong field modifier.";
    assert JModifier.isValidFieldModifier(modifier);
    this.modifier = modifier;
    this.enclosingType = enclosingType;
    this.fieldId = new JFieldId(name, type,
        JModifier.isStatic(modifier) ? FieldKind.STATIC : FieldKind.INSTANCE, this);
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

  public boolean isExternal() {
    return getEnclosingType().isExternal();
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
      // Do not visit declStmt, it gets visited within its own code block.
      annotations.traverse(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    annotations.traverse(schedule);
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
  public void addAnnotation(@Nonnull JAnnotationLiteral annotation) {
    annotations.addAnnotation(annotation);
  }

  @Override
  @Nonnull
  public List<JAnnotationLiteral> getAnnotations(@Nonnull JAnnotation annotationType) {
    return annotations.getAnnotation(annotationType);
  }

  @Override
  @Nonnull
  public Collection<JAnnotationLiteral> getAnnotations() {
    return annotations.getAnnotations();
  }

  @Override
  @Nonnull
  public Collection<JAnnotation> getAnnotationTypes() {
    return annotations.getAnnotationTypes();
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!annotations.transform(existingNode, newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JDefinedClassOrInterface)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
