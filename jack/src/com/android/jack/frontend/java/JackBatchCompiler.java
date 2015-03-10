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

package com.android.jack.frontend.java;

import com.google.common.base.Joiner;

import com.android.jack.JackUserException;
import com.android.jack.Options;
import com.android.jack.ecj.loader.jast.JAstClasspath;
import com.android.jack.ir.ast.JSession;
import com.android.jack.reporting.Reporter;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.log.LoggerFactory;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BatchAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.batch.ClasspathDirectory;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.ClasspathSourceJar;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Entry-point to call ECJ compiler.
 */
public class JackBatchCompiler extends Main {
  /**
   * Error used to transport {@link RuntimeException} through ECJ catch.
   */
  public static class TransportExceptionAroundEcjError extends Error {

    private static final long serialVersionUID = 1L;

    public TransportExceptionAroundEcjError(@Nonnull RuntimeException cause) {
      super(cause);
    }

    @Nonnull
    @Override
    public RuntimeException getCause() {
      return (RuntimeException) super.getCause();
    }

  }

  /**
   * Error used to transport {@link JackUserException} through ECJ catch.
   */
  public static class TransportJUEAroundEcjError extends Error {

    private static final long serialVersionUID = 1L;

    public TransportJUEAroundEcjError(@Nonnull JackUserException cause) {
      super(cause);
    }

    @Nonnull
    @Override
    public JackUserException getCause() {
      return (JackUserException) super.getCause();
    }

  }

  @Nonnull
  private static final String USE_SINGLE_THREAD_SYSPROP = "jdt.compiler.useSingleThread";

  @Nonnull
  private static final java.util.logging.Logger jackLogger = LoggerFactory.getLogger();

  @Nonnull
  private final JSession session;

  public JackBatchCompiler(@Nonnull JSession session) {
    super(new PrintWriter(System.out), new PrintWriter(System.err),
        false /* systemExitWhenFinished */, null /* customDefaultOptions */,
        null /* compilationProgress */);
    this.session = session;
  }

