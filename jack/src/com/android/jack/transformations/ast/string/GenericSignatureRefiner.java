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

package com.android.jack.transformations.ast.string;

import com.android.jack.Jack;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JCompositeStringLiteral;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeStringLiteral;
import com.android.jack.ir.ast.JTypeStringLiteral.Kind;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.signature.GenericSignatureAction;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Decomposed a generic signature into {@code JStringLiteral} and {@code JTypeLiteral}.
 */
@Constraint(need = OriginalNames.class)
@Transform(add = {JTypeStringLiteral.class, JCompositeStringLiteral.class, JStringLiteral.class})
public class GenericSignatureRefiner implements GenericSignatureAction<JType> {

  @CheckForNull
  private JAbstractStringLiteral jstringLiteral = null;

  @Nonnull
  private StringBuilder strBuf = new StringBuilder();

  @Nonnull
  private final  JLookup jlookup;

  @Nonnull
  private final TypeFormatter formatter = BinaryQualifiedNameFormatter.getFormatter();

  public GenericSignatureRefiner() {
    jlookup = Jack.getSession().getLookup();
  }

  @Override
  public void parsedSymbol(char symbol) {
    strBuf.append(symbol);
  }

  @Override
  public void parsedIdentifier(@Nonnull String identifier) {
    strBuf.append(identifier);
  }

  @Override
  @CheckForNull
  public JType parsedTypeName(@Nonnull String name) {
    updateJStringLiteral(getJStringLiteralFromBuffer());
    try {
      JType type = jlookup.getType(NamingTools.getTypeSignatureName(name));
      updateJStringLiteral(new JTypeStringLiteral(SourceOrigin.UNKNOWN, Kind.BINARY_QN, type));
      return type;
    } catch (JLookupException e) {
      // Type not found, keep it as a JStringLiteral
      updateJStringLiteral(new JStringLiteral(SourceOrigin.UNKNOWN, name));
      return null;
    }
  }

  @Override
  @CheckForNull
  public JType parsedInnerTypeName(@CheckForNull JType enclosingType, @Nonnull String name) {
    updateJStringLiteral(getJStringLiteralFromBuffer());
    if (enclosingType != null) {
      try {
        JType type = jlookup.getType(NamingTools.getTypeSignatureName(
            formatter.getName(enclosingType) + '$' + name));
        updateJStringLiteral(new JTypeStringLiteral(SourceOrigin.UNKNOWN, Kind.SIMPLE_NAME, type));
        return type;
      } catch (JLookupException e) {
        // Type not found, keep it as a JStringLiteral
      }
    }
    updateJStringLiteral(new JStringLiteral(SourceOrigin.UNKNOWN, name));
    return null;
  }

  @Override
  public void start() {
    strBuf = new StringBuilder();
    jstringLiteral = null;
  }

  @Override
  public void stop() {
    updateJStringLiteral(getJStringLiteralFromBuffer());
  }

  @Nonnull
  public JAbstractStringLiteral getNewSignature() {
    assert jstringLiteral != null;
    return jstringLiteral;
  }

  @Nonnull
  private JAbstractStringLiteral getJStringLiteralFromBuffer() {
    JAbstractStringLiteral newStringLiteral =
        new JStringLiteral(SourceOrigin.UNKNOWN, strBuf.toString());
    strBuf = new StringBuilder();
    return newStringLiteral;
  }

  private void updateJStringLiteral(@Nonnull JAbstractStringLiteral stringLiteral) {
    if (jstringLiteral == null) {
      jstringLiteral = stringLiteral;
    } else {
      assert jstringLiteral != null;
      jstringLiteral =
          new JCompositeStringLiteral(SourceOrigin.UNKNOWN, jstringLiteral, stringLiteral);
    }
  }
}
