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
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.GatherConfigBuilder;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class AnnotationProcessorTests {

  @Nonnull
  private static final File ANNOTATED_DIR = AbstractTestTools.getTestRootDir(
      "com.android.jack.annotation.processor.sample.annotated");
  @Nonnull
  private static final File ANNOTATIONS_DIR = AbstractTestTools.getTestRootDir(
      "com.android.jack.annotation.processor.sample.annotations");

  @CheckForNull
  private static File noConfigProcessors;

  @CheckForNull
  private static File autoProcessors;

  @BeforeClass
  public static void setupClass() throws ConfigurationException {
    // required for creating InputJackLibrary when running tests on cli
    ThreadConfig.setConfig(new GatherConfigBuilder().build());
    noConfigProcessors = null;
    autoProcessors = null;
  }

  @Nonnull
  private static File getNoConfigProcessors() throws Exception {
    if (noConfigProcessors == null) {
      IToolchain toolchain = AbstractTestTools.getReferenceToolchain();
      File processorsDir = AbstractTestTools.createTempDir();
      File processorsSrcDir = AbstractTestTools.getTestRootDir(
          "com.android.jack.annotation.processor.sample.processors");
      toolchain.srcToLib(processorsDir, /*zipFiles=*/ false,
          ANNOTATIONS_DIR,
          processorsSrcDir
          );
      noConfigProcessors = processorsDir;
    }
    return noConfigProcessors;
  }

  @Nonnull
  private static File getAutoProcessors() throws Exception {
    if (autoProcessors == null) {
      IToolchain toolchain = AbstractTestTools.getReferenceToolchain();
      File processorsDir = AbstractTestTools.createTempDir();
      File processorsSrcDir = AbstractTestTools.getTestRootDir(
          "com.android.jack.annotation.processor.sample.processors");
      toolchain.srcToLib(processorsDir, /*zipFiles=*/ false,
          ANNOTATIONS_DIR,
          processorsSrcDir
          );
      AbstractTestTools.copyFileToDir(new File(processorsSrcDir,
          "javax.annotation.processing.Processor"),
          "META-INF/services/javax.annotation.processing.Processor", processorsDir);
      autoProcessors = processorsDir;
    }
    return autoProcessors;
  }

  @Test
  public void compileWithAnnotationProcessorAuto_classpath() throws Exception {
    JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors();
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.addToClasspath(processors);
    jack.srcToLib(jackOut, /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorAuto_processorPath() throws Exception {
    JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors();
    jack.setAnnotationProcessorPath(processors.getPath());
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.srcToLib(jackOut,
        /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorNoAuto_processorPath() throws Exception {
    JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
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
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
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
    JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getNoConfigProcessors();
    jack.setAnnotationProcessorPath(processors.getPath());
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.srcToLib(jackOut,
        /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
        );
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
    try {
      libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }
    try {
      libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }
  }

  @Test
  public void compileWithAnnotationProcessorNoAuto_classpath() throws Exception {
    JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
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
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
    try {
      libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorReuseClassOut() throws Exception {
    File classesOut = AbstractTestTools.createTempDir();
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors();
    {
      JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      jack.setAnnotationProcessorPath(processors.getPath());
      jack.addResourceDir(classesOut);
      jack.addToClasspath(jack.getDefaultBootClasspath());
      jack.srcToLib(jackOut,
          /*zipFiles=*/false,
          ANNOTATIONS_DIR,
          ANNOTATED_DIR
              );
    }
    {
      JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      jack.setAnnotationProcessorPath(processors.getPath());
      jack.addResourceDir(classesOut);
      jack.addToClasspath(jack.getDefaultBootClasspath());
      jack.srcToLib(jackOut,
          /*zipFiles=*/false,
          ANNOTATIONS_DIR,
          ANNOTATED_DIR
              );
    }
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile0", '/'));
    libOut.getFile(FileType.RSC, new VPath("rscGeneratedFile1", '/'));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2Duplicated", '/'));
  }

  @Test
  public void compileWithAnnotationProcessorOption() throws Exception {
    JackBasedToolchain jack = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File jackOut = AbstractTestTools.createTempDir();
    File processors = getAutoProcessors();
    jack.addAnnotationProcessorOption(
        SourceAnnotationProcessor.SOURCE_ANNOTATION_PROCESSOR_SUFFIX, "WithOption");
    jack.addToClasspath(jack.getDefaultBootClasspath());
    jack.addToClasspath(processors);
    jack.srcToLib(jackOut, /*zipFiles=*/false,
        ANNOTATIONS_DIR,
        ANNOTATED_DIR
            );
    InputJackLibrary libOut =
        JackLibraryFactory.getInputLibrary(
            new DirectFS(new Directory(jackOut.getPath(), /* hooks = */ null,
                Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE),
                Permission.READ));
    libOut.getFile(FileType.JAYCE, new VPath("Annotated2WithOption", '/'));
  }
}
