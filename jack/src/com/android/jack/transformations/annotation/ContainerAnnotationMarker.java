/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.transformations.annotation;

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.SerializableMarker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A {@link Marker} allowing to define which is the annotation type that is a container of an
 * annotation and its retention policy.
 */
@Description("Container.")
@ValidOn(value = {JAnnotation.class})
public class ContainerAnnotationMarker implements SerializableMarker {

  @Nonnull
  private final JAnnotationType containerAnnotationType;

  @Nonnull
  private final JRetentionPolicy retentionPolicy;


  public ContainerAnnotationMarker(@Nonnull JAnnotationType containerAnnotationType,
      @Nonnull JRetentionPolicy retentionPolicy) {
    this.containerAnnotationType = containerAnnotationType;
    this.retentionPolicy = retentionPolicy;
  }

  @Nonnull
  public JAnnotationType getContainerAnnotationType() {
    return containerAnnotationType;
  }

  @Nonnull
  public JRetentionPolicy getRetentionPolicy() {
    return retentionPolicy;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
