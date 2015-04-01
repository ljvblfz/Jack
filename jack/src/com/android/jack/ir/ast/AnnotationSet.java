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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class AnnotationSet {

  @Nonnull
  private final Map<JAnnotationType, ArrayList<JAnnotationLiteral>> annotations =
    new HashMap<JAnnotationType, ArrayList<JAnnotationLiteral>>();

  AnnotationSet() {
  }

  void addAnnotation(@Nonnull JAnnotationLiteral annotation) throws UnsupportedOperationException {
    JAnnotationType type = annotation.getType();
    ArrayList<JAnnotationLiteral> annotationLiterals = annotations.get(type);
    if (annotationLiterals == null) {
      annotationLiterals = new ArrayList<JAnnotationLiteral>(1);
      annotations.put(type, annotationLiterals);
    }
    annotationLiterals.add(annotation);
  }

  /**
   * @return {@link List} of {@link JAnnotationLiteral} contained into this
   *         {@link AnnotationSet} and having the type {@code annotationType}.
   */
  @Nonnull
  List<JAnnotationLiteral> getAnnotation(@Nonnull JAnnotationType annotationType) {
    List<JAnnotationLiteral> annotationLiterals = annotations.get(annotationType);
    if (annotationLiterals == null) {
      return Collections.emptyList();
    }
    return annotationLiterals;
  }

  /**
   * @return {@link Collection} of {@link JAnnotationLiteral} contained into this
   *         {@link AnnotationSet}.
   */
  @Nonnull
  Collection<JAnnotationLiteral> getAnnotations() {
    Collection<JAnnotationLiteral> allAnnotations = new ArrayList<JAnnotationLiteral>();
    for (Collection<JAnnotationLiteral> annotationLiterals : annotations.values()) {
      allAnnotations.addAll(annotationLiterals);
    }
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(allAnnotations);
  }

  /**
   * @return {@link Collection} of {@link JAnnotationType} contained into this
   *         {@link AnnotationSet}.
   */
  @Nonnull
  Collection<JAnnotationType> getAnnotationTypes() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(annotations.keySet());
  }

  /**
   * @return true if the transformation was applied. False if the transformation could not be
   * applied because {@code existingNode} was not present in this {@link AnnotationSet}.
   */
  boolean transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (existingNode instanceof JAnnotationLiteral) {
      JAnnotationLiteral existingAnnotation = (JAnnotationLiteral) existingNode;
      List<JAnnotationLiteral> annotationLiterals = getAnnotation(existingAnnotation.getType());
      switch (transformation) {
        case INSERT_AFTER:
        case INSERT_BEFORE:
          throw new UnsupportedOperationException();
        case REPLACE:
          assert newNode instanceof JAnnotationLiteral;
          annotationLiterals.remove(existingAnnotation);
          annotationLiterals.add((JAnnotationLiteral) newNode);
          return true;
        case REMOVE:
          assert newNode == null;
          annotationLiterals.remove(existingAnnotation);
          if (annotationLiterals.isEmpty()) {
            annotations.remove(existingAnnotation.getType());
          }
          return true;
      }
    }

    return false;
  }

  void traverse(@Nonnull JVisitor visitor) {
    for (ArrayList<JAnnotationLiteral> annotation : annotations.values()) {
      visitor.accept(annotation);
    }
  }

  void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    for (List<JAnnotationLiteral> annotationLiterals : annotations.values()) {
      for (JAnnotationLiteral annotation : annotationLiterals) {
        annotation.traverse(schedule);
      }
    }
  }
}
