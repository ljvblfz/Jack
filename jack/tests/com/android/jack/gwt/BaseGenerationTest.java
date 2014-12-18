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

package com.android.jack.gwt;

import com.android.jack.Main;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.impl.BaseGenerationVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.util.DefaultTextOutput;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BaseGenerationTest {

  @BeforeClass
  public static void setUpClass() throws Exception {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void toStringNewArray001() {
    List<JExpression> dims = new ArrayList<JExpression>();
    dims.add(new JAbsentArrayDimension(SourceInfo.UNKNOWN));
    JNewArray newArray =
        JNewArray.createWithDims(SourceInfo.UNKNOWN,
            JPrimitiveTypeEnum.INT.getType().getArray(), dims);
    DefaultTextOutput out = new DefaultTextOutput(false);
    BaseGenerationVisitor v = new BaseGenerationVisitor(out);
    v.accept(newArray);
    Assert.assertTrue("new int[]".equals(out.toString()));
  }

  @Test
  public void toStringNewArray002() {
    List<JExpression> dims = new ArrayList<JExpression>();
    dims.add(new JAbsentArrayDimension(SourceInfo.UNKNOWN));
    dims.add(new JAbsentArrayDimension(SourceInfo.UNKNOWN));
    JNewArray newArray = JNewArray.createWithDims(
        SourceInfo.UNKNOWN, JPrimitiveTypeEnum.INT.getType().getArray().getArray(), dims);
    DefaultTextOutput out = new DefaultTextOutput(false);
    BaseGenerationVisitor v = new BaseGenerationVisitor(out);
    v.accept(newArray);
    Assert.assertTrue("new int[][]".equals(out.toString()));
  }

  @Test
  public void toStringNewArray003() {
    List<JExpression> dims = new ArrayList<JExpression>();
    dims.add(new JAbsentArrayDimension(SourceInfo.UNKNOWN));
    dims.add(new JIntLiteral(SourceInfo.UNKNOWN, 4));
    JNewArray newArray = JNewArray.createWithDims(
        SourceInfo.UNKNOWN, JPrimitiveTypeEnum.INT.getType().getArray().getArray(), dims);
    DefaultTextOutput out = new DefaultTextOutput(false);
    BaseGenerationVisitor v = new BaseGenerationVisitor(out);
    v.accept(newArray);
    Assert.assertTrue("new int[][4]".equals(out.toString()));
  }

  @Test
  public void toStringNewArray004() {
    List<JExpression> init = new ArrayList<JExpression>();
    init.add(new JIntLiteral(SourceInfo.UNKNOWN, 4));
    init.add(new JIntLiteral(SourceInfo.UNKNOWN, 5));
    JNewArray newArray =
        JNewArray.createWithInits(SourceInfo.UNKNOWN, JPrimitiveTypeEnum.INT.getType().getArray(),
            init);
    DefaultTextOutput out = new DefaultTextOutput(false);
    BaseGenerationVisitor v = new BaseGenerationVisitor(out);
    v.accept(newArray);
    Assert.assertTrue("new int[]{4, 5}".equals(out.toString()));
  }

  @Test
  public void toStringNewArray005() {
    List<JExpression> init = new ArrayList<JExpression>();
    List<JExpression> init2 = new ArrayList<JExpression>();
    init2.add(new JIntLiteral(SourceInfo.UNKNOWN, 4));
    init2.add(new JIntLiteral(SourceInfo.UNKNOWN, 5));
    JArrayType intArray1 = JPrimitiveTypeEnum.INT.getType().getArray();
    init.add(JNewArray.createWithInits(SourceInfo.UNKNOWN, intArray1, init2));
    JNewArray newArray = JNewArray.createWithInits(
        SourceInfo.UNKNOWN, intArray1.getArray(), init);
    DefaultTextOutput out = new DefaultTextOutput(false);
    BaseGenerationVisitor v = new BaseGenerationVisitor(out);
    v.accept(newArray);
    Assert.assertTrue("new int[][]{new int[]{4, 5}}".equals(out.toString()));
  }
}
