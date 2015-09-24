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

package com.android.jack.test.helper;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is used to write runtime tests.
 */
public class RuntimeTestHelper {

  @Nonnull
  private List<String> jUnitClasses = new ArrayList<String>(1);

  @Nonnull
  private List<Class<? extends IToolchain>> ignoredCandidateToolchains =
      new ArrayList<Class<? extends IToolchain>>(0);

  @Nonnull
  RuntimeTestInfo[] runtimeTestInfos;

  private boolean withDebugInfos = false;

  private SourceLevel level = SourceLevel.JAVA_6;

  @Nonnull
  private List<FileChecker> testExeCheckers = new ArrayList<FileChecker>(0);

  @Nonnull
  private Map<String, String> runtimeProperties = new HashMap<String, String>(0);

  public RuntimeTestHelper(@Nonnull RuntimeTestInfo... rtTestInfos) {
    runtimeTestInfos = Arrays.copyOf(rtTestInfos, rtTestInfos.length);

    for (RuntimeTestInfo info : rtTestInfos) {
      jUnitClasses.add(info.jUnit);
    }
  }

  @Nonnull
  public RuntimeTestHelper addIgnoredCandidateToolchain(Class<? extends IToolchain> toolchain) {
    ignoredCandidateToolchains.add(toolchain);
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setWithDebugInfos(boolean withDebugInfos) {
    this.withDebugInfos = withDebugInfos;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setSourceLevel(@Nonnull SourceLevel level) {
    this.level = level;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper addTestExeFileChecker(@Nonnull FileChecker checker) {
    this.testExeCheckers.add(checker);
    return this;
  }

  @Nonnull
  public RuntimeTestHelper addProperty(@Nonnull String key, @Nonnull String value) {
    String oldValue = runtimeProperties.get(key);
    if (oldValue != null && !oldValue.equals(value)) {
      throw new AssertionError("property: " + key + " with value: " + oldValue
          + " is already defined");
    }
    runtimeProperties.put(key, value);
    return this;
  }

  @Nonnull
  List<File> getReferenceExtraSources() {
    List<File> result = new ArrayList<File>();
    for (RuntimeTestInfo info : runtimeTestInfos) {
      result.addAll(info.referenceExtraSources);
    }
    return result;
  }


  public void compileAndRunTest() throws Exception {
    compileAndRunTest(/* checkStructure = */ false);
  }

  public void compileAndRunTest(boolean checkStructure) throws Exception {
    Properties testProperties = new Properties();
    try {
      loadTestProperties(testProperties);
    } catch (FileNotFoundException e) {
      // No file, no pb
    }

    AndroidToolchain candidateTestTools = createCandidateToolchain();
    AndroidToolchain referenceTestTools = createReferenceToolchain();

    File[] candidateBootClasspath = candidateTestTools.getDefaultBootClasspath();
    File[] referenceBootClasspath = referenceTestTools.getDefaultBootClasspath();

    String candidateBootClasspathAsString =
        AbstractTestTools.getClasspathAsString(candidateTestTools.getDefaultBootClasspath());
    String referenceBootClasspathAsString =
        AbstractTestTools.getClasspathAsString(referenceTestTools.getDefaultBootClasspath());

    // Compile lib src
    File libLibRef = null;
    File libBinaryRef = null;
    File libLibCandidate = null;
    File[] libSources = getLibSrc();
    if (libSources.length != 0) {
      libLibRef =
          AbstractTestTools.createTempFile("lib-ref", referenceTestTools.getLibraryExtension());
      File libBinaryRefDir = AbstractTestTools.createTempDir();
      libBinaryRef = new File(libBinaryRefDir, referenceTestTools.getBinaryFileName());
      referenceTestTools.addToClasspath(referenceBootClasspath)
      .srcToLib(libLibRef, /* zipFiles = */ true, libSources);
      referenceTestTools.libToExe(libLibRef, libBinaryRefDir, /* zipFile */ false);

      libLibCandidate = AbstractTestTools.createTempFile("lib-candidate",
          candidateTestTools.getLibraryExtension());
      candidateTestTools.addToClasspath(candidateBootClasspath)
      .srcToLib(libLibCandidate, /* zipFiles = */ true, libSources);
    }

    // Compile test src
    candidateTestTools = createCandidateToolchain();

    String candidateClasspathAsString;
    String referenceClasspathAsString;
    File[] candidateClassPath;
    File[] referenceClasspath;
    if (libSources.length != 0) {
      candidateClassPath = new File[candidateBootClasspath.length + 1];
      System.arraycopy(candidateBootClasspath, 0, candidateClassPath, 0,
          candidateBootClasspath.length);
      candidateClassPath[candidateClassPath.length - 1] = libLibCandidate;
      candidateClasspathAsString = AbstractTestTools.getClasspathAsString(candidateClassPath);
      referenceClasspath = new File[referenceBootClasspath.length + 1];
      System.arraycopy(referenceBootClasspath, 0, referenceClasspath, 0,
          referenceBootClasspath.length);
      referenceClasspath[referenceClasspath.length - 1] = libLibRef;
      referenceClasspathAsString = AbstractTestTools.getClasspathAsString(referenceClasspath);
    } else {
      candidateClassPath = candidateBootClasspath;
      referenceClasspath = referenceBootClasspath;
      candidateClasspathAsString = candidateBootClasspathAsString;
      referenceClasspathAsString = referenceBootClasspathAsString;
    }

    File jarjarRules = getJarjarRules();
    List<File> proguargFlags = getProguardFlags();

    File testBinaryDir = AbstractTestTools.createTempDir();
    File testBinary = new File(testBinaryDir, candidateTestTools.getBinaryFileName());

    if (checkStructure) {
      CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(getSrcDir());
      helper.setCandidateClasspath(candidateClassPath);
      helper.setCandidateTestTools(candidateTestTools);
      if (jarjarRules != null) {
        helper.setJarjarRulesFile(jarjarRules);
      }
      helper.setProguardFlags(proguargFlags.toArray(new File[proguargFlags.size()]));
      helper.compare();
      Files.copy(helper.getCandidateDex(),
          new File(testBinaryDir, helper.getCandidateDex().getName()));
    } else {
      if (jarjarRules != null) {
        candidateTestTools.setJarjarRules(jarjarRules);
      }
      candidateTestTools.addProguardFlags(proguargFlags.toArray(new File [proguargFlags.size()]));
      candidateTestTools.addToClasspath(candidateClassPath)
      .srcToExe(testBinaryDir, /* zipFile = */ false,
          getSrcDir());
    }

    for (FileChecker checker : testExeCheckers) {
      checker.check(testBinary);
    }

    // Compile link src
    candidateTestTools = createCandidateToolchain();

    File linkBinary = null;
    if (getLinkSrc().length != 0) {
      File linkBinaryDir = AbstractTestTools.createTempDir();
      linkBinary = new File(linkBinaryDir, candidateTestTools.getBinaryFileName());
      candidateTestTools.setJarjarRules(jarjarRules);
      candidateTestTools.addProguardFlags(proguargFlags.toArray(new File [proguargFlags.size()]));
      candidateTestTools.addToClasspath(candidateBootClasspath)
      .srcToExe(linkBinaryDir, /* zipFile = */ false, getLinkSrc());
    }

    // Compile ref part src
    File [] refSources = getRefSrcDir();
    List<File> extras = getReferenceExtraSources();
    List<File> sources = new ArrayList<File>(extras.size() + refSources.length);
    Collections.addAll(sources, refSources);
    sources.addAll(extras);

    File refPartBinaryDir = null;
    File refPartBinary = null;
    if (sources.size() > 0) {
      referenceTestTools = createReferenceToolchain();
      File testLib =
          AbstractTestTools.createTempFile("testRef", referenceTestTools.getLibraryExtension());
      referenceTestTools.addToClasspath(referenceClasspath)
      .srcToLib(testLib, /* zipFiles = */ true,
          getSrcDir());

      List<File> referenceClasspathAsList = new ArrayList<File>();
      for (File f : referenceBootClasspath) {
        referenceClasspathAsList.add(f);
      }
      if (libLibRef != null) {
        referenceClasspathAsList.add(libLibRef);
      }
      if (testLib != null) {
        referenceClasspathAsList.add(testLib);
      }

      referenceClasspath =
          referenceClasspathAsList.toArray(new File[referenceClasspathAsList.size()]);

      referenceTestTools = createReferenceToolchain();
      refPartBinaryDir = AbstractTestTools.createTempDir();
      refPartBinary = new File(refPartBinaryDir, referenceTestTools.getBinaryFileName());
      referenceTestTools.addToClasspath(referenceClasspath)
      .srcToExe(
          refPartBinaryDir,
          /* zipFile = */ false,
          sources.toArray(new File[sources.size()]));
    }

    List<File> rtClasspath = new ArrayList<File>();
    rtClasspath.add(new File(TestsProperties.getJackRootDir(),
        "jack-tests/prebuilts/core-hostdex.jar"));
    rtClasspath.add(new File(TestsProperties.getJackRootDir(),
        "jack-tests/prebuilts/junit4-hostdex.jar"));
   if (refPartBinary != null) {
      rtClasspath.add(refPartBinary);
    }
    if (linkBinary != null) {
      rtClasspath.add(linkBinary);
    }
    if (testBinary != null) {
      rtClasspath.add(testBinary);
    }
    if (libBinaryRef != null) {
      rtClasspath.add(libBinaryRef);
    }

    // Run JUnit on runtime env(s)
    runOnRuntimeEnvironments(jUnitClasses, testProperties,
        rtClasspath.toArray(new File[rtClasspath.size()]));
  }

  @Nonnull
  private AndroidToolchain createCandidateToolchain() {
    AndroidToolchain candidateTestTools =
        AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, ignoredCandidateToolchains);
    candidateTestTools.setSourceLevel(level);
    candidateTestTools.setWithDebugInfos(withDebugInfos);
    if (!runtimeProperties.isEmpty() && candidateTestTools instanceof JackBasedToolchain) {
      // if the tool chain is type of JackBasedToolchain and the customized properties are set,
      // configure the runtime testing properties
      JackBasedToolchain jackBasedToolchain = (JackBasedToolchain) candidateTestTools;
      for (Map.Entry<String, String> entry : runtimeProperties.entrySet()) {
        jackBasedToolchain.addProperty(entry.getKey(), entry.getValue());
      }
    }
    return candidateTestTools;
  }

  @Nonnull
  private AndroidToolchain createReferenceToolchain() {
    AndroidToolchain referenceTestTools =
        AbstractTestTools.getReferenceToolchain(AndroidToolchain.class);
    referenceTestTools.setSourceLevel(level);
    referenceTestTools.setWithDebugInfos(withDebugInfos);
    if (!runtimeProperties.isEmpty() && referenceTestTools instanceof JackBasedToolchain) {
      // if the tool chain is type of JackBasedToolchain and the customized properties are set,
      // configure the runtime testing properties
      JackBasedToolchain jackBasedToolchain = (JackBasedToolchain) referenceTestTools;
      for (Map.Entry<String, String> entry : runtimeProperties.entrySet()) {
        jackBasedToolchain.addProperty(entry.getKey(), entry.getValue());
      }
    }
    return referenceTestTools;
  }

  private static void runOnRuntimeEnvironments(@Nonnull List<String> jUnitClasses,
      @Nonnull Properties testProperties, @Nonnull File... classpathFiles) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(testProperties);
    String[] names = jUnitClasses.toArray(new String[jUnitClasses.size()]);
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(0, runner.runJUnit(
          getRuntimeArgs(runner.getClass().getSimpleName(), testProperties),
          AbstractTestTools.JUNIT_RUNNER_NAME, names, classpathFiles));
    }
  }

