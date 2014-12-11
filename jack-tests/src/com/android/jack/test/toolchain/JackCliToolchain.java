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
  private List<String> extraJackArgs = new ArrayList<String>(0);
  @CheckForNull
  private File incrementalFolder;

  JackCliToolchain(@Nonnull File prebuilt) {
    this.jackPrebuilt = prebuilt;
  }

  @Override
  @Nonnull
  public void srcToExe(@CheckForNull String classpath, @Nonnull File out,
      boolean zipFile, @Nonnull File... sources) throws Exception {

    List<String> args = new ArrayList<String>();
    args.add("java");
    args.add("-cp");
    args.add(jackPrebuilt.getAbsolutePath());

    if (incrementalFolder != null) {
      args.add("--incremental-folder");
      args.add(incrementalFolder.getAbsolutePath());
    }

    args.add(com.android.jack.Main.class.getName());

    addProperties(properties, args);

    if (classpath != null) {
      args.add("--classpath");
      args.add(classpath);
    }

    if (zipFile) {
      args.add("--output-dex-zip");
    } else {
      args.add("--output-dex");
    }
    args.add(out.getAbsolutePath());

    if (jarjarRules != null) {
      args.add("--config-jarjar");
      args.add(jarjarRules.getAbsolutePath());
    }

    for (File flags : proguardFlags) {
      args.add("--config-proguard");
      args.add(flags.getAbsolutePath());
    }

    for (File staticLib : staticLibs) {
      args.add("--import");
      args.add(staticLib.getAbsolutePath());
    }

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
    exec.setVerbose(true);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

  }

  @Override
  @Nonnull
  public void srcToLib(@CheckForNull String classpath, @Nonnull File out,
      boolean zipFiles, @Nonnull File... sources) throws Exception {

    List<String> args = new ArrayList<String>();
    args.add("java");
    args.add("-cp");
    args.add(jackPrebuilt.getAbsolutePath());

    if (incrementalFolder != null) {
      args.add("--incremental-folder");
      args.add(incrementalFolder.getAbsolutePath());
    }
    args.add(com.android.jack.Main.class.getName());

    addProperties(properties, args);

    if (classpath != null) {
      args.add("--classpath");
      args.add(classpath);
    }

    if (zipFiles) {
      args.add("--output-jack");
    } else {
      args.add("--output-jack-dir");
    }
    args.add(out.getAbsolutePath());

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
    for (String ecjArg : extraEcjArgs) {
      args.add(ecjArg);
    }

    AbstractTestTools.addFile(args, /* mustExist = */ false, sources);

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(true);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

  }

  @Override
  @Nonnull
  public void libToExe(@Nonnull File in, @Nonnull File out, boolean zipFile) throws Exception {

    List<String> args = new ArrayList<String>();
    args.add("java");
    args.add("-cp");
    args.add(jackPrebuilt.getAbsolutePath());

    if (incrementalFolder != null) {
      args.add("--incremental-folder");
      args.add(incrementalFolder.getAbsolutePath());
    }
    args.add(com.android.jack.Main.class.getName());

    if (withDebugInfos) {
      args.add("-D");
      args.add("jack.dex.optimize=false");
    } else {
      args.add("-D");
      args.add("jack.dex.optimize=true");
    }

    addProperties(properties, args);

    args.add("--import");
    args.add(in.getAbsolutePath());

    for (File staticLib : staticLibs) {
      args.add("--import");
      args.add(staticLib.getAbsolutePath());
    }

    if (zipFile) {
      args.add("--output-dex-zip");
    } else {
      args.add("--output-dex");
    }

    args.add(out.getAbsolutePath());

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(true);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
    }
  }

  @Override
  @Nonnull
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    List<String> args = new ArrayList<String>();
    args.add("java");
    args.add("-cp");
    args.add(jackPrebuilt.getAbsolutePath());

    if (incrementalFolder != null) {
      args.add("--incremental-folder");
      args.add(incrementalFolder.getAbsolutePath());
    }
    args.add(com.android.jack.Main.class.getName());

    addProperties(properties, args);

    if (jarjarRules != null) {
      args.add("--config-jarjar");
      args.add(jarjarRules.getAbsolutePath());
    }

    for (File flags : proguardFlags) {
      args.add("--config-proguard");
      args.add(flags.getAbsolutePath());
    }

    for (File staticlib : in) {
      args.add("--import");
      args.add(staticlib.getAbsolutePath());
    }

    for (File staticLib : staticLibs) {
      args.add("--import");
      args.add(staticLib.getAbsolutePath());
    }

    if (zipFiles) {
      args.add("--output-jack");
    } else {
      args.add("--output-jack-dir");
    }
    args.add(out.getAbsolutePath());

    ExecuteFile exec = new ExecuteFile(args.toArray(new String[args.size()]));
    exec.setErr(outRedirectStream);
    exec.setOut(errRedirectStream);
    exec.setVerbose(true);

    if (!exec.run()) {
      throw new RuntimeException("Jack compiler exited with an error");
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

  private static void addProperties(@Nonnull Map<String, String> properties,
      @Nonnull List<String> args) {
    for (Entry<String, String> entry : properties.entrySet()) {
      args.add("-D");
      args.add(entry.getKey() + "=" + entry.getValue());
    }
  }

}
