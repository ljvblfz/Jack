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
import com.android.jack.ir.ast.JNode.Transformation;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class AnnotationSet implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Map<JAnnotation, JAnnotationLiteral> annotations =
    new HashMap<JAnnotation, JAnnotationLiteral>();

  AnnotationSet() {
  }

  void addAnnotation(@Nonnull JAnnotationLiteral annotation) throws UnsupportedOperationException {
    JAnnotation type = annotation.getType();
    assert getAnnotation(type) == null;
    annotations.put(type, annotation);
  }

  @CheckForNull
  JAnnotationLiteral getAnnotation(@Nonnull JAnnotation annotationType) {
    return annotations.get(annotationType);
  }

  /**
   * @return the annotations
   */
  @Nonnull
  Collection<JAnnotationLiteral> getAnnotations() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(annotations.values());
  }

  /**
   * @return true if the transformation was applied. False if the transformation could not be
   * applied because {@code existingNode} was not present in this {@code AnnotationList}.
   */
  boolean transform(
      @Nonnull JNode existingNode,
      @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (existingNode instanceof JAnnotationLiteral) {
      JAnnotationLiteral existingAnnotation = (JAnnotationLiteral) existingNode;

      if (annotations.get(existingAnnotation.getType()) == existingAnnotation) {

        switch (transformation) {
          case INSERT_AFTER:
          case INSERT_BEFORE:
            throw new UnsupportedOperationException();
          case REPLACE:
            assert newNode instanceof JAnnotationLiteral;
            annotations.put(existingAnnotation.getType(), (JAnnotationLiteral) newNode);
            return true;
          case REMOVE:
            assert newNode == null;
            annotations.remove(existingAnnotation.getType());
            return true;
        }
      }
    }
    return false;
  }

  void traverse(@Nonnull JVisitor visitor) {
    for (JAnnotationLiteral annotation : annotations.values()) {
      visitor.accept(annotation);
    }
  }

  void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    for (JAnnotationLiteral annotation : annotations.values()) {
      annotation.traverse(schedule);
    }
  }
}
