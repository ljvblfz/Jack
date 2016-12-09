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

package com.android.jack.jayce.v0004.nodes;

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.util.NamingTools;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@code NNode} holds information of a Java annotation type for Jayce.
 */
public class NAnnotationType extends NInterfaceType {

  @SuppressWarnings("hiding")
  @Nonnull
  public static final Token TOKEN = Token.ANNOTATION_TYPE;

  @CheckForNull
  public JRetentionPolicy retentionPolicy;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDefinedAnnotationType jAnnotationType = (JDefinedAnnotationType) node;
    retentionPolicy = jAnnotationType.getRetentionPolicy();
    modifiers = jAnnotationType.getModifier();
    signature = ImportHelper.getSignatureName(jAnnotationType);
    superInterfaces = ImportHelper.getSignatureNameList(jAnnotationType.getImplements());
    enclosingType = ImportHelper.getSignatureName(jAnnotationType.getEnclosingType());
    inners = ImportHelper.getSignatureNameList(jAnnotationType.getMemberTypes());
    setFields(loader.load(NField.class, jAnnotationType.getFields()));
    setMethods(loader.load(NMethod.class, jAnnotationType.getMethods()));
    annotations = loader.load(NAnnotation.class, jAnnotationType.getAnnotations());
    markers = loader.load(NMarker.class, jAnnotationType.getAllMarkers());
    sourceInfo = jAnnotationType.getSourceInfo();
  }

  @Nonnull
  @Override
  public JDefinedAnnotationType exportAsJast(@Nonnull ExportSession exportSession) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public JDefinedAnnotationType create(@Nonnull JPackage enclosingPackage,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert signature != null;
    assert retentionPolicy != null;
    String binaryName = NamingTools.getClassBinaryNameFromDescriptor(signature);
    String simpleName = NamingTools.getSimpleClassNameFromBinaryName(binaryName);
    JDefinedAnnotationType jInterfaceType = new JDefinedAnnotationType(SourceInfo.UNKNOWN,
        simpleName, modifiers, enclosingPackage, loader);
    jInterfaceType.setRetentionPolicy(retentionPolicy);
    return jInterfaceType;
  }

  @Override
  public void loadStructure(@Nonnull JDefinedClassOrInterface loading,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert sourceInfo != null;
    assert signature != null;
    JDefinedAnnotationType jInterfaceType = (JDefinedAnnotationType) loading;
    ExportSession exportSession = new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
    exportSession.setCurrentType(jInterfaceType);
    loading.setSourceInfo(sourceInfo);
    for (String superInterface : superInterfaces) {
      jInterfaceType.addImplements(
          exportSession.getLookup().getInterface(superInterface));
    }
    if (enclosingType != null) {
      jInterfaceType.setEnclosingType(
          (JClassOrInterface) exportSession.getLookup().getType(enclosingType));
    }
    for (String memberType : inners) {
      jInterfaceType.addMemberType(
          (JClassOrInterface) exportSession.getLookup().getType(memberType));
    }
    for (NField field : getFields()) {
      JField jField = field.exportAsJast(exportSession, loader);
      jInterfaceType.addField(jField);
    }
    for (NMethod method : getMethods()) {
      JMethod jMethod = method.exportAsJast(exportSession, loader);
      jInterfaceType.addMethod(jMethod);
    }
    for (NMarker marker : markers) {
      jInterfaceType.addMarker(marker.exportAsJast(exportSession));
    }
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert retentionPolicy != null;
    out.writeRetentionPolicyEnum(retentionPolicy);
    out.writeInt(modifiers);
    out.writeId(signature);
    out.writeIds(superInterfaces);
    out.writeId(enclosingType);
    out.writeIds(inners);
    out.writeNodes(getFields());
    out.writeNodes(getMethods());
    out.writeNodes(annotations);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    level = in.getNodeLevel();
    retentionPolicy = in.readRetentionPolicyEnum();
    modifiers = in.readInt();
    signature = in.readId();
    if (level != NodeLevel.TYPES) {
      superInterfaces = in.readIds();
      enclosingType = in.readId();
      inners = in.readIds();
      setFields(in.readNodes(NField.class));
      setMethods(in.readNodes(NMethod.class));
      annotations = in.readNodes(NAnnotation.class);
      markers = in.readNodes(NMarker.class);
    }
  }

  @Nonnull
  @Override
  public Token getToken() {
    return TOKEN;
  }
}
