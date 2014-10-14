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
import com.android.jack.experimental.incremental.JackIncremental;
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
  public JackApiToolchain disableDxOptimizations() {
    jackOptions.disableDxOptimizations();
    return this;
  }

  @Override
  @Nonnull
  public JackApiToolchain enableDxOptimizations() {
    jackOptions.enableDxOptimizations();
    return this;
  }

  @Override
  @Nonnull
  public void srcToExe(@Nonnull String classpath, @Nonnull File out, @Nonnull File... sources)
      throws Exception {

    try {
      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      addProperties(properties, jackOptions);

      if (jackOptions.getFlags() != null) {
        jackOptions.applyShrobFlags();
      }

      jackOptions.setEcjArguments(AbstractTestTools.buildEcjArgs());

      if (annotationProcessorClass != null) {
        jackOptions.getEcjArguments().add("-processor");
        jackOptions.getEcjArguments().add(annotationProcessorClass.getName());
      }

      if (annotationProcessorOutDir != null) {
        jackOptions.getEcjArguments().add("-d");
        jackOptions.getEcjArguments().add(annotationProcessorOutDir.getAbsolutePath());
      }

      for (String ecjArg : extraEcjArgs) {
        jackOptions.getEcjArguments().add(ecjArg);
      }

      AbstractTestTools.addFile(jackOptions.getEcjArguments(),
      /* mustExist = */false, sources);
      jackOptions.setClasspath(classpath);

      // !zip
      jackOptions.setOutputDir(out);

      jackOptions.setJayceImports(staticLibs);

      jackOptions.setJarjarRulesFile(jarjarRules);
      List<File> proguardFlagsFiles = new ArrayList<File>();

      for (File flagFile : proguardFlagsFiles) {
        proguardFlagsFiles.add(flagFile);
      }

      if (proguardFlagsFiles.size() > 0) {
        jackOptions.setProguardFlagsFile(proguardFlagsFiles);
      }

      jackOptions.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(),
          Boolean.toString(withDebugInfos));

      if (jackOptions.getIncrementalFolder() != null) {
        JackIncremental.run(jackOptions);
      } else {
        Jack.run(jackOptions);
      }

    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  @Nonnull
  public void srcToLib(@Nonnull String classpath, @Nonnull File out, boolean zipFiles,
      @Nonnull File... sources) throws Exception {

    try {
      Options options = jackOptions;

      addProperties(properties, options);

      options.setClasspath(classpath);

      if (zipFiles) {
        options.setJayceOutputZip(out);
      } else {
        options.setJayceOutputDir(out);
      }

      options.setEcjArguments(AbstractTestTools.buildEcjArgs());

      if (annotationProcessorClass != null) {
        options.getEcjArguments().add("-processor");
        options.getEcjArguments().add(annotationProcessorClass.getName());
      }

      if (annotationProcessorOutDir != null) {
        options.getEcjArguments().add("-d");
        options.getEcjArguments().add(annotationProcessorOutDir.getAbsolutePath());
      }

      for (String ecjArg : extraEcjArgs) {
        options.getEcjArguments().add(ecjArg);
      }

      AbstractTestTools.addFile(options.getEcjArguments(),
      /* mustExist = */false, sources);

      options.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(),
          Boolean.toString(withDebugInfos));

      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      if (options.getIncrementalFolder() != null) {
        JackIncremental.run(options);
      } else {
        Jack.run(options);
      }

    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  @Nonnull
  public void libToDex(@Nonnull File in, @Nonnull File out) throws Exception {
    System.setOut(outRedirectStream);
    System.setErr(errRedirectStream);

    try {
      Options options = jackOptions;
      addProperties(properties, options);

      options.getJayceImport().add(in);
      options.getJayceImport().addAll(staticLibs);

      // !zip
      options.setOutputDir(out);

      if (options.getIncrementalFolder() != null) {
        JackIncremental.run(options);
      } else {
        Jack.run(options);
      }

    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
    }
  }

  @Override
  @Nonnull
  public void libToLib(@Nonnull File in, @Nonnull File out) throws Exception {
    throw new AssertionError("Not Yet Implemented");
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
}
