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

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.OptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Jack code coverage reporter.
 */
public class Main {
  public static void main(String[] args) {
    try {
      Options options = parseCommandLine(Arrays.asList(args));
      if (options.isHelpRequested()) {
        printUsage(System.out);
      } else {
        createReport(options);
      }
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      CmdLineParser parser = e.getParser();
      if (parser != null) {
        printUsage(System.err);
      } else {
        System.err.println("Try --help for help.");
      }
    } catch (IOException e) {
      e.printStackTrace(System.err);
      printErrorAndExit(ErrorCode.INERNAL_ERROR, e.getMessage());
    }
  }

  @Nonnull
  private static Options parseCommandLine(@Nonnull List<String> list) throws CmdLineException {
    Options options = new Options();
    CmdLineParser parser =
        new CmdLineParser(options, ParserProperties.defaults().withUsageWidth(100));
    parser.parseArgument(list);
    parser.stopOptionParsing();
    return options;
  }

  private static void printUsage(@Nonnull PrintStream printStream) {
    CmdLineParser parser =
        new CmdLineParser(new Options(), ParserProperties.defaults().withUsageWidth(100));

    // Prints one line with all required options.
    StringBuilder oneLineUsage = new StringBuilder("Usage:");
    for (OptionHandler<?> optionHandler : parser.getOptions()) {
      if (optionHandler.option.required()) {
        oneLineUsage.append(' ');
        oneLineUsage.append(optionHandler.option.toString());
        oneLineUsage.append(' ');
        oneLineUsage.append(optionHandler.option.metaVar());
        if (optionHandler.option.isMultiValued()) {
          oneLineUsage.append(" ...");
        }
      }
    }
    oneLineUsage.append(" [<options>]");
    printStream.println(oneLineUsage.toString());

    // Print all options with their usage.
    printStream.println();
    printStream.println("Options:");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    parser.printUsage(outputStream);
    printStream.append(outputStream.toString());
  }

  private static void createReport(@Nonnull Options options) throws IOException {
    List<File> coverageDescriptionFiles = options.getCoverageDescriptionFiles();
    for (File coverageDescriptionFile : coverageDescriptionFiles) {
      checkFileExists(coverageDescriptionFile);
      checkCanReadFromFile(coverageDescriptionFile);
    }

    List<File> coverageExecutionDataFiles = options.getCoverageExecutionFiles();
    for (File coverageExecutionDataFile : coverageExecutionDataFiles) {
      checkFileExists(coverageExecutionDataFile);
      checkCanReadFromFile(coverageExecutionDataFile);
    }

    File reportOutputFile = options.getReportOutputDirectory();
    assert reportOutputFile != null;
    checkDirectoryExists(reportOutputFile);
    checkCanWriteToFile(reportOutputFile);

    List<File> sourceFilesDirectories = options.getSourceFilesDirectories();
    for (File sourceFilesDirectory : sourceFilesDirectories) {
      checkDirectoryExists(sourceFilesDirectory);
      checkCanReadFromFile(sourceFilesDirectory);
    }

    String reportName = options.getReportName();
    ReportType reportType = options.getReportType();
    String outputEncoding = options.getOutputReportEncoding();

    // Load coverage execution files.
    ExecFileLoader loader = new ExecFileLoader();
    for (File coverageExecutionDataFile : coverageExecutionDataFiles) {
      loader.load(coverageExecutionDataFile);
    }

    // Analyze coverage.
    CoverageBuilder coverageBuilder = new CoverageBuilder();
    JackCoverageAnalyzer analyzer =
        new JackCoverageAnalyzer(loader.getExecutionDataStore(), coverageBuilder);
    for (File coverageDescriptionFile : coverageDescriptionFiles) {
      analyzer.analyze(coverageDescriptionFile);
    }
    IBundleCoverage bundleCoverage = coverageBuilder.getBundle(reportName);

    // Create report.
    IReportVisitor visitor = null;
    switch (reportType) {
      case HTML:
        HTMLFormatter htmlFormatter = new HTMLFormatter();
        htmlFormatter.setOutputEncoding(outputEncoding);
        visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportOutputFile));
        break;

      case XML:
        XMLFormatter xmlFormatter = new XMLFormatter();
        xmlFormatter.setOutputEncoding(outputEncoding);
        File xmlReportFile = new File(reportOutputFile, "report.xml");
        visitor = xmlFormatter.createVisitor(new FileOutputStream(xmlReportFile));
        break;

      case CSV:
        CSVFormatter csvFormatter = new CSVFormatter();
        csvFormatter.setOutputEncoding(outputEncoding);
        File csvReportFile = new File(reportOutputFile, "report.csv");
        visitor = csvFormatter.createVisitor(new FileOutputStream(csvReportFile));
        break;

      default:
        throw new IllegalArgumentException("Unknown report type");
    }

    if (visitor == null) {
      throw new NullPointerException("Report's visitor has not been created");
    }


    // Initialize the report with all of the execution and session
    // information. At this point the report doesn't know about the
    // structure of the report being created
    visitor.visitInfo(
        loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());

    // Populate the report structure with the bundle coverage information.
    // Call visitGroup if you need groups in your report.
    int tabWidth = options.getTabWidth();
    MultiSourceFileLocator sourceFileLocator = new MultiSourceFileLocator(tabWidth);
    final String sourceFilesEncoding = options.getInputSourceFilesEncoding();
    for (File sourceFilesDirectory : sourceFilesDirectories) {
      sourceFileLocator.add(
          new DirectorySourceFileLocator(sourceFilesDirectory, sourceFilesEncoding, tabWidth));
    }
    visitor.visitBundle(bundleCoverage, sourceFileLocator);

    // Signal end of structure information to allow report to write all
    // information out
    visitor.visitEnd();

    System.out.println("Created report at " + reportOutputFile);
  }

  private static void checkFileExists(@Nonnull File file) {
    if (!file.exists()) {
      printErrorAndExit(
          ErrorCode.USAGE_ERROR, MessageFormat.format("File {0} does not exist", file));
    }
  }

  private static void checkCanReadFromFile(@Nonnull File file) {
    if (!file.canRead()) {
      printErrorAndExit(
          ErrorCode.USAGE_ERROR, MessageFormat.format("Cannot read from file {0}", file));
    }
  }

  private static void checkCanWriteToFile(@Nonnull File file) {
    if (!file.canWrite()) {
      printErrorAndExit(
          ErrorCode.USAGE_ERROR, MessageFormat.format("Cannot write to file {0}", file));
    }
  }

  private static void checkDirectoryExists(@Nonnull File file) {
    checkFileExists(file);
    if (!file.isDirectory()) {
      printErrorAndExit(
          ErrorCode.USAGE_ERROR, MessageFormat.format("File {0} is not a directory", file));
    }
  }

  private static void printErrorAndExit(@Nonnull ErrorCode error, String msg) {
    System.err.println(msg);
    System.exit(error.getErrorCode());
  }
}
