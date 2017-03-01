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
import com.google.common.collect.Lists;

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
import com.android.sched.util.Version;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.Files;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.ByteStreamSucker;
import com.android.sched.util.stream.LocationByteStreamSucker;
import com.android.sched.vfs.ZipUtils;

import org.junit.Assume;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Utility class that act also as a Factory for toolchains. It holds the global
 * configuration for tests.
 */
public abstract class AbstractTestTools {

  @Nonnull
  public static final String JUNIT_RUNNER_NAME = "org.junit.runner.JUnitCore";

  @CheckForNull
  private static HashMap<String, ToolchainBuilder> toolchainBuilders;

  @Nonnull
  private static final String JACK_TESTS_FOLDER = "jack-tests";

  @Nonnull
  private static final String PROPERTY_VALUE_SEPARATOR  = ",";
  @Nonnull
  private static final String TOOLCHAIN_REFERENCE_KEY   = "reference.toolchain";
  @Nonnull
  private static final String TOOLCHAIN_CANDIDATE_KEY   = "candidate.toolchain";
  @Nonnull
  private static final String RUNTIME_LIST_KEY          = "runtime.list";
  @Nonnull
  private static final String RUNTIME_LOCATION_PREFIX   = "runtime.location.";
  @Nonnull
  private static final String TOOL_PREFIX               = "tool.";
  @Nonnull
  private static final String TOOLCHAIN_PREBUILT_PREFIX = "toolchain.prebuilt.";
  @Nonnull
  private static final String TMP_PREFIX                = "test-jack-";
  @Nonnull
  private static final String LEGACY_COMPILER_KEY       = "toolchain.prebuilt.legacy-java-compiler";
  @Nonnull
  private static final String ANDROID_SDK_KEY           = "android.sdk";


  @Nonnull
  private static final String RUNTIME_TOLERANT          = "tests.runtime.tolerant";

  @CheckForNull
  private static List<RuntimeRunner> runtimes;

  @Nonnull
  private static final Map<String, File> runtimeEnvironmentLocations = new HashMap<String, File>();

  @Nonnull
  private static final Splitter listSplitter =
      Splitter.on(PROPERTY_VALUE_SEPARATOR).trimResults().omitEmptyStrings();

  private static boolean hasRuntimeWarningBeenEmitted = false;


  static {

    LoggerFactory.configure(LogLevel.ERROR);

    if (!Boolean.parseBoolean(System.getProperty("tests.dump", "false"))) {
      printConfig();
    }

  }

  private interface ToolchainBuilder {
    IToolchain build();
  }

