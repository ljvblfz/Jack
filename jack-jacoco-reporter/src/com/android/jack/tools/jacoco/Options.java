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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Jack coverage report tool command-line options.
 */
public class Options {
  @Option(name = "--metadata-file", metaVar = "<file>",
      usage = "coverage description file generated at compilation-time (repeatable)",
      required = true)
  @Nonnull
  private List<File> coverageDescriptionFiles = new ArrayList<File>();

  @Option(name = "--coverage-file", metaVar = "<file>",
      usage = "coverage execution file generated at run-time (repeatable)", required = true)
  @Nonnull
  private List<File> coverageExecutionFiles = new ArrayList<File>();

  @Option(name = "--report-dir", metaVar = "<dir>",
      usage = "the directory where the report must be generated.", required = true)
  @CheckForNull
  private File reportOutputDirectory;

  @Option(name = "-h", aliases = "--help", usage = "show help", help = true)
  private boolean showHelp;

  @Option(name = "--source-dir", metaVar = "<dir>",
      usage = "a directory containing Java source files (repeatable)")
  @Nonnull
  private List<File> sourceFilesDirectories = new ArrayList<File>();

  @Option(name = "--report-name", metaVar = "<name>", usage = "the name of the report")
  @Nonnull
  private String reportName = "Report";

  @Option(name = "--report-type", metaVar = "<type>",
      usage = "the type of the report (default is HTML)")
  @Nonnull
  private ReportType reportType = ReportType.HTML;

  @Option(name = "--output-encoding", metaVar = "<encoding>",
      usage = "the encoding for output report files (default is UTF-8)")
  @Nonnull
  private String outputReportEncoding = "UTF-8";

  @Option(name = "--input-encoding", metaVar = "<encoding>",
      usage = "the encoding for input source files (default is UTF-8)")
  @Nonnull
  private String inputSourceFilesEncoding = "UTF-8";

  @Option(name = "--tab-width", metaVar = "<value>",
      usage = "the width of tabs in source code (default is 4)")
  private int tabWidth = 4;

  @Nonnull
  public List<File> getCoverageDescriptionFiles() {
    return coverageDescriptionFiles;
  }

  @Nonnull
  public List<File> getCoverageExecutionFiles() {
    return coverageExecutionFiles;
  }

  @Nonnull
  public List<File> getSourceFilesDirectories() {
    return sourceFilesDirectories;
  }

  @CheckForNull
  public File getReportOutputDirectory() {
    return reportOutputDirectory;
  }

  @Nonnull
  public String getReportName() {
    return reportName;
  }

  @Nonnull
  public ReportType getReportType() {
    return reportType;
  }

  @Nonnull
  public String getOutputReportEncoding() {
    return outputReportEncoding;
  }

  @Nonnull
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
