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

package com.android.jack.dextag;

import com.android.jack.backend.dex.DexWriter;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DexTagTests {

  @Test
  public void testDexHasTag() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.dextag.test001.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        out,
        /* zipFile = */ false,
        testFolder);
    File classesDex = new File(out, "classes.dex");
    Assert.assertTrue(hasTag(classesDex));
  }

  private boolean hasTag(File dexFile) throws IOException {
    DexBuffer dex = new DexBuffer(dexFile);
    return dex.strings().contains(DexWriter.getJackDexTag());
  }
}
