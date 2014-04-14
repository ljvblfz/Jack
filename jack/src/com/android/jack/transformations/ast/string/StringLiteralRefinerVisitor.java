/*
 * Copyright (C) 2012 The Android Open Source Project
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
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeStringLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Refine {@code JStringLiteral} into more specific string literals.
 */
@Constraint(need = {JStringLiteral.class, OriginalNames.class})
@Transform(add = JTypeStringLiteral.class)
public class StringLiteralRefinerVisitor extends JVisitor {

  @Nonnull
  private static final String SRC_ARRAY_REPRESENTATION = "[]";

  @Nonnull
  private final TransformationRequest tr;

  @Nonnull
  private final JLookup lookup;

  public StringLiteralRefinerVisitor(@Nonnull TransformationRequest tr) {
    this.tr = tr;
    lookup = Jack.getSession().getLookup();
  }

  @Override
  public boolean visit(@Nonnull JStringLiteral stringLiteral) {
    String strValue = stringLiteral.getValue();
    boolean hasDot = strValue.contains(".");
    boolean hasSlash = strValue.contains("/");

    if (hasDot && hasSlash) {
      // String mixing '.' and '/' is not considered as type.
      return false;
    }


    JType type = getTypeFromString(strValue);
    if (type != null) {
      tr.append(new Replace(stringLiteral, new JTypeStringLiteral(stringLiteral.getSourceInfo(),
          hasDot ? Kind.SRC_QN : Kind.BINARY_QN, type)));
    }
    return false;
  }

  @CheckForNull
  private JType getTypeFromString(@Nonnull String str) {
    String signatureName = getSignatureName(str);
    if (NamingTools.isTypeDescriptor(signatureName)) {
      try {
        JType type = lookup.getType(signatureName);
        return type;
      } catch (JLookupException e) {
        // The string was not a valid type, do not replace it.
      }
    }

    return null;
  }

  @Nonnull
  private String getSignatureName(@Nonnull String str) {
    StringBuilder signatureName = new StringBuilder();
    if (str.endsWith(SRC_ARRAY_REPRESENTATION)) {
      // The string is ended by []*, transform it in array signature [LtypeSignature;
      while (str.endsWith(SRC_ARRAY_REPRESENTATION)) {
        str = str.substring(0, str.length() - SRC_ARRAY_REPRESENTATION.length());
        signatureName.append('[');
      }
    }
    signatureName.append('L').append(str.replace('.', '/')).append(';');

    return signatureName.toString();
  }
}
