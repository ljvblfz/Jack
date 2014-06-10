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
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.ir.naming.CompositeName;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.signature.GenericSignatureAction;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Constraint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Decompose a generic signature into {@code String}s and {@code TypeName}s.
 */
@Constraint(need = OriginalNames.class)
public class GenericSignatureRefiner implements GenericSignatureAction<JType> {

  @CheckForNull
  private CharSequence refinedSequence = null;

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
    updateRefinedSequence(getStringFromBuffer());
    try {
      JType type = jlookup.getType(NamingTools.getTypeSignatureName(name));
      updateRefinedSequence(new TypeName(Kind.BINARY_QN, type));
      return type;
    } catch (JLookupException e) {
      // Type not found, keep it as a String
      updateRefinedSequence(name);
      return null;
    }
  }

  @Override
  @CheckForNull
  public JType parsedInnerTypeName(@CheckForNull JType enclosingType, @Nonnull String name) {
    updateRefinedSequence(getStringFromBuffer());
    if (enclosingType != null) {
      try {
        JType type = jlookup.getType(NamingTools.getTypeSignatureName(
            formatter.getName(enclosingType) + '$' + name));
        // Append inner classes name discriminating id (e.g. 1 in La/b$1c;)
        int index = 0;
        char c = name.charAt(index);
        while (index < name.length() && Character.isDigit(c)) {
          strBuf.append(c);
          index++;
          c = name.charAt(index);
        }
        updateRefinedSequence(new TypeName(Kind.SIMPLE_NAME, type));
        return type;
      } catch (JLookupException e) {
        // Type not found, keep it as a String
      }
    }
    updateRefinedSequence(name);
    return null;
  }

  @Override
  public void start() {
    strBuf = new StringBuilder();
    refinedSequence = null;
  }

  @Override
  public void stop() {
    updateRefinedSequence(getStringFromBuffer());
  }

  @Nonnull
  public CharSequence getNewSignature() {
    assert refinedSequence != null;
    return refinedSequence;
  }

  @Nonnull
  private String getStringFromBuffer() {
    CharSequence oldBuffer = strBuf;
    strBuf = new StringBuilder();
    return oldBuffer.toString();
  }

  private void updateRefinedSequence(@Nonnull CharSequence newSequence) {
    if (refinedSequence == null) {
      refinedSequence = newSequence;
    } else if (newSequence.length() > 0){
      assert refinedSequence != null;
      refinedSequence = new CompositeName(refinedSequence, newSequence);
    }
  }
}
