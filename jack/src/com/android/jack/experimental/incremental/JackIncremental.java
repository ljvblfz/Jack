/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.experimental.incremental;

import com.android.jack.CommandLine;
import com.android.jack.ExitStatus;
import com.android.jack.IllegalOptionsException;
import com.android.jack.Jack;
import com.android.jack.JackIOException;
import com.android.jack.JackUserException;
import com.android.jack.NothingToDoException;
import com.android.jack.Options;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.merge.CollisionPolicy;
import com.android.jack.dx.merge.DexMerger;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.load.JackLoadingException;
import com.android.jack.util.TextUtils;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.UnrecoverableException;
import com.android.sched.util.codec.DirectoryCodec;
import com.android.sched.util.config.ChainedException;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Executable class to run the jack compiler with incremental support.
 */
@HasKeyId
public class JackIncremental extends CommandLine {

  public static final BooleanPropertyId GENERATE_COMPILER_STATE = BooleanPropertyId.create(
  "jack.experimental.compilerstate.generate", "Generate compiler state").addDefaultValue(
  Boolean.FALSE);

  @Nonnull
  public static final PropertyId<Directory> COMPILER_STATE_OUTPUT_DIR = PropertyId.create(
      "jack.experimental.compilerstate.output.dir", "Compiler state output folder",
      new DirectoryCodec(Existence.MUST_EXIST, Permission.READ | Permission.WRITE))
      .requiredIf(GENERATE_COMPILER_STATE.getValue().isTrue());

  @CheckForNull
  private static CompilerState compilerState = null;

  @Nonnull
  private static final String INCREMENTAL_DEX_OUTPUT = "partialcompilation.dex";

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @CheckForNull
  private static File jackFilesFolder;

