/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.reporting;

import com.android.jack.IllegalOptionsException;
import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.jack.test.TestsProperties;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.location.ColumnAndLineLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.log.LogFormatter;
import com.android.sched.util.log.LoggerConfiguration;
import com.android.sched.util.log.LoggerFactory;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import javax.annotation.Nonnull;

public class ReporterFormatTest {

  private File reportFile;

  private RunnableHooks hooks;

  @Before
  public void setUp() throws CannotCreateFileException, CannotChangePermissionException,
      ConfigurationException, IllegalOptionsException {
    // get rid of Sched-lib INFO logging:
    LoggerFactory.configure(new LoggerConfiguration() {

      @Override
      @Nonnull
      public List<PackageLevel> getLevels() {
        return Arrays.asList(new PackageLevel("", Level.WARNING));
      }

      @Override
      @Nonnull
      public Collection<Handler> getHandlers() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        handler.setLevel(Level.FINEST);
        return Collections.<Handler>singletonList(handler);
      }
    });

    Options options = new Options();
    hooks = new RunnableHooks();
    reportFile = TestTools.createTempFile("reporter", ".txt");
    options.addProperty(Reporter.REPORTER_WRITER.getName(), reportFile.getPath());
    options.checkValidity(hooks);
    Config config = options.getConfig();
    ThreadConfig.setConfig(config);
    Jack.getSession().setHooks(hooks);
  }

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  @Test
  public void printFilteredProblemTest_defaultReporter() throws IOException {

    File refFile = new File(TestsProperties.getJackRootDir(),
        "jack/tests/com/android/jack/reporting/defaultreporter-ref.txt");

    DefaultReporter reporter = new DefaultReporter();

    checkPrintFilteredProblem(reporter, refFile);
  }

  @Test
  public void printFilteredProblemTest_sdkReporter() throws IOException {

    File refFile = new File(TestsProperties.getJackRootDir(),
        "jack/tests/com/android/jack/reporting/sdkreporter-ref.txt");

    SdkReporter reporter = new SdkReporter();

    checkPrintFilteredProblem(reporter, refFile);
  }

  private void checkPrintFilteredProblem(@Nonnull CommonReporter reporter, @Nonnull File refFile)
      throws FileNotFoundException, IOException {
    try {
      FileLocation fileLoc = new FileLocation("/my/file");

      reporter.printFilteredProblem(ProblemLevel.WARNING, "message", /* location = */ null);
      reporter.printFilteredProblem(ProblemLevel.WARNING, "message", fileLoc);
      reporter.printFilteredProblem(ProblemLevel.ERROR, "message",
          new ColumnAndLineLocation(fileLoc, 2));
      reporter.printFilteredProblem(ProblemLevel.ERROR, "message",
          new ColumnAndLineLocation(fileLoc, 2, 3));
      reporter.printFilteredProblem(ProblemLevel.ERROR, "message",
          new ColumnAndLineLocation(fileLoc, 2, 3, 4, 5));
      reporter.printFilteredProblem(ProblemLevel.ERROR, "message",
          new ColumnAndLineLocation(fileLoc, 2, 2, 4, 5));
      reporter.printFilteredProblem(ProblemLevel.ERROR, "message",
          new ColumnAndLineLocation(fileLoc, 2, 2, 4, 4));
      reporter.printFilteredProblem(ProblemLevel.ERROR, "message",
          new ColumnAndLineLocation(fileLoc, 2, 3, 4, 4));
    } finally {
      hooks.runHooks();

      assertFilesAreEqual(refFile, reportFile);
    }
  }

  private void assertFilesAreEqual(@Nonnull File expected, @Nonnull File actual)
      throws FileNotFoundException, IOException {
    try (BufferedReader expectedReader = new BufferedReader(new FileReader(expected));
        BufferedReader actualReader = new BufferedReader(new FileReader(actual));) {
      String line;
      while ((line = expectedReader.readLine()) != null) {
        Assert.assertEquals(line, actualReader.readLine());
      }
      Assert.assertNull(actualReader.readLine());
    }
  }
}
