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

package com.android.jack.jayce.v0003.io;

import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.linker.SymbolResolver;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.util.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A session for {@link com.android.jack.jayce.v0003.NNode#exportAsJast(ExportSession)}.
 */
public class ExportSession {

  @Nonnull
  private final SymbolResolver<JCatchBlock> catchBlockResolver =
      new SymbolResolver<JCatchBlock>();

  @Nonnull
  private final SymbolResolver<JField> fieldInitializerFieldResolver =
      new SymbolResolver<JField>();

  @Nonnull
  private final SymbolResolver<JLabeledStatement> labelResolver =
      new SymbolResolver<JLabeledStatement>();

  @Nonnull
  private final SymbolResolver<JVariable> variableResolver;

  @Nonnull
  private final SymbolResolver<JCaseStatement> caseResolver =
      new SymbolResolver<JCaseStatement>();

  @Nonnull
  private final JPhantomLookup lookup;

  @Nonnull
  private final NodeLevel nodeLevel;

  @CheckForNull
  private JDefinedClassOrInterface currentType;

  @CheckForNull
  private JMethod currentMethod;

  @Nonnull
  private final JSession session;

  public ExportSession(@Nonnull JSession session, @Nonnull NodeLevel nodeLevel) {
    this.session = session;
    this.lookup = session.getPhantomLookup();
    this.nodeLevel = nodeLevel;
    variableResolver =  new SymbolResolver<JVariable>();
  }

  @Nonnull
  public JPhantomLookup getLookup() {
    return lookup;
  }

  @Nonnull
  public NodeLevel getNodeLevel() {
    return nodeLevel;
  }

  public void setCurrentType(@CheckForNull JDefinedClassOrInterface currentType) {
    this.currentType = currentType;
  }

  public void setCurrentMethod(@CheckForNull JMethod currentMethod) {
    this.currentMethod = currentMethod;
  }

  @Nonnull
  public JDefinedClassOrInterface getCurrentType() {
    assert currentType != null;
    return currentType;
  }

  @Nonnull
  public JMethod getCurrentMethod() {
    assert currentMethod != null;
    return currentMethod;
  }

  @Nonnull
  public JSession getSession() {
    return session;
  }

  @Nonnull
  public SymbolResolver<JCaseStatement> getCaseResolver() {
    return caseResolver;
  }

  @Nonnull
  public SymbolResolver<JCatchBlock> getCatchBlockResolver() {
    return catchBlockResolver;
  }

  @Nonnull
  public SymbolResolver<JField> getFieldInitializerFieldResolver() {
    return fieldInitializerFieldResolver;
  }

  @Nonnull
  public SymbolResolver<JLabeledStatement> getLabelResolver() {
    return labelResolver;
  }

  @Nonnull
  public SymbolResolver<JVariable> getVariableResolver() {
    return variableResolver;
  }

  @Nonnull
  public List<JType> getTypeListFromSignatureList(@Nonnull List<String> typeSignatures)
      throws JTypeLookupException {
    List<JType> argsType;
    if (typeSignatures.isEmpty()) {
      argsType = Lists.create();
    } else {
      argsType = new ArrayList<JType>(typeSignatures.size());
    }

    for (String signature : typeSignatures) {
      argsType.add(lookup.getType(signature));
    }
    return argsType;
  }

  @Nonnull
  public JMethod getDeclaredMethod(@Nonnull JDefinedClassOrInterface receiver,
      @Nonnull String methodsignature) throws JMethodLookupException, JTypeLookupException {
    int argStart = methodsignature.indexOf('(');
    int argEnd = methodsignature.indexOf(')');
    assert argStart > 0 && argEnd > 0 && argStart < argEnd
    && argStart == methodsignature.lastIndexOf('(') && argEnd == methodsignature.lastIndexOf(')')
    && (argEnd + 1 < methodsignature.length());
    String methodName = methodsignature.substring(0, argStart);
    String argsTypeSignatures = methodsignature.substring(argStart + 1, argEnd);
    String returnSignature = methodsignature.substring(argEnd + 1);

    return receiver.getMethod(methodName, lookup.getType(returnSignature),
        getTypeList(argsTypeSignatures));
  }

  @Nonnull
  private List<JType> getTypeList(@Nonnull String argsTypeSignatures) throws JTypeLookupException {
    List<JType> argsType = new ArrayList<JType>();
    int index = 0;
    int len = argsTypeSignatures.length();
    int arrayDim = 0;
    while (index < len) {
      JType type = null;
      switch (argsTypeSignatures.charAt(index)) {
        case '[':
          arrayDim++;
          break;
        case 'L':
        {
          int signatureEnd = argsTypeSignatures.indexOf(';', index);
          type = lookup.getType(argsTypeSignatures.substring(index, signatureEnd + 1));
          index = signatureEnd;
        }
          break;
        case 'V':
          type = JPrimitiveTypeEnum.VOID.getType();
          break;
        case 'Z':
          type = JPrimitiveTypeEnum.BOOLEAN.getType();
          break;
        case 'B':
          type = JPrimitiveTypeEnum.BYTE.getType();
          break;
        case 'C':
          type = JPrimitiveTypeEnum.CHAR.getType();
          break;
        case 'S':
          type = JPrimitiveTypeEnum.SHORT.getType();
          break;
        case 'I':
          type = JPrimitiveTypeEnum.INT.getType();
          break;
        case 'J':
          type = JPrimitiveTypeEnum.LONG.getType();
          break;
        case 'F':
          type = JPrimitiveTypeEnum.FLOAT.getType();
          break;
        case 'D':
          type = JPrimitiveTypeEnum.DOUBLE.getType();
          break;
        default:
          throw new AssertionError();
      }
      if (type != null) {
        for (int i = 0; i < arrayDim; i++) {
          type = type.getArray();
        }
        arrayDim = 0;
        argsType.add(type);
      }
      index++;
    }
    return argsType;
  }

  @Nonnull
  public JFieldId getFieldId(@Nonnull JClassOrInterface receiver, @Nonnull String fieldName,
      @Nonnull JType fieldType, @Nonnull FieldKind kind) {
    return receiver.getOrCreateFieldId(fieldName, fieldType, kind);
  }
}