  protected static void runJackAndExitOnError(@Nonnull Options options) {
    try {
      run(options);
    } catch (NothingToDoException e1) {
      // End normally since there is nothing to do
    } catch (ConfigurationException exceptions) {
      System.err.println(exceptions.getNextExceptionCount() + " error"
          + (exceptions.getNextExceptionCount() > 1 ? "s" : "")
          + " during configuration. Try --help-properties for help.");
      for (ChainedException exception : exceptions) {
        System.err.println("  " + exception.getMessage());
      }

      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (IllegalOptionsException e) {
      System.err.println(e.getMessage());
      System.err.println("Try --help for help.");

      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (FrontendCompilationException e) {
      // Cause exception has already been logged
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (JackUserException e) {
      System.err.println(e.getMessage());
      logger.log(Level.FINE, "Jack user exception:", e);
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (JackLoadingException e) {
      System.err.println(e.getMessage());
      logger.log(Level.FINE, "Jack loading exception:", e);
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (OutOfMemoryError e) {
      printExceptionMessage(e, "Out of memory error.");
      System.err.println("Try increasing heap size with java option '-Xmx<size>'");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Out of memory error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (StackOverflowError e) {
      printExceptionMessage(e, "Stack overflow error.");
      System.err.println("Try increasing stack size with java option '-Xss<size>'");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Stack overflow error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (VirtualMachineError e) {
      printExceptionMessage(e, "Virtual machine error: " + e.getClass() + ".");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Virtual machine error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (UnrecoverableException e) {
      System.err.println("Unrecoverable error: " + e.getMessage());
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Unrecoverable exception:", e);
      System.exit(ExitStatus.FAILURE_UNRECOVERABLE);
    } catch (Throwable e) {
      System.err.println("Internal compiler error.");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Internal compiler error:", e);

      System.exit(ExitStatus.FAILURE_INTERNAL);
    }
  }

  public static void run(@Nonnull Options options) throws ConfigurationException,
      IllegalOptionsException, NothingToDoException, JackUserException {

    RunnableHooks hooks = new RunnableHooks();
    List<String> ecjArgsSave = new ArrayList<String>(options.getEcjArguments());
    options.checkValidity(hooks);
    if (!ecjArgsSave.isEmpty()) {
      options.setEcjArguments(ecjArgsSave);
    }
    ThreadConfig.setConfig(options.getConfig());

    jackFilesFolder = new File(ThreadConfig.get(
        JackIncremental.COMPILER_STATE_OUTPUT_DIR).getFile(), "jackFiles");

    // Add options to control incremental support
    options.addProperty(Options.GENERATE_JACK_FILE.getName(), "true");
    options.addProperty(Options.JACK_OUTPUT_CONTAINER_TYPE.getName(), "dir");
    assert jackFilesFolder != null;
    options.addProperty(Options.JACK_FILE_OUTPUT_DIR.getName(), jackFilesFolder.getAbsolutePath());

    compilerState = new CompilerState(ThreadConfig.get(JackIncremental.COMPILER_STATE_OUTPUT_DIR));

    if (isIncrementalCompilation(options)) {
      logger.log(Level.INFO, "Incremental compilation");

      List<String> javaFilesNames = getJavaFilesSpecifiedOnCommandLine(options);

      getCompilerState().read();

      Map<String, Set<String>> fileDependencies = getCompilerState().computeDependencies();
      printDependencyStat(fileDependencies);
      logger.log(Level.FINE, "Compiler state {0}", getCompilerState());
      logger.log(Level.FINE, "File dependencies {0}", dependenciesToString(fileDependencies));

      Set<String> filesToRecompile = getFilesToRecompile(fileDependencies, options, javaFilesNames);

      if (!filesToRecompile.isEmpty()) {
        logger.log(Level.INFO, "{0} Files to recompile {1}",
            new Object[] {Integer.valueOf(filesToRecompile.size()), filesToRecompile});

        File outDexFile = options.getOutputFile();
        updateOptions(options, filesToRecompile);

        logger.log(Level.INFO, "Update compiler state");
        getCompilerState().updateCompilerState(filesToRecompile);

        logger.log(Level.INFO, "Generate {0}", options.getOutputFile());
        logger.log(Level.INFO, "Ecj options {0}", options.getEcjArguments());
        Jack.run(options);

        logger.log(Level.INFO, "Merge {0} with {1}",
            new Object[] {outDexFile, options.getOutputFile()});
        mergeDexFiles(options, outDexFile);
      } else {
        logger.log(Level.INFO, "No files to recompile");
      }
    } else {
      Jack.run(options);
    }
  }

  @Nonnull
  public static CompilerState getCompilerState() throws JackUserException {
    if (compilerState == null) {
      throw new JackUserException(
          "Incremental support must be used with experimental Main class from "
          + "com.android.jack.experimental.incremental");
    }
    return compilerState;
  }

  private static String dependenciesToString(@Nonnull Map<String, Set<String>> fileDependencies) {
    StringBuilder builder = new StringBuilder();
    builder.append(TextUtils.LINE_SEPARATOR);
    builder.append("*Dependencies list*");
    builder.append(TextUtils.LINE_SEPARATOR);

    for (Map.Entry<String, Set<String>> entry : fileDependencies.entrySet()) {
      builder.append(entry.getKey());
      builder.append("->");
      builder.append(entry.getValue());
      builder.append(TextUtils.LINE_SEPARATOR);
    }

    return (builder.toString());
  }

  private static void printDependencyStat(@Nonnull Map<String, Set<String>> fileDependencies) {
    int dependencyNumber = 0;
    int maxDependencyNumber = -1;
    int minDependencyNumber = -1;

    for (Set<String> dependency : fileDependencies.values()) {
      int currentDepSize = dependency.size();
      dependencyNumber += currentDepSize;
      if (minDependencyNumber == -1 || minDependencyNumber > currentDepSize) {
        minDependencyNumber = currentDepSize;
      }
      if (maxDependencyNumber == -1 || maxDependencyNumber < currentDepSize) {
        maxDependencyNumber = currentDepSize;
      }
    }

    logger.log(
        Level.INFO,
        "There are {0} dependencies, with {1} files per dependency in average",
        new Object[] {Integer.valueOf(fileDependencies.size()),
            Double.valueOf((double) dependencyNumber / (double) fileDependencies.size())});
    logger.log(Level.INFO, "Dependencies are at minimun {0} and at maximun {1}", new Object[] {
        Integer.valueOf(minDependencyNumber), Integer.valueOf(maxDependencyNumber)});
  }

  private static void mergeDexFiles(@Nonnull Options options, @Nonnull File outDexFile)
      throws JackIOException {
    try {
      DexBuffer merged =
          new DexMerger(new DexBuffer(options.getOutputFile()), new DexBuffer(outDexFile),
              CollisionPolicy.KEEP_FIRST).merge(false);
      merged.writeTo(outDexFile);
    } catch (IOException e) {
      throw new JackIOException("Failed to merge dex files: \""
          + options.getOutputFile().getAbsolutePath() + "\" and \"" + outDexFile.getAbsolutePath()
          + "\"", e);
    }
  }

  private static void updateOptions(@Nonnull Options options,
      @Nonnull Set<String> javaFilesToRecompile) {
    List<String> newEcjArguments = new ArrayList<String>();

    for (String ecjOptions : options.getEcjArguments()) {
      if (!ecjOptions.startsWith("@") && !ecjOptions.endsWith(".java")) {
        newEcjArguments.add(ecjOptions);
      }
    }

    for (String fileToRecompile : javaFilesToRecompile) {
      newEcjArguments.add(fileToRecompile);
    }

    // Move imported jack files from import to classpath option
    StringBuilder newClasspath = new StringBuilder(options.getClasspathAsString());

    if (jackFilesFolder != null) {
      newClasspath.append(File.pathSeparator);
      newClasspath.append(jackFilesFolder.getAbsolutePath());
    }

    List<File> jayceImport = options.getJayceImport();
    if (!jayceImport.isEmpty()) {
      for (File importedJackFiles : jayceImport) {
        newClasspath.append(File.pathSeparator);
        newClasspath.append(importedJackFiles.getAbsolutePath());
      }
      options.setJayceImports(Collections.<File>emptyList());
    }
    options.setClasspath(newClasspath.toString());

    if (!newEcjArguments.isEmpty()) {
      options.setEcjArguments(newEcjArguments);
    }
    options
        .setOutputFile(new File(options.getOutputFile().getParentFile(), INCREMENTAL_DEX_OUTPUT));
  }

  @Nonnull
  private static Set<String> getFilesToRecompile(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull Options options, @Nonnull List<String> javaFileNames) throws JackUserException {
    Set<String> filesToRecompile = new HashSet<String>();

    filesToRecompile.addAll(getModifiedFiles(fileDependencies, options, javaFileNames));
    filesToRecompile.addAll(getAddedFiles(fileDependencies, javaFileNames));
    filesToRecompile.addAll(getDeletedFiles(fileDependencies, javaFileNames));

    return filesToRecompile;
  }

  @Nonnull
  private static Set<String> getDeletedFiles(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull List<String> javaFileNames) throws JackUserException {
    Set<String> deletedFiles = new HashSet<String>();
    Iterator<String> previousFilesIt = getCompilerState().getJavaFilename().iterator();

    while (previousFilesIt.hasNext()) {
      String previousFileName = previousFilesIt.next();
      if (!javaFileNames.contains(previousFileName)) {
        logger.log(Level.INFO, "{0} was deleted", previousFileName);
        deletedFiles.addAll(fileDependencies.get(previousFileName));
        deleteJackFilesFromJavaFiles(previousFileName);
        previousFilesIt.remove();
      }
    }

    return deletedFiles;
  }

  private static void deleteJackFilesFromJavaFiles(@Nonnull String javaFileName)
      throws JackUserException {
    for (String jackFileToRemove : getCompilerState()
        .getJacksFileNameFromJavaFileName(javaFileName)) {
      File jackFile = new File(jackFileToRemove);
      if (jackFile.exists() && !jackFile.delete()) {
        throw new JackIOException("Failed to delete file " + jackFileToRemove);
      }
    }
  }

  @Nonnull
  private static Set<String> getAddedFiles(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull List<String> javaFileNames) {
    Set<String> addedFiles = new HashSet<String>();
    Set<String> previousFiles = fileDependencies.keySet();

    for (String javaFileName : javaFileNames) {
      if (!previousFiles.contains(javaFileName)) {
        logger.log(Level.INFO, "{0} was added", javaFileName);
        addedFiles.add(javaFileName);
      }
    }

    return addedFiles;
  }

  @Nonnull
  private static Set<String> getModifiedFiles(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull Options options, @Nonnull List<String> javaFileNames) throws JackUserException {
    Set<String> modifiedFiles = new HashSet<String>();

    for (Map.Entry<String, Set<String>> previousFileEntry : fileDependencies.entrySet()) {
      String javaFileName = previousFileEntry.getKey();
      File javaFile = new File(javaFileName);
      if (javaFileNames.contains(javaFileName) &&
          javaFile.lastModified() > options.getOutputFile().lastModified()) {
        logger.log(Level.INFO, "{0} was modified", javaFileName);
        modifiedFiles.add(javaFileName);
        modifiedFiles.addAll(previousFileEntry.getValue());
        deleteJackFilesFromJavaFiles(javaFileName);
      }
    }

    return modifiedFiles;
  }

  @Nonnull
  private static List<String> getJavaFilesSpecifiedOnCommandLine(@Nonnull Options options)
      throws NothingToDoException, IllegalOptionsException {
    assert !options.getEcjArguments().isEmpty();

    org.eclipse.jdt.internal.compiler.batch.Main compiler =
        new org.eclipse.jdt.internal.compiler.batch.Main(new PrintWriter(System.out),
            new PrintWriter(System.err), false /* exit */, null /* options */
            , null /* compilationProgress */
        );

    try {
      compiler.configure(options.getEcjArguments().toArray(
          new String[options.getEcjArguments().size()]));
      if (!compiler.proceed) {
        throw new NothingToDoException();
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalOptionsException(e.getMessage(), e);
    }

    ArrayList<String> javaFiles = new ArrayList<String>();
    for (String fileName : compiler.filenames) {
      File file = new File(fileName);
      assert file.exists();
      try {
        fileName = file.getCanonicalPath();
      } catch (IOException e) {
        // if we got exception keep the specified name
      }
      javaFiles.add(fileName);
    }

    return javaFiles;
  }

  private static boolean isIncrementalCompilation(@Nonnull Options options) {
    if (!options.getEcjArguments().isEmpty()
        && ThreadConfig.get(Options.GENERATE_DEX_FILE).booleanValue()
        && ThreadConfig.get(JackIncremental.GENERATE_COMPILER_STATE).booleanValue()
        && getCompilerState().exists()) {
      return true;
    }

    return false;
  }
}
