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

package com.android.jack;

import com.android.jack.CLILogConfiguration.LogConfigurationException;
import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.location.NoLocation;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Command line to run the jack compiler.
 */
public abstract class Main extends CommandLine {

  /**
   * Runs the jack compiler from the command line
   *
   * @param args supported arguments are the same as for the ecj batch compiler.
   */
  public static void main(@Nonnull String[] args) {
    try {
      CLILogConfiguration.setupLogs();
    } catch (LogConfigurationException e) {
      System.err.println("Failed to setup logs: " + e.getMessage());
      System.exit(ExitStatus.FAILURE_USAGE);
    }

    if (args.length == 0) {
      printVersion(System.out);
      System.err.println("Try --help for help.");
      System.exit(ExitStatus.SUCCESS);
    }

    try {
      TokenIterator iterator = new TokenIterator(new NoLocation(), args);
      List<String> list = new ArrayList<String>();
      while (iterator.hasNext()) {
        list.add(iterator.next());
      }
      Options options = parseCommandLine(list);

      if (options.askForHelp()) {
        printUsage(System.out);
        System.exit(ExitStatus.SUCCESS);
      }

      if (options.askForPropertiesHelp()) {
        printHelpProperties(System.out, options);
        System.exit(ExitStatus.SUCCESS);
      }

      if (options.askForVersion()) {
        printVersion(System.out);
        System.exit(ExitStatus.SUCCESS);
      }

      // Compile
      System.exit(runJack(System.err, options));
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      CmdLineParser parser = e.getParser();
      if (parser != null) {
        printUsage(System.err);
      } else {
        System.err.println("Try --help for help.");
      }

      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (IOException e) {
      System.err.println(e.getMessage());

      System.exit(ExitStatus.FAILURE_USAGE);
    }
  }

  @Nonnull
  public static Options parseCommandLine(@Nonnull List<String> list)
      throws CmdLineException {
    Options options = new Options();

    CmdLineParser parser =
        new CmdLineParser(options, ParserProperties.defaults().withUsageWidth(100));

    parser.parseArgument(list);
    parser.stopOptionParsing();

    return options;
  }

}
