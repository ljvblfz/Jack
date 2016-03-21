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

package com.android.jack.transformations.ast;

import com.android.jack.frontend.ParentSetter;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.ast.TypeLegalizer.TypeLegalizerVisitor;
import com.android.jack.transformations.request.TransformationRequest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TypeLegalizerTest {


  @Test
  public void testNewArrayWithJLongLiteralAsDimension() throws Exception {
    SourceInfo info = SourceInfo.UNKNOWN;
    JBlock bodyBlock = new JBlock(info);
    JMethodBody methodBody = new JMethodBody(info, bodyBlock);
    JArrayType type = JPrimitiveTypeEnum.INT.getType().getArray();
    List<JExpression> dims = new ArrayList<JExpression>(1);
    JLongLiteral longLiteral = new JLongLiteral(info, 58L);
    dims.add(longLiteral);
    JNewArray newArray = JNewArray.createWithDims(info, type, dims);
    JExpressionStatement newArrayStatement = newArray.makeStatement();
    bodyBlock.addStmt(newArrayStatement);

    ParentSetter parentSetter = new ParentSetter();
    parentSetter.accept(methodBody);

    TransformationRequest tr = new TransformationRequest(methodBody);
    TypeLegalizer.TypeLegalizerVisitor visitor = new TypeLegalizerVisitor(tr);
    visitor.accept(methodBody);
    tr.commit();

    NumericConversionChecker.Visitor checker = new NumericConversionChecker.Visitor();
    checker.accept(methodBody);
  }

}
