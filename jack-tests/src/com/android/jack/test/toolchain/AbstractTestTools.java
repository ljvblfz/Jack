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

import com.google.common.base.Splitter;

import com.android.jack.Sourcelist;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputJackLibraryCodec;
import com.android.jack.test.TestConfigurationException;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runner.RuntimeRunnerException;
import com.android.jack.test.runner.RuntimeRunnerFactory;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;
import com.android.jack.util.NamingTools;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.file.Files;
import com.android.sched.util.stream.ByteStreamSucker;

import org.junit.Assume;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Utility class that act also as a Factory for toolchains. It holds the global
 * configuration for tests.
 */
public abstract class AbstractTestTools {

  @Nonnull
  public static final String JUNIT_RUNNER_NAME = "org.junit.runner.JUnitCore";

  @Nonnull
  private static HashMap<String, ToolchainBuilder> toolchainBuilders;

  @Nonnull
  private static final String JACK_TESTS_FOLDER = "jack-tests";

  @Nonnull
  private static final String PROPERTY_VALUE_SEPARATOR  = ",";
  @Nonnull
  private static final String TOOLCHAIN_REFERENCE_KEY   = "toolchain.reference";
  @Nonnull
  private static final String TOOLCHAIN_CANDIDATE_KEY   = "toolchain.candidate";
  @Nonnull
  private static final String RUNTIME_LIST_KEY          = "runtime.list";
  @Nonnull
  private static final String RUNTIME_LOCATION_PREFIX   = "runtime.location.";
  @Nonnull
  private static final String TOOL_PREFIX               = "tool.";
  @Nonnull
  private static final String TOOLCHAIN_PREBUILT_PREFIX = "toolchain.prebuilt.";

  @Nonnull
  private static final List<RuntimeRunner> runtimes = new ArrayList<RuntimeRunner>();

  @Nonnull
  private static final Map<String, File> runtimeEnvironmentLocations = new HashMap<String, File>();

  @Nonnull
  private static final Splitter listSplitter =
      Splitter.on(PROPERTY_VALUE_SEPARATOR).trimResults().omitEmptyStrings();


  static {

toolchainBuilders = new HashMap<String, ToolchainBuilder>();
    toolchainBuilders.put("jack-cli", new JackCliToolchainBuilder());
    toolchainBuilders.put("jack-api-v01", new JackApiV01ToolchainBuilder());
    toolchainBuilders.put("jack-api-inc-v01", new JackApiV01IncrementalToolchainBuilder());
    toolchainBuilders.put("jack-api-2steps-v01", new JackApiV01TwoStepsToolchainBuilder());
    toolchainBuilders.put("legacy", new LegacyToolchainBuilder());
    toolchainBuilders.put("jill-legacy", new LegacyJillToolchainBuilder());

    try {
      runtimes.addAll(parseRuntimeList(TestsProperties.getProperty(RUNTIME_LIST_KEY)));
    } catch (SecurityException e) {
      throw new TestConfigurationException(e);
    } catch (IllegalArgumentException e) {
      throw new TestConfigurationException(e);
    } catch (RuntimeRunnerException e) {
      throw new TestConfigurationException(e);
    }

  }

  private interface ToolchainBuilder {
    IToolchain build();
  }

