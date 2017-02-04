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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsTest {
  private static Options parseOptions(String str) throws CmdLineException {
    String[] strings = str.split(" ");
    return Options.parseCommandLine(Arrays.asList(strings));
  }

  private static final File METADATA_FILE = new File("pathToMetadataFile");
  private static final File COVERAGE_FILE = new File("pathToCoverageFile");
  private static final File REPORT_DIR = new File("pathToReportDir");

  private List<String> strings = new ArrayList<String>();

  @Before
  public void setUp() {
    resetCommandLine();
  }

  private void resetCommandLine() {
    strings.clear();
    strings.add("--metadata-file");
    strings.add(METADATA_FILE.getPath());
    strings.add("--coverage-file");
    strings.add(COVERAGE_FILE.getPath());
    strings.add("--report-dir");
    strings.add(REPORT_DIR.getPath());
  }

  @Test
  public void testMinimum() throws CmdLineException {
    Options options = Options.parseCommandLine(strings);
    assertNotNull(options);
    assertNotNull(options.getCoverageExecutionFiles());
    assertNotNull(options.getCoverageDescriptionFiles());
    assertNotNull(options.getReportOutputDirectory());
    assertNotNull(options.getSourceFilesDirectories());
    assertNotNull(options.getReportType());
    assertEquals(1, options.getCoverageDescriptionFiles().size());
    assertEquals(1, options.getCoverageExecutionFiles().size());
    assertEquals(METADATA_FILE.getPath(), options.getCoverageDescriptionFiles().get(0).getPath());
    assertEquals(COVERAGE_FILE.getPath(), options.getCoverageExecutionFiles().get(0).getPath());
    assertEquals(REPORT_DIR.getPath(), options.getReportOutputDirectory().getPath());
    assertTrue(options.getSourceFilesDirectories().isEmpty());
    assertEquals(ReportType.HTML, options.getReportType());
    assertEquals("UTF-8", options.getOutputReportEncoding());
    assertEquals("UTF-8", options.getInputSourceFilesEncoding());
    assertEquals(4, options.getTabWidth());
    assertFalse(options.askForHelp());
    assertFalse(options.askForVersion());
  }

  @Test
  public void testReport() throws CmdLineException {
    for (ReportType type : ReportType.values()) {
      strings.add("--report-type");
      strings.add(type.name());
      Options options = Options.parseCommandLine(strings);
      assertNotNull(options);
      assertEquals(options.getReportType(), type);

      // Test in lower case mode.
      resetCommandLine();
      strings.add("--report-type");
      strings.add(type.name().toLowerCase());
      options = Options.parseCommandLine(strings);
      assertNotNull(options);
      assertEquals(options.getReportType(), type);
    }
  }

  @Test
  public void testHelp() throws CmdLineException {
    Options options = parseOptions("-h");
    assertTrue(options.askForHelp());

    options = parseOptions("--help");
    assertTrue(options.askForHelp());
  }

  @Test
  public void testVersion() throws CmdLineException {
    Options options = parseOptions("--version");
    assertTrue(options.askForVersion());
  }

  @Test
  public void testEmpty() {
    try {
      parseOptions("");
      fail();
    } catch (CmdLineException expected) {
    }
  }
}
