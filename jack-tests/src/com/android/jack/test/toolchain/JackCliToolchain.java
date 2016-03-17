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

import com.google.common.base.Joiner;

import com.android.jack.Options;
import com.android.jack.Options.VerbosityLevel;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
  @Nonnull
  protected final Map<String, String> properties = new HashMap<String, String>();

  protected boolean sanityChecks = true;

  @CheckForNull
  protected File outputJack;

  boolean zipOutputJackFiles;

  JackCliToolchain(@Nonnull File prebuilt) {
    this.jackPrebuilt = prebuilt;
    addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");
  }

  @Override
  @Nonnull
  public JackCliToolchain setVerbose(boolean isVerbose) {
    super.setVerbose(isVerbose);
    verbosityLevel = isVerbose ? VerbosityLevel.INFO : VerbosityLevel.WARNING;
    return this;
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {

    List<String> commandLine = new ArrayList<String>();

    srcToCommon(commandLine, sources);

    if (zipFile) {
      commandLine.add("--output-dex-zip");
    } else {
      commandLine.add("--output-dex");
    }
    commandLine.add(out.getAbsolutePath());

    commandLine.addAll(extraJackArgs);

    if (withDebugInfos) {
      commandLine.add("-g");
    }

    addSourceList(commandLine, sources);

    run(commandLine);

  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources)
      throws Exception {

    List<String> commandLine = new ArrayList<String>();

    setOutputJack(out, zipFiles);

    srcToCommon(commandLine, sources);

    addSourceList(commandLine, sources);

    run(commandLine);

  }

  private void srcToCommon(@Nonnull List<String> commandLine, @Nonnull File... sources) {

    buildJackCall(commandLine);

    commandLine.add("--verbose");
    commandLine.add(verbosityLevel.name());

    commandLine.add("--sanity-checks");
    commandLine.add(Boolean.toString(sanityChecks));

    if (incrementalFolder != null) {
      commandLine.add("--incremental-folder");
      commandLine.add(incrementalFolder.getAbsolutePath());
    }

    addProperties(properties, commandLine);

    if (classpath.size() > 0) {
      commandLine.add("--classpath");
      commandLine.add(getClasspathAsString());
    }

    for (File res : resImport) {
      commandLine.add("--import-resource");
      commandLine.add(res.getPath());
    }

    for (File meta : metaImport) {
      commandLine.add("--import-meta");
      commandLine.add(meta.getPath());
    }

    if (outputJack != null) {
      if (zipOutputJackFiles) {
        commandLine.add("--output-jack");
      } else {
        commandLine.add("--output-jack-dir");
      }
      commandLine.add(outputJack.getAbsolutePath());
    }

    commandLine.addAll(extraJackArgs);

    for (File jarjarFile : jarjarRules) {
      commandLine.add("--config-jarjar");
      commandLine.add(jarjarFile.getAbsolutePath());
    }

    for (File flags : proguardFlags) {
      commandLine.add("--config-proguard");
      commandLine.add(flags.getAbsolutePath());
    }

    if (withDebugInfos) {
      commandLine.add("-g");
    }

    addAnnotationProcessorArgs(commandLine);

    for (File staticLib : staticLibs) {
      commandLine.add("--import");
      commandLine.add(staticLib.getAbsolutePath());
    }
  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {
    List<String> commandLine = new ArrayList<String>();

    libToCommon(commandLine, getClasspathAsString(), in);

    if (zipFile) {
      commandLine.add("--output-dex-zip");
    } else {
      commandLine.add("--output-dex");
    }

    commandLine.add(out.getAbsolutePath());

    run(commandLine);

  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    List<String> commandLine = new ArrayList<String>();

    setOutputJack(out, zipFiles);

    libToCommon(commandLine, getClasspathAsString(), in);

    run(commandLine);

  }

  protected void libToCommon(@Nonnull List<String> commandLine, @Nonnull String classpath,
      @Nonnull File[] in) throws Exception {

    buildJackCall(commandLine);

    commandLine.add("--verbose");
    commandLine.add(verbosityLevel.name());

    commandLine.add("--sanity-checks");
    commandLine.add(Boolean.toString(sanityChecks));

    if (incrementalFolder != null) {
      commandLine.add("--incremental-folder");
      commandLine.add(incrementalFolder.getAbsolutePath());
    }

    for (File res : resImport) {
      commandLine.add("--import-resource");
      commandLine.add(res.getPath());
    }

    for (File meta : metaImport) {
      commandLine.add("--import-meta");
      commandLine.add(meta.getPath());
    }

    if (outputJack != null) {
      if (zipOutputJackFiles) {
        commandLine.add("--output-jack");
      } else {
        commandLine.add("--output-jack-dir");
      }
      commandLine.add(outputJack.getAbsolutePath());
    }

    addProperties(properties, commandLine);

    if (!classpath.equals("")) {
      commandLine.add("--classpath");
      commandLine.add(classpath);
    }

    for (File jarjarFile : jarjarRules) {
      commandLine.add("--config-jarjar");
      commandLine.add(jarjarFile.getAbsolutePath());
    }

    for (File flags : proguardFlags) {
      commandLine.add("--config-proguard");
      commandLine.add(flags.getAbsolutePath());
    }

    if (withDebugInfos) {
      commandLine.add("-g");
    }

    libToImportStaticLibs(commandLine, in);

  }

  protected void buildJackCall(@Nonnull List<String> commandLine) {

    if (jackPrebuilt.getName().endsWith(".jar")) {
      boolean assertEnable = false;
      assert true == (assertEnable = true);

      commandLine.add("java");
      commandLine.add(assertEnable ? "-ea" : "-da");
      commandLine.add("-jar");
    }

    commandLine.add(jackPrebuilt.getAbsolutePath());

  }

  protected void libToImportStaticLibs(@Nonnull List<String> commandLine, @Nonnull File[] in)
      throws Exception {
    for (File staticlib : in) {
      commandLine.add("--import");
      commandLine.add(staticlib.getAbsolutePath());
    }

    for (File staticLib : staticLibs) {
      commandLine.add("--import");
      commandLine.add(staticLib.getAbsolutePath());
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

  @Override
  @Nonnull
  public JackBasedToolchain addProperty(@Nonnull String propertyName,
      @Nonnull String propertyValue) {
    properties.put(propertyName, propertyValue);
    return this;
  }

  protected static void addProperties(@Nonnull Map<String, String> properties,
      @Nonnull List<String> commandLine) {
    for (Entry<String, String> entry : properties.entrySet()) {
      commandLine.add("-D");
      commandLine.add(entry.getKey() + "=" + entry.getValue());
    }
  }

  @Override
  @Nonnull
  public JackBasedToolchain setSanityChecks(boolean sanityChecks){
    this.sanityChecks = sanityChecks;
    return this;
  }

  @Override
  @Nonnull
  public JackCliToolchain setSourceLevel(@Nonnull SourceLevel sourceLevel) {
    super.setSourceLevel(sourceLevel);
    switch (sourceLevel) {
      case JAVA_6:
        addProperty("jack.java.source.version", "1.6");
        break;
      case JAVA_7:
        addProperty("jack.java.source.version", "1.7");
        break;
      case JAVA_8:
        addProperty("jack.java.source.version", "1.8");
        break;
      default:
        throw new AssertionError("Unkown level: '" + sourceLevel.toString() + "'");
    }
    return this;
  }

  private void addAnnotationProcessorArgs(@Nonnull List<String> commandLine) {
    for (Entry<String, String> entry : annotationProcessorOptions.entrySet()) {
        commandLine.add("-A");
        commandLine.add(entry.getKey() + "=" + entry.getValue());
      }

    if (annotationProcessorClasses != null) {
      commandLine.add("--processor");
      commandLine.add(Joiner.on(',').join(annotationProcessorClasses));
    }

    if (processorPath != null) {
        commandLine.add("--processorpath");
        commandLine.add(processorPath);
    }
  }

  @Override
  public void setOutputJack(@Nonnull File outputJack, boolean zipFiles) throws Exception {
    this.outputJack = outputJack;
    this.zipOutputJackFiles = zipFiles;
  }


  protected void run(@Nonnull List<String> commandLine) {
    ExecuteFile exec = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    exec.inheritEnvironment();
    exec.setErr(errRedirectStream);
    exec.setOut(outRedirectStream);
    exec.setVerbose(isVerbose);

    try {
      if (exec.run() != 0) {
        throw new RuntimeException("Jack compiler exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running Jack", e);
    }
  }

  protected void addSourceList(@Nonnull List<String> commandLine, @Nonnull File... sources)
      throws Exception {
    List<String> files = new ArrayList<String>(sources.length);
    AbstractTestTools.addFile(files, /* mustExist = */ false, sources);
    File sourceList = AbstractTestTools.createTempFile("source-list", ".txt");
    BufferedWriter writer = new BufferedWriter(new FileWriter(sourceList.getAbsolutePath()));
    for (String f : files) {
      writer.write(f);
      writer.write('\n');
    }
    writer.close();
    commandLine.add('@' + sourceList.getAbsolutePath());
  }
}
