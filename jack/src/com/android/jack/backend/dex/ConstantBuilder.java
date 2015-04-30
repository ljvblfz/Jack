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

package com.android.jack.backend.dex;

import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.rop.annotation.Annotation;
import com.android.jack.dx.rop.annotation.AnnotationVisibility;
import com.android.jack.dx.rop.annotation.NameValuePair;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstAnnotation;
import com.android.jack.dx.rop.cst.CstArray;
import com.android.jack.dx.rop.cst.CstBoolean;
import com.android.jack.dx.rop.cst.CstByte;
import com.android.jack.dx.rop.cst.CstChar;
import com.android.jack.dx.rop.cst.CstDouble;
import com.android.jack.dx.rop.cst.CstEnumRef;
import com.android.jack.dx.rop.cst.CstFloat;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.cst.CstKnownNull;
import com.android.jack.dx.rop.cst.CstLong;
import com.android.jack.dx.rop.cst.CstNat;
import com.android.jack.dx.rop.cst.CstShort;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAnnotation;
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
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JVisitor;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Build {@link Constant} from {@link JLiteral}.
 *
 * {@code ConstantBuilder} is not thread safe.
 */
public class ConstantBuilder {
  private class Visitor extends JVisitor {

    @CheckForNull
    private Constant result;


    @Override
    public boolean visit(@Nonnull JAnnotation annotation) {
      Annotation ropAnnotation = new Annotation(RopHelper.getCstType(annotation.getType()),
          AnnotationVisibility.EMBEDDED);
      createAnnotationPairs(annotation, ropAnnotation);
      ropAnnotation.setImmutable();
      CstAnnotation cstAnnotation = new CstAnnotation(ropAnnotation);
      result = cstAnnotation;
      return false;
    }

    @Override
    public boolean visit(@Nonnull JArrayLiteral array) {
      List<JLiteral> literals = array.getValues();
      CstArray.List constants = new CstArray.List(literals.size());
      int i = 0;
      for (JLiteral literal : literals) {
        result = null;
        accept(literal);
        assert result != null;
        constants.set(i, result);
        i++;
      }
      constants.setImmutable();
      result = new CstArray(constants);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JEnumLiteral literal) {
      CstNat nat = RopHelper.createSignature(literal.getFieldId());
      result = new CstEnumRef(nat);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JClassLiteral literal) {
      result = RopHelper.getCstType(literal.getRefType());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMethodLiteral x) {
      result = RopHelper.createMethodRef(x.getMethod());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JCharLiteral literal) {
      result = CstChar.make(literal.getValue());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBooleanLiteral literal) {
      result = CstBoolean.make(literal.getValue());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JByteLiteral literal) {
      result = CstByte.make(literal.getValue());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JShortLiteral literal) {
      result = CstShort.make(literal.getValue());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JIntLiteral literal) {
      result = CstInteger.make(literal.getValue());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLongLiteral literal) {
      result = CstLong.make(literal.getValue());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JFloatLiteral literal) {
      result = CstFloat.make(Float.floatToIntBits(literal.getValue()));
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDoubleLiteral literal) {
      result = CstDouble.make(Double.doubleToLongBits(literal.getValue()));
      return false;
    }

    @Override
    public boolean visit(@Nonnull JAbstractStringLiteral literal) {
      result = RopHelper.createString(literal);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNullLiteral literal) {
      result = CstKnownNull.THE_ONE;
      return false;
    }
 }
  @Nonnull
  private final Visitor constantBuilder = new Visitor();

  @Nonnull
  public Constant parseLiteral(@Nonnull JLiteral literal) {
    constantBuilder.result = null;
    constantBuilder.accept(literal);
    Constant constant = constantBuilder.result;
    assert constant != null;
    return constant;
  }

  public void createAnnotationPairs(@Nonnull JAnnotation annotation,
      @Nonnull Annotation ropAnnotation) {
    for (JNameValuePair jPair : annotation.getNameValuePairs()) {
      ropAnnotation.add(new NameValuePair(RopHelper.createString(jPair.getName()),
          parseLiteral(jPair.getValue())));
    }
  }

}