  private static class LegacyToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public LegacyToolchain build() {
      return new LegacyToolchain(getPrebuilt("legacy-java-compiler"),
          getPrebuiltsAsClasspath("legacy-java-compiler.bootclasspath"), getPrebuilt("jarjar"),
          getPrebuilt("proguard"), getPrebuilt("dx"));
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
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV01Toolchain(jackPrebuilt);
    }
  }

  private static class JackApiV02IncrementalToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV02IncrementalToolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV02IncrementalToolchain(jackPrebuilt);
    }
  }

  private static class JackApiV03IncrementalToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV03IncrementalToolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV03IncrementalToolchain(jackPrebuilt);
    }
  }

  private static class JackApiV04IncrementalToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV04IncrementalToolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV04IncrementalToolchain(jackPrebuilt);
    }
  }

  private static class JackApiV02TwoStepsToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV02TwoStepsToolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV02TwoStepsToolchain(jackPrebuilt);
    }
  }

  private static class JackApiV03TwoStepsToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV03TwoStepsToolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV03TwoStepsToolchain(jackPrebuilt);
    }
  }

  private static class JackApiV04TwoStepsToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV04TwoStepsToolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV04TwoStepsToolchain(jackPrebuilt);
    }
  }

  private static class JackApiV02ToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV02Toolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV02Toolchain(jackPrebuilt);
    }
  }

  private static class JackApiV03ToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV03Toolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV03Toolchain(jackPrebuilt);
    }
  }

  private static class JackApiV04ToolchainBuilder implements ToolchainBuilder {

    @Override
    @Nonnull
    public JackApiV04Toolchain build() {
      File jackPrebuilt = isPrebuiltAvailable("jack") ? getPrebuilt("jack") : null;
      return new JackApiV04Toolchain(jackPrebuilt);
    }
  }

  private static class LegacyJillToolchainBuilder implements ToolchainBuilder {

    @Override
    public LegacyJillToolchain build() {
      return new LegacyJillToolchain(getPrebuilt("legacy-java-compiler"), getPrebuilt("jill"),
          getPrebuilt("jack"), getPrebuilt("jarjar"), getPrebuilt("proguard"));
    }
  }

  private static class EmbeddedJillBasedToolchainBuilder implements ToolchainBuilder {
    @Override
    public IToolchain build() {
      return new EmbeddedJillBasedToolchain(getPrebuilt("jack"),
          getPrebuilt("legacy-java-compiler"), getPrebuilt("jarjar"), getPrebuilt("proguard"));
    }
  }

  private static class JillApiV01ToolchainBuilder implements ToolchainBuilder {

    @Override
    public JillApiV01Toolchain build() {
      File jillPrebuilt = isPrebuiltAvailable("jill") ? getPrebuilt("jill") : null;
      return new JillApiV01Toolchain(jillPrebuilt, getPrebuilt("jack"),
          getPrebuilt("legacy-java-compiler"), getPrebuilt("jarjar"), getPrebuilt("proguard"));
    }
  }

  public static boolean isPrebuiltAvailable(@Nonnull String prebuiltName) {
    return !getPrebuiltPath(prebuiltName).equals("");
  }

  @Nonnull
  private static String getPrebuiltPath(@Nonnull String prebuiltName) {
    return TestsProperties.getProperty(TOOLCHAIN_PREBUILT_PREFIX + prebuiltName).trim();
  }

  @Nonnull
  public static File getPrebuilt(@Nonnull String prebuiltName) {
    String prebuiltVarName = TOOLCHAIN_PREBUILT_PREFIX + prebuiltName;
    String prebuiltPath = TestsProperties.getProperty(prebuiltVarName).trim();

    if (prebuiltPath.equals("")) {
      throw new AssertionError("Property '" + prebuiltVarName + "' is not set");
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
  public static List<File> getPrebuiltsAsClasspath(@Nonnull String prebuiltClasspathName) {
    String keyName = TOOLCHAIN_PREBUILT_PREFIX + prebuiltClasspathName;
    String value = TestsProperties.getProperty(keyName).trim();

    if (value.equals("")) {
      throw new AssertionError("Property '" + prebuiltClasspathName + "' is not set");
    }

    List<File> result = new ArrayList<>();
    for (String prebuiltPath : getClasspathsAsList(value)) {

      File file = new File(prebuiltPath);
      if (!file.isAbsolute()) {
        file = new File(TestsProperties.getJackRootDir(), prebuiltPath);
      }

      if (!file.exists()) {
        throw new TestConfigurationException(
            "Can not find prebuilt at '"
                + file.getPath()
                + "' for prebuilt classpath '"
                + prebuiltClasspathName
                + "'");
      }


      result.add(file);
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
    return new File(getTestsRootDir(), packageName.replace('.', File.separatorChar));
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

    if (toolchainBuilders == null) {
      HashMap<String, ToolchainBuilder> toolchainBuildersTmp;
      toolchainBuildersTmp = new HashMap<String, ToolchainBuilder>();
      toolchainBuildersTmp.put("jack-cli", new JackCliToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-v01", new JackApiV01ToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-v02", new JackApiV02ToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-inc-v02", new JackApiV02IncrementalToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-2steps-v02", new JackApiV02TwoStepsToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-v03", new JackApiV03ToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-inc-v03", new JackApiV03IncrementalToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-2steps-v03", new JackApiV03TwoStepsToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-v04", new JackApiV04ToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-inc-v04", new JackApiV04IncrementalToolchainBuilder());
      toolchainBuildersTmp.put("jack-api-2steps-v04", new JackApiV04TwoStepsToolchainBuilder());
      toolchainBuildersTmp.put("legacy", new LegacyToolchainBuilder());
      toolchainBuildersTmp.put("jill-legacy", new EmbeddedJillBasedToolchainBuilder());
      toolchainBuildersTmp.put("jill-api-v01", new JillApiV01ToolchainBuilder());
      toolchainBuildersTmp.put("jill-legacy-prebuilt", new LegacyJillToolchainBuilder());
      toolchainBuilders = toolchainBuildersTmp;
    }

    assert toolchainBuilders != null;

    ToolchainBuilder toolchainBuilder = toolchainBuilders.get(toolchainName);
    if (toolchainBuilder == null) {
      throw new TestConfigurationException("Unknown toolchain: '" + toolchainName + "'");
    }
    return toolchainBuilder;
  }

  @Nonnull
  public static File createTempFile(@Nonnull String prefix, @Nonnull String suffix)
      throws CannotCreateFileException, CannotChangePermissionException {
    File tmp = Files.createTempFile(TMP_PREFIX + prefix, suffix);
    tmp.deleteOnExit();
    return tmp;
  }

  @Nonnull
  public static File createTempDir() throws CannotCreateFileException,
      CannotChangePermissionException, IOException {
    try {
      final File tmpDir = Files.createTempDir(TMP_PREFIX);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            deleteTempDir(tmpDir);
          } catch (IOException e) {
            throw new RuntimeException(new CannotDeleteFileException(new FileLocation(tmpDir)));
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
  public static List<String> getClasspathsAsList(@Nonnull String classpath) {
    return Lists.newArrayList(Splitter.on(File.pathSeparatorChar).split(classpath));
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

  public static void addFile(@Nonnull List<String> commandLine,
      boolean mustExist, @Nonnull File... filesOrSourceLists) {
    for (File file : filesOrSourceLists) {
      addFile(file, commandLine, mustExist);
    }
  }

  private static void addFile(@Nonnull File fileOrSourceList, @Nonnull List<String> commandLine,
      boolean mustExist) {
    if (fileOrSourceList instanceof Sourcelist) {
      commandLine.add("@" + fileOrSourceList.getAbsolutePath());
    } else {
      List<File> sourceFiles = new ArrayList<File>();
      try {
        getJavaFiles(fileOrSourceList, sourceFiles, mustExist);
      } catch (IOException e) {
      }
      for (File sourceFile : sourceFiles) {
        commandLine.add(sourceFile.getAbsolutePath());
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

  public static void unzip(@Nonnull File zipFile, @Nonnull File outputFolder, boolean verbose)
      throws CannotCreateFileException, CannotReadException,
          CannotWriteException, CannotCloseException, NoSuchFileException {

    if (verbose) {
      System.out.println(
          "Unzipping '" + zipFile.getPath() + "' to '" + outputFolder.getPath() + "'...");
    }

    FileInputStream fis;
    try {
      fis = new FileInputStream(zipFile);
    } catch (FileNotFoundException e1) {
      throw new NoSuchFileException(new FileLocation(zipFile));
    }

    try (ZipInputStream zis = new ZipInputStream(fis)) {

      ZipEntry zipEntry;

      try {
        zipEntry = zis.getNextEntry();
      } catch (IOException e) {
        throw new CannotReadException(new FileLocation(zipFile), e);
      }

      while (zipEntry != null) {

        File outputFile = new File(outputFolder, zipEntry.getName());

        if (verbose) {
          System.out.println("Extracting: '" + outputFile.getPath() + "'");
        }

        if (zipEntry.isDirectory()) {
          if (!outputFile.exists() && !outputFile.mkdirs()) {
            throw new CannotCreateFileException(new DirectoryLocation(outputFile));
          }
        } else {

          File parentDir = outputFile.getParentFile();
          if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new CannotCreateFileException(new DirectoryLocation(outputFile.getParentFile()));
          }

          try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            new LocationByteStreamSucker(
                zis,
                fos,
                new ZipLocation(new FileLocation(zipFile), zipEntry),
                new FileLocation(outputFile))
            .suck();
          } catch (FileNotFoundException e) {
            throw new NoSuchFileException(new FileLocation(outputFile), e);
          } catch (IOException e) {
            throw new CannotCloseException(new FileLocation(outputFile), e);
          }
        }

        try {
          zipEntry = zis.getNextEntry();
        } catch (IOException e) {
          throw new CannotReadException(new FileLocation(zipFile), e);
        }
     }
    } catch (IOException e) {
      throw new CannotCloseException(new FileLocation(zipFile), e);
    }
  }

  public static void zip(@Nonnull File directory, @Nonnull File outputFile, boolean verbose)
      throws CannotCloseException, CannotReadException,
          CannotWriteException, NoSuchFileException {
    assert directory.isDirectory();

    FileOutputStream fos;
    try {
      fos = new FileOutputStream(outputFile);
    } catch (FileNotFoundException e) {
      throw new NoSuchFileException(new FileLocation(outputFile), e);
    }

    try (ZipOutputStream out = new ZipOutputStream(fos)) {

      if (verbose) {
        System.out.println(
            "Zipping '" + directory.getPath() + "' to '" + outputFile.getPath() + "'...");
      }

      addFilesToZip(out, directory, "", outputFile, verbose);
    } catch (IOException e) {
      throw new CannotCloseException(new FileLocation(outputFile), e);
    }
  }

  private static void addFilesToZip(
      @Nonnull ZipOutputStream zip,
      @Nonnull File file,
      @Nonnull String entryPath,
      @Nonnull File outputZipFile,
      boolean verbose)
      throws CannotCloseException, CannotReadException, CannotWriteException, NoSuchFileException {

    if (file.isFile()) {
      if (verbose) {
        System.out.println("Adding: '" + file.getPath() + "'");
      }
      ZipEntry zipEntry = new ZipEntry(entryPath);
      try {
        zip.putNextEntry(zipEntry);
      } catch (IOException e) {
        throw new CannotWriteException(new FileLocation(outputZipFile), e);
      }
      try (InputStream in = new FileInputStream(file)) {
        new LocationByteStreamSucker(
                in,
                zip,
                new FileLocation(file),
                new ZipLocation(new FileLocation(outputZipFile), zipEntry))
            .suck();
        try {
          zip.closeEntry();
        } catch (IOException e) {
          throw new CannotCloseException(
              new ZipLocation(new FileLocation(outputZipFile), zipEntry), e);
        }
      } catch (FileNotFoundException e) {
        // Thrown by FileInputStream ctor
        throw new NoSuchFileException(new FileLocation(outputZipFile), e);
      } catch (IOException e) {
        throw new CannotCloseException(new FileLocation(file), e);
      }
    } else {
      assert file.isDirectory();
      File [] filesInDir = file.listFiles();
      assert filesInDir != null;
      for (File sub: filesInDir) {
        // Zip entries names must not start with /
        String subEntryPath = entryPath.isEmpty() ? sub.getName() :
          entryPath + ZipUtils.ZIP_SEPARATOR + sub.getName();
        addFilesToZip(zip, sub, subEntryPath, outputZipFile, verbose);
      }
    }
  }

  public static void createjar(@Nonnull File jarfile, @Nonnull File inputFiles, boolean verbose) {

    String options = verbose ? "-cfv" : "-cf";

    String[] commandLine = new String[] {"jar",
        options,
        jarfile.getAbsolutePath(),
        "-C",
        inputFiles.getAbsolutePath(),
        "."};

    ExecuteFile execFile = new ExecuteFile(commandLine);
    execFile.inheritEnvironment();
    execFile.setVerbose(verbose);
    execFile.setErr(System.err);
    execFile.setOut(System.out);

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
  public static List<RuntimeRunner> listRuntimeTestRunners()
      throws SecurityException, IllegalArgumentException, RuntimeRunnerException {
    return listRuntimeTestRunners(/*properties = */ null);
  }

  @Nonnull
  public static List<RuntimeRunner> listRuntimeTestRunners(@CheckForNull Properties properties)
      throws SecurityException, IllegalArgumentException, RuntimeRunnerException {

    if (runtimes == null) {
      try {
        List<RuntimeRunner> runtimesTmp = new ArrayList<RuntimeRunner>();
        runtimesTmp.addAll(parseRuntimeList(TestsProperties.getProperty(RUNTIME_LIST_KEY)));
        runtimes = runtimesTmp;
      } catch (SecurityException e) {
        throw new TestConfigurationException(e);
      } catch (IllegalArgumentException e) {
        throw new TestConfigurationException(e);
      } catch (RuntimeRunnerException e) {
        throw new TestConfigurationException(e);
      }
    }

    if (properties != null) {
      String rtAsString = properties.getProperty(RUNTIME_LIST_KEY);
      if (rtAsString != null) {
        return parseRuntimeList(rtAsString);
      }
    }

    assert runtimes != null; // Make FindBugs happy
    if (runtimes.size() == 0) {
      if (Boolean.parseBoolean(System.getProperty(RUNTIME_TOLERANT, "false"))) {
        if (!hasRuntimeWarningBeenEmitted) {
          System.err.println("WARNING: no runtime has been provided");
          hasRuntimeWarningBeenEmitted = true;
        }
      } else {
        throw new TestConfigurationException(
            "No runtime has been provided. Set property '" + RUNTIME_TOLERANT + "' to 'true'"
            + " to allow it.");
      }
    }

    assert runtimes != null; // Make FindBugs happy
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

    rtLocationPath = TestsProperties.getProperty(RUNTIME_LOCATION_PREFIX + rtName);

    if (rtLocationPath.equals("")) {
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

  private static void printConfig() {
    System.out.println("Tests configuration:");

    System.out.println(TestsProperties.TEST_CONFIG_KEY + " = "
        + System.getProperty(TestsProperties.TEST_CONFIG_KEY));

    printProperty(TestsProperties.JACK_HOME_KEY);

    printProperty(TOOLCHAIN_CANDIDATE_KEY);
    printProperty(TOOLCHAIN_REFERENCE_KEY);

    printProperty(TOOLCHAIN_PREBUILT_PREFIX + "jack");
    System.out.println(TOOLCHAIN_PREBUILT_PREFIX + "jack.version = " + getVersion("jack"));

    printProperty(TOOLCHAIN_PREBUILT_PREFIX + "jill");
    System.out.println(TOOLCHAIN_PREBUILT_PREFIX + "jill.version = " + getVersion("jill"));

    printProperty(TOOLCHAIN_PREBUILT_PREFIX + "jarjar");
    printProperty(TOOLCHAIN_PREBUILT_PREFIX + "proguard");

    TestsProperties.getProperty(LEGACY_COMPILER_KEY);
    String runtimeList = printProperty(RUNTIME_LIST_KEY);

    if (runtimeList.trim().equals("")) {
      for (String runtimeName : listSplitter.split(runtimeList)) {
        printProperty(RUNTIME_LOCATION_PREFIX + runtimeName);
      }
    }

    String legacyCompiler = printProperty(LEGACY_COMPILER_KEY);
    if (!legacyCompiler.equals("")) {
      System.out.println(
          LEGACY_COMPILER_KEY + ".version = " + getReferenceCompilerVersion().trim());
    }

    printProperty(ANDROID_SDK_KEY);

  }

  private static String printProperty(@Nonnull String propertyName) {
    String value = TestsProperties.getProperty(propertyName).trim();
    System.out.println(propertyName + " = " + value);
    return value;
  }


  private static String getReferenceCompilerVersion() {
    try {
      File legacyCompilerPrebuilt = getPrebuilt("legacy-java-compiler");

      String[] arguments = new String[2];
      arguments[0] = legacyCompilerPrebuilt.getAbsolutePath();
      arguments[1] = "-version";

      ExecuteFile exec = new ExecuteFile(arguments);
      exec.inheritEnvironment();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      exec.setErr(bos);

      String path = System.getenv("PATH");
      if (path != null) {
        exec.addEnvVar("PATH", path);
      }
      try {
        if (exec.run() != 0) {
          return "<unknown>";
        }

        return bos.toString();

      } catch (ExecFileException e) {
        return "<unknown> " + e.getMessage();
      } finally {
        try {
          bos.close();
        } catch (IOException e) {
        }
      }
    } catch (Throwable t) {
      return "<unknown> " + t.getMessage();

    }

  }

  @Nonnull
  private static String getVersion(@Nonnull String name) {

    String versionFileName = name + "-version.properties";

    InputStream is = null;
    JarFile jarFile = null;

    try {

      if (isPrebuiltAvailable(name)) {
        File prebuilt = getPrebuilt(name);

        if (prebuilt.getName().endsWith(".jar")) {

          jarFile = new JarFile(prebuilt);
          ZipEntry entry = jarFile.getEntry(name + "-version.properties");
          is = jarFile.getInputStream(entry);
          Version version = new Version(is);

          return version.getVerboseVersion();

        } else if (prebuilt.getName().endsWith("jack")) {
          return getServerJackVersion(prebuilt);
        } else {
          System.err.println(
              "Could not fetch version of prebuilt '" + name + "': '" + prebuilt.getName() + "'");
          return "<unknown>";
        }

      } else {
        is = AbstractTestTools.class.getClassLoader().getResourceAsStream(versionFileName);

        if (is == null) {
          throw new TestConfigurationException("Could not find '" + versionFileName + "'");
        }

        return new Version(is).getVerboseVersion() + " (found on classpath)";
      }
    } catch (Throwable t) {
      return "<unknown> " + t.getMessage();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
        }
      }
      if (jarFile != null) {
        try {
          jarFile.close();
        } catch (IOException e) {
        }
      }
    }
  }

  private static String getServerJackVersion(@Nonnull File jackScript) {

    String[] arguments = new String[2];
    arguments[0] = jackScript.getAbsolutePath();
    arguments[1] = "--version";

    ExecuteFile exec = new ExecuteFile(arguments);
    exec.inheritEnvironment();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    exec.setOut(bos);
    ByteArrayOutputStream errOs = new ByteArrayOutputStream();
    exec.setErr(errOs);

    try {
      if (exec.run() != 0) {
        System.err.println(errOs.toString().trim());
        throw new RuntimeException("Could not fetch version of Jack");
      }

      return bos.toString().replace('\n', ' ');

    } catch (ExecFileException e) {
      throw new RuntimeException("Could not fetch version of  Jack", e);
    } finally {
      try {
        bos.close();
      } catch (IOException e) {
      }
    }
  }

  @CheckForNull
  public static File getAndroidSdkLocation() {
    return new File(TestsProperties.getProperty(ANDROID_SDK_KEY));
  }

}
