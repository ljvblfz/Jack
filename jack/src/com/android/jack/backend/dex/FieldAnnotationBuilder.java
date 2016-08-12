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

import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.rop.annotation.Annotations;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Builds the {@link com.android.dx.rop.annotation.Annotations Annotations} of a field.
 */
@Description("Builds the rop annotations of a field")
@Synchronized
@Constraint(need = {ClassDefItemMarker.class, ClassDefItemMarker.Field.class})
@Transform(add = ClassDefItemMarker.FieldAnnotation.class, modify = ClassDefItemMarker.class)
@Use(AnnotationBuilder.class)
@Optional(@ToSupport(feature = SourceVersion8.class,
    add = @Constraint(no = JAnnotation.RepeatedAnnotation.class)))
@Filter(TypeWithoutValidTypePrebuilt.class)
public class FieldAnnotationBuilder implements RunnableSchedulable<JField> {

  @Override
  public synchronized void run(@Nonnull JField field) {
    JDefinedClassOrInterface declaringClass = field.getEnclosingType();

    Collection<JAnnotation> annotations = field.getAnnotations();
    if (!annotations.isEmpty()) {
      Annotations ropAnnotations = new AnnotationBuilder().createAnnotations(annotations);
      if (ropAnnotations.size() > 0) {
        ClassDefItemMarker classDefItemMarker = declaringClass.getMarker(ClassDefItemMarker.class);
        assert classDefItemMarker != null;

        ClassDefItem classDefItem = classDefItemMarker.getClassDefItem();
        classDefItem.addFieldAnnotations(
            RopHelper.createFieldRef(field, field.getEnclosingType()), ropAnnotations);
      }
    }
  }
}
