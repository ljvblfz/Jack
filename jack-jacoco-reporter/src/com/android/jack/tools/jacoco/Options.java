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

import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Jack coverage report tool command-line options.
 */
public class Options {
  @Option(name = "--metadata-file", metaVar = "<file>",
      usage = "coverage description file generated at compilation-time", required = true)
  private File coverageDescriptionFile;

  @Option(name = "--coverage-file", metaVar = "<file>",
      usage = "coverage execution file generated at run-time", required = true)
  private File coverageExecutionFile;

  @Option(name = "--report-dir", metaVar = "<dir>",
      usage = "the directory where the report must be generated.", required = true)
  private File reportOutputDirectory;

  @Option(name = "-h", aliases = "--help", usage = "show help", help = true)
  private boolean showHelp;

  @Option(name = "--source-dir", metaVar = "<dir>",
      usage = "a directory containing Java source files")
  private List<File> sourceFilesDirectories = new ArrayList<File>();

  @Option(name = "--report-name", metaVar = "<name>", usage = "the name of the report")
  private String reportName = "Report";

  @Option(name = "--report-type", metaVar = "<type>",
      usage = "the type of the report (default is HTML)")
  private ReportType reportType = ReportType.HTML;

  @Option(name = "--output-encoding", metaVar = "<encoding>",
      usage = "the encoding for output report files (default is UTF-8)")
  private String outputReportEncoding = "UTF-8";

  @Option(name = "--input-encoding", metaVar = "<encoding>",
      usage = "the encoding for input source files (default is UTF-8)")
  private String inputSourceFilesEncoding = "UTF-8";

  @Option(name = "--tab-width", metaVar = "<value>",
      usage = "the width of tabs in source code (default is 4)")
  private int tabWidth = 4;

  public File getCoverageDescriptionFile() {
    return coverageDescriptionFile;
  }

  public File getCoverageExecutionFile() {
    return coverageExecutionFile;
  }

  public List<File> getSourceFilesDirectories() {
    return sourceFilesDirectories;
  }

  public File getReportOutputDirectory() {
    return reportOutputDirectory;
  }

  public String getReportName() {
    return reportName;
  }

  public ReportType getReportType() {
    return reportType;
  }

  public String getOutputReportEncoding() {
    return outputReportEncoding;
  }

  public String getInputSourceFilesEncoding() {
    return inputSourceFilesEncoding;
  }

  public int getTabWidth() {
    return tabWidth;
  }

  public boolean isHelpRequested() {
    return showHelp;
  }
}
