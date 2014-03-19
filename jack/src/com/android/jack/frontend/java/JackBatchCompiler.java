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

import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.ecj.loader.jast.JAstClasspath;
import com.android.jack.ir.ast.JProgram;
import com.android.sched.util.log.LoggerFactory;

import org.eclipse.jdt.internal.compiler.batch.ClasspathDirectory;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.ClasspathSourceJar;
import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Entry-point to call JDT compiler.
 */
public class JackBatchCompiler extends Main {

  @Nonnull
  public static final String JACK_LOGICAL_PATH_ENTRY = "<jack-logical-entry>";

  @Nonnull
  private final JayceFileImporter jayceImporter;

  @Nonnull
  private final java.util.logging.Logger jackLogger =
    LoggerFactory.getLogger();

  @Nonnull
  private final JProgram program;

  public JackBatchCompiler(@Nonnull JProgram program,
      @Nonnull JayceFileImporter jayceFileImporter) {
    super(new PrintWriter(System.out), new PrintWriter(System.err), true, null, null);
    this.program = program;
    jayceImporter = jayceFileImporter;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
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
    } else if (JACK_LOGICAL_PATH_ENTRY.equals(currentClasspathName)) {
      paths.add(new JAstClasspath(currentClasspathName, program.getLookup(), null));
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

}