  @Nonnull
  private static final String[] getRuntimeArgs(@Nonnull String rtName,
      @Nonnull Properties properties) {

    String args = properties.getProperty("rt.args." + rtName);
    List<String> result = new ArrayList<String>(0);
    if (args != null) {
      for (String arg : Splitter.on(CharMatcher.WHITESPACE).split(args)) {
        result.add(arg);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  @Nonnull
  public static final String[] getRuntimeArgs(@Nonnull String rtName, @Nonnull File rtArgsFile)
      throws FileNotFoundException, IOException {
    Properties properties = new Properties();
    loadTestProperties(rtArgsFile, properties);
    return getRuntimeArgs(rtName, properties);
  }

  private void loadTestProperties(@Nonnull Properties properties) throws FileNotFoundException,
      IOException {
    for (RuntimeTestInfo info : runtimeTestInfos) {
      File propertyFile = new File(info.directory, info.propertyFileName);
      if (propertyFile.exists()) {
        if (runtimeTestInfos.length > 1) {
          throw new AssertionError("Not a regression test: " + info.directory.getAbsolutePath());
        }
        loadTestProperties(propertyFile, properties);
        break;
      }
    }
  }

  private static void loadTestProperties(@Nonnull File propertyFile, @Nonnull Properties properties)
      throws FileNotFoundException, IOException {
    FileInputStream fis = new FileInputStream(propertyFile);
    try {
      properties.load(fis);
    } finally {
      fis.close();
    }
  }

  @Nonnull
  private File[] getSrcDir() {
    List<File> result = new ArrayList<File>();
    for (RuntimeTestInfo info : runtimeTestInfos) {
      File f = new File(info.directory, info.srcDirName);
      if (f.exists()) {
        result.add(f);
      }
    }
    return result.toArray(new File[result.size()]);
  }

  @Nonnull
  private File[] getLibSrc() {
    List<File> result = new ArrayList<File>();
    for (RuntimeTestInfo info : runtimeTestInfos) {
      File f = new File(info.directory, info.libDirName);
      if (f.exists()) {
        result.add(f);
      }
    }
    return result.toArray(new File[result.size()]);
  }

  @Nonnull
  private File[] getLinkSrc() {
    List<File> result = new ArrayList<File>();
    for (RuntimeTestInfo info : runtimeTestInfos) {
      File f = new File(info.directory, info.linkDirName);
      if (f.exists()) {
        if (runtimeTestInfos.length > 1) {
          // TODO(jmhenaff): check it's necessary
          throw new AssertionError("Not a regression test: " + info.directory.getAbsolutePath());
        }
        result.add(f);
      }
    }
    return result.toArray(new File[result.size()]);
  }

  @Nonnull
  private File[] getRefSrcDir() {
    List<File> result = new ArrayList<File>();
    for (RuntimeTestInfo info : runtimeTestInfos) {
      File f = new File(info.directory, info.refDirName);
      if (f.exists()) {
        result.add(f);
      }
    }
    return result.toArray(new File[result.size()]);
  }

  @CheckForNull
  private File getJarjarRules() {
    for (RuntimeTestInfo info : runtimeTestInfos) {
      File f = new File(info.directory, info.jarjarRulesFileName);
      if (f.exists()) {
        if (runtimeTestInfos.length > 1) {
          throw new AssertionError("Not a regression test: " + info.directory.getAbsolutePath());
        }
        return f;
      }
    }
    return null;
  }

  @Nonnull
  private List<File> getProguardFlags() {
    List<File> result = new ArrayList<File>();
    for (RuntimeTestInfo info : runtimeTestInfos) {
      for (String name : info.proguardFilesNames) {
        File f = new File(info.directory, name);
        if (f.exists()) {
          if (runtimeTestInfos.length > 1) {
            throw new AssertionError("Not a regression test: " + info.directory.getAbsolutePath());
          }
          result.add(f);
        }
      }
    }
    return result;
  }

}
