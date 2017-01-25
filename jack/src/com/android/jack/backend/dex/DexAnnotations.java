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

import javax.annotation.Nonnull;

/**
 * This interfaces holds signature of annotation used by dex.
 */
public interface DexAnnotations {
  @Nonnull
  final String ANNOTATION_ANNOTATION_DEFAULT = "Ldalvik/annotation/AnnotationDefault;";
  @Nonnull
  final String ANNOTATION_MEMBER_CLASSES = "Ldalvik/annotation/MemberClasses;";
  @Nonnull
  final String ANNOTATION_INNER = "Ldalvik/annotation/InnerClass;";
  @Nonnull
  final String ANNOTATION_ENCLOSING_CLASS = "Ldalvik/annotation/EnclosingClass;";
  @Nonnull
  final String ANNOTATION_ENCLOSING_METHOD = "Ldalvik/annotation/EnclosingMethod;";
  @Nonnull
  final String ANNOTATION_THROWS = "Ldalvik/annotation/Throws;";
  @Nonnull
  final String ANNOTATION_SIGNATURE = "Ldalvik/annotation/Signature;";
}
