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

import com.android.jack.Options;
import com.android.jack.Options.VerbosityLevel;
import com.android.jack.util.ExecuteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class implements a {@link JackBasedToolchain} by calling Jack via command line.
 */
public class JackCliToolchain extends JackBasedToolchain {

  @Nonnull
  protected File jackPrebuilt;

  @Nonnull
  protected List<String> extraJackArgs = new ArrayList<String>(0);
  @CheckForNull
  protected File incrementalFolder;
  @Nonnull
  protected Options.VerbosityLevel verbosityLevel = VerbosityLevel.WARNING;

  JackCliToolchain(@Nonnull File prebuilt) {
    this.jackPrebuilt = prebuilt;
  }

  @Override
  @Nonnull
  protected JackCliToolchain setVerbosityLevel(@Nonnull Options.VerbosityLevel level) {
    verbosityLevel = level;
    return this;
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {

    List<String> args = new ArrayList<String>();

    srcToCommon(args, sources);

    if (zipFile) {
      args.add("--output-dex-zip");
    } else {
      args.add("--output-dex");
    }
    args.add(out.getAbsolutePath());

    args.addAll(extraJackArgs);

    if (withDebugInfos) {
      args.add("-g");
    }

    if (annotationProcessorClass != null) {
      args.add("-processor");
      args.add(annotationProcessorClass.getName());
    }
    if (annotationProcessorOutDir != null) {
      args.add("-d");
      args.add(annotationProcessorOutDir.getAbsolutePath());
    }
    for (String ecjArg : extraEcjArgs) {
      args.add(ecjArg);
    }

    AbstractTestTools.addFile(args, /* mustExist = */ false, sources);

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(isVerbose);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources)
      throws Exception {

    List<String> args = new ArrayList<String>();

    srcToCommon(args, sources);

    if (zipFiles) {
      args.add("--output-jack");
    } else {
      args.add("--output-jack-dir");
    }
    args.add(out.getAbsolutePath());

    for (String ecjArg : extraEcjArgs) {
      args.add(ecjArg);
    }

    AbstractTestTools.addFile(args, /* mustExist = */ false, sources);

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(isVerbose);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

  }

  private void srcToCommon(@Nonnull List<String> args, @Nonnull File... sources) {
    args.add("java");
    args.add("-cp");
    args.add(jackPrebuilt.getAbsolutePath());

    args.add(com.android.jack.Main.class.getName());

    args.add("--verbose");
    args.add(verbosityLevel.name());

    args.add("--sanity-checks");
    args.add(Boolean.toString(sanityChecks));

    if (incrementalFolder != null) {
      args.add("--incremental-folder");
      args.add(incrementalFolder.getAbsolutePath());
    }

    addProperties(properties, args);

    if (classpath.size() > 0) {
      args.add("--classpath");
      args.add(getClasspathAsString());
    }

    for (File res : resImport) {
      args.add("--import-resource");
      args.add(res.getPath());
    }

    args.addAll(extraJackArgs);

    if (jarjarRules != null) {
      args.add("--config-jarjar");
      args.add(jarjarRules.getAbsolutePath());
    }

    for (File flags : proguardFlags) {
      args.add("--config-proguard");
      args.add(flags.getAbsolutePath());
    }

    if (withDebugInfos) {
      args.add("-g");
    }

    if (annotationProcessorClass != null) {
      args.add("-processor");
      args.add(annotationProcessorClass.getName());
    }
    if (annotationProcessorOutDir != null) {
      args.add("-d");
      args.add(annotationProcessorOutDir.getAbsolutePath());
    }

    for (File staticLib : staticLibs) {
      args.add("--import");
      args.add(staticLib.getAbsolutePath());
    }
  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {
    List<String> args = new ArrayList<String>();

    libToCommon(args, getClasspathAsString(), in);

    if (zipFile) {
      args.add("--output-dex-zip");
    } else {
      args.add("--output-dex");
    }

    args.add(out.getAbsolutePath());

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(isVerbose);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    List<String> args = new ArrayList<String>();

    libToCommon(args, getClasspathAsString(), in);

    if (zipFiles) {
      args.add("--output-jack");
    } else {
      args.add("--output-jack-dir");
    }
    args.add(out.getAbsolutePath());

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(isVerbose);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

  }

  protected void libToCommon(@Nonnull List<String> args, @Nonnull String classpath,
      @Nonnull File[] in) throws Exception {
    args.add("java");
    args.add("-cp");
    args.add(jackPrebuilt.getAbsolutePath());

    args.add(com.android.jack.Main.class.getName());

    args.add("--verbose");
    args.add(verbosityLevel.name());

    args.add("--sanity-checks");
    args.add(Boolean.toString(sanityChecks));

    if (incrementalFolder != null) {
      args.add("--incremental-folder");
      args.add(incrementalFolder.getAbsolutePath());
    }

    for (File res : resImport) {
      args.add("--import-resource");
      args.add(res.getPath());
    }

    addProperties(properties, args);

    if (!classpath.equals("")) {
      args.add("--classpath");
      args.add(classpath);
    }

    if (jarjarRules != null) {
      args.add("--config-jarjar");
      args.add(jarjarRules.getAbsolutePath());
    }

    for (File flags : proguardFlags) {
      args.add("--config-proguard");
      args.add(flags.getAbsolutePath());
    }

    libToImportStaticLibs(args, in);

  }

  protected void libToImportStaticLibs(@Nonnull List<String> args, @Nonnull File[] in)
      throws Exception {
    for (File staticlib : in) {
      args.add("--import");
      args.add(staticlib.getAbsolutePath());
    }

    for (File staticLib : staticLibs) {
      args.add("--import");
      args.add(staticLib.getAbsolutePath());
    }
  }

  @Nonnull
  public JackCliToolchain addJackArg(@Nonnull String arg) {
    extraJackArgs.add(arg);
    return this;
  }

  @Override
  @Nonnull
  public JackCliToolchain setIncrementalFolder(@Nonnull File incrementalFolder) {
    this.incrementalFolder = incrementalFolder;
    return this;
  }

  protected static void addProperties(@Nonnull Map<String, String> properties,
      @Nonnull List<String> args) {
    for (Entry<String, String> entry : properties.entrySet()) {
      args.add("-D");
      args.add(entry.getKey() + "=" + entry.getValue());
    }
  }

}