  private static class LegacyToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public LegacyToolchain build() {
      return new LegacyToolchain(getPrebuilt("legacy-java-compiler"), getPrebuilt("jarjar"),
          getPrebuilt("proguard"));
    }
  }

  private static class JackCliToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackCliToolchain build() {
      return new JackCliToolchain(getPrebuilt("jack"));
    }
  }

  private static class JackApiV01ToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV01Toolchain build() {
      return new JackApiV01Toolchain(getPrebuilt("jack"));
    }
  }

  private static class JackApiV01IncrementalToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV01Toolchain build() {
      return new JackApiV01IncrementalToolchain(getPrebuilt("jack"));
    }
  }

  private static class JackApiV01TwoStepsToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV01Toolchain build() {
      return new JackApiV01TwoStepsToolchain(getPrebuilt("jack"));
    }
  }

  private static class LegacyJillToolchainBuilder implements ToolchainBuilder {

    @Override
    public IToolchain build() {
      return new LegacyJillToolchain(getPrebuilt("legacy-java-compiler"), getPrebuilt("jill"),
          getPrebuilt("jack"), getPrebuilt("jarjar"), getPrebuilt("proguard"));
    }
  }

  public static File getPrebuilt(@Nonnull String prebuiltName) {
    String prebuiltVarName = TOOLCHAIN_PREBUILT_PREFIX + prebuiltName;
    String prebuiltPath;

    try {
      prebuiltPath = TestsProperties.getProperty(prebuiltVarName);
    } catch (TestConfigurationException e) {
      throw new TestConfigurationException(
          "Cannot find path for prebuilt '" + prebuiltName + "'", e);
    }

    File result = new File(prebuiltPath);
    if (!result.isAbsolute()) {
      result = new File(TestsProperties.getJackRootDir(), prebuiltPath);
    }

    if (!result.exists()) {
      throw new TestConfigurationException(
          "Can not find '" + prebuiltName + "' prebuilt at '" + result.getPath() + "'");
    }
    return result;
  }


  @Nonnull
  private static final File getTestsRootDir() {
    return new File(TestsProperties.getJackRootDir(),
        JACK_TESTS_FOLDER + File.separator + "tests");
  }

  @Nonnull
  public static final File getTestRootDir(@Nonnull String packageName) {
    return new File(getTestsRootDir(), packageName.replace(".", File.separator));
  }

  @Nonnull
  public static final IToolchain getCandidateToolchain() {
    return createToolchain("candidate.toolchain");
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public static final <T extends IToolchain> T getCandidateToolchain(
      @Nonnull Class<? extends IToolchain> clazz) {
    IToolchain result = getCandidateToolchain();
    Assume.assumeTrue(clazz.isAssignableFrom(result.getClass()));
    return (T) result;
  }

  @Nonnull
  public static final <T extends IToolchain> T getCandidateToolchain(
      @Nonnull Class<? extends IToolchain> clazz,
      @Nonnull List<Class<? extends IToolchain>> excludeClazz) {
    T result = getCandidateToolchain(clazz);

    for (Class<? extends IToolchain> c : excludeClazz) {
      Assume.assumeTrue(!c.isAssignableFrom(result.getClass()));
    }

    return result;
  }

  @Nonnull
  public static final IToolchain getReferenceToolchain() {
    return createToolchain("reference.toolchain");
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public static final <T extends IToolchain> T getReferenceToolchain(
      @Nonnull Class<? extends IToolchain> clazz) {
    IToolchain result = getReferenceToolchain();
    Assume.assumeTrue(clazz.isAssignableFrom(result.getClass()));
    return (T) result;
  }

  @Nonnull
  public static final <T extends IToolchain> T getReferenceToolchain(
      @Nonnull Class<? extends IToolchain> clazz,
      @Nonnull List<Class<? extends IToolchain>> excludeClazz) {
    T result = getReferenceToolchain(clazz);

    for (Class<? extends IToolchain> c : excludeClazz) {
      Assume.assumeTrue(!c.isAssignableFrom(result.getClass()));
    }

    return result;
  }

  @Nonnull
  private static IToolchain createToolchain(@Nonnull String propertyName) {
    return getToolchainBuilder(TestsProperties.getProperty(propertyName)).build();
  }

  @Nonnull
  private static ToolchainBuilder getToolchainBuilder(@Nonnull String toolchainName) {
    ToolchainBuilder toolchainBuilder = toolchainBuilders.get(toolchainName);
    if (toolchainBuilder == null) {
      throw new TestConfigurationException("Unknown toolchain: '" + toolchainName + "'");
    }
    return toolchainBuilder;
  }

  @Nonnull
  public static File createTempFile(@Nonnull String prefix, @Nonnull String suffix)
      throws IOException {
    File tmp = File.createTempFile(prefix, suffix);
    tmp.deleteOnExit();
    return tmp;
  }

  @Nonnull
  public static File createTempDir() throws IOException {
    try {
      final File tmpDir = Files.createTempDir();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            deleteTempDir(tmpDir);
          } catch (IOException e) {
            System.err.println(e.getMessage());
          }
        }
      });
      return tmpDir;
    } catch (IllegalStateException e) {
      throw new IOException(e);
    }
  }

  @Nonnull
  public static File createDir(@Nonnull File directory, @Nonnull String name) throws IOException {
    if (!directory.exists() || !directory.isDirectory()) {
      throw new AssertionError();
    }

    File result = new File(directory, name);
    if (!result.mkdir()) {
      throw new IOException("Failed to create dir " + result.getAbsolutePath());
    }
    return result;
  }

  public static void deleteTempDir(@Nonnull File tmp) throws IOException {

    if (tmp.isDirectory()) {
      File[] fileList = tmp.listFiles();
      if (fileList == null) {
        throw new IOException("Failed to delete dir " + tmp.getAbsolutePath()
            + " because listing of content failed");
      }
      for (File sub : fileList) {
        deleteTempDir(sub);
      }
    }
    if (!tmp.delete()) {
      throw new IOException("Failed to delete file " + tmp.getAbsolutePath());
    }
  }

  public static void copyDirectory(@Nonnull File source, @Nonnull File dest) throws IOException {
    if (!source.isDirectory() || !dest.isDirectory()) {
      throw new AssertionError("Existing directories must be provided");
    }
    recursiveFileCopy(source, dest);
  }

  private static void recursiveFileCopy(@Nonnull File src, @Nonnull File dest)
      throws IOException {

    if (src.isDirectory()) {

      if (!dest.exists() && !dest.mkdir()) {
        throw new AssertionError("Unable to create directory '" + dest.getPath() + "'");
      }

      String [] files = src.list();
      if (files != null) {
        for (String file : files) {
          File srcFile = new File(src, file);
          File destFile = new File(dest, file);
          recursiveFileCopy(srcFile, destFile);
        }
      }

    } else {

      assert src.isFile();

      com.google.common.io.Files.copy(src, dest);
    }
  }


  @Nonnull
  public static String getClasspathAsString(@Nonnull File[] files) {
    if (files.length == 0) {
      return "";
    }
    StringBuilder classpathStr = new StringBuilder();
    for (int i = 0; i < files.length; i++) {
      classpathStr.append(files[i].getAbsolutePath());
      if (i != files.length - 1) {
        classpathStr.append(File.pathSeparatorChar);
      }
    }
    return classpathStr.toString();
  }

  @Nonnull
  public static String getClasspathsAsString(
      @Nonnull File[] bootClasspath, @Nonnull File[] classpath) {
    if (bootClasspath.length == 0) {
      return getClasspathAsString(classpath);
    } else if (classpath.length == 0) {
      return getClasspathAsString(bootClasspath);
    } else {
      return concatClasspathStrings(
          getClasspathAsString(bootClasspath), getClasspathAsString(classpath));
    }
  }

  @Nonnull
  private static String concatClasspathStrings(
      @Nonnull String bootclasspath, @Nonnull String classpath) {
    if (bootclasspath.isEmpty()) {
      return classpath;
    } else if (classpath.isEmpty()) {
      return bootclasspath;
    } else {
      StringBuilder classpathStr = new StringBuilder(bootclasspath);
      classpathStr.append(File.pathSeparatorChar);
      classpathStr.append(classpath);
      return classpathStr.toString();
    }
  }

  @Nonnull
  public static File createFile(@Nonnull File folder, @Nonnull String packageName,
      @Nonnull String fileName, @Nonnull String fileContent) throws IOException {
    File packageFolder = new File(folder, packageName.replace('.', File.separatorChar));
    if (!packageFolder.exists() && !packageFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + packageFolder.getAbsolutePath());
    }
    File javaFile = new File(packageFolder, fileName);
    if (javaFile.exists() && !javaFile.delete()) {
      throw new IOException("Failed to delete file " + javaFile.getAbsolutePath());
    }
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      fos.write(fileContent.getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
    return javaFile;
  }

  public static void deleteJavaFile(@Nonnull File folder, @Nonnull String packageName,
      @Nonnull String fileName) throws IOException {
    File packageFolder = new File(folder, NamingTools.getBinaryName(packageName));
    File javaFile = new File(packageFolder, fileName);
    deleteFile(javaFile);
  }

  public static void deleteFile(@Nonnull File file) throws IOException {
    if (!file.delete()) {
      throw new IOException("Failed to delete file " + file.getAbsolutePath());
    }
  }

  @Nonnull
  public static File getDir(@Nonnull File file) {
    if (file.isDirectory()) {
      return file;
    } else {
      return file.getParentFile();
    }
  }

  @Nonnull
  public static List<File> getFiles(@Nonnull File folder, @Nonnull String extension) {
    assert folder.isDirectory();
    List<File> jackFiles = new ArrayList<File>();
    fillWithFiles(folder, jackFiles, extension);
    return jackFiles;
  }

  private static void fillWithFiles(@Nonnull File file, @Nonnull List<File> jackFiles,
      @Nonnull String extension) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File subFile : files) {
          fillWithFiles(subFile, jackFiles, extension);
        }
      }
    } else if (extension == null || extension.equals("*") || file.getName().endsWith(extension)) {
      jackFiles.add(file);
    }
  }

  public static void addFile(@Nonnull List<String> args,
      boolean mustExist, @Nonnull File... filesOrSourceLists) {
    for (File file : filesOrSourceLists) {
      addFile(file, args, mustExist);
    }
  }

  private static void addFile(@Nonnull File fileOrSourceList, @Nonnull List<String> args,
      boolean mustExist) {
    if (fileOrSourceList instanceof Sourcelist) {
      args.add("@" + fileOrSourceList.getAbsolutePath());
    } else {
      List<File> sourceFiles = new ArrayList<File>();
      try {
        getJavaFiles(fileOrSourceList, sourceFiles, mustExist);
      } catch (IOException e) {
      }
      for (File sourceFile : sourceFiles) {
        args.add(sourceFile.getAbsolutePath());
      }
    }
  }

  public static void getJavaFiles(@Nonnull File fileObject, @Nonnull List<File> filePaths,
      boolean mustExist) throws IOException {
    if (fileObject.isDirectory()) {
      File allFiles[] = fileObject.listFiles();
      if (allFiles != null) {
        for (File aFile : allFiles) {
          getJavaFiles(aFile, filePaths, mustExist);
        }
      }
    } else if (fileObject.getName().endsWith(".java") && (!mustExist || fileObject.isFile())) {
      filePaths.add(fileObject.getCanonicalFile());
    }
  }

  public static void appendMessageToFileContent(@Nonnull File file, @Nonnull File out,
      @Nonnull String message) throws IOException {
    PrintStream fos = new PrintStream(new FileOutputStream(out));
    FileInputStream fis = new FileInputStream(file);

    new ByteStreamSucker(fis, fos, /* toBeClose = */ false).suck();
    fos.print(message);

    fos.close();
  }

  public static void prependMessageToFileContent(@Nonnull File file, @Nonnull File out,
      @Nonnull String message) throws IOException {
    PrintStream fos = new PrintStream(new FileOutputStream(out));
    FileInputStream fis = new FileInputStream(file);

    fos.print(message);
    new ByteStreamSucker(fis, fos, /* toBeClose = */ true).suck();

  }

  public static void unzip(@Nonnull File jarfile, @Nonnull File outputFolder) {
    String[] args = new String[] {"unzip", "-qo", jarfile.getAbsolutePath(), "-d",
        outputFolder.getAbsolutePath()};

    ExecuteFile execFile = new ExecuteFile(args);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Unzip exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running unzip", e);
    }
  }

  public static void zip(@Nonnull File directory, @Nonnull File outputFile) throws IOException {
    String[] args = new String[] {"zip", "-r", outputFile.getAbsolutePath(), "."};

    ExecuteFile execFile = new ExecuteFile(args);
    execFile.setWorkingDir(directory, /* create = */ false);
    execFile.setErr(System.err);
    execFile.setOut(System.out);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Zip exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running zip", e);
    }
  }

  public static void createjar(@Nonnull File jarfile, @Nonnull File inputFiles) {
    String[] args = new String[] {"jar",
        "cf",
        jarfile.getAbsolutePath(),
        "-C",
        inputFiles.getAbsolutePath(),
        "."};

    ExecuteFile execFile = new ExecuteFile(args);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Jar exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running jar command", e);
    }
  }

  @Nonnull
  protected static List<String> buildEcjArgs() {
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add("-nowarn");

    return ecjArgs;
  }

  @Nonnull
  public static List<RuntimeRunner> listRuntimeTestRunners(@CheckForNull Properties properties)
      throws SecurityException, IllegalArgumentException, RuntimeRunnerException {

    if (properties != null) {
      String rtAsString = properties.getProperty(RUNTIME_LIST_KEY);
      if (rtAsString != null) {
        return parseRuntimeList(rtAsString);
      }
    }
    return runtimes;
  }

  @Nonnull
  private static List<RuntimeRunner> parseRuntimeList(@CheckForNull String runtimeList)
      throws SecurityException, IllegalArgumentException, RuntimeRunnerException {
    List<RuntimeRunner> result = new ArrayList<RuntimeRunner>(0);
    if (runtimeList != null) {
      for (String rtName : listSplitter.split(runtimeList)) {
        result.add(RuntimeRunnerFactory.create(rtName));
      }
    }
    return result;
  }

  @Nonnull
  public static File getRuntimeEnvironmentRootDir(@Nonnull String rtName) {
    String rtLocationPath;

    try {
      rtLocationPath = TestsProperties.getProperty(RUNTIME_LOCATION_PREFIX + rtName);
    } catch (TestConfigurationException e) {
      throw new TestConfigurationException("Location for runtime '" + rtName
          + "' is not specified. Set property '" + RUNTIME_LOCATION_PREFIX + rtName + "'");
    }

    File rtLocation = new File(rtLocationPath);
    if (!rtLocation.exists()) {
      throw new TestConfigurationException(
          "Location for runtime " + rtName + " does not exist: '" + rtLocationPath + "'");
    }
    if (!rtLocation.isDirectory()) {
      throw new TestConfigurationException(
          "Location for runtime " + rtName + " is not a directory: '" + rtLocationPath + "'");
    }

    return rtLocation;
  }

  public static void copyFileToDir(@Nonnull File fileToCopy, @Nonnull String relativePath,
      @Nonnull File dest) throws IOException {
    FileOutputStream fos = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileToCopy);
      File copiedFile = new File(dest, relativePath);
      File parentDir = copiedFile.getParentFile();
      if (!parentDir.exists()) {
        boolean res = parentDir.mkdirs();
        if (!res) {
          throw new AssertionError();
        }
      }
      try {
        fos = new FileOutputStream(copiedFile);
        ByteStreamSucker sucker = new ByteStreamSucker(fis, fos);
        sucker.suck();
      } finally {
        if (fos != null) {
          fos.close();
        }
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  /**
   * The returned {@link InputJackLibrary} must be closed.
   */
  public static InputJackLibrary getInputJackLibrary(@Nonnull File dirOrZip) {
    return new InputJackLibraryCodec().parseString(new CodecContext(), dirOrZip.getPath());
  }
}
