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

package com.android.jack.transformations.ast.string.parameterrefiners;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JPhantomLookup;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Common part of string parameter refiners.
 */
abstract class CommonStringParameterRefiner {
  @Nonnull
  protected final JClass javaLangClass;

  @Nonnull
  protected final JClass javaLangString;

  @Nonnull
  protected final JType javaLangClassArray;

  @Nonnull
  protected final JLookup lookup;

  @Nonnull
  protected static final String FIELD_CLASS_SIGNATURE = "Ljava/lang/reflect/Field;";

  @Nonnull
  protected static final String METHOD_CLASS_SIGNATURE = "Ljava/lang/reflect/Method;";

  @Nonnull
  protected static final String NEWUPDATER_METHOD_NAME = "newUpdater";

  @Nonnull
  protected static final TypeFormatter formatter = BinarySignatureFormatter.getFormatter();

  CommonStringParameterRefiner() {
    lookup = Jack.getSession().getLookup();
    JPhantomLookup phantomLookup = Jack.getSession().getPhantomLookup();
    javaLangClass = phantomLookup.getClass(CommonTypes.JAVA_LANG_CLASS);
    javaLangString = phantomLookup.getClass(CommonTypes.JAVA_LANG_STRING);
    javaLangClassArray = javaLangClass.getArray();
  }

  @CheckForNull
  protected JStringLiteral getExpressionToRefine(@Nonnull JMethodCall call,
      @Nonnegative int paramIndex) {
    assert paramIndex >= 0 &&  paramIndex < call.getArgs().size();
    JExpression arg = call.getArgs().get(paramIndex);

    if (arg instanceof JStringLiteral) {
      return (JStringLiteral) arg;
    }

    return null;
  }

  @CheckForNull
  protected JDefinedClassOrInterface getTypeFromClassLiteralExpression(
      @CheckForNull JExpression expr) {
    if (expr != null && expr instanceof JClassLiteral) {
      JType type = ((JClassLiteral) expr).getRefType();
      if (type instanceof JDefinedClassOrInterface) {
        return (JDefinedClassOrInterface) type;
      }
    }
    return null;
  }

  protected boolean isOrIsSubClassOf(
      @Nonnull JClassOrInterface rootType, @Nonnull JReferenceType searchedType) {
    if (rootType == searchedType) {
      return true;
    } else if (rootType instanceof JDefinedClass) {
      return ((JDefinedClass) rootType).isSubTypeOf(searchedType);
    }
    return false;
  }
}
