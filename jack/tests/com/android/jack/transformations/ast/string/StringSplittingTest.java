/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.jack.signature.GenericSignatureParser;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringSplittingTest {

  @BeforeClass
  public static void setUpClass() throws Exception {
    StringSplittingTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testTypeStringSplitting() {
    DummyAction parserActions = new DummyAction();
    GenericSignatureParser parser = new GenericSignatureParser(parserActions);
    String signature = "LOuter.Inner;";
    parser.parseClassSignature(signature);
    Assert.assertEquals(signature, parserActions.getNewSignature());
  }
}
