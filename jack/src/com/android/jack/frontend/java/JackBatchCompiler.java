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

import com.android.jack.JackUserException;
import com.android.jack.ecj.loader.jast.JAstClasspath;
import com.android.jack.ir.ast.JSession;
import com.android.sched.util.log.LoggerFactory;

import org.eclipse.jdt.internal.compiler.batch.ClasspathDirectory;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.ClasspathSourceJar;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;

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
  private final java.util.logging.Logger jackLogger =
    LoggerFactory.getLogger();

  @Nonnull
  private final JSession session;

  public JackBatchCompiler(@Nonnull JSession session) {
    super(new PrintWriter(System.out), new PrintWriter(System.err),
        false /* systemExitWhenFinished */, null /* customDefaultOptions */,
        null /* compilationProgress */);
    this.session = session;
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
          jackLogger.log(
              Level.WARNING,
              "Invalid entry in classpath or bootclasspath: directories are " +
                  "not supported: \"{0}\"",
              currentClasspathName);
        } else {
          assert path instanceof ClasspathJar;
          File pathFile = new File(currentClasspathName);
          if (pathFile.exists()) {
            jackLogger.log(Level.WARNING, "Invalid entry in classpath or bootclasspath: ''{0}''",
                currentClasspathName);
          } else {
            jackLogger.log(
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
      // ECJ is throwing this one for missing source files, let them be reported correctly.
      // Most other IllegalArgumentException are wrapped by JAstBuilder.
      throw new TransportJUEAroundEcjError(new JackUserException(e));
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

  @Override
  public void configure(String[] argv) {
    super.configure(argv);
    checkedClasspaths = new FileSystem.Classpath[] {
        new JAstClasspath("<jack-logical-entry>", session.getLookup(), null)};
  }
}
