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
import com.google.common.io.Files;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link AndroidToolchain} uses Jill to convert legacy library format.
 */
public class EmbeddedJillBasedToolchain extends JackCliToolchain implements JillBasedToolchain {

  @Nonnull
  private static final String META_DIR = "meta";
  @Nonnull
  private static final String RSC_DIR = "rsc";

  @Nonnull
  private File refCompilerPrebuilt;
  @Nonnull
  private File jarjarPrebuilt;
  @Nonnull
  private File proguardPrebuilt;

  EmbeddedJillBasedToolchain(@Nonnull File jackPrebuilt, @Nonnull File refCompilerPrebuilt,
      @Nonnull File jarjarPrebuilt, @Nonnull File proguardPrebuilt) {
    super(jackPrebuilt);
    this.refCompilerPrebuilt = refCompilerPrebuilt;
    this.jarjarPrebuilt = jarjarPrebuilt;
    this.proguardPrebuilt = proguardPrebuilt;
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {
    try {
      File jarFile = srcToJar(sources);
      libToExe(jarFile, out, zipFile);
    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources)
      throws Exception {

    try {
      File classesDir;
      if (zipFiles) {
        classesDir = AbstractTestTools.createTempDir();
      } else {
        classesDir = out;
      }

      if (staticLibs.size() > 0) {
        for (File staticLib : staticLibs) {
          if (staticLib.isDirectory()) {
            AbstractTestTools.copyDirectory(staticLib, classesDir);
          } else {
            assert staticLib.isFile();
            AbstractTestTools.unzip(staticLib, classesDir, isVerbose);
          }
        }
      }

      if (sources.length > 0) {
        compileWithExternalRefCompiler(sources,
            getClasspathAsString() + File.pathSeparatorChar
            + classesDir.getPath(), classesDir);
      }

      File resDestDir;
      if (resImport.size() > 0) {
        resDestDir = new File(classesDir, RSC_DIR);
        if (!resDestDir.exists() && !resDestDir.mkdir()) {
          throw new AssertionError("Could not create resource dir");
        }

        for (File res : resImport) {
          AbstractTestTools.copyDirectory(res, resDestDir);
        }
      }

      File metaDestDir;
      if (metaImport.size() > 0) {
        metaDestDir = new File(classesDir, META_DIR);
        if (!metaDestDir.exists() && !metaDestDir.mkdir()) {
          throw new AssertionError("Could not create meta dir");
        }

        for (File meta : metaImport) {
          AbstractTestTools.copyDirectory(meta, metaDestDir);
        }
      }

      File tmpJarsDir = AbstractTestTools.createTempDir();
      File jarFile = new File(tmpJarsDir, "legacyLib.jar");
      File jarFileJarjar = new File(tmpJarsDir, "legacyLibJarjar.jar");
      File jarFileProguard = new File(tmpJarsDir, "legacyLibProguard.jar");

      AbstractTestTools.createjar(jarFile, classesDir, isVerbose);

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

      if (zipFiles) {
        Files.copy(jarFileProguard, out);
      } else {
        AbstractTestTools.unzip(jarFileProguard, out, isVerbose);
      }

    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    List<String> commandLine = new ArrayList<String>();
    libToCommon(commandLine,  getClasspathAsString(), in);

    if (zipFiles) {
      commandLine.add("--output-jack");
    } else {
      commandLine.add("--output-jack-dir");
    }
    commandLine.add(out.getAbsolutePath());

    ExecuteFile exec = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    exec.inheritEnvironment();
    exec.setErr(errRedirectStream);
    exec.setOut(outRedirectStream);
    exec.setVerbose(isVerbose);

    if (exec.run() != 0) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

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
  public String getLibraryExtension() {
    return ".jar";
  }

  @Override
  @Nonnull
  public String getLibraryElementsExtension() {
    return ".class";
  }

  @Nonnull
  protected File srcToJar(@Nonnull File... sources) throws Exception {
    File jarFile    = AbstractTestTools.createTempFile("legacyLib", ".jar");
    File classesDir = AbstractTestTools.createTempDir();
    List<String> staticLibsAsCp = new ArrayList<String>(staticLibs.size());
    for (File staticLib : staticLibs) {
      staticLibsAsCp.add(staticLib.getAbsolutePath());
    }
    String staticLibsAsCpAsString = Joiner.on(File.pathSeparatorChar).join(staticLibsAsCp);

    if (sources.length > 0) {
      compileWithExternalRefCompiler(sources,
          getClasspathAsString() + File.pathSeparatorChar + staticLibsAsCpAsString, classesDir);
    }
    AbstractTestTools.createjar(jarFile, classesDir, isVerbose);

    return jarFile;
  }

  protected void compileWithExternalRefCompiler(@Nonnull File[] sources,
      @Nonnull String classpath, @Nonnull File out) {

    List<String> commandLine = new ArrayList<String>();
    commandLine.add(refCompilerPrebuilt.getPath());

    if (isVerbose) {
      commandLine.add("-verbose");
    }

    addSourceLevel(sourceLevel, commandLine);

    if (annotationProcessorClasses != null) {
      commandLine.add("-processor");
      commandLine.add(Joiner.on(',').join(annotationProcessorClasses));
    }

    if (classpath != null) {
      commandLine.add("-classpath");
      commandLine.add(classpath);
    }

    AbstractTestTools.addFile(commandLine, false, sources);

    if (withDebugInfos) {
      commandLine.add("-g");
    }

    commandLine.add("-d");
    commandLine.add(out.getAbsolutePath());

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
  }

  protected void processWithJarJar(@Nonnull File jarjarRules,
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

  protected void processWithProguard(@Nonnull String bootclasspathStr,
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
    commandLine.add("-libraryjars");
    commandLine.add(bootclasspathStr);
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

  protected static void addSourceLevel(
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

  @Override
  protected void libToImportStaticLibs(@Nonnull List<String> commandLine, @Nonnull File[] in)
      throws Exception {

    for (int i = 0; i < staticLibs.size(); i++) {
      if (staticLibs.get(i).isDirectory()) {
        File zippedLib = new File(
            AbstractTestTools.createTempDir(), staticLibs.get(i).getName() + getLibraryExtension());

        AbstractTestTools.zip(staticLibs.get(i), zippedLib, isVerbose);
        staticLibs.set(i, zippedLib);
      }
    }

    File[] zippedInFiles = new File[in.length];
    for (int i = 0; i < in.length; i++) {
      File inFile = in[i];
      if (inFile.isDirectory()) {
        File zippedInLib =
            new File(AbstractTestTools.createTempDir(), inFile.getName() + getLibraryExtension());

        AbstractTestTools.zip(inFile, zippedInLib, isVerbose);
        zippedInFiles[i] = zippedInLib;
      } else {
        zippedInFiles[i] = inFile;
      }
    }

    super.libToImportStaticLibs(commandLine, zippedInFiles);

    importResourcesFromLibs(commandLine, staticLibs.toArray(new File[staticLibs.size()]));
    importResourcesFromLibs(commandLine, zippedInFiles);
  }

  private void importResourcesFromLibs(@Nonnull List<String> commandLine, @Nonnull File[] libs)
      throws Exception {

    for (File lib : libs) {
      File rscDir;
      if (lib.isDirectory()) {
        rscDir = new File(lib, META_DIR);
      } else {
        // Assume it's a library archive
        File tmpUnzippedLib = AbstractTestTools.createTempDir();
        AbstractTestTools.unzip(lib, tmpUnzippedLib, isVerbose);
        rscDir = new File(tmpUnzippedLib, RSC_DIR);
      }

      if (rscDir.exists()) {
        commandLine.add("--import-resource");
        commandLine.add(rscDir.getAbsolutePath());
      }

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

    ExecuteFile exec = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    exec.inheritEnvironment();
    exec.setErr(errRedirectStream);
    exec.setOut(outRedirectStream);
    exec.setVerbose(isVerbose);

    if (exec.run() != 0) {
      throw new RuntimeException("Jack compiler exited with an error");
    }
  }

}
