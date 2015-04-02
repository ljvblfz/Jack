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

import com.android.jack.backend.dex.annotations.tag.ReflectAnnotations;
import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.rop.annotation.Annotation;
import com.android.jack.dx.rop.annotation.AnnotationVisibility;
import com.android.jack.dx.rop.annotation.Annotations;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Builds {@link Annotations} from list of {@link JAnnotation}.
 */
@Constraint(need = {JAnnotation.class, ReflectAnnotations.class})
@Protect(add = JAnnotation.class, modify = JAnnotation.class,
    remove = JAnnotation.class)
public class AnnotationBuilder {

  /**
   * Thrown to cancel creation of source annotation.
   */
  private static class SourceAnnotationException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  @Nonnull
  private static SourceAnnotationException sourceAnnotationException =
      new SourceAnnotationException();

  @Nonnull
  private final ConstantBuilder constantBuilder = new ConstantBuilder();

  public AnnotationBuilder() {
  }

  @Nonnull
  public Annotations createAnnotations(@Nonnull Collection<JAnnotation> annotations) {
    Annotations ropAnnotations = new Annotations();
    for (JAnnotation annotation : annotations) {
      Annotation ropAnnotation;
      try {
        ropAnnotation = createAnnotation(annotation);
      } catch (SourceAnnotationException e) {
        // expected for source visible annotations, just ignore
        continue;
      }
      ropAnnotations.add(ropAnnotation);
    }
    ropAnnotations.setImmutable();
    return ropAnnotations;
  }

  @Nonnull
  private Annotation createAnnotation(@Nonnull JAnnotation annotation)
      throws SourceAnnotationException {
    Annotation ropAnnotation = new Annotation(RopHelper.getCstType(annotation.getType()),
        getVisibility(annotation.getRetentionPolicy()));
    constantBuilder.createAnnotationPairs(annotation, ropAnnotation);
    ropAnnotation.setImmutable();
    return ropAnnotation;
  }

  @Nonnull
  private static AnnotationVisibility getVisibility(@Nonnull JRetentionPolicy retentionPolicy)
      throws SourceAnnotationException {
    switch (retentionPolicy) {
      case CLASS:
        return AnnotationVisibility.BUILD;
      case RUNTIME:
        return AnnotationVisibility.RUNTIME;
      case SOURCE:
        throw sourceAnnotationException;
      case SYSTEM:
        return AnnotationVisibility.SYSTEM;
      default:
        throw new AssertionError();
    }
  }

}
