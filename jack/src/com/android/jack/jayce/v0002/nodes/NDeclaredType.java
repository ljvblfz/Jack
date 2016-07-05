/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.jayce.v0002.nodes;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.jayce.DeclaredTypeNode;
import com.android.jack.jayce.FieldNode;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.MethodNode;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.io.ExportSession;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any declared type.
 */
public abstract class NDeclaredType extends NNode implements HasSourceInfo, DeclaredTypeNode {

  @CheckForNull
  protected NodeLevel level;

  @Nonnull
  public List<NMethod> methods = Collections.emptyList();

  @Nonnull
  public List<NField> fields = Collections.emptyList();

  @Nonnull
  public List<NAnnotation> annotations = Collections.emptyList();

  @Nonnull
  private final TypeAndMethodFormatter lookupFormatter = Jack.getLookupFormatter();

  @Override
  @Nonnull
  public abstract JDefinedClassOrInterface exportAsJast(@Nonnull ExportSession exportSession);

  @Override
  @Nonnull
  public NodeLevel getLevel() {
    assert level != null;
    return level;
  }

  @Override
  public void loadAnnotations(@Nonnull JDefinedClassOrInterface loading,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    if (!annotations.isEmpty()) {
      ExportSession exportSession =
          new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
      for (NAnnotation annotation : annotations) {
        JAnnotation annotationLiteral = annotation.exportAsJast(exportSession);
        loading.addAnnotation(annotationLiteral);
        annotationLiteral.updateParents(loading);
      }
    }
  }

  @Override
  @Nonnull
  public FieldNode getFieldNode(int fieldNodeIndex) {
    return fields.get(fieldNodeIndex);
  }

  @Override
  @Nonnull
  public MethodNode getMethodNode(@Nonnull int methodNodeIndex) {
    return methods.get(methodNodeIndex);
  }

  protected boolean areIndicesValid() {
    boolean allValid = true;
    int size = methods.size();
    for (int i = 0; (i < size) && allValid; i++) {
      allValid &= (i == methods.get(i).methodNodeIndex);
    }
    size = fields.size();
    for (int i = 0; (i < size) && allValid; i++) {
      allValid &= (i == fields.get(i).fieldNodeIndex);
    }

    return allValid;
  }

}
