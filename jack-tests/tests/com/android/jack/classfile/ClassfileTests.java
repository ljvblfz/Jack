/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.classfile;

import com.android.jack.Options;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ClassfileTests {

  @Test
  public void testClassFileIsCompiled() throws Exception {
    // class file generation support is only for sources not imports.
    List<Class<? extends IToolchain>> excludeList =
        Collections.<Class<? extends IToolchain>>singletonList(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class,
            excludeList);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File jackOut = AbstractTestTools.createTempFile("jackOut", toolchain.getLibraryExtension());
    File classOut = AbstractTestTools.createTempDir();
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.classfile.test001");

    toolchain.addProperty(Options.EMIT_CLASS_FILES.getName(), "true");
    toolchain.addProperty(Options.EMIT_CLASS_FILES_FOLDER.getName(), classOut.getPath());

    toolchain.addToClasspath(defaultClasspath)
    .srcToLib(jackOut, /* zipFiles = */ true, sourceDir);

    Assert.assertTrue(
        new File(classOut, "com/android/jack/classfile/test001/jack/ClassTest.class").isFile());

  }
}
