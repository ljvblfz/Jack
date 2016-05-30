/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.annotation;

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.library.DumpInLibrary;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * Common class for annotation removers.
 */
@HasKeyId
public abstract class AnnotationRemover {

  @Nonnull
  public static final BooleanPropertyId EMIT_SOURCE_RETENTION_ANNOTATION =
      BooleanPropertyId.create(
              "jack.annotation.source-retention",
              "Emit annotations that have a source retention")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_CLASS_RETENTION_ANNOTATION =
      BooleanPropertyId.create(
              "jack.annotation.class-retention",
              "Emit annotations that have a class retention")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_RUNTIME_RETENTION_ANNOTATION =
      BooleanPropertyId.create(
              "jack.annotation.runtime-retention",
              "Emit annotations that have a runtime retention")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class);

  private final boolean keepSourceAnnotations;
  private final boolean keepClassAnnotations;
  private final boolean keepRuntimeAnnotations;
  private final boolean keepSystemAnnotations;

  protected AnnotationRemover(
      boolean keepSourceAnnotations,
      boolean keepClassAnntations,
      boolean keepRuntimeAnnotations,
      boolean keepSystemAnnotations) {
    this.keepSourceAnnotations = keepSourceAnnotations;
    this.keepClassAnnotations = keepClassAnntations;
    this.keepRuntimeAnnotations = keepRuntimeAnnotations;
    this.keepSystemAnnotations = keepSystemAnnotations;
  }

  boolean mustBeKept(@Nonnull JAnnotation annotation) {
    switch (annotation.getRetentionPolicy()) {
      case SOURCE:
        return keepSourceAnnotations;
      case CLASS:
        return keepClassAnnotations;
      case RUNTIME:
        return keepRuntimeAnnotations;
      case SYSTEM:
        return keepSystemAnnotations;
      default:
        throw new AssertionError();
    }
  }
}
