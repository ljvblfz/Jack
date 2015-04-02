/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.ir.ast;

import com.android.jack.Jack;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link JAnnotation} is an instance of {@link JAnnotationType} and consist of a reference to an
 * annotation type and zero or more element-value pairs, each of which associates a value with a
 * different element of the annotation type.
 */
@Description("Annotation instance.")
public class JAnnotation extends JLiteral {

  @Nonnull
  private final List<JNameValuePair> elements = new ArrayList<JNameValuePair>();
  @Nonnull
  private final JAnnotationType type;
  @Nonnull
  private final JRetentionPolicy retentionPolicy;

  public JAnnotation(@Nonnull SourceInfo sourceInfo,
      @Nonnull JRetentionPolicy retentionPolicy, @Nonnull JAnnotationType type) {
    super(sourceInfo);
    this.type = type;
    this.retentionPolicy = retentionPolicy;
  }

  @Nonnull
  @Override
  public JAnnotationType getType() {
    return type;
  }

  @Nonnull
  public JRetentionPolicy getRetentionPolicy() {
    return retentionPolicy;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      for (JNameValuePair pair : elements) {
        visitor.accept(pair);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JNameValuePair pair : elements) {
      pair.traverse(schedule);
    }
  }

  /**
   * Put an element into the set of (name, value) pairs for this instance.
   * If there is a preexisting element with the same name, it will be
   * replaced by this method.
   *
   * @param pair the (name, value) pair to place into this instance
   */
  public void put(@Nonnull JNameValuePair pair) {
    for (int i = 0; i < elements.size(); i++) {
      if (elements.get(i).getName().equals(pair.getName())) {
        elements.remove(i);
      }
    }

    elements.add(pair);
  }

  /**
   * Add an element to the set of (name, value) pairs for this instance.
   * It is an error to call this method if there is a preexisting element
   * with the same name.
   *
   * @param pair the (name, value) pair to add to this instance
   */
  public void add(@Nonnull JNameValuePair pair) {
    String name = pair.getName();
    if (getNameValuePair(name) != null) {
      throw new IllegalArgumentException("Name already added: " + name);
    }

    elements.add(pair);
  }

  /**
   * Gets the set of name-value pairs contained in this instance. The
   * result is always unmodifiable.
   *
   * @return the set of name-value pairs
   */
  @Nonnull
  public Collection<JNameValuePair> getNameValuePairs() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(elements);
  }

  @CheckForNull
  public JNameValuePair getNameValuePair(@Nonnull JMethodId methodId) {
    for (JNameValuePair pair : elements) {
      if (pair.getMethodId().equals(methodId)) {
        return pair;
      }
    }
    return null;
  }

  @CheckForNull
  public JNameValuePair getNameValuePair(@Nonnull String name) {
    for (JNameValuePair pair : elements) {
      if (pair.getName().equals(name)) {
        return pair;
      }
    }
    return null;
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(elements, existingNode, (JNameValuePair) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JDefinedClassOrInterface || parent instanceof JMethod
        || parent instanceof JField || parent instanceof JVariable
        || parent instanceof JArrayLiteral || parent instanceof JNameValuePair)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
