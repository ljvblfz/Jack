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
import com.android.jack.comparator.util.BytesStreamSucker;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;
import com.android.sched.util.codec.CodecContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The legacy based android toolchain.
 */
public abstract class LegacyBasedToolchain extends AndroidToolchain {

  @Nonnull
  private final File legacyCompilerPrebuilt;
  @Nonnull
  private final List<File> legacyCompilerBootclasspath;
  @Nonnull
  private final File jarjarPrebuilt;
  @Nonnull
  private final File proguardPrebuilt;
  @Nonnull
  private final File dxPrebuilt;

  private boolean useDxOptimization = true;

  @Nonnull
  private String minApiLevel = "1";

  LegacyBasedToolchain(@Nonnull File legacyCompilerPrebuilt,
      @Nonnull List<File> legacyCompilerBootclasspath,
      @Nonnull File jarjarPrebuilt,
      @Nonnull File proguardPrebuilt, @Nonnull File dxPrebuilt) {
    this.legacyCompilerPrebuilt = legacyCompilerPrebuilt;
    this.legacyCompilerBootclasspath = legacyCompilerBootclasspath;
    this.jarjarPrebuilt = jarjarPrebuilt;
    this.proguardPrebuilt = proguardPrebuilt;
    this.dxPrebuilt = dxPrebuilt;
  }

