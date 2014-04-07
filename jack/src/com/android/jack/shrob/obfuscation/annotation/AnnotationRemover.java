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

import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Common class for annotation removers.
 */
@HasKeyId
public abstract class AnnotationRemover {

  @Nonnull
  public static final
      BooleanPropertyId EMIT_RUNTIME_INVISIBLE_ANNOTATION = BooleanPropertyId.create(
          "jack.annotation.runtimeinvisible", "Emit annotations that are runtime invisible")
          .addDefaultValue(Boolean.TRUE);

  @Nonnull
  public static final BooleanPropertyId EMIT_RUNTIME_VISIBLE_ANNOTATION = BooleanPropertyId.create(
      "jack.annotation.runtimevisible", "Emit annotations that are runtime visible")
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  private final boolean addRuntimeVisibleAnnotations;
  private final boolean addRuntimeInvisibleAnnotations;
  private final boolean addSystemAnnotations;

  protected AnnotationRemover(boolean addRuntimeVisibleAnnotations,
      boolean addRuntimeInvisibleAnnotations, boolean addSystemAnnotations) {
    this.addRuntimeVisibleAnnotations = addRuntimeVisibleAnnotations;
    this.addRuntimeInvisibleAnnotations = addRuntimeInvisibleAnnotations;
    this.addSystemAnnotations = addSystemAnnotations;
  }

  boolean mustBeKept(JAnnotationLiteral annotation) {
    switch (annotation.getRetentionPolicy()) {
      case RUNTIME:
        return addRuntimeVisibleAnnotations;
      case CLASS:
      case SOURCE:
        return addRuntimeInvisibleAnnotations;
      case SYSTEM:
        return addSystemAnnotations;
      default:
        throw new AssertionError();
    }
  }
}
