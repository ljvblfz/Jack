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

import com.android.jack.library.FileType;
import com.android.jack.library.JackLibrary;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * This {@link JillBasedToolchain} uses legacy java compiler as a frontend.
 */
public class LegacyJillToolchain extends JillBasedToolchain {

  @Nonnull
  private File refCompilerPrebuilt;
  @Nonnull
  private File jarjarPrebuilt;
  @Nonnull
  private File proguardPrebuilt;

  public LegacyJillToolchain(@Nonnull File refCompilerPrebuilt, @Nonnull File jillPrebuilt,
      @Nonnull File jackPrebuilt, @Nonnull File jarjarPrebuilt, @Nonnull File proguardPrebuilt) {
    super(jillPrebuilt, jackPrebuilt);
    this.refCompilerPrebuilt = refCompilerPrebuilt;
    this.jarjarPrebuilt = jarjarPrebuilt;
    this.proguardPrebuilt = proguardPrebuilt;
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {
    try {

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
      AbstractTestTools.createjar(jarFile, classesDir);

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
            AbstractTestTools.unzip(staticLib, classesDir);
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
        resDestDir = new File(classesDir, FileType.RSC.getPrefix());
        if (!resDestDir.exists() && !resDestDir.mkdir()) {
          throw new AssertionError("Could not create rsc dir");
        }

        for (File res : resImport) {
          AbstractTestTools.copyDirectory(res, resDestDir);
        }
      }

      File tmpJarsDir = AbstractTestTools.createTempDir();
      File jarFile = new File(tmpJarsDir, "legacyLib.jar");
      File jarFileJarjar = new File(tmpJarsDir, "legacyLibJarjar.jar");
      File jarFileProguard = new File(tmpJarsDir, "legacyLibProguard.jar");

      AbstractTestTools.createjar(jarFile, classesDir);

      if (jarjarRules != null) {
        processWithJarJar(jarjarRules, jarFile, jarFileJarjar);
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
        AbstractTestTools.unzip(jarFileProguard, out);
      }

    } catch (IOException e) {
      throw new RuntimeException("Legacy toolchain exited with an error", e);
    }
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    List<String> args = new ArrayList<String>();
    libToCommon(args, convertClasspahtWithJillAsString(), in);

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

    if (exec.run() != 0) {
      throw new RuntimeException("Jack compiler exited with an error");
    }

  }

  @Override
  protected void libToImportStaticLibs(@Nonnull List<String> args, @Nonnull File[] in)
      throws Exception {

    for (int i = 0; i < in.length; i++) {
      File tmpDir = AbstractTestTools.createTempDir();
      File jilledLib = new File(tmpDir, "jilledLib_" + i + ".jack");
      executeJillWithResources(in[i], jilledLib, /* zipFiles = */ true);
      args.add("--import");
      args.add(jilledLib.getAbsolutePath());
    }

    for (int i = 0; i < staticLibs.size(); i++) {
      File jilledLib = AbstractTestTools.createTempFile("jilledLib", ".jack");
      executeJillWithResources(staticLibs.get(i), jilledLib, /* zipFiles = */ true);
      args.add("--import");
      args.add(jilledLib.getAbsolutePath());
    }
  }

  private void executeJillWithResources(@Nonnull File in, @Nonnull File out, boolean zipFiles)
      throws IOException {

    File tmpOut = AbstractTestTools.createTempFile("out", ".jack");
    executeJill(in, tmpOut);

    File rscDir;
    if (in.isDirectory()) {
      rscDir = new File(in, FileType.RSC.getPrefix());
    } else {
      // Assume it's a library archive
      File tmpUnzippedLib = AbstractTestTools.createTempDir();
      AbstractTestTools.unzip(in, tmpUnzippedLib);
      rscDir = new File(tmpUnzippedLib, FileType.RSC.getPrefix());
    }

    if (rscDir.exists()) {

      File tmpUnzippedOutLib = AbstractTestTools.createTempDir();
      AbstractTestTools.unzip(tmpOut, tmpUnzippedOutLib);

      File destRscDir = new File(tmpUnzippedOutLib, FileType.RSC.getPrefix());
      if (!destRscDir.mkdir()) {
        throw new AssertionError("Could not create directory: '" + destRscDir.getPath() + "'");
      }

      AbstractTestTools.copyDirectory(rscDir, destRscDir);

      File jackProperties;
      jackProperties = new File(tmpUnzippedOutLib, JackLibrary.LIBRARY_PROPERTIES);

      Properties prop = new Properties();
      FileReader fr = new FileReader(jackProperties);
      prop.load(fr);
      fr.close();
      prop.setProperty("rsc", "true");
      FileWriter fw = new FileWriter(jackProperties);
      prop.store(fw, "Edited by legacy-jill toolchain");
      fw.close();

      AbstractTestTools.zip(tmpUnzippedOutLib, out);
    } else {
      Files.copy(tmpOut, out);
    }
  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {
    List<String> args = new ArrayList<String>();
    libToCommon(args, convertClasspahtWithJillAsString(), in);

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

    if (exec.run() != 0) {
      throw new RuntimeException("Jack compiler exited with an error");
    }
  }

  @Nonnull
  private String convertClasspahtWithJillAsString() throws IOException {
    File[] result = new File[classpath.size()];
    for (int i = 0; i < classpath.size(); i++) {
      result[i] = AbstractTestTools.createTempFile(classpath.get(i).getName(), ".jack");
      executeJill(classpath.get(i), result[i]);
    }
    return AbstractTestTools.getClasspathAsString(result);
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

  private void compileWithExternalRefCompiler(@Nonnull File[] sources,
      @Nonnull String classpath, @Nonnull File out) {

    List<String> arguments = new ArrayList<String>();
    arguments.add(refCompilerPrebuilt.getPath());

    if (isVerbose) {
      arguments.add("-verbose");
    }

    addSourceLevel(sourceLevel, arguments);

    if (annotationProcessorClasses != null) {
      arguments.add("-processor");
      arguments.add(Joiner.on(',').join(annotationProcessorClasses));
    }

    arguments.add("-bootclasspath");
    arguments.add("no-bootclasspath.jar");

    if (classpath != null) {
      arguments.add("-classpath");
      arguments.add(classpath);
    }

    AbstractTestTools.addFile(arguments, false, sources);

    if (withDebugInfos) {
      arguments.add("-g");
    }

    arguments.add("-d");
    arguments.add(out.getAbsolutePath());

    ExecuteFile execFile = new ExecuteFile(arguments.toArray(new String[arguments.size()]));
    execFile.setErr(outRedirectStream);
    execFile.setOut(errRedirectStream);
    execFile.setVerbose(isVerbose);
    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Reference compiler exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running reference compiler", e);
    }
  }

  private void processWithJarJar(@Nonnull File jarjarRules,
      @Nonnull File inJar, @Nonnull File outJar) {
    String[] args = new String[]{"java", "-Dverbose=" + String.valueOf(isVerbose), "-jar",
        jarjarPrebuilt.getAbsolutePath(), "process", jarjarRules.getAbsolutePath(),
        inJar.getAbsolutePath(), outJar.getAbsolutePath()};

    ExecuteFile execFile = new ExecuteFile(args);
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
    if (isVerbose) {
      args.add("-verbose");
    }
    for (File flags : proguardFlags) {
      args.add("-include");
      args.add(flags.getAbsolutePath());
    }

    ExecuteFile execFile = new ExecuteFile(args.toArray(new String[args.size()]));
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
