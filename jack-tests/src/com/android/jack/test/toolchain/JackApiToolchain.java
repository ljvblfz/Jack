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

import javax.annotation.CheckForNull;
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
  @Nonnull
  public void srcToExe(@CheckForNull String classpath, @Nonnull File out, boolean zipFile,
      @Nonnull File... sources) throws Exception {

    try {
      addProperties(properties, jackOptions);

      if (jackOptions.getFlags() != null) {
        jackOptions.applyShrobFlags();
      }

      fillEcjArgs(sources);

      for (File res : resImport) {
        jackOptions.addResource(res);
      }

      jackOptions.setClasspath(classpath);

      if (zipFile) {
        jackOptions.setOutputZip(out);
      } else {
        jackOptions.setOutputDir(out);
      }

      jackOptions.setJayceImports(staticLibs);

      jackOptions.setJarjarRulesFile(jarjarRules);

      if (proguardFlags.size() > 0) {
        jackOptions.setProguardFlagsFile(proguardFlags);
      }

      jackOptions.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(),
          Boolean.toString(withDebugInfos));

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Jack.run(jackOptions);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  @Nonnull
  public void srcToLib(@CheckForNull String classpath, @Nonnull File out, boolean zipFiles,
      @Nonnull File... sources) throws Exception {

    try {
      addProperties(properties, jackOptions);

      if (jackOptions.getFlags() != null) {
        jackOptions.applyShrobFlags();
      }

      jackOptions.setClasspath(classpath);

      if (zipFiles) {
        jackOptions.setJayceOutputZip(out);
      } else {
        jackOptions.setJayceOutputDir(out);
      }

      fillEcjArgs(sources);

      for (File res : resImport) {
        jackOptions.addResource(res);
      }

      jackOptions.setJayceImports(staticLibs);

      jackOptions.setJarjarRulesFile(jarjarRules);

      if (proguardFlags.size() > 0) {
        jackOptions.setProguardFlagsFile(proguardFlags);
      }

      jackOptions.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(),
          Boolean.toString(withDebugInfos));

      jackOptions.addProperty(CodeItemBuilder.DEX_OPTIMIZE.getName(),
          Boolean.toString(!withDebugInfos));

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Jack.run(jackOptions);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  @Nonnull
  public void libToExe(@Nonnull File in, @Nonnull File out, boolean zipFile) throws Exception {

    try {
      addProperties(properties, jackOptions);

      if (jackOptions.getFlags() != null) {
        jackOptions.applyShrobFlags();
      }

      jackOptions.setJarjarRulesFile(jarjarRules);

      if (proguardFlags.size() > 0) {
        jackOptions.setProguardFlagsFile(proguardFlags);
      }

      List<File> libsToImport = new ArrayList<File>();
      libsToImport.add(in);
      libsToImport.addAll(staticLibs);
      jackOptions.setJayceImports(libsToImport);

      for (File res : resImport) {
        jackOptions.addResource(res);
      }

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
  @Nonnull
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {

    try {
      addProperties(properties, jackOptions);

      jackOptions.setJarjarRulesFile(jarjarRules);

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
      jackOptions.setJayceImports(libsToImport);

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

    AbstractTestTools.addFile(ecjArgs, /* mustExist = */false, sources);

    if (ecjArgs.size() > 0) {
      jackOptions.setEcjArguments(ecjArgs);
    }
  }

}
