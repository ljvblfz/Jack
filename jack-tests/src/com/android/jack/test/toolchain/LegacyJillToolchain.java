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

import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link JillBasedToolchain} uses legacy java compiler as a frontend.
 */
public class LegacyJillToolchain extends JillBasedToolchain {

  @Nonnull
  private File jarjarPrebuilt;
  @Nonnull
  private File proguardPrebuilt;

  public LegacyJillToolchain(@Nonnull File jillPrebuilt, @Nonnull File jackPrebuilt,
      @Nonnull File jarjarPrebuilt, @Nonnull File proguardPrebuilt) {
    super(jillPrebuilt, jackPrebuilt);
    this.jarjarPrebuilt = jarjarPrebuilt;
    this.proguardPrebuilt = proguardPrebuilt;
  }

  @Override
  @Nonnull
  public void srcToExe(@Nonnull String classpath, @Nonnull File out, boolean zipFile,
      @Nonnull File... sources) throws Exception {
    try {

      File jarFile         = AbstractTestTools.createTempFile("legacyLib", ".jar");
      File jarFileJarjar   = AbstractTestTools.createTempFile("legacyLibJarjar", ".jar");
      File jarFileProguard = AbstractTestTools.createTempFile("legacyLibProguard", ".jar");

      srcToLib(classpath, jarFile, true /* zipFiles = */, sources);

      if (jarjarRules != null) {
        processWithJarJar(jarjarRules, jarFile, jarFileJarjar);
      } else {
        jarFileJarjar = jarFile;
      }

      if (proguardFlags.size() > 0) {
        processWithProguard(classpath, proguardFlags, jarFileJarjar,
            jarFileProguard);
      } else {
        jarFileProguard = jarFileJarjar;
      }

      File jillLib = AbstractTestTools.createTempFile("jillLib", ".jar");
      executeJill(jarFileProguard, jillLib, true);

      libToExe(jillLib, out, zipFile);

    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  @Nonnull
  public void srcToLib(@Nonnull String classpath, @Nonnull File out, boolean zipFiles,
      @Nonnull File... sources) throws Exception {

    if (withDebugInfos) {
      // TODO(jmhenaff): warning log?
    }

    try {
      File classesDir;
      if (zipFiles) {
        classesDir = AbstractTestTools.createTempDir();
      } else {
        classesDir = out;
      }

      compileWithExternalRefCompiler(sources, classpath, classesDir);

      if (staticLibs.size() > 0) {
        for (File staticLib : staticLibs) {
          AbstractTestTools.unzip(staticLib, classesDir);
        }
      }
      if (zipFiles) {
        AbstractTestTools.createjar(out, classesDir);
      }
    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  @Nonnull
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    throw new AssertionError("Not Yet Implemented");
  }

  @Override
  @Nonnull
  public JackBasedToolchain addResource(@Nonnull File resource) {
    throw new AssertionError("Not applicable");
  }

  private void compileWithExternalRefCompiler(@Nonnull File[] sources,
      @Nonnull String classpath, @Nonnull File out) {

    List<String> arguments = new ArrayList<String>();
    String refCompilerPath = System.getenv("REF_JAVA_COMPILER");

    if (refCompilerPath == null) {
      throw new RuntimeException("REF_JAVA_COMPILER environment variable not set");
    }

    arguments.add(refCompilerPath.trim());

    addSourceLevel(sourceLevel, arguments);

    if (annotationProcessorClass != null) {
      arguments.add("-processor");
      arguments.add(annotationProcessorClass.getName());
    }

    if (classpath != null) {
      arguments.add("-classpath");
      arguments.add(classpath);
    }

    AbstractTestTools.addFile(arguments, false, sources);

    arguments.add("-d");
    arguments.add(out.getAbsolutePath());

    ExecuteFile execFile = new ExecuteFile(arguments.toArray(new String[arguments.size()]));
    execFile.setErr(outRedirectStream);
    execFile.setOut(errRedirectStream);
    execFile.setVerbose(true);
    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Reference compiler exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occured while running reference compiler", e);
    }
  }

  private void processWithJarJar(@Nonnull File jarjarRules,
      @Nonnull File inJar, @Nonnull File outJar) {
    String[] args = new String[]{"java", "-jar", jarjarPrebuilt.getAbsolutePath(),
        "process", jarjarRules.getAbsolutePath(),
        inJar.getAbsolutePath(), outJar.getAbsolutePath()};

    ExecuteFile execFile = new ExecuteFile(args);
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(true);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("JarJar exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occured while running Jarjar", e);
    }
  }

  private void processWithProguard(@Nonnull String bootclasspathStr,
      @Nonnull List<File> proguardFlags, @Nonnull File inJar, @Nonnull File outJar) {

    List<String> args = new ArrayList<String>();
    args.add("java");
    args.add("-jar");
    args.add(proguardPrebuilt.getAbsolutePath());
    args.add("-injar");
    args.add(inJar.getAbsolutePath());
    args.add("-outjars");
    args.add(outJar.getAbsolutePath());
    args.add("-libraryjars");
    args.add(bootclasspathStr);
    args.add("-verbose");
    args.add("-forceprocessing");
    args.add("-dontoptimize");
    for (File flags : proguardFlags) {
      args.add("-include");
      args.add(flags.getAbsolutePath());
    }

    ExecuteFile execFile = new ExecuteFile(args.toArray(new String[args.size()]));
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(true);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Proguard exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occured while running Proguard", e);
    }
  }

  private static void addSourceLevel(@Nonnull SourceLevel level, @Nonnull List<String> args) {
    args.add("-source");
    switch (level) {
      case JAVA_6:
        args.add("1.6");
        break;
      case JAVA_7:
        args.add("1.7");
        break;
      default:
        throw new AssertionError("Unkown level: '" + level.toString() + "'");
    }
  }
}
