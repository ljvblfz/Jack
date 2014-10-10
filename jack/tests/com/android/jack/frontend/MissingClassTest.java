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

package com.android.jack.frontend;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.category.KnownBugs;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class MissingClassTest {

  @Test
  @Category(KnownBugs.class)
  public void test001() throws Exception {
    File outJackTmpMissing = TestTools.createTempDir("MissingClassTest001-missing", ".jayce");
    File outJackTmpSuper = TestTools.createTempDir("MissingClassTest001-super", ".jayce");
    File outJackTmpTest = TestTools.createTempDir("MissingClassTest001-test", ".jayce");

    TestTools.compileSourceToJack(new Options(),
        new File(TestTools.getJackTestsWithJackFolder("frontend/test001"), "missing"),
        TestTools.getDefaultBootclasspathString(), outJackTmpMissing, false /* zip */);

    TestTools.compileSourceToJack(new Options(),
        new File(TestTools.getJackTestsWithJackFolder("frontend/test001"), "sub2"),
            TestTools.getDefaultBootclasspathString() + File.pathSeparator
            + outJackTmpMissing.getPath(), outJackTmpSuper, false /* zip */);

    TestTools.compileSourceToJack(new Options(),
        new File(TestTools.getJackTestsWithJackFolder("frontend/test001"), "test"),
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + outJackTmpSuper.getPath(),
        outJackTmpTest, false /* zip */);

  }

}
