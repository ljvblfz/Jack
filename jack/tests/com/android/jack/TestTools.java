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

import com.android.dx.command.dexer.Main.Arguments;
import com.android.jack.Options.VerbosityLevel;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.IntermediateDexProduct;
import com.android.jack.backend.jayce.JayceFormatProduct;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.formatter.MethodFormatter;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.lookup.JMethodSignatureLookupException;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.shrob.ListingComparator;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.shrink.ShrinkStructurePrinter;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.util.ExecuteFile;
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
import com.android.sched.vfs.DirectVFS;

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
  private static final String JACK_UNIT_TESTS_PATH = "toolchain/jack/jack/tests/";

  @Nonnull
  private static final String JACK_TESTS_PATH = "toolchain/jack/jack-tests/tests/";

  @Nonnull
  private static final String JACK_PACKAGE = "com/android/jack/";

  private static class ExternalTools {
    @Nonnull
    private static final File JARJAR = getFromAndroidTree("out/host/linux-x86/framework/jarjar.jar");

    @Nonnull
    private static final File PROGUARD = getFromAndroidTree("prebuilts/tools/common/proguard/proguard4.7/lib/proguard.jar");
  }

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
    return getFromAndroidTree(JACK_TESTS_PATH + JACK_PACKAGE + testName + "/jack");
  }

  @Nonnull
  public static File getJackTestFolder(@Nonnull String testName) {
    return getFromAndroidTree(JACK_TESTS_PATH + JACK_PACKAGE + testName);
  }

  @Nonnull
  public static File getJackTestFromBinaryName(@Nonnull String signature) {
    return getFromAndroidTree(JACK_TESTS_PATH + signature + ".java");
  }

  @Nonnull
  public static File getJackTestLibFolder(@Nonnull String testName) {
    return getFromAndroidTree(JACK_TESTS_PATH + JACK_PACKAGE + testName + "/lib");
  }

  @Nonnull
  public static File getJackUnitTestSrc(@Nonnull String path) {
    return getFromAndroidTree(JACK_UNIT_TESTS_PATH + path);
  }

  @Nonnull
  public static File getJackUnitTestFromBinaryName(@Nonnull String signature) {
    return getFromAndroidTree(JACK_UNIT_TESTS_PATH + signature + ".java");
  }

  @Nonnull
  public static File getOpcodeTestFolder(@Nonnull String testName) {
    return getFromAndroidTree(JACK_TESTS_PATH + JACK_PACKAGE + "opcodes/" + testName + "/jm");
  }

  @Nonnull
  public static File getArtTestFolder(@Nonnull String testName) {
    return getFromAndroidTree("art/test/" + testName + "/src");
  }

  @Nonnull
  public static Sourcelist getSourcelistWithAbsPath(@Nonnull String fileName) {
    File sourcelist = new File(getAndroidTop(), fileName);

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
        outBr.write(getAndroidTop() + File.separator + line + TextUtils.LINE_SEPARATOR);
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
  public static Sourcelist getTargetAppSourcelist(@Nonnull String moduleName) {
    return getSourcelistWithAbsPath("out/target/common/obj/APPS/" + moduleName
        + "_intermediates/jack.java-source-list");
  }

  @Nonnull
  public static File getFromAndroidTree(@Nonnull String filePath) {
    File sourceFile = new File(getAndroidTop(), filePath);
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

  public static void runCompilation(@Nonnull Options compilerArgs) throws Exception {
    compilerArgs.verbose = VerbosityLevel.WARNING;
    Jack.run(compilerArgs);
  }

  public static void compileSourceToJack(
      Options options, File sourceFolderOrSourceList, String classpath, File out, boolean zip)
      throws Exception {
    compileSourceToJack(options, sourceFolderOrSourceList, classpath, out, zip, false);
  }

  public static void compileSourceToJack(
      Options options, File sourceFolderOrSourceList, String classpath, File out, boolean zip,
      boolean withDebugInfos) throws Exception {
    options.classpath = classpath;
    if (zip) {
      options.libraryOutZip = out;
    } else {
      options.libraryOutDir = out;
    }
    options.ecjArguments = buildEcjArgs();
    addFile(sourceFolderOrSourceList, options.ecjArguments);
    options.emitLocalDebugInfo = withDebugInfos;
    Jack.run(options);
  }

  public static void compileJackToDex(
      Options options, File in, File out, boolean zip) throws Exception {
    options.jayceImport = new ArrayList<File>(1);
    options.jayceImport.add(in);
    if (zip) {
      options.outZip = out;
    } else {
      options.out = out;
    }
    Jack.run(options);
  }

  public static void shrobJackToJack(Options options,
      File in,
      String classpath,
      File out,
      List<ProguardFlags> flagFiles,
      boolean zip) throws Exception {
    options.jayceImport = new ArrayList<File>(1);
    options.jayceImport.add(in);
    options.classpath = classpath;
    if (zip) {
      options.libraryOutZip = out;
    } else {
      options.libraryOutDir = out;
    }
    options.proguardFlagsFiles = new ArrayList<File>();
    for (ProguardFlags flagFile : flagFiles) {
      options.proguardFlagsFiles.add(flagFile);
    }
    Jack.run(options);
  }

  public static void compileSourceToDex(@Nonnull Options options,
      @Nonnull File sourceFolderOrSourceList,
      @CheckForNull String classpath,
      @Nonnull File out,
      boolean zip) throws Exception {
    compileSourceToDex(options,
        sourceFolderOrSourceList,
        classpath,
        out,
        zip,
        null,
        null,
        false);
  }

  public static void compileSourceToDex(@Nonnull Options options,
      @Nonnull File sourceFolderOrSourceList,
      @CheckForNull String classpath,
      @Nonnull File out,
      boolean zip,
      @CheckForNull JarJarRules jarjarRules,
      @CheckForNull ProguardFlags[] flagFiles,
      boolean withDebugInfo) throws Exception {
    options.ecjArguments = buildEcjArgs();
    addFile(sourceFolderOrSourceList, options.ecjArguments);
    options.classpath = classpath;
    if (zip) {
      options.outZip = out;
    } else {
      options.out = out;
    }
    options.jarjarRulesFile = jarjarRules;
    if (flagFiles != null) {
      options.proguardFlagsFiles = new ArrayList<File>();
      for (ProguardFlags flagFile : flagFiles) {
        options.proguardFlagsFiles.add(flagFile);
      }
    }
    options.emitLocalDebugInfo = withDebugInfo;
    Jack.run(options);
  }

  public static void jarjarJackToJack(Options options,
      File in,
      String classpath,
      File out,
      File jarjarRules,
      boolean zip) throws Exception {
    options.jayceImport = new ArrayList<File>(1);
    options.jayceImport.add(in);
    options.classpath = classpath;
    if (zip) {
      options.libraryOutZip = out;
    } else {
      options.libraryOutDir = out;
    }
    options.jarjarRulesFile = jarjarRules;
    Jack.run(options);
  }

  @Nonnull
  public static File getDefaultDexBootclasspath() {
    return getFromAndroidTree(
        "out/host/common/obj/JAVA_LIBRARIES/core-hostdex_intermediates/classes.dex");
  }



  @Nonnull
  public static File[] getDefaultBootclasspath() {
    return new File[] {getFromAndroidTree(
        "toolchain/jack/jack/libs/core-stubs-mini.jack")};
  }

  @Nonnull
  public static String getDefaultBootclasspathString() {
    return getFromAndroidTree(
        "toolchain/jack/jack/libs/core-stubs-mini.jack")
          .getAbsolutePath();
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
    Options options = buildCommandLineArgs(null /* bootclasspath */, null /* classpath */,
        new File[]{fileOrSourcelist});
    options.jarjarRulesFile = jarjarRules;

    return options;
  }

  @Nonnull
  public static Options buildCommandLineArgs(@Nonnull File[] filesOrSourcelists)
      throws IOException {
    return buildCommandLineArgs(null /* bootclasspath */, null /* classpath */, filesOrSourcelists);
  }

  @Nonnull
  public static Options buildCommandLineArgs(
      @CheckForNull File[] bootclasspath, @CheckForNull File[] classpath,
      @Nonnull File fileOrSourceList, @CheckForNull ProguardFlags[] proguardFlagsFiles,
      boolean runDxOptimizations, boolean emitDebugInfo) throws IOException {
    Options options = buildCommandLineArgs(bootclasspath, classpath, fileOrSourceList);
    options.proguardFlagsFiles = new ArrayList<File>();
    options.emitLocalDebugInfo = emitDebugInfo;
    if (runDxOptimizations) {
      options.enableDxOptimizations();
    } else {
      options.disableDxOptimizations();
    }
    if (proguardFlagsFiles != null) {
      for (ProguardFlags proguardFlagsFile : proguardFlagsFiles) {
        options.proguardFlagsFiles.add(proguardFlagsFile);
      }
    }
    options.setOutputDir(TestTools.createTempDir("test", "dex"));
    return options;
  }

  @Nonnull
  public static Options buildCommandLineArgs(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath, @Nonnull File fileOrSourcelist) throws IOException {
    return buildCommandLineArgs(bootclasspath, classpath, new File[]{fileOrSourcelist});
  }
  @Nonnull
  public static Options buildCommandLineArgs(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath, @Nonnull File[] filesOrSourcelists) throws IOException {
    Options options = new Options();

    if (bootclasspath == null) {
      bootclasspath = getDefaultBootclasspath();
    }
    if (bootclasspath.length != 0) {
      String bootclasspathStr = getClasspathAsString(bootclasspath);
      assert bootclasspathStr != null;
      options.bootclasspath = bootclasspathStr;
    }

    if (classpath != null && classpath.length != 0) {
      String classpathStr = getClasspathAsString(classpath);
      assert classpathStr != null;
      options.classpath = classpathStr;
    }

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
    request.addProduction(IntermediateDexProduct.class);
    if (ThreadConfig.get(Options.GENERATE_JACK_LIBRARY).booleanValue()) {
      request.addProduction(JayceFormatProduct.class);
    }

    OutputJackLibrary outputLibrary = null;
    try {
      outputLibrary = JackLibraryFactory.getOutputLibrary(new DirectVFS(new Directory(
          TestTools.createTempDir("unused", "").getPath(), hooks, Existence.MUST_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE)), Jack.getEmitterId(),
          Jack.getVersionString());
      session.setJackInternalOutputLibrary(outputLibrary);

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

  public static void checkStructure(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      @Nonnull File fileOrSourceList,
      boolean withDebugInfo) throws Exception {
    checkStructure(bootclasspath,
        classpath,
        fileOrSourceList,
        withDebugInfo,
        false /* compareInstructionNumber */,
        0f,
        (JarJarRules) null,
        (ProguardFlags[]) null);
  }

  public static void checkStructure(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      @Nonnull File fileOrSourceList,
      boolean withDebugInfo,
      boolean compareInstructionNumber,
      float instructionNumberTolerance) throws Exception {
    checkStructure(bootclasspath,
        classpath,
        fileOrSourceList,
        withDebugInfo,
        compareInstructionNumber,
        instructionNumberTolerance,
        (JarJarRules) null,
        (ProguardFlags[]) null);
  }

  public static void checkStructure(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      @Nonnull File fileOrSourceList,
      boolean withDebugInfo,
      @CheckForNull ProguardFlags[] proguardFlagFiles) throws Exception {
    checkStructure(bootclasspath,
        classpath,
        fileOrSourceList,
        withDebugInfo,
        false,
        0f,
        (JarJarRules) null,
        proguardFlagFiles);
  }

  public static void checkStructure(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      @Nonnull File fileOrSourceList,
      boolean withDebugInfo,
      boolean compareInstructionNumber,
      float instructionNumberTolerance,
      @CheckForNull JarJarRules jarjarRules,
      @CheckForNull ProguardFlags[] proguardFlagFiles) throws Exception {
    checkStructure(new Options(),
        bootclasspath,
        classpath,
        /* refBootclasspath = */ null,
        /* refClasspath = */ null,
        fileOrSourceList,
        withDebugInfo,
        compareInstructionNumber,
        instructionNumberTolerance,
        jarjarRules,
        proguardFlagFiles);
  }

    public static void checkStructure(@Nonnull Options options,
        @CheckForNull File[] bootclasspath,
        @CheckForNull File[] classpath,
        @CheckForNull File[] refBootclasspath,
        @CheckForNull File[] refClasspath,
        @Nonnull File fileOrSourceList,
        boolean withDebugInfo,
        boolean compareInstructionNumber,
        float instructionNumberTolerance,
        @CheckForNull JarJarRules jarjarRules,
        @CheckForNull ProguardFlags[] proguardFlagFiles) throws Exception {

    boolean runDxOptimizations = !withDebugInfo;
    boolean useEcjAsRefCompiler = withDebugInfo;
    String classpathStr = getClasspathsAsString(bootclasspath, classpath);

    File jackDexFolder = TestTools.createTempDir("jack", "dex");

    if (runDxOptimizations) {
      options.enableDxOptimizations();
    } else {
      options.disableDxOptimizations();
    }

    compileSourceToDex(options,
        fileOrSourceList,
        classpathStr,
        jackDexFolder,
        false /* zip */,
        jarjarRules,
        proguardFlagFiles,
        withDebugInfo);

    Options refOptions = buildCommandLineArgs(refBootclasspath, refClasspath, fileOrSourceList);

    TestTools.compareDexToReference(jackDexFolder,
        refOptions,
        proguardFlagFiles,
        null,
        null,
        withDebugInfo,
        useEcjAsRefCompiler,
        compareInstructionNumber,
        instructionNumberTolerance,
        jarjarRules,
        false);
  }

  public static void runWithFlags(@Nonnull Options jackOptions,
      @CheckForNull File[] jackBootclasspath,
      @CheckForNull File[] jackClasspath,
      @Nonnull File fileOrSourceList,
      @CheckForNull Flags flags) throws Exception {
    jackOptions.flags = flags;
    if (flags != null) {
    jackOptions.applyShrobFlags();
    }
    jackOptions.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    File outFolder = TestTools.createTempDir("checklisting", "dex");
    TestTools.compileSourceToDex(jackOptions,
        fileOrSourceList,
        TestTools.getClasspathsAsString(jackBootclasspath, jackClasspath),
        outFolder,
        false /* zip */);
  }

  public static void checkListing(@CheckForNull File[] jackBootclasspath,
      @CheckForNull File[] jackClasspath,
      @Nonnull File fileOrSourceList,
      @CheckForNull ProguardFlags[] proguardFlags,
      @Nonnull File refNodeListing) throws Exception {
    Options options = new Options();
    File candidateNodeListing = TestTools.createTempFile("nodeListing", ".txt");
    options.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING.getName(), "true");
    options.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING_FILE.getName(),
        candidateNodeListing.getPath());
    options.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    options.disableDxOptimizations();

    File outFolder = TestTools.createTempDir("checklisting", "dex");
    TestTools.compileSourceToDex(options,
        fileOrSourceList,
        TestTools.getClasspathsAsString(jackBootclasspath, jackClasspath),
        outFolder,
        false /* zip */,
        null /* jarjarRules */,
        proguardFlags,
        true /* emitDebugInfo */);

    ListingComparator.compare(refNodeListing, candidateNodeListing);
  }

  public static void checkListingWhenMultiDex(@Nonnull Options options,
      @CheckForNull File[] jackBootclasspath,
      @CheckForNull File[] jackClasspath,
      @Nonnull File fileOrSourceList,
      @CheckForNull ProguardFlags[] proguardFlags,
      @Nonnull File refNodeListing) throws Exception {
    File candidateNodeListing = TestTools.createTempFile("nodeListing", ".txt");
    options.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING.getName(), "true");
    options.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING_FILE.getName(),
        candidateNodeListing.getPath());
    options.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    options.disableDxOptimizations();

    File out = TestTools.createTempFile("checklisting", ".zip");
    TestTools.compileSourceToDex(options,
        fileOrSourceList,
        TestTools.getClasspathsAsString(jackBootclasspath, jackClasspath),
        out,
        true /* zip */,
        null /* jarjarRules */,
        proguardFlags,
        true /* emitDebugInfo */);

    ListingComparator.compare(refNodeListing, candidateNodeListing);
  }

  @Nonnull
  public static String getAndroidTop() {
    String androidTop = System.getenv("ANDROID_BUILD_TOP");
    if (androidTop == null) {
      throw new AssertionError("Failed to locate environment variable ANDROID_BUILD_TOP.");
    }
    return androidTop;
  }

  @Nonnull
  public static JMethod getJMethodWithRejectAllFilter(@Nonnull File fileName,
      @Nonnull String className, @Nonnull String methodSignature) throws Exception {
    Options commandLineArgs = TestTools.buildCommandLineArgs(fileName);
    commandLineArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");
    commandLineArgs.keepMethodBody = true;
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
    commandLineArgs.keepMethodBody = true;
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

  public static void compileWithRefCompiler(
      Options compilerArgs, boolean useEcjAsRefCompiler, File refCompilerOut) {
    if (useEcjAsRefCompiler) {
      compileWithEcj(compilerArgs, refCompilerOut);
    } else {
      compileWithExternalRefCompiler(compilerArgs, refCompilerOut);
    }
  }

  /**
   * Creates the reference compiler files.
   *
   * @param compilerArgs the arguments given to a reference compiler
   * @throws IOException
   * @throws InterruptedException
   */
  public static ReferenceCompilerFiles createReferenceCompilerFiles(@Nonnull File testDir,
      @Nonnull Options compilerArgs,
      @CheckForNull ProguardFlags[] proguardFlags,
      @CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      boolean withDebugInfo,
      boolean useEcjAsRefCompiler,
      @CheckForNull JarJarRules jarjarRules) throws IOException, InterruptedException {

    if (withDebugInfo) {
      compilerArgs.ecjArguments.add(0, "-g");
    }

    File refCompilerOut = new File(testDir, "refcompilerout");
    File refDex = new File(testDir, "testref.dex");
    if (!refCompilerOut.exists() && !refCompilerOut.mkdir()) {
      throw new IOException("Could not create directory \"" + refCompilerOut.getName() + "\"");
    }

    // Create reference Dex file
    if (classpath != null) {
      for (File f : classpath) {
        unzip(f, refCompilerOut);
      }
    }
    compileWithRefCompiler(compilerArgs, useEcjAsRefCompiler, refCompilerOut);
    File refJar = new File(testDir, "ref.jar");
    File refJarJar = new File(testDir, "refJarJar.jar");
    File refProguard = new File(testDir, "refProguard.jar");
    createjar(refJar, refCompilerOut);
    if (jarjarRules != null) {
      processWithJarJar(jarjarRules, refJar, refJarJar);
    } else {
      refJarJar = refJar;
    }
    if (proguardFlags != null) {
      processWithProguard(proguardFlags, refJarJar, refProguard, bootclasspath);
    } else {
      refProguard = refJarJar;
    }

    compileWithDx(refProguard, refDex, withDebugInfo);
    return new ReferenceCompilerFiles(refProguard, refDex);
  }

  /**
   * Compares the classes.dex file into {@code jackDexFolder} to a a dex file generated with a
   * reference compiler and {@code dx}.
   * <p>
   * If {@code stopsOnError} is set to true, the comparison will stop after the first error and the
   * test will fail. If not, the test can succeed even if there are differences found.
   *
   * @param compilerArgs the arguments given to a reference compiler
   * @param withDebugInfo generate debug infos and compare them
   * @param compareInstructionNumber enable comparison of number of instructions
   * @param instructionNumberTolerance tolerance factor for comparison of number of instructions
   * @throws DifferenceFoundException if a difference between the two Dex files is found and
   *         haltOnError is set to true
   * @throws IOException
   * @throws InterruptedException
   */
  private static void compareDexToReference(@Nonnull File jackDexFolder,
      @Nonnull Options compilerArgs,
      @CheckForNull ProguardFlags[] proguardFlags,
      @CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      boolean withDebugInfo,
      boolean useEcjAsRefCompiler,
      boolean compareInstructionNumber,
      float instructionNumberTolerance,
      @CheckForNull JarJarRules jarjarRules,
      boolean strict) throws DifferenceFoundException, IOException, InterruptedException {
    File testDir = null;

    // Prepare files and directories
    testDir = TestTools.createTempDir("jacktest", null);

    File refDex = createReferenceCompilerFiles(testDir,
        compilerArgs,
        proguardFlags,
        bootclasspath,
        classpath,
        withDebugInfo,
        useEcjAsRefCompiler,
        jarjarRules).dexFile;

    // Compare Jack Dex file to reference
    File candidateFile = new File(jackDexFolder, DexFileWriter.DEX_FILENAME);
    new DexComparator(withDebugInfo, strict, false /* compareDebugInfoBinary */,
        compareInstructionNumber, instructionNumberTolerance).compare(refDex, candidateFile);
    new DexAnnotationsComparator().compare(refDex, candidateFile);
  }

  private static void unzip(@Nonnull File jarfile, @Nonnull File outputFolder) {
    String[] args = new String[]{"unzip", "-qo", jarfile.getAbsolutePath(),
        "-d", outputFolder.getAbsolutePath(),};

    ExecuteFile execFile = new ExecuteFile(args);
    if (!execFile.run()) {
      throw new RuntimeException("Unzip exited with an error");
    }
  }

  private static void createjar(@Nonnull File jarfile, @Nonnull File inputFiles) {
    String[] args = new String[]{"jar", "cf", jarfile.getAbsolutePath(),
        "-C", inputFiles.getAbsolutePath(), "."};

    ExecuteFile execFile = new ExecuteFile(args);
    if (!execFile.run()) {
      throw new RuntimeException("Reference compiler exited with an error");
    }
  }

  private static void processWithJarJar(@Nonnull File jarjarRules,
      @Nonnull File inJar, @Nonnull File outJar) {
    String[] args = new String[]{"java", "-jar", ExternalTools.JARJAR.getAbsolutePath(),
        "process", jarjarRules.getAbsolutePath(),
        inJar.getAbsolutePath(), outJar.getAbsolutePath()};

    ExecuteFile execFile = new ExecuteFile(args);
    if (!execFile.run()) {
      throw new RuntimeException("JarJar exited with an error");
    }
  }

  private static void processWithProguard(@Nonnull ProguardFlags[] proguardFlagsFiles,
      @Nonnull File inJar, @Nonnull File outJar, @CheckForNull File[] bootclasspath) {
    String bootclasspathStr = null;
    if (bootclasspath == null) {
      bootclasspathStr = getDefaultBootclasspathString();
    } else {
      bootclasspathStr = getClasspathAsString(bootclasspath);
    }
    String[] args = new String[12 + proguardFlagsFiles.length * 2];
    int i = 0;
    args[i++] = "java";
    args[i++] = "-jar";
    args[i++] = ExternalTools.PROGUARD.getAbsolutePath();
    args[i++] = "-injars";
    args[i++] = inJar.getAbsolutePath();
    args[i++] = "-outjars";
    args[i++] = outJar.getAbsolutePath();
    args[i++] = "-libraryjars";
    args[i++] = bootclasspathStr;
    args[i++] = "-verbose";
    args[i++] = "-forceprocessing";
    args[i++] = "-dontoptimize";
    for (ProguardFlags proguardFlags : proguardFlagsFiles) {
      args[i++] = "-include";
      args[i++] = proguardFlags.getAbsolutePath();
    }

    ExecuteFile execFile = new ExecuteFile(args);
    execFile.setOut(System.out);
    execFile.setErr(System.err);
    execFile.setVerbose(true);
    if (!execFile.run()) {
      throw new RuntimeException("Proguard exited with an error");
    }
  }

  private static void compileWithExternalRefCompiler(@Nonnull Options compilerArgs, @Nonnull File out) {

    List<String> arguments = getRefCompilerArguments(compilerArgs);

    String[] args = new String[arguments.size() + 3];
    String refCompilerPath = System.getenv("REF_JAVA_COMPILER");
    if (refCompilerPath == null) {
      throw new RuntimeException("REF_JAVA_COMPILER environment variable not set");
    }
    int i = 0;
    args[i++] = refCompilerPath.trim();

    for (String compilerArg : arguments) {
      args[i++] = compilerArg;
    }
    args[i++] = "-d";
    args[i++] = out.getAbsolutePath();

    ExecuteFile execFile = new ExecuteFile(args);
    if (!execFile.run()) {
      throw new RuntimeException("Reference compiler exited with an error");
    }
  }

  private static List<String> getRefCompilerArguments(Options compilerArgs) {
    List<String> arguments = new ArrayList<String>(compilerArgs.ecjArguments);
    if (compilerArgs.classpath != null) {
      arguments.add("-classpath");
      // TODO(jmhenaff): This hack will be removed as soon as TestTools will be removed
      arguments.add(compilerArgs.classpath.replace("core-stubs-mini.jack", "core-stubs-mini.jar"));
    }

    if (compilerArgs.bootclasspath != null) {
      arguments.add("-bootclasspath");
      // TODO(jmhenaff): This hack will be removed as soon as TestTools will be removed
      arguments.add(compilerArgs.bootclasspath.replace("core-stubs-mini.jack", "core-stubs-mini.jar"));
    }
    return arguments;
  }

  private static void compileWithEcj(Options compilerArgs, File out) {
    List<String> jackEcjArgs = getRefCompilerArguments(compilerArgs);
    String[] args = new String[jackEcjArgs.size() + 5];
    int i = 0;
    args[i++] = "-noExit";
    args[i++] = "-1.6";
    args[i++] = "-preserveAllLocals";
    for (String compilerArg : jackEcjArgs) {
      args[i++] = compilerArg;
    }
    args[i++] = "-d";
    args[i++] = out.getAbsolutePath();

    org.eclipse.jdt.internal.compiler.batch.Main.main(args);
  }

  private static void compileWithDx(@Nonnull File src, @Nonnull File refDex, boolean withDebugInfo)
      throws IOException {

    Arguments arguments = new Arguments();

    arguments.jarOutput = false;
    arguments.outName = refDex.getAbsolutePath();
    arguments.optimize = !withDebugInfo;
    // this only means we deactivate the check that no core classes are included
    arguments.coreLibrary = true;
    arguments.parse(new String[] {src.getAbsolutePath()});

    int retValue = com.android.dx.command.dexer.Main.run(arguments);
    if (retValue != 0) {
      throw new RuntimeException("Dx failed and returned " + retValue);
    }
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
