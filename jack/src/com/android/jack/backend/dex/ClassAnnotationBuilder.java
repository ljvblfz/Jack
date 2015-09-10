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

package com.android.jack.backend.dex;

import com.android.jack.backend.dex.annotations.AnnotationMethodDefaultValue;
import com.android.jack.backend.dex.annotations.ClassAnnotationSchedulingSeparator;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.rop.annotation.Annotations;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Builds the {@link com.android.dx.rop.annotation.Annotations Annotations} of a class or interface.
 */
@Description("Builds the rop annotations of a JDeclaredType.")
@Constraint(need = ClassDefItemMarker.class, no = AnnotationMethodDefaultValue.class)
@Transform(add = ClassDefItemMarker.ClassAnnotation.class, modify = ClassDefItemMarker.class,
    remove = ClassAnnotationSchedulingSeparator.SeparatorTag.class)
@Use(AnnotationBuilder.class)
public class ClassAnnotationBuilder implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    Collection<JAnnotation> annotations = declaredType.getAnnotations();
    if (!annotations.isEmpty()) {
      Annotations classAnnotations = new AnnotationBuilder().createAnnotations(annotations);
      if (classAnnotations.size() > 0) {
        ClassDefItemMarker marker = declaredType.getMarker(ClassDefItemMarker.class);
        assert marker != null;

        ClassDefItem item = marker.getClassDefItem();
        item.setClassAnnotations(classAnnotations);
      }
    }
  }
}
