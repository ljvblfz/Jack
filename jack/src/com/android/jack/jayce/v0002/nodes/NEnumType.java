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

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.util.NamingTools;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Java enum type reference expression.
 */
public class NEnumType extends NClassType {

  @SuppressWarnings("hiding")
  @Nonnull
  public static final Token TOKEN = Token.ENUM;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDefinedEnum jEnumType = (JDefinedEnum) node;
    modifiers = jEnumType.getModifier();
    signature = ImportHelper.getSignatureName(jEnumType);
    superClass = ImportHelper.getSignatureName(jEnumType.getSuperClass());
    superInterfaces = ImportHelper.getSignatureNameList(jEnumType.getImplements());
    enclosingType = ImportHelper.getSignatureName(jEnumType.getEnclosingType());
    enclosingMethodClass = ImportHelper.getMethodClassSignature(jEnumType.getEnclosingMethod());
    enclosingMethod = ImportHelper.getMethodSignature(jEnumType.getEnclosingMethod());
    inners = ImportHelper.getSignatureNameList(jEnumType.getMemberTypes());
    fields = loader.load(NField.class, jEnumType.getFields());
    methods = loader.load(NMethod.class, jEnumType.getMethods());
    annotations = loader.load(NAnnotationLiteral.class, jEnumType.getAnnotations());
    markers = loader.load(NMarker.class, jEnumType.getAllMarkers());
    sourceInfo = loader.load(jEnumType.getSourceInfo());
  }

  @Nonnull
  @Override
  public JDefinedEnum exportAsJast(@Nonnull ExportSession exportSession) {
    throw new UnsupportedOperationException();
  }
  @Override
  @Nonnull
  public JDefinedEnum create(@Nonnull JPackage enclosingPackage,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert sourceInfo != null;
    assert signature != null;
    String binaryName = NamingTools.getClassBinaryNameFromDescriptor(signature);
    String simpleName = NamingTools.getSimpleClassNameFromBinaryName(binaryName);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(
        new ExportSession(loader.getLookup(), enclosingPackage.getProgram(), NodeLevel.TYPES));
    JDefinedEnum jEnumType =
        new JDefinedEnum(jSourceInfo, simpleName, modifiers, enclosingPackage, loader);
    return jEnumType;
  }
  @Override
  public void updateToStructure(@Nonnull JDefinedClassOrInterface loading,
      @Nonnull JayceClassOrInterfaceLoader loader) {
    assert sourceInfo != null;
    assert signature != null;
    JDefinedEnum jEnumType = (JDefinedEnum) loading;
    ExportSession exportSession = new ExportSession(loader.getLookup(), loading.getJProgram(),
        NodeLevel.STRUCTURE);
    exportSession.setCurrentType(jEnumType);
      try {
        exportSession.setCurrentType(jEnumType);
        if (superClass != null) {
          jEnumType.setSuperClass(exportSession.getLookup().getClass(superClass));
        }
        for (String superInterface : superInterfaces) {
          jEnumType.addImplements(exportSession.getLookup().getInterface(superInterface));
        }
        if (enclosingType != null) {
          jEnumType.setEnclosingType(
              (JClassOrInterface) exportSession.getLookup().getType(enclosingType));
        }
        if (enclosingMethodClass != null) {
          assert enclosingMethod != null;
          JClass enclosingMethodJClass =
              exportSession.getLookup().getClass(enclosingMethodClass);
          if (enclosingMethodJClass instanceof JDefinedClass) {
            try {
              jEnumType.setEnclosingMethod(
                  exportSession.getDeclaredMethod((JDefinedClass) enclosingMethodJClass,
                      enclosingMethod));
            } catch (JMethodLookupException e) {
              // Method does not longer exists but anonymous already exists, could be trigger by
              // build tricky mechanism, skip it to go ahead
            }
          }
        }
        for (String memberType : inners) {
          jEnumType.addMemberType(
              (JClassOrInterface) exportSession.getLookup().getType(memberType));
        }
        for (NField field : fields) {
          JField jField = field.exportAsJast(exportSession);
          jEnumType.addField(jField);
        }
        for (NMethod method : methods) {
          JMethod jMethod = method.exportAsJast(exportSession, loader);
          jEnumType.addMethod(jMethod);
        }
        for (NAnnotationLiteral annotation : annotations) {
          jEnumType.addAnnotation(annotation.exportAsJast(exportSession));
        }
      } finally {
        exportSession.setCurrentType(null);
      }
    for (NMarker marker : markers) {
      jEnumType.addMarker(marker.exportAsJast(exportSession));
    }
    exportSession.getFieldInitializerFieldResolver().clear();
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeInt(modifiers);
    out.writeId(signature);
    out.writeId(superClass);
    out.writeIds(superInterfaces);
    out.writeId(enclosingType);
    out.writeId(enclosingMethodClass);
    out.writeId(enclosingMethod);
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
    superClass = in.readId();
    superInterfaces = in.readIds();
    enclosingType = in.readId();
    enclosingMethodClass = in.readId();
    enclosingMethod = in.readId();
    inners = in.readIds();
    fields = in.readNodes(NField.class);
    methods = in.readNodes(NMethod.class);
    annotations = in.readNodes(NAnnotationLiteral.class);
    markers = in.readNodes(NMarker.class);

  }

  @Nonnull
  @Override
  public Token getToken() {
    return TOKEN;
  }

}
