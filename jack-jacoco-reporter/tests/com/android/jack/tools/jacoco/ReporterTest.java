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

package com.android.jack.tools.jacoco;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class ReporterTest {
  /**
   * Checks the {@link Options} and {@link Reporter} classes use the same default values.
   */
  @Test
  public void testDefaultOptions() {
    Options options = new Options();
    Reporter reporter = new Reporter();

    Assert.assertEquals(options.getCoverageDescriptionFiles().size(),
        reporter.getCoverageDescriptionFiles().size());
    Assert.assertEquals(options.getCoverageExecutionFiles().size(),
        reporter.getCoverageExecutionDataFiles().size());
    Assert.assertEquals(options.getSourceFilesDirectories().size(),
        reporter.getSourceFilesDirectories().size());
    Assert.assertEquals(options.getReportName(), reporter.getReportName());
    Assert.assertEquals(options.getReportType(), reporter.getReportType());
    Assert.assertEquals(options.getOutputReportEncoding(), reporter.getOutputEncoding());
    Assert.assertEquals(options.getInputSourceFilesEncoding(), reporter.getSourceFilesEncoding());
    Assert.assertEquals(options.getTabWidth(), reporter.getTabWidth());
  }

  private static final File DUMMY_FILE = new File("foo");

  private Reporter reporter = new Reporter();
  private File tempCoverageDescriptionFile;
  private File tempCoverageExecutionFile;
  private File tempOutputReportDir;

  @Before
  public void setUp() throws IOException {
    tempCoverageDescriptionFile = Files.createTempFile("coverage", "em.tmp").toFile();
    tempCoverageExecutionFile = Files.createTempFile("coverage", "ec.tmp").toFile();
    tempOutputReportDir = Files.createTempDirectory("coverage-report").toFile();
  }

  @After
  public void tearDown() {
    boolean tempCoverageDescriptionFileDeleted = tempCoverageDescriptionFile.delete();
    boolean tempCoverageExecutionFileDeleted = tempCoverageExecutionFile.delete();
    boolean tempOutputReportDirDeleted = tempOutputReportDir.delete();

    Assert.assertTrue(tempCoverageDescriptionFileDeleted);
    Assert.assertTrue(tempCoverageExecutionFileDeleted);
    Assert.assertTrue(tempOutputReportDirDeleted);
  }

  @Test
  public void testNoCoverageDescriptionFile() throws IOException {
    // No coverage description file must throw a ReporterException.
    try {
      reporter.createReport();
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testNullCoverageDescriptionFile() throws ReporterException {
    // Non-existent coverage description file must throw a ReporterException.
    try {
      reporter.setCoverageDescriptionFiles(Collections.<File>singletonList(null));
      Assert.fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testInvalidCoverageDescriptionFile() {
    // Non-existent coverage description file must throw a ReporterException.
    try {
      reporter.setCoverageDescriptionFiles(Collections.singletonList(DUMMY_FILE));
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testNoCoverageExecutionFile() throws IOException, ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));

    // No coverage execution file must throw a ReporterException.
    try {
      reporter.createReport();
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testInvalidCoverageExecutionFile() throws ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));
    try {
      reporter.setCoverageExecutionDataFiles(Collections.singletonList(DUMMY_FILE));
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testNoOutputReportDir() throws IOException, ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));
    reporter.setCoverageExecutionDataFiles(Collections.singletonList(tempCoverageExecutionFile));

    // No report output directory must throw a ReporterException.
    try {
      reporter.createReport();
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testInvalidOutputReportDir() throws ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));
    reporter.setCoverageExecutionDataFiles(Collections.singletonList(tempCoverageExecutionFile));
    try {
      reporter.setReportOutputDirectory(DUMMY_FILE);
      Assert.fail();
    } catch (ReporterException expected) {
    }
    // Testing with an existing file but not a directory.
    try {
      reporter.setReportOutputDirectory(tempCoverageExecutionFile);
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testNullSourceDirectory() throws ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));
    reporter.setCoverageExecutionDataFiles(Collections.singletonList(tempCoverageExecutionFile));
    reporter.setReportOutputDirectory(tempOutputReportDir);
    try {
      reporter.setSourceFilesDirectories(Collections.<File>singletonList(null));
      Assert.fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testInvalidSourceDirectory() throws ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));
    reporter.setCoverageExecutionDataFiles(Collections.singletonList(tempCoverageExecutionFile));
    reporter.setReportOutputDirectory(tempOutputReportDir);
    try {
      reporter.setSourceFilesDirectories(Collections.singletonList(DUMMY_FILE));
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }

  @Test
  public void testInvalidMappingFile() throws ReporterException {
    reporter.setCoverageDescriptionFiles(Collections.singletonList(tempCoverageDescriptionFile));
    reporter.setCoverageExecutionDataFiles(Collections.singletonList(tempCoverageExecutionFile));
    reporter.setReportOutputDirectory(tempOutputReportDir);
    try {
      reporter.setMappingFile(DUMMY_FILE);
      Assert.fail();
    } catch (ReporterException expected) {
    }
  }
}
