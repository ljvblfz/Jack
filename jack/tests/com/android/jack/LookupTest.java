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

package com.android.jack;

import com.android.jack.category.KnownBugs;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class LookupTest {

  @Category(KnownBugs.class)
  @Test
  public void test001() throws Exception {
    File lib = TestTools.createTempDir("Lookup001Lib", "");
    TestTools.compileSourceToJack(
        new Options(),
        TestTools.getJackTestLibFolder("lookup/test001"),
        TestTools.getDefaultBootclasspathString(),
        lib,
        false);

    File libOverride = TestTools.createTempDir("Lookup001LibOverride", "");
    TestTools.compileSourceToJack(
        new Options(),
        new File(TestTools.getJackTestFolder("lookup/test001"), "liboverride"),
        TestTools.getDefaultBootclasspathString(),
        libOverride,
        false);

    File jacks = TestTools.createTempDir("Lookup001Jacks", "");
    TestTools.compileSourceToJack(
        new Options(),
        TestTools.getJackTestsWithJackFolder("lookup/test001"),
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + lib.getAbsolutePath(),
        jacks,
        false);

    Options options = new Options();
    options.addJayceImport(jacks);
    options.addJayceImport(libOverride);
    options.outZip = TestTools.createTempFile("Lookup001", ".zip");
    Jack.run(options);
  }

}
