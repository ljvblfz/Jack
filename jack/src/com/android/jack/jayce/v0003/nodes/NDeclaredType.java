/*
* Copyright (C) 2015 The Android Open Source Project
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

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.jayce.DeclaredTypeNode;
import com.android.jack.jayce.FieldNode;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.MethodNode;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0003.NNode;
import com.android.jack.jayce.v0003.io.ExportSession;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any declared type.
 */
public abstract class NDeclaredType extends NNode implements HasSourceInfo, DeclaredTypeNode {

  @CheckForNull
  protected NodeLevel level;

  @Nonnull
  private Map<String, NMethod> methods = Collections.emptyMap();

  @Nonnull
  private Map<String, NField> fields = Collections.emptyMap();

  @Nonnull
  public List<NAnnotation> annotations = Collections.emptyList();

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
      ExportSession exportSession = new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
      for (NAnnotation annotation : annotations) {
        JAnnotation annotationLiteral = annotation.exportAsJast(exportSession);
        loading.addAnnotation(annotationLiteral);
        annotationLiteral.updateParents(loading);
      }
    }
  }

  @Override
  @Nonnull
  public FieldNode getFieldNode(@Nonnull String fieldId) {
    return fields.get(fieldId);
  }

  @Override
  @Nonnull
  public MethodNode getMethodNode(@Nonnull String methodId) {
    return methods.get(methodId);
  }

  @Nonnull
  public Collection<NMethod> getMethods() {
    return methods.values();
  }

  public void setMethods(@Nonnull List<NMethod> methods) {
    this.methods = new HashMap<String, NMethod>(methods.size() + 1, 1.0f);
    for (NMethod nMethod : methods) {
      assert nMethod.getName() != null;
      StringBuilder builder = new StringBuilder(nMethod.getName()).append('(');
      for (NParameter param : nMethod.getParameters()) {
        assert param.type != null;
        builder.append(param.type);
      }
      assert nMethod.getReturnType() != null;
      builder.append(')').append(nMethod.getReturnType());
      String id = builder.toString();
      this.methods.put(id, nMethod);
      nMethod.setId(id);
    }
  }

  @Nonnull
  public Collection<NField> getFields() {
    return fields.values();
  }

  public void setFields(@Nonnull List<NField> fields) {
    this.fields = new HashMap<String, NField>(fields.size() + 1, 1.0f);
    for (NField nField : fields) {
      assert nField.name != null;
      assert nField.type != null;
      String id = nField.name + '-' + nField.type;
      this.fields.put(id, nField);
      nField.setId(id);
    }
  }
}
