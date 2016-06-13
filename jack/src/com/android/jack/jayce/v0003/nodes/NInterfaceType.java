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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.util.NamingTools;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java interface type definition.
 */
public class NInterfaceType extends NDeclaredType {

  @Nonnull
  public static final Token TOKEN = Token.INTERFACE;

  public int modifiers;

  @CheckForNull
  public String signature;

  @Nonnull
  public List<String> superInterfaces = Collections.emptyList();

  @CheckForNull
  public String enclosingType;

  @Nonnull
  public List<String> inners = Collections.emptyList();

  @Nonnull
  public List<NField> fields = Collections.emptyList();

  @Nonnull
  public List<NAnnotation> annotations = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDefinedInterface jInterfaceType = (JDefinedInterface) node;
    modifiers = jInterfaceType.getModifier();
    signature = ImportHelper.getSignatureName(jInterfaceType);
    superInterfaces = ImportHelper.getSignatureNameList(jInterfaceType.getImplements());
    enclosingType = ImportHelper.getSignatureName(jInterfaceType.getEnclosingType());
    inners = ImportHelper.getSignatureNameList(jInterfaceType.getMemberTypes());
    fields = loader.load(NField.class, jInterfaceType.getFields());
    methods = loader.load(NMethod.class, jInterfaceType.getMethods());
    annotations = loader.load(NAnnotation.class, jInterfaceType.getAnnotations());
    markers = loader.load(NMarker.class, jInterfaceType.getAllMarkers());
    sourceInfo = loader.load(jInterfaceType.getSourceInfo());
  }

  @Nonnull
  @Override
  public JDefinedInterface exportAsJast(@Nonnull ExportSession exportSession) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public JDefinedInterface create(@Nonnull JPackage enclosingPackage,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert signature != null;
    String binaryName = NamingTools.getClassBinaryNameFromDescriptor(signature);
    String simpleName = NamingTools.getSimpleClassNameFromBinaryName(binaryName);
    JDefinedInterface jInterfaceType =
        new JDefinedInterface(SourceInfo.UNKNOWN, simpleName, modifiers, enclosingPackage, loader);
    return jInterfaceType;
  }

  @Override
  public void updateToStructure(@Nonnull JDefinedClassOrInterface loading,
      @Nonnull JayceClassOrInterfaceLoader loader) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert signature != null;
    JDefinedInterface jInterfaceType = (JDefinedInterface) loading;
    ExportSession exportSession = new ExportSession(loader.getLookup(), Jack.getSession(),
        NodeLevel.STRUCTURE);
    exportSession.setCurrentType(jInterfaceType);
    loading.setSourceInfo(sourceInfo.exportAsJast(exportSession));
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
    for (NField field : fields) {
      JField jField = field.exportAsJast(exportSession);
      jField.setEnclosingType(jInterfaceType);
      jInterfaceType.addField(jField);
    }
    for (NMethod method : methods) {
      JMethod jMethod = method.exportAsJast(exportSession, loader);
      jInterfaceType.addMethod(jMethod);
    }
    for (NAnnotation annotation : annotations) {
      jInterfaceType.addAnnotation(annotation.exportAsJast(exportSession));
    }
    for (NMarker marker : markers) {
      jInterfaceType.addMarker(marker.exportAsJast(exportSession));
    }
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeInt(modifiers);
    out.writeId(signature);
    out.writeIds(superInterfaces);
    out.writeId(enclosingType);
    out.writeIds(inners);
    out.writeNodes(fields);
    out.writeNodes(methods);
    out.writeNodes(annotations);
    out.writeNodes(markers);

  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    level = in.getNodeLevel();
    modifiers = in.readInt();
    signature = in.readId();
    if (level != NodeLevel.TYPES) {
      superInterfaces = in.readIds();
      enclosingType = in.readId();
      inners = in.readIds();
      fields = in.readNodes(NField.class);
      methods = in.readNodes(NMethod.class);
      assert areMethodIndicesValid();
      annotations = in.readNodes(NAnnotation.class);
      markers = in.readNodes(NMarker.class);
    }
  }

  @Nonnull
  @Override
  public Token getToken() {
    return TOKEN;
  }

  @Nonnull
  @Override
  public String getSignature() {
    assert signature != null;
    return signature;
  }

  @Override
  @Nonnull
  public NSourceInfo getSourceInfos() {
    assert sourceInfo != null;
    return sourceInfo;
  }

  @Override
  public void setSourceInfos(@Nonnull NSourceInfo sourceInfo) {
    this.sourceInfo = sourceInfo;
  }

}
