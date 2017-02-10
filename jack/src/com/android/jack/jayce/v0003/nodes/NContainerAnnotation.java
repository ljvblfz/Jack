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

package com.android.jack.jayce.v0003.nodes;

import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.transformations.annotation.ContainerAnnotationMarker;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link NMarker} holds container annotation type and its runtime visibility.
 */
public class NContainerAnnotation extends NMarker {

  @Nonnull
  public static final Token TOKEN = Token.CONTAINER_ANNOTATION;

  @CheckForNull
  public JRetentionPolicy retentionPolicy;

  @CheckForNull
  public String annotationTypeSig;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    ContainerAnnotationMarker cam = (ContainerAnnotationMarker) node;
    retentionPolicy = cam.getRetentionPolicy();
    annotationTypeSig = ImportHelper.getSignatureName(cam.getContainerAnnotationType());
  }

  @Override
  @Nonnull
  public ContainerAnnotationMarker exportAsJast(@Nonnull ExportSession exportSession) {
    assert retentionPolicy != null;
    assert annotationTypeSig != null;
    return new ContainerAnnotationMarker(
        exportSession.getLookup().getAnnotationType(annotationTypeSig), retentionPolicy);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    retentionPolicy = in.readRetentionPolicyEnum();
    annotationTypeSig = in.readId();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
