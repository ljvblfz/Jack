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

package com.android.jack.jayce.v0002.io;

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JType;
import com.android.jack.jayce.linker.SymbolManager;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.NodeFactory;
import com.android.jack.jayce.v0002.nodes.NSourceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Import helper.
 */
public class ImportHelper {

  @Nonnull
  private final NodeFactory factory;

  @Nonnull
  private final SymbolManager<JCatchBlock> catchBlockSymbols = new SymbolManager<JCatchBlock>();

  @Nonnull
  private final SymbolManager<JCaseStatement> caseSymbols = new SymbolManager<JCaseStatement>();

  @Nonnull
  private final SymbolManager<JField> fieldSymbols = new SymbolManager<JField>();

  @Nonnull
  private final SymbolManager<JLocal> localSymbols = new SymbolManager<JLocal>();

  @Nonnull
  private final SymbolManager<JParameter> parameterSymbols = new SymbolManager<JParameter>();

  @Nonnull
  private final SymbolManager<JLabeledStatement> labelSymbols =
      new SymbolManager<JLabeledStatement>();

  public ImportHelper(@Nonnull NodeFactory factory) {
    this.factory = factory;
  }

  @CheckForNull
  public static String getMethodSignature(@CheckForNull JMethod method) {
    return method != null ? NNode.getFormatter().getName(method) : null;
  }

  @CheckForNull
  public static String getSignatureName(@CheckForNull JType type) {
    return type != null ? NNode.getFormatter().getName(type) : null;
  }

  @Nonnull
  public static List<String> getSignatureNameList(@Nonnull List<? extends JType> types) {
    int typesNumber = types.size();
    List<String> signatures = new ArrayList<String>(typesNumber);
    for (JType type : types) {
      signatures.add(NNode.getFormatter().getName(type));
    }
    return signatures;
  }

  @CheckForNull
  public static String getLabelName(@CheckForNull JLabel label) {
    if (label != null) {
      return label.getName();
    }
    return null;
  }

  @CheckForNull
  public NNode load(@CheckForNull Object jElement) {
    if (jElement == null) {
      return null;
    }
    NNode node = factory.createNNode(jElement);
    node.importFromJast(this, jElement);
    return node;
  }

  @CheckForNull
  public NSourceInfo load(@CheckForNull SourceInfo sourceInfo) {
    if (sourceInfo == null) {
      return null;
    }
    if (sourceInfo == SourceOrigin.UNKNOWN) {
      return NSourceInfo.UNKNOWN;
    }
    NSourceInfo nSourceInfo = new NSourceInfo();
    nSourceInfo.importFromJast(sourceInfo);
    return nSourceInfo;
  }

  @Nonnull
  @SuppressWarnings("unchecked")
  public <T extends NNode> List<T> load(
      @Nonnull Class<T> nodeClass, @Nonnull Iterable<?> jElements) {
    List<T> nodes = new ArrayList<T>();
    for (Object jElement : jElements) {
      NNode node = load(jElement);
      assert node != null;
      assert nodeClass.isAssignableFrom(node.getClass());
      nodes.add((T) node);
    }
    return nodes;
  }

  @Nonnull
  public <T> List<String> getIds(SymbolManager<T> symbolManager, List<? extends T> nodes) {
    List<String> ids = new ArrayList<String>(nodes.size());
    for (T node: nodes) {
      ids.add(symbolManager.getId(node));
    }
    return ids;
  }

  @Nonnull
  public SymbolManager<JCatchBlock> getCatchBlockSymbols() {
    return catchBlockSymbols;
  }

  @Nonnull
  public SymbolManager<JCaseStatement> getCaseSymbols() {
    return caseSymbols;
  }

  @Nonnull
  public SymbolManager<JField> getFieldSymbols() {
    return fieldSymbols;
  }

  @Nonnull
  public SymbolManager<JParameter> getParameterSymbols() {
    return parameterSymbols;
  }


  @Nonnull
  public SymbolManager<JLocal> getLocalSymbols() {
    return localSymbols;
  }

  @Nonnull
  public SymbolManager<JLabeledStatement> getLabelSymbols() {
    return labelSymbols;
  }

  @CheckForNull
  public static String getMethodClassSignature(@CheckForNull JMethod method) {
    return method != null ? NNode.getFormatter().getName(method.getEnclosingType()) : null;

  }

  @Nonnull
  public static List<String> getMethodArgsSignature(@Nonnull JMethodId methodId) {
    List<JType> argTypes = methodId.getParamTypes();
    if (argTypes.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> argsTypeAsSignature = new ArrayList<String>(argTypes.size());
    for (JType type : argTypes) {
      argsTypeAsSignature.add(NNode.getFormatter().getName(type));
    }
    return argsTypeAsSignature;
  }
}