  @Nonnull
  Reporter getReporter() {
    return session.getReporter();
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  protected void addNewEntry(ArrayList paths,
      String currentClasspathName,
      ArrayList currentRuleSpecs,
      String customEncoding,
      String destPath,
      boolean isSourceOnly,
      boolean rejectDestinationPathOnJars) {

    if (isSourceOnly) {
      // no need for special support on source only entries.
      super.addNewEntry(paths,
          currentClasspathName,
          currentRuleSpecs,
          customEncoding,
          destPath,
          isSourceOnly,
          rejectDestinationPathOnJars);
    } else {

      /* Call super so that it make the required checks and prepare ClasspathDex
       * constructor arguments (accessRuleSet and destinationPath)
       */
      ArrayList<ClasspathLocation> tmpPaths =
          new ArrayList<ClasspathLocation>(1);
      super.addNewEntry(tmpPaths,
          currentClasspathName,
          currentRuleSpecs,
          customEncoding,
          destPath,
          isSourceOnly,
          rejectDestinationPathOnJars);

      if (tmpPaths.size() == 1) {
        ClasspathLocation path = tmpPaths.get(0);
        assert !(path instanceof ClasspathSourceJar);
        if (path instanceof ClasspathDirectory) {
          session.getUserLogger().log(
              Level.WARNING,
              "Invalid entry in classpath or bootclasspath: directories are " +
                  "not supported: \"{0}\"",
              currentClasspathName);
        } else {
          assert path instanceof ClasspathJar;
          File pathFile = new File(currentClasspathName);
          if (pathFile.exists()) {
            session.getUserLogger().log(Level.WARNING,
                "Invalid entry in classpath or bootclasspath: ''{0}''", currentClasspathName);
          } else {
            session.getUserLogger().log(
                Level.WARNING,
                "Invalid entry in classpath or bootclasspath: " +
                    "missing file ''{0}''",
                currentClasspathName);
          }
        }
      } else {
        // necessary error reporting has already be done by super.addNewEntry
      }

    }

  }

  @Override
  public boolean compile(String[] argv) {
    return super.compile(argv);
  }

  @Override
  public void performCompilation() throws TransportJUEAroundEcjError,
      TransportExceptionAroundEcjError {
    startTime = System.currentTimeMillis();

    compilerOptions = new CompilerOptions(options);
    compilerOptions.performMethodsFullRecovery = false;
    compilerOptions.performStatementsRecovery = false;
    compilerOptions.produceReferenceInfo = produceRefInfo;
    compilerOptions.verbose = verbose;

    // Initialize the current instance of the JackBatchCompiler
    FileSystem environment = getLibraryAccess();
    batchCompiler = new JAstBuilder(environment,
        getHandlingPolicy(),
        compilerOptions,
        getBatchRequestor(),
        getProblemFactory(),
        out,
        progress,
        session);
    batchCompiler.remainingIterations = maxRepetition - currentRepetition;
    batchCompiler.useSingleThread = Boolean.getBoolean(USE_SINGLE_THREAD_SYSPROP);

    if (compilerOptions.processAnnotations) {
      initializeAnnotationProcessorManager();
    }

    // Compiles every compilation units with logging support.
    logger.startLoggingSources();
    try {
      batchCompiler.compile(getCompilationUnits());
    } catch (IllegalArgumentException e) {
      // IllegalArgumentException should no longer exist
      throw new AssertionError(e);
    } catch (TransportJUEAroundEcjError e) {
      throw e;
    } catch (RuntimeException e) {
      throw new TransportExceptionAroundEcjError(e);
    } finally {
      logger.endLoggingSources();
      // Clean up environment
      environment.cleanup();
    }

    if (extraProblems != null) {
      loggingExtraProblems();
      extraProblems = null;
    }

    logger.printStats();
  }

  @SuppressWarnings("unchecked")
  @CheckForNull
  public List<CategorizedProblem> getExtraProblems() {
    return extraProblems;
  }

  @Override
  public void configure(String[] argv) {
    super.configure(argv);
    checkedClasspaths = new FileSystem.Classpath[] {
        new JAstClasspath("<jack-logical-entry>", session.getLookup(), null)};
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void initialize(PrintWriter outWriter, PrintWriter errWriter, boolean systemExit,
      Map customDefaultOptions, CompilationProgress compilationProgress) {
    super.initialize(outWriter, errWriter, systemExit, customDefaultOptions, compilationProgress);
    logger = new EcjLogger(this, outWriter, errWriter, this);
  }

  @Override
  public CompilationUnit[] getCompilationUnits() {
    CompilationUnit[] cu = new CompilationUnit[filenames.length];
    int idx = 0;
    for (String fileName : filenames) {
      try {
        new InputStreamFile(fileName);
        cu[idx] = new CompilationUnit(null, fileName, encodings[idx]);
        idx++;
      } catch (WrongPermissionException e) {
        throw new JackUserException(e);
      } catch (NoSuchFileException e) {
        throw new JackUserException(e);
      } catch (NotFileException e) {
        throw new JackUserException(e);
      }
    }
    return cu;
  }

  @Override
  protected void initializeAnnotationProcessorManager() {
    List<String> processorArgs = new ArrayList<String>();
    Config config = ThreadConfig.getConfig();
    for (Map.Entry<String, String> entry :
      config.get(Options.ANNOTATION_PROCESSOR_OPTIONS).entrySet()) {
      processorArgs.add("-A" + entry.getKey() + "=" + entry.getValue());
    }
    if (config.get(Options.ANNOTATION_PROCESSOR_MANUAL).booleanValue()) {
      processorArgs.add("-processor");
      processorArgs.add(Joiner.on(',').join(config.get(Options.ANNOTATION_PROCESSOR_MANUAL_LIST)));
    }
    if (config.get(Options.ANNOTATION_PROCESSOR_PATH).booleanValue()) {
      processorArgs.add("-processorpath");
      processorArgs.add(getPathString(config.get(Options.ANNOTATION_PROCESSOR_PATH_LIST)));
    }
    processorArgs.add("-s");
    processorArgs.add(config.get(Options.ANNOTATION_PROCESSOR_SOURCE_OUTPUT_DIR).getPath());
    processorArgs.add("-d");
    processorArgs.add(config.get(Options.ANNOTATION_PROCESSOR_CLASS_OUTPUT_DIR).getPath());
    {
      processorArgs.add("-classpath");
      processorArgs.add(config.get(Options.CLASSPATH));
    }
    String[] args = processorArgs.toArray(new String[processorArgs.size()]);

    BatchAnnotationProcessorManager manager = new BatchAnnotationProcessorManager();
    manager.configure(this, args);
    manager.setOut(out);
    this.batchCompiler.annotationProcessorManager = manager;
  }

  @Nonnull
  private static String getPathString(@Nonnull List<FileOrDirectory> pathList) {
    StringBuilder path = new StringBuilder();
    for (Iterator<FileOrDirectory> iter = pathList.iterator();
        iter.hasNext();) {
      path.append(iter.next().getPath());
      if (iter.hasNext()) {
        path.append(File.pathSeparatorChar);
      }
    }
    return path.toString();
  }
}
