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

package com.android.jack.annotation.processor;

import com.android.jack.annotation.processor.sample.processors.ResourceAnnotationProcessor;
import com.android.jack.annotation.processor.sample.processors.SourceAnnotationProcessor;
import com.android.jack.comparator.util.BytesStreamSucker;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;
import com.android.sched.vfs.VPath;
import com.android.sched.vfs.ZipUtils;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class AnnotationProcessorTests {

  @Nonnull
  private static final File ANNOTATED_DIR = AbstractTestTools.getTestRootDir(
      "com.android.jack.annotation.processor.sample.annotated");
  @Nonnull
  private static final File ANNOTATIONS_DIR = AbstractTestTools.getTestRootDir(
      "com.android.jack.annotation.processor.sample.annotations");
  @Nonnull
  private static final File TEST2_DIR = AbstractTestTools.getTestRootDir(
      "com.android.jack.annotation.processor.sample2.src");

  @Nonnull
  private static File getNoConfigProcessors() throws Exception {
    File processorsDir = AbstractTestTools.createTempDir();
    File processorsJar = AbstractTestTools.createTempFile("processor", ".jar");

    File processorsSrcDir = compileProcessorsToDir(processorsDir,
        "com.android.jack.annotation.processor.sample.processors");

    makeZipFromDir(processorsDir, processorsJar);

    return processorsJar;
  }

  @Nonnull
  private static File getAutoProcessors(@Nonnull String processorName) throws Exception {
    File processorsDir = AbstractTestTools.createTempDir();
    File processorsJar = AbstractTestTools.createTempFile("autoProcessor", ".jar");

    File processorsSrcDir = compileProcessorsToDir(processorsDir, processorName);

    AbstractTestTools.copyFileToDir(
        new File(processorsSrcDir, "javax.annotation.processing.Processor"),
        "META-INF/services/javax.annotation.processing.Processor", processorsDir);

    makeZipFromDir(processorsDir, processorsJar);

    return processorsJar;
  }

  @Nonnull
  private static File compileProcessorsToDir(@Nonnull File outputDir, @Nonnull String processorName)
      throws ExecFileException {
    File compiler = AbstractTestTools.getPrebuilt("legacy-java-compiler");
    File processorsSrcDir = AbstractTestTools.getTestRootDir(processorName);
    List<String> compilerArgs = new ArrayList<String>();
    compilerArgs.add("-cp");
    compilerArgs.add(
        new File(TestsProperties.getJackRootDir(), "jack-tests/libs/jsr305-lib.jar").getPath());
    compilerArgs.add("-source");
    compilerArgs.add("1.7");
    compilerArgs.add("-encoding");
    compilerArgs.add("utf8");
    compilerArgs.add("-d");
    compilerArgs.add(outputDir.getPath());
    AbstractTestTools.addFile(compilerArgs, true, processorsSrcDir, ANNOTATIONS_DIR);

    ExecuteFile compile = new ExecuteFile(compiler.getPath(),
        compilerArgs.toArray(new String[compilerArgs.size()]));
    compile.setErr(System.err);
    compile.setOut(System.out);
    if (compile.run() != 0) {
      throw new AssertionError();
    }
    return processorsSrcDir;
  }

  private static void makeZipFromDir(@Nonnull File dir, @Nonnull File zip)
      throws IOException {
    assert dir.isDirectory();
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
    try {
      addFilesToZip(out, dir, "");
    } finally {
      out.close();
    }
  }

  private static void addFilesToZip(ZipOutputStream zip, File file, String entryPath)
      throws IOException {
    if (file.isFile()) {
      zip.putNextEntry(new ZipEntry(entryPath));
      InputStream in = new FileInputStream(file);
      try {
        new BytesStreamSucker(in, zip).suck();
        zip.closeEntry();
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          // ignore
        }
      }
    } else {
      for (File sub: file.listFiles()) {
        // Zip entries names must not start with /
        String subEntryPath = entryPath.isEmpty() ? sub.getName() :
          entryPath + ZipUtils.ZIP_SEPARATOR + sub.getName();
        addFilesToZip(zip, sub, subEntryPath);
      }
    }
  }

  @Test
  public void compileWithAnnotationProcessorAuto_classpath() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors("com.android.jack.annotation.processor.sample.processors");
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.addToClasspath(processors);
    jack.srcToLib(jackOut, /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(jackOut);
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorAuto_processorPath() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors("com.android.jack.annotation.processor.sample.processors");
    jack.setAnnotationProcessorPath(processors.getPath());
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.srcToLib(jackOut,
        /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(jackOut);
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorNoAuto_processorPath() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getNoConfigProcessors();
    jack.setAnnotationProcessorPath(processors.getPath());
    jack.setAnnotationProcessorClasses(
        Collections.singletonList(ResourceAnnotationProcessor.class.getName()));
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.srcToLib(jackOut,
        /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(jackOut);
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
    try {
      libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }
  }

  @Test
  public void compileWithAnnotationProcessorNoAutoNoClasses_processorPath() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getNoConfigProcessors();
    jack.setAnnotationProcessorPath(processors.getPath());
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.srcToLib(jackOut,
        /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
        );
    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(jackOut);
    Assert.assertFalse(libOut.containsFileType(FileType.RSC));
    try {
      libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }
  }

  @Test
  public void compileWithAnnotationProcessorNoAuto_classpath() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getNoConfigProcessors();
    jack.setAnnotationProcessorClasses(
        Collections.singletonList(SourceAnnotationProcessor.class.getName()));
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.addToClasspath(processors);
    jack.srcToLib(jackOut, /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(jackOut);
    Assert.assertFalse(libOut.containsFileType(FileType.RSC));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorOption() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors("com.android.jack.annotation.processor.sample.processors");
    jack.addAnnotationProcessorOption(
        SourceAnnotationProcessor.SOURCE_ANNOTATION_PROCESSOR_SUFFIX, "WithOption");
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.addToClasspath(processors);
    jack.srcToLib(jackOut, /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(jackOut);
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2WithOption", '/'));
  }

  /**
   * Checks that type resolution succeed when a source file depends on a type generated by an
   * annotation processor.
   */
  @Test
  public void annotationProcessorTest2() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain jack =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File jackOut = AbstractTestTools.createTempDir();
    File srcOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors("com.android.jack.annotation.processor.sample2.processors");
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.addToClasspath(processors);
    jack.srcToLib(jackOut, /* zipFiles= */false, TEST2_DIR, srcOut);
  }
}
