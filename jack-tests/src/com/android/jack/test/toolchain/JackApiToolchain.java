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

package com.android.jack.test.toolchain;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.shrob.spec.Flags;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

/**
 * This class implements a {@link JackBasedToolchain} by calling Jack via API.
 */
public class JackApiToolchain extends JackBasedToolchain {

  @Nonnull
  private Options jackOptions = new Options();

  JackApiToolchain() {}

  @Override
  @Nonnull
  protected JackApiToolchain setVerbosityLevel(@Nonnull Options.VerbosityLevel level) {
    jackOptions.setVerbosityLevel(level);
    return this;
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile,
      @Nonnull File... sources) throws Exception {

    try {

      srcToCommon(sources);

      if (zipFile) {
        jackOptions.setOutputZip(out);
      } else {
        jackOptions.setOutputDir(out);
      }

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Jack.run(jackOptions);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles,
      @Nonnull File... sources) throws Exception {

    try {

      srcToCommon(sources);

      if (zipFiles) {
        jackOptions.setJayceOutputZip(out);
      } else {
        jackOptions.setJayceOutputDir(out);
      }

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Jack.run(jackOptions);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  private void srcToCommon(@Nonnull File... sources) {
    addProperties(properties, jackOptions);

    jackOptions.setSanityChecks(sanityChecks);

    if (jackOptions.getFlags() != null) {
      jackOptions.applyShrobFlags();
    }

    if (classpath.size() > 0) {
      jackOptions.setClasspath(getClasspathAsString());
    }

    fillEcjArgs(sources);

    for (File res : resImport) {
      jackOptions.addResource(res);
    }

    jackOptions.setImportedLibraries(staticLibs);

    jackOptions.setJarjarRulesFile(jarjarRules);

    if (proguardFlags.size() > 0) {
      jackOptions.setProguardFlagsFile(proguardFlags);
    }

    jackOptions.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(),
        Boolean.toString(withDebugInfos));

    jackOptions.addProperty(CodeItemBuilder.DEX_OPTIMIZE.getName(),
        Boolean.toString(!withDebugInfos));

  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {

    try {

      libToCommon(in);

      if (zipFile) {
        jackOptions.setOutputZip(out);
      } else {
        jackOptions.setOutputDir(out);
      }

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Jack.run(jackOptions);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {

    try {

      libToCommon(in);

      if (zipFiles) {
        jackOptions.setJayceOutputZip(out);
      } else {
        jackOptions.setJayceOutputDir(out);
      }

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Jack.run(jackOptions);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  private void libToCommon(@Nonnull File[] in) {
    addProperties(properties, jackOptions);

    jackOptions.setSanityChecks(sanityChecks);

    jackOptions.setJarjarRulesFile(jarjarRules);

    if (classpath.size() > 0) {
      jackOptions.setClasspath(getClasspathAsString());
    }

    if (jackOptions.getFlags() != null) {
      jackOptions.applyShrobFlags();
    }

    if (proguardFlags.size() > 0) {
      jackOptions.setProguardFlagsFile(proguardFlags);
    }

    for (File res : resImport) {
      jackOptions.addResource(res);
    }

    List<File> libsToImport = new ArrayList<File>();
    for (File staticLib : in) {
      libsToImport.add(staticLib);
    }
    libsToImport.addAll(staticLibs);
    jackOptions.setImportedLibraries(libsToImport);

  }

  @Nonnull
  public JackApiToolchain setShrobFlags(@Nonnull Flags shrobFlags)  {
    jackOptions.setFlags(shrobFlags);
    return this;
  }

  @Override
  @Nonnull
  public JackApiToolchain setIncrementalFolder(@Nonnull File incrementalFolder) {
    jackOptions.setIncrementalFolder(incrementalFolder);
    return this;
  }

  private static final void addProperties(@Nonnull Map<String, String> properties,
      @Nonnull Options jackOptions) {
    for (Entry<String, String> entry : properties.entrySet()) {
      jackOptions.addProperty(entry.getKey(), entry.getValue());
    }
  }

  private final void fillEcjArgs(@Nonnull File... sources) {
    List<String> ecjArgs = new ArrayList<String>();

    if (annotationProcessorClass != null) {
      ecjArgs.add("-processor");
      ecjArgs.add(annotationProcessorClass.getName());
    }

    if (annotationProcessorOutDir != null) {
      ecjArgs.add("-d");
      ecjArgs.add(annotationProcessorOutDir.getAbsolutePath());
    }

    for (String ecjArg : extraEcjArgs) {
      ecjArgs.add(ecjArg);
    }

    AbstractTestTools.addFile(ecjArgs, /* mustExist = */ false, sources);

    if (sources.length > 0) {
      jackOptions.setEcjArguments(ecjArgs);
    }
  }

}
