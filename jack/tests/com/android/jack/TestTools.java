/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack;

import com.android.jack.backend.dex.DexInLibraryProduct;
import com.android.jack.backend.jayce.JayceInLibraryProduct;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.formatter.MethodFormatter;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.lookup.JMethodSignatureLookupException;
import com.android.jack.scheduling.feature.DropMethodBody;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.test.TestsProperties;
import com.android.jack.util.TextUtils;
import com.android.jack.util.filter.SignatureMethodFilter;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.vfs.CachedDirectFS;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tools used by tests.
 */
public class TestTools {

  @Nonnull
  private static final String JACK_UNIT_TESTS_PATH = "jack/tests/";

  @Nonnull
  private static final String JACK_TESTS_PATH = "jack-tests/tests/";

  @Nonnull
  private static final String JACK_PACKAGE = "com/android/jack/";

  public static class ReferenceCompilerFiles {
    @Nonnull
    public File jarFile;
    @Nonnull
    public File dexFile;
    public ReferenceCompilerFiles(@Nonnull File jarFile, @Nonnull File dexFile){
      this.jarFile = jarFile;
      this.dexFile = dexFile;
    }
  }

  @Nonnull
  public static JMethod getMethod(@Nonnull JDefinedClassOrInterface declaringClass,
      @Nonnull final String signature) throws JMethodSignatureLookupException {
    MethodFormatter formatter = Jack.getLookupFormatter();
    for (JMethod m : declaringClass.getMethods()) {
      if (formatter.getName(m).equals(signature)) {
        return m;
      }
    }
    throw new JMethodSignatureLookupException(declaringClass, signature);
  }

  @Nonnull
  public static File getJackTestsWithJackFolder(@Nonnull String testName) {
    return new File(getJackTestFolder(testName), "jack");
  }

  @Nonnull
  public static File getJackTestFolder(@Nonnull String testName) {
    return new File(TestsProperties.getJackRootDir(),
        JACK_TESTS_PATH + JACK_PACKAGE + testName);
  }

  @Nonnull
  public static File getJackTestFromBinaryName(@Nonnull String signature) {
    return new File(TestsProperties.getJackRootDir(), JACK_TESTS_PATH +
        signature + ".java");
  }

  @Nonnull
  public static File getJackUnitTestFromBinaryName(@Nonnull String signature) {
    return new File(TestsProperties.getJackRootDir(),
        JACK_UNIT_TESTS_PATH + signature + ".java");
  }

  @Nonnull
  public static File getArtTestFolder(@Nonnull String testName) {
    return getFromAndroidTree("art/test/" + testName + "/src");
  }

  @Nonnull
  public static Sourcelist getSourcelistWithAbsPath(@Nonnull String fileName) {
    File sourcelist = new File(TestsProperties.getAndroidRootDir(), fileName);

    if (!sourcelist.exists()) {
      throw new AssertionError("Failed to locate sourcelist for \"" + fileName + "\".");
    }
    try {
      File fileWithAbsPath = TestTools.createTempFile("tmpSourceList", "txt");
      Sourcelist sourcelistWithAbsPath =
          new Sourcelist(fileWithAbsPath);
      BufferedWriter outBr = new BufferedWriter(new FileWriter(sourcelistWithAbsPath));
      BufferedReader inBr = new BufferedReader(new FileReader(sourcelist));

      String line;
      while ((line = inBr.readLine()) != null) {
        outBr.write(TestsProperties.getAndroidRootDir().getPath() + File.separator + line
            + TextUtils.LINE_SEPARATOR);
      }

      outBr.close();
      inBr.close();

      return sourcelistWithAbsPath;
    } catch (IOException e) {
      throw new AssertionError("Failed to build sourcelist for \"" + fileName + "\".");
    }
  }

  @Nonnull
  public static Sourcelist getTargetLibSourcelist(@Nonnull String moduleName) {
    return getSourcelistWithAbsPath("out/target/common/obj/JAVA_LIBRARIES/" + moduleName
        + "_intermediates/jack.java-source-list");
  }

  @Nonnull
  public static Sourcelist getHostLibSourcelist(@Nonnull String moduleName) {
    return getSourcelistWithAbsPath("out/host/common/obj/JAVA_LIBRARIES/" + moduleName
        + "_intermediates/jack.java-source-list");
  }

  @Nonnull
  public static File getFromAndroidTree(@Nonnull String filePath) {
    File sourceFile = new File(TestsProperties.getAndroidRootDir(), filePath);
    if (!sourceFile.exists()) {
      throw new AssertionError("Failed to locate file \"" + filePath + "\".");
    }
    return sourceFile;
  }

  public static void getJavaFiles(@Nonnull File fileObject, @Nonnull List<File> filePaths) throws IOException {
    if (fileObject.isDirectory()) {
      File allFiles[] = fileObject.listFiles();
      for (File aFile : allFiles) {
        getJavaFiles(aFile, filePaths);
      }
    } else if (fileObject.isFile() && fileObject.getName().endsWith(".java")) {
      filePaths.add(fileObject.getCanonicalFile());
    }
  }