  @Override
  public void srcToExe(@Nonnull File out,
      boolean zipFile, @Nonnull File... sources) throws Exception {

    try {

      File tmpJarsDir = AbstractTestTools.createTempDir();
      File jarFile = new File(tmpJarsDir, "legacyLib.jar");

      srcToLib(jarFile, true /* zipFiles = */, sources);

      libToExe(jarFile, out, zipFile);

    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void srcToLib(@Nonnull File out,
      boolean zipFiles, @Nonnull File... sources) throws Exception {

    try {
      File classesDir;
      if (zipFiles) {
        classesDir = AbstractTestTools.createTempDir();
      } else {
        classesDir = out;
      }

      if (withDebugInfos) {
        compileWithEcj(sources, getClasspathAsString(), classesDir);
      } else {
        compileWithExternalRefCompiler(sources, getClasspathAsString(), classesDir);
      }
      if (staticLibs.size() > 0) {
        for (File staticLib : staticLibs) {
          if (staticLib.isFile()) {
            AbstractTestTools.unzip(staticLib, classesDir, isVerbose);
          } else if (staticLib.isDirectory()) {
            AbstractTestTools.copyDirectory(staticLib, classesDir);
          }
        }
      }

      File tmpDir = AbstractTestTools.createTempDir();
      File jarFile = new File(tmpDir, "file.jar");
      AbstractTestTools.createjar(jarFile, classesDir, isVerbose);

      File jarFileJarjar = new File(tmpDir, "file-jarjar.jar");
      if (jarjarRules.size() > 0) {
        if (jarjarRules.size() > 1) {
          throw new AssertionError("Not yet supported");
        }
        processWithJarJar(jarjarRules.get(0), jarFile, jarFileJarjar);
      } else {
        jarFileJarjar = jarFile;
      }

      File jarFileProguard = new File(tmpDir, "file-proguard.jar");
      if (proguardFlags.size() > 0) {
        processWithProguard(getClasspathAsString(), proguardFlags, jarFileJarjar,
            jarFileProguard);
      } else {
        jarFileProguard = jarFileJarjar;
      }

      if (zipFiles) {
        new BytesStreamSucker(new FileInputStream(jarFileProguard), new FileOutputStream(out))
            .suck();
      } else {
        assert out.isDirectory();
        AbstractTestTools.unzip(jarFileProguard, out, isVerbose);
      }
    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {

    if (in.length > 1) {
      throw new AssertionError("Not yet supported");
    }
    for (File lib : in) {
      compileWithDx(in[0], out, zipFile);
    }
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    throw new AssertionError("Not Yet Implemented");
  }

  @Override
  @Nonnull
  public File[] getDefaultBootClasspath() {
    List<File> result = new ArrayList<>(legacyCompilerBootclasspath);
    result.add(new File(TestsProperties.getJackRootDir(), "jack-tests/libs/junit4.jar"));
    return result.toArray(new File[result.size()]);
  }

  @Override
  @Nonnull
  public final String getLibraryExtension() {
    return ".jar";
  }

  @Override
  @Nonnull
  public String getLibraryElementsExtension() {
    return ".class";
  }

  private void processWithJarJar(@Nonnull File jarjarRules,
      @Nonnull File inJar, @Nonnull File outJar) {
    boolean assertEnable = false;
    assert true == (assertEnable = true);

    String[] commandLine = new String[] {"java", (assertEnable ? "-ea" : "-da"),
        "-Dverbose=" + String.valueOf(isVerbose), "-jar", jarjarPrebuilt.getAbsolutePath(),
        "process", jarjarRules.getAbsolutePath(), inJar.getAbsolutePath(),
        outJar.getAbsolutePath()};

    ExecuteFile execFile = new ExecuteFile(commandLine);
    execFile.inheritEnvironment();
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(isVerbose);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("JarJar exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running Jarjar", e);
    }
  }

  private void processWithProguard(@Nonnull String bootclasspathStr,
      @Nonnull List<File> proguardFlags, @Nonnull File inJar, @Nonnull File outJar) {
    boolean assertEnable = false;
    assert true == (assertEnable = true);

    List<String> commandLine = new ArrayList<String>();
    commandLine.add("java");
    commandLine.add(assertEnable ? "-ea" : "-da");
    commandLine.add("-jar");
    commandLine.add(proguardPrebuilt.getAbsolutePath());
    commandLine.add("-injar");
    commandLine.add(inJar.getAbsolutePath());
    commandLine.add("-outjars");
    commandLine.add(outJar.getAbsolutePath());
    if (bootclasspathStr != null) {
      commandLine.add("-libraryjars");
      commandLine.add(bootclasspathStr);
    }
    if (isVerbose) {
      commandLine.add("-verbose");
    }
    for (File flags : proguardFlags) {
      commandLine.add("-include");
      commandLine.add(flags.getAbsolutePath());
    }

    ExecuteFile execFile = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    execFile.inheritEnvironment();
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(isVerbose);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Proguard exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running Proguard", e);
    }
  }

  private void processWithDesugar(@Nonnull File input, @Nonnull File output) throws Exception {
    boolean assertEnable = false;
    assert true == (assertEnable = true);

    List<String> commandLine = new ArrayList<String>();
    commandLine.add("java");
    commandLine.add(assertEnable ? "-ea" : "-da");
    commandLine.add("-jar");
    commandLine.add(AbstractTestTools.getPrebuilt("desugar").getAbsolutePath());

    if (isVerbose) {
      commandLine.add("--verbose");
    }

    List<File> tmpClasspath = ensureOnlyFilesOnClasspath(classpath);

    commandLine.add("--bootclasspath_entry");
    commandLine.add(
        new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/emptyjar.jar")
            .getAbsolutePath());

    if (tmpClasspath != null) {
      for (File cpEntry : tmpClasspath) {
        commandLine.add("--classpath_entry");
        commandLine.add(cpEntry.getAbsolutePath());
      }
    }

    if (Options.ANDROID_MIN_API_LEVEL.getCodec().parseString(new CodecContext(), minApiLevel)
        .getReleasedLevel() >= 24) {
      commandLine.add("--min_sdk_version");
      commandLine.add(minApiLevel);
    }

    commandLine.add("--desugar_try_with_resources_if_needed");
    commandLine.add("--input");
    commandLine.add(input.getAbsolutePath());
    commandLine.add("--output");
    commandLine.add(output.getAbsolutePath());


    ExecuteFile execFile = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    execFile.inheritEnvironment();
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(isVerbose);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Desugar exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running Desugar", e);
    }

  }


  @Nonnull
  private List<File> ensureOnlyFilesOnClasspath(@Nonnull List<File> classpath) throws Exception {
    List<File> result = new ArrayList<>(classpath.size());
    for (File item : classpath) {
      File fileToAdd;
      if (item.isDirectory()) {
        fileToAdd = AbstractTestTools.createTempFile("zipped-cp-entry", ".jar");
        AbstractTestTools.createjar(fileToAdd, item, isVerbose);
      } else {
        fileToAdd = item;
      }
      result.add(fileToAdd);
    }
    return result;
  }

  private void compileWithEcj(@Nonnull File[] sources, @CheckForNull String classpath,
      @Nonnull File out) throws Exception {
    List<String> commandLine = new ArrayList<String>(4 + sources.length);

    commandLine.add("-bootclasspath");
    commandLine.add("no-bootclasspath.jar");

    if (classpath != null) {
      commandLine.add("-classpath");
      commandLine.add(classpath);
    }

    // for now, we only use ECJ for debug info comparison
    commandLine.add("-g");

    if (isVerbose) {
      commandLine.add("-verbose");
    }

    addSourceLevel(sourceLevel, commandLine);
    addTargetLevel(sourceLevel, commandLine);

    if (annotationProcessorClasses != null) {
      commandLine.add("-processor");
      commandLine.add(Joiner.on(',').join(annotationProcessorClasses));
    }

    commandLine.add("-encoding");
    commandLine.add("utf8");

    commandLine.add("-noExit");
    commandLine.add("-preserveAllLocals");

    File outputClassesDir = AbstractTestTools.createTempDir();

    if (isDesugarEnabled() && sourceLevel.compareTo(SourceLevel.JAVA_8) >= 0) {
      outputClassesDir = AbstractTestTools.createTempDir();
    } else {
      outputClassesDir = out;
    }

    commandLine.add("-d");
    commandLine.add(outputClassesDir.getAbsolutePath());

    addSourceList(commandLine, sources);
    org.eclipse.jdt.internal.compiler.batch.Main.main(
        commandLine.toArray(new String[commandLine.size()]));

    if (isDesugarEnabled() && sourceLevel.compareTo(SourceLevel.JAVA_8) >= 0) {
      File tmpOutFile = AbstractTestTools.createTempFile("jack-test", "no-desugar.jar");
      File tmpOutFileDesugared = AbstractTestTools.createTempFile("jack-test", "desugar.jar");
      AbstractTestTools.zip(outputClassesDir, tmpOutFile, isVerbose);
      processWithDesugar(tmpOutFile, tmpOutFileDesugared);
      AbstractTestTools.unzip(tmpOutFileDesugared, out, isVerbose);
    }

  }

  @Override
  @Nonnull
  public LegacyBasedToolchain disableDxOptimizations() {
    useDxOptimization = false;
    return this;
  }

  @Override
  @Nonnull
  public LegacyBasedToolchain enableDxOptimizations() {
    useDxOptimization = true;
    return this;
  }

  private static void addSourceLevel(
      @Nonnull SourceLevel level, @Nonnull List<String> commandLine) {
    commandLine.add("-source");
    switch (level) {
      case JAVA_6:
        commandLine.add("1.6");
        break;
      case JAVA_7:
        commandLine.add("1.7");
        break;
      case JAVA_8:
        commandLine.add("1.8");
        break;
      default:
        throw new AssertionError("Unkown level: '" + level.toString() + "'");
    }
  }

  private static void addTargetLevel(
      @Nonnull SourceLevel level, @Nonnull List<String> commandLine) {
    commandLine.add("-target");
    switch (level) {
      case JAVA_6:
        commandLine.add("1.6");
        break;
      case JAVA_7:
        commandLine.add("1.7");
        break;
      case JAVA_8:
        commandLine.add("1.8");
        break;
      default:
        throw new AssertionError("Unkown level: '" + level.toString() + "'");
    }
  }

  private void compileWithExternalRefCompiler(@Nonnull File[] sources,
      @CheckForNull String classpath, @Nonnull File out) throws Exception {
    List<String> commandLine = new ArrayList<String>();

    commandLine.add(legacyCompilerPrebuilt.getAbsolutePath());

    if (isVerbose) {
      commandLine.add("-verbose");
    }

    addSourceLevel(sourceLevel, commandLine);
    addTargetLevel(sourceLevel, commandLine);

    commandLine.add("-encoding");
    commandLine.add("utf8");

    if (annotationProcessorClasses != null) {
      commandLine.add("-processor");
      commandLine.add(Joiner.on(',').join(annotationProcessorClasses));
    }

    commandLine.add("-bootclasspath");
    commandLine.add("no-bootclasspath.jar");

    if (classpath != null) {
      commandLine.add("-classpath");
      commandLine.add(classpath);
    }

    addSourceList(commandLine, sources);

    File outputClassesDir = AbstractTestTools.createTempDir();

    if (isDesugarEnabled() && sourceLevel.compareTo(SourceLevel.JAVA_8) >= 0) {
      outputClassesDir = AbstractTestTools.createTempDir();
    } else {
      outputClassesDir = out;
    }

    commandLine.add("-d");
    commandLine.add(outputClassesDir.getAbsolutePath());

    ExecuteFile execFile = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    execFile.inheritEnvironment();
    execFile.setErr(errRedirectStream);
    execFile.setOut(outRedirectStream);
    execFile.setVerbose(isVerbose);
    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Reference compiler exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running reference compiler", e);
    }

    // Desugar is use for Java 8 feature such as lambda, default method or Java 7 feature such as
    // try with resources.
    if (isDesugarEnabled() && sourceLevel.compareTo(SourceLevel.JAVA_7) >= 0) {
      File tmpOutFile = AbstractTestTools.createTempFile("jack-test", "no-desugar.jar");
      File tmpOutFileDesugared = AbstractTestTools.createTempFile("jack-test", "desugar.jar");
      AbstractTestTools.zip(outputClassesDir, tmpOutFile, isVerbose);
      processWithDesugar(tmpOutFile, tmpOutFileDesugared);
      AbstractTestTools.unzip(tmpOutFileDesugared, out, isVerbose);
    }
  }

  private void compileWithDx(@Nonnull File in, @Nonnull File out, boolean zipFile) {
    List<String> commandLine = new ArrayList<String>();

    if (dxPrebuilt.getAbsolutePath().endsWith(".jar")) {
      commandLine.add("java");
      commandLine.add("-jar");
    }

    commandLine.add(dxPrebuilt.getAbsolutePath());

    commandLine.add("--dex");

    if (!useDxOptimization) {
      commandLine.add("--no-optimize");
    }

    commandLine.add("--min-sdk-version");
    commandLine.add(minApiLevel);

    if (isVerbose) {
      commandLine.add("--verbose");
    }

    commandLine.add("--output=" + out.getAbsolutePath());

    commandLine.add(in.getAbsolutePath());

    ExecuteFile execFile = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    execFile.inheritEnvironment();
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(isVerbose);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Dx exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running dx", e);
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

  @Override
  @Nonnull
  public AndroidToolchain setAndroidMinApiLevel(@Nonnull String minApiLevel) throws Exception {
    this.minApiLevel = minApiLevel;
    return this;
  }

  protected abstract boolean isDesugarEnabled();
}
