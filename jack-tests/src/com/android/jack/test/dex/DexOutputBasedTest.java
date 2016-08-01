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

package com.android.jack.test.dex;

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.jf.dexlib.DexFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/** Base implementation for all tests based on checking the resulting Dex output */
public abstract class DexOutputBasedTest {

  /** Get test resource file */
  @Nonnull
  public File resource(@Nonnull String testPackage, @Nonnull String file) {
    return new File(
        AbstractTestTools.getTestRootDir(testPackage), file);
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  /** Properties to be used in the compilation */
  public static final class CompilationProperties {
    @Nonnull
    public static final CompilationProperties EMPTY =
        new CompilationProperties(Collections.<String, Object>emptyMap(), true);

    private boolean allowJillToolchains;

    @Nonnull
    private final Map<String, Object> properties;

    private CompilationProperties(
        @Nonnull Map<String, Object> properties, boolean allowJillToolchains) {
      this.properties = properties;
      this.allowJillToolchains = allowJillToolchains;
    }

    @Nonnull
    public CompilationProperties with(@Nonnull String property, @Nonnull Object value) {
      HashMap<String, Object> map = new HashMap<String, Object>(this.properties);
      map.put(property, value);
      return new CompilationProperties(map, allowJillToolchains);
    }

    @Nonnull
    public CompilationProperties excludeJillToolchain() {
      return new CompilationProperties(properties, false);
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public boolean usingLegacyCompiler() {
    return AbstractTestTools
        .getCandidateToolchain(JackBasedToolchain.class) instanceof JillBasedToolchain;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public final void compileAndValidate(
      @Nonnull String testPackage,
      @Nonnull CompilationProperties properties,
      @Nonnull DexValidator<DexFile> validator) throws Exception {

    File testFolder = new File(AbstractTestTools.getTestRootDir(testPackage), "jack");
    File unitTestFolder = new File(AbstractTestTools.getTestRootDir(testPackage), "dx");
    File outFolder = AbstractTestTools.createTempDir();
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);

    List<Class<? extends IToolchain>> exclude = new ArrayList<>();
    // Because source path is not supported by the toolchain
    if (!properties.allowJillToolchains) {
      exclude.add(JillBasedToolchain.class);
    }

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    for (Map.Entry<String, Object> e : properties.properties.entrySet()) {
      toolchain.addProperty(e.getKey(), e.getValue().toString());
    }

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .srcToExe(outFolder, /* zipFile = */false, testFolder, unitTestFolder);

    validator.validate(new DexFile(out));

    // Run runtime tests
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList(testPackage + ".Tests"),
        RuntimeTestHelper.getJunitDex(), out);
  }
}