  @Nonnull
  public static String getDefaultClasspathString() {
    return new File(TestsProperties.getJackRootDir(),
        "jack-tests/prebuilts/core-stubs-mini.jack").getAbsolutePath();
  }

  @CheckForNull
  public static String getClasspathAsString(@CheckForNull File[] files) {
    if (files == null || files.length == 0) {
      return null;
    }
    StringBuilder classpathStr = new StringBuilder();
    for (int i = 0; i < files.length; i++) {
      classpathStr.append(files[i].getAbsolutePath());
      if (i != files.length -1) {
        classpathStr.append(File.pathSeparatorChar);
      }
    }
    return classpathStr.toString();
  }

  @CheckForNull
  public static String getClasspathsAsString(
      @CheckForNull File[] bootClasspath, @CheckForNull File[] classpath) {
    if (bootClasspath == null) {
      return getClasspathAsString(classpath);
    } else if (classpath == null) {
      return getClasspathAsString(bootClasspath);
    } else {
      return concatClasspathStrings(
          getClasspathAsString(bootClasspath), getClasspathAsString(classpath));
    }
  }

  @CheckForNull
  private static String concatClasspathStrings(
      @CheckForNull String bootclasspath, @CheckForNull String classpath) {
    if (bootclasspath == null || bootclasspath.isEmpty()) {
      return classpath;
    } else if (classpath == null || classpath.isEmpty()) {
      return bootclasspath;
    } else {
      StringBuilder classpathStr = new StringBuilder(bootclasspath);
      classpathStr.append(File.pathSeparatorChar);
      classpathStr.append(classpath);
      return classpathStr.toString();
    }
  }

  @Nonnull
  protected static List<String> buildEcjArgs() {
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add("-nowarn");

    return ecjArgs;
  }

  protected static void addFile(@Nonnull File fileOrSourceList, @Nonnull List<String> args) {
    if (fileOrSourceList instanceof Sourcelist) {
      args.add("@" + fileOrSourceList.getAbsolutePath());
    } else {
      List<File> sourceFiles = new ArrayList<File>();
      try {
        getJavaFiles(fileOrSourceList, sourceFiles);
      } catch (IOException e) {
      }
      for (File sourceFile : sourceFiles) {
        args.add(sourceFile.getAbsolutePath());
      }
    }
  }

  @Nonnull
  public static Options buildCommandLineArgs(@Nonnull File fileOrSourcelist) throws IOException {
    return buildCommandLineArgs(fileOrSourcelist, null);
  }

  @Nonnull
  public static Options buildCommandLineArgs(@Nonnull File fileOrSourcelist,
      @CheckForNull File jarjarRules) throws IOException {
    Options options = buildCommandLineArgs(null /* classpath */, new File[] {fileOrSourcelist});
    options.jarjarRulesFile = jarjarRules;

    return options;
  }

  @Nonnull
  public static Options buildCommandLineArgs(@Nonnull File[] filesOrSourcelists)
      throws IOException {
    return buildCommandLineArgs(null /* classpath */, filesOrSourcelists);
  }

  @Nonnull
  public static Options buildCommandLineArgs(@CheckForNull File[] classpath,
      @Nonnull File fileOrSourcelist) throws IOException {
    return buildCommandLineArgs(classpath, new File[] {fileOrSourcelist});
  }

  @Nonnull
  public static Options buildCommandLineArgs(@CheckForNull File[] classpath,
      @Nonnull File[] filesOrSourcelists) throws IOException {
    Options options = new Options();

    String classpathStr = getDefaultClasspathString();
    classpathStr += File.pathSeparatorChar;

    if (classpath != null && classpath.length != 0) {
      classpathStr += getClasspathAsString(classpath);
    }

    options.classpath = classpathStr;

    List<String> ecjArgs = buildEcjArgs();
    for (File file : filesOrSourcelists) {
      addFile(file, ecjArgs);
    }
    options.ecjArguments = ecjArgs;
    options.setOutputDir(TestTools.createTempDir("test", "dex"));

    return options;
  }

  @Nonnull
  public static JSession buildJAst(@Nonnull Options options) throws Exception {
    RunnableHooks hooks = new RunnableHooks();
    try {
      options.checkValidity(hooks);
      ThreadConfig.setConfig(options.getConfig());

      JSession session = Jack.buildSession(options, hooks);

      return (session);
    } finally {
      hooks.runHooks();
    }
  }

  /**
   * Build a {@code JSession} by using the monolithic plan.
   */
  @Nonnull
  public static JSession buildSession(@Nonnull Options options) throws Exception {
    RunnableHooks hooks = new RunnableHooks();
    try {
      return buildSession(options, hooks);
    } finally {
      hooks.runHooks();
    }

  }

