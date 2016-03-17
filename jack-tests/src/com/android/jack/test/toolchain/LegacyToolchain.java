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

import com.android.dx.command.dexer.Main.Arguments;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The legacy android toolchain.
 */
public class LegacyToolchain extends AndroidToolchain {

  @Nonnull
  private final File legacyCompilerPrebuilt;
  @Nonnull
  private final File jarjarPrebuilt;
  @Nonnull
  private final File proguardPrebuilt;

  private boolean useDxOptimization = true;

  LegacyToolchain(@Nonnull File legacyCompilerPrebuilt, @Nonnull File jarjarPrebuilt,
      @Nonnull File proguardPrebuilt) {
    this.legacyCompilerPrebuilt = legacyCompilerPrebuilt;
    this.jarjarPrebuilt         = jarjarPrebuilt;
    this.proguardPrebuilt       = proguardPrebuilt;
  }

  @Override
  public void srcToExe(@Nonnull File out,
      boolean zipFile, @Nonnull File... sources) throws Exception {

    try {

      File tmpJarsDir = AbstractTestTools.createTempDir();
      File jarFile = new File(tmpJarsDir, "legacyLib.jar");
      File jarFileJarjar = new File(tmpJarsDir, "legacyLibJarjar.jar");
      File jarFileProguard = new File(tmpJarsDir, "legacyLibProguard.jar");

      srcToLib(jarFile, true /* zipFiles = */, sources);

      if (jarjarRules.size() > 0) {
        if (jarjarRules.size() > 1) {
          throw new AssertionError("Not yet supported");
        }
        processWithJarJar(jarjarRules.get(0), jarFile, jarFileJarjar);
      } else {
        jarFileJarjar = jarFile;
      }

      if (proguardFlags.size() > 0) {
        processWithProguard(getClasspathAsString(), proguardFlags, jarFileJarjar,
            jarFileProguard);
      } else {
        jarFileProguard = jarFileJarjar;
      }

      libToExe(jarFileProguard, out, zipFile);

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
          AbstractTestTools.unzip(staticLib, classesDir, isVerbose);
        }
      }
      if (zipFiles) {
        AbstractTestTools.createjar(out, classesDir, isVerbose);
      }
    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {

    try {
      if (in.length > 1) {
        throw new AssertionError("Not yet supported");
      }
      for (File lib : in) {
        compileWithDx(in[0], out, zipFile);
      }
    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    throw new AssertionError("Not Yet Implemented");
  }

  @Override
  @Nonnull
  public File[] getDefaultBootClasspath() {
    return new File[] {
        new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/core-stubs-mini.jar"),
        new File(TestsProperties.getJackRootDir(), "jack-tests/libs/junit4.jar")
    };
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

    if (annotationProcessorClasses != null) {
      commandLine.add("-processor");
      commandLine.add(Joiner.on(',').join(annotationProcessorClasses));
    }

    commandLine.add("-encoding");
    commandLine.add("utf8");

    commandLine.add("-noExit");
    commandLine.add("-preserveAllLocals");
    commandLine.add("-d");
    commandLine.add(out.getAbsolutePath());
    addSourceList(commandLine, sources);
    org.eclipse.jdt.internal.compiler.batch.Main.main(
        commandLine.toArray(new String[commandLine.size()]));
  }

  @Override
  @Nonnull
  public LegacyToolchain disableDxOptimizations() {
    useDxOptimization = false;
    return this;
  }

  @Override
  @Nonnull
  public LegacyToolchain enableDxOptimizations() {
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

  private void compileWithExternalRefCompiler(@Nonnull File[] sources,
      @CheckForNull String classpath, @Nonnull File out) throws Exception {

    List<String> commandLine = new ArrayList<String>();

    commandLine.add(legacyCompilerPrebuilt.getAbsolutePath());

    if (isVerbose) {
      commandLine.add("-verbose");
    }

    addSourceLevel(sourceLevel, commandLine);

    commandLine.add("-target");
    commandLine.add("1.7");

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

    commandLine.add("-d");
    commandLine.add(out.getAbsolutePath());

    ExecuteFile execFile = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
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
  }

  private void compileWithDx(@Nonnull File in, @Nonnull File out, boolean zipFile)
      throws IOException {

    try {
      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);

      Arguments arguments = new Arguments();

      arguments.jarOutput = zipFile;
      arguments.outName = new File(out, getBinaryFileName()).getAbsolutePath();

      arguments.optimize = useDxOptimization;
      // this only means we deactivate the check that no core classes are included
      arguments.coreLibrary = true;
      arguments.verbose = isVerbose;
      arguments.parse(new String[] {in.getAbsolutePath()});

      int retValue = com.android.dx.command.dexer.Main.run(arguments);
      if (retValue != 0) {
        throw new RuntimeException("Dx failed and returned " + retValue);
      }
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
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
