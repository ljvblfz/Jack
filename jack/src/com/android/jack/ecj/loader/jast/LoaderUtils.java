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

package com.android.jack.ecj.loader.jast;

import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.marker.OriginalTypeInfo;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.formatter.TypeFormatter;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * Utility methods and constants for presenting J-AST in an ecj
 * {@link org.eclipse.jdt.internal.compiler.batch.ClasspathLocation}.
 */
class LoaderUtils {

  @Nonnull
  private static final TypeAndMethodFormatter signatureFormatter =
      BinarySignatureFormatter.getFormatter();
  @Nonnull
  private static final TypeFormatter qualifiedNameFormatter =
      BinaryQualifiedNameFormatter.getFormatter();

  private static final int MODIFIER_MASK = ~JModifier.STATIC_INIT;

  static int convertJAstModifiersToEcj(int accessFlags,
      @CheckForNull Annotable annotable) {
    int filtered = accessFlags & MODIFIER_MASK;

    if ((annotable != null
        && (AnnotationUtils.getAnnotation(annotable,
            AnnotationUtils.DEPRECATED_ANNOTATION) != null))
        || JModifier.isDeprecated(filtered)) {
      filtered |= ClassFileConstants.AccDeprecated;
    }

    return filtered;
  }

  static boolean isDeprecated(@Nonnull IBinaryType enclosing) {
    return ((enclosing.getTagBits() & TagBits.AnnotationDeprecated) != 0) || ((
        enclosing.getModifiers() & ClassFileConstants.AccDeprecated) != 0);
  }

  @Nonnull
  static Constant convertJLiteralToEcj(@CheckForNull JLiteral literal) {
    Constant constant = Constant.NotAConstant;
    if (literal != null) {
      if ((literal instanceof JAnnotationLiteral)
          || (literal instanceof JArrayLiteral)
          || (literal instanceof JEnumLiteral)
          || (literal instanceof JMethodLiteral)
          || (literal instanceof JClassLiteral)) {
        // not an ecj constant
      } else if (literal instanceof JBooleanLiteral) {
        constant = BooleanConstant.fromValue(((JBooleanLiteral) literal).getValue());
      } else if (literal instanceof JByteLiteral) {
        constant = ByteConstant.fromValue(((JByteLiteral) literal).getValue());
      } else if (literal instanceof JCharLiteral) {
        constant = CharConstant.fromValue(((JCharLiteral) literal).getValue());
      } else if (literal instanceof JShortLiteral) {
        constant = ShortConstant.fromValue(((JShortLiteral) literal).getValue());
      } else if (literal instanceof JIntLiteral) {
        constant = IntConstant.fromValue(((JIntLiteral) literal).getValue());
      } else if (literal instanceof JFloatLiteral) {
        constant = FloatConstant.fromValue(((JFloatLiteral) literal).getValue());
      } else if (literal instanceof JLongLiteral) {
        constant = LongConstant.fromValue(((JLongLiteral) literal).getValue());
      } else if (literal instanceof JDoubleLiteral) {
        constant = DoubleConstant.fromValue(((JDoubleLiteral) literal).getValue());
      } else if (literal instanceof JAbstractStringLiteral) {
        constant = StringConstant.fromValue(((JAbstractStringLiteral) literal).getValue());
      } else if (literal instanceof JNullLiteral) {
        throw new AssertionError();
      } else {
        throw new AssertionError();
      }
    }
    return constant;
  }

  @CheckForNull
  static <T extends JNode & Annotable> char[] getGenericSignature(@Nonnull T annotableNode) {
    OriginalTypeInfo typeInfo = annotableNode.getMarker(OriginalTypeInfo.class);
    if (typeInfo != null) {
      String genericSignature = typeInfo.getGenericSignature();
      if (genericSignature != null) {
        return genericSignature.toCharArray();
      }
    }

    JAnnotationLiteral signatureAnnotation =
        AnnotationUtils.getAnnotation(annotableNode, AnnotationUtils.SIGNATURE_ANNOTATION);
    if (signatureAnnotation != null) {
      JNameValuePair pair =
          signatureAnnotation.getNameValuePair(AnnotationUtils.DEFAULT_ANNOTATION_FIELD);
      assert pair != null;
      String genericSignature = LoaderUtils.concatenate((JArrayLiteral) pair.getValue());
      return genericSignature.toCharArray();
    } else {
      return null;
    }

  }

  @Nonnull
  private static String concatenate(@Nonnull JArrayLiteral valueArray) {
    StringBuilder builder = new StringBuilder();
    for (JLiteral value : valueArray.getValues()) {
      builder.append(((JAbstractStringLiteral) value).getValue());
    }
    return builder.toString();
  }

  @Nonnull
  static TypeAndMethodFormatter getSignatureFormatter() {
    return signatureFormatter;
  }

  @Nonnull
  static TypeFormatter getQualifiedNameFormatter() {
    return qualifiedNameFormatter;
  }

}
