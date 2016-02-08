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

import com.android.sched.util.Version;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.OptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 * Main class for command-line usage.
 */
public class Main {
  /**
   * Generates a report from command-line.
   *
   * @param args the command-line arguments.
   */
  public static void main(@Nonnull String[] args) {
    try {
      Options options = Options.parseCommandLine(Arrays.asList(args));
      if (options.askForHelp()) {
        printUsage(System.out);
      } else if (options.askForVersion()) {
        printVersion(System.out);
      } else {
        Reporter reporter = createReporter(options);
        reporter.createReport();
      }
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      CmdLineParser parser = e.getParser();
      if (parser != null) {
        printUsage(System.err);
      } else {
        System.err.println("Try --help for help.");
      }
    } catch (ReporterException e) {
      e.printStackTrace(System.err);
      printErrorAndExit(ExitStatus.USAGE_ERROR, e.getMessage());
    } catch (IOException e) {
      e.printStackTrace(System.err);
      printErrorAndExit(ExitStatus.INTERNAL_ERROR, e.getMessage());
    }
  }

  private static void printVersion(@Nonnull PrintStream out) {
    try {
      Version version = new Version("jack-jacoco-reporter", Main.class.getClassLoader());
      out.println("Jack Jacoco reporter: " + version.getVerboseVersion());
    } catch (IOException e) {
      throw new AssertionError("Failed to find reporter version", e);
    }
  }

  @Nonnull
  private static Reporter createReporter(@Nonnull Options options) throws ReporterException {
    Reporter reporter = new Reporter();
    reporter.setCoverageExecutionDataFiles(options.getCoverageExecutionFiles());
    reporter.setCoverageDescriptionFiles(options.getCoverageDescriptionFiles());
    reporter.setReportOutputDirectory(options.getReportOutputDirectory());
    reporter.setSourceFilesDirectories(options.getSourceFilesDirectories());
    reporter.setReportName(options.getReportName());
    reporter.setReportType(options.getReportType());
    reporter.setOutputEncoding(options.getOutputReportEncoding());
    reporter.setSourceFilesEncoding(options.getInputSourceFilesEncoding());
    reporter.setTabWidth(options.getTabWidth());
    return reporter;
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

  private static void printErrorAndExit(@Nonnull ExitStatus error, @Nonnull String msg) {
    System.err.println(msg);
    System.exit(error.getExitStatus());
  }
}