  /**
   * Build a {@code JSession} by using the monolithic plan.
   */
  @Nonnull
  public static JSession buildSession(@Nonnull Options options, @Nonnull RunnableHooks hooks)
      throws Exception {
    if (options.proguardFlagsFiles != null && !options.proguardFlagsFiles.isEmpty()) {
      if (options.flags == null) {
        options.flags = new Flags();
      }
      for (File proguardFlagsFile : options.proguardFlagsFiles) {
        GrammarActions.parse(proguardFlagsFile.getAbsolutePath(), ".", options.flags);
      }
      options.applyShrobFlags();
    }

    options.checkValidity(hooks);
    ThreadConfig.setConfig(options.getConfig());

    JSession session = Jack.buildSession(options, hooks);

    Request request = Jack.createInitialRequest();
    request.addInitialTagsOrMarkers(Jack.getJavaSourceInitialTagSet());
    request.addProduction(DexInLibraryProduct.class);
    if (ThreadConfig.get(Options.GENERATE_JAYCE_IN_LIBRARY).booleanValue()) {
      request.addProduction(JayceInLibraryProduct.class);
    }

    if (options.getConfig().get(Options.DROP_METHOD_BODY).booleanValue()) {
      request.addFeature(DropMethodBody.class);
    }

    OutputJackLibrary outputLibrary = null;
    try {
      outputLibrary = JackLibraryFactory.getOutputLibrary(new CachedDirectFS(new Directory(
          TestTools.createTempDir("unused", "").getPath(), hooks, Existence.MUST_EXIST,
          Permission.READ | Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE),
          Jack.getEmitterId(), Jack.getVersionString());
      session.setJackOutputLibrary(outputLibrary);

      PlanBuilder<JSession> planBuilder = request.getPlanBuilder(JSession.class);
      Jack.fillDexPlan(options, planBuilder);
      request.addTargetIncludeTagOrMarker(ClassDefItemMarker.Complete.class);

      planBuilder.getPlan().getScheduleInstance().process(session);
    } finally {
      if (outputLibrary != null) {
        outputLibrary.close();
      }
    }

    return (session);
  }

  @Nonnull
  public static JMethod getJMethodWithRejectAllFilter(@Nonnull File fileName,
      @Nonnull String className, @Nonnull String methodSignature) throws Exception {
    Options commandLineArgs = TestTools.buildCommandLineArgs(fileName);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");
    commandLineArgs.addProperty(Options.DROP_METHOD_BODY.getName(), "false");
    JSession session = TestTools.buildSession(commandLineArgs);
    Assert.assertNotNull(session);

    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(className);
    Assert.assertNotNull(type);

    JMethod foundMethod = null;
    foundMethod = getMethod(type, methodSignature);

    Assert.assertNotNull(foundMethod);

    return foundMethod;
  }

  @Nonnull
  public static JMethod getJMethodWithSignatureFilter(@Nonnull File fileName,
      @Nonnull String className, @Nonnull String methodSignature) throws Exception {
    Options commandLineArgs = TestTools.buildCommandLineArgs(fileName);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    commandLineArgs.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        methodSignature);
    commandLineArgs.addProperty(Options.DROP_METHOD_BODY.getName(), "false");
    JSession session = TestTools.buildSession(commandLineArgs);
    Assert.assertNotNull(session);

    JDefinedClassOrInterface type =
        (JDefinedClassOrInterface) session.getLookup().getType(className);
    Assert.assertNotNull(type);

    JMethod foundMethod = null;
    foundMethod = getMethod(type, methodSignature);

    Assert.assertNotNull(foundMethod);

    return foundMethod;
  }

  public static File createTempDir(String prefix, String suffix) throws IOException {
    final File tmp = File.createTempFile(prefix, suffix);
    if (!tmp.delete()) {
      throw new IOException("Failed to delete file " + tmp.getAbsolutePath());
    }
    if (!tmp.mkdirs()) {
      throw new IOException("Failed to create folder " + tmp.getAbsolutePath());
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          FileUtils.deleteDir(tmp);
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
      }
    });
    return tmp;
  }

  public static File createTempFile(String prefix, String suffix) throws IOException {
    File tmp = File.createTempFile(prefix, suffix);
    tmp.deleteOnExit();
    return tmp;
  }

  @Nonnull
  public static EncodedMethod getEncodedMethod(@Nonnull org.jf.dexlib.DexFile dexFile,
      @Nonnull String typeSig, @Nonnull String methodName, @Nonnull String methodSig) {
    for (ClassDefItem classDef : dexFile.ClassDefsSection.getItems()) {
      if (classDef.getClassType().getTypeDescriptor().equals(typeSig)) {
        ClassDataItem classData = classDef.getClassData();
        for (EncodedMethod em : classData.getDirectMethods()) {
          if (em.method.getMethodName().getStringValue().equals(methodName)
              && em.method.getPrototype().getPrototypeString().equals(methodSig)) {
            return em;
          }
        }
        for (EncodedMethod em : classData.getVirtualMethods()) {
          if (em.method.getMethodName().getStringValue().equals(methodName)
              && em.method.getPrototype().getPrototypeString().equals(methodSig)) {
            return em;
          }
        }
      }
    }
    throw new AssertionError("Encoded method not found.");
  }
}
