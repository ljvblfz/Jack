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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;

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
    if (args.length == 0) {
      printVersion();
      System.err.println("Try --help for help.");
      System.exit(ExitStatus.SUCCESS);
    }

    try {
      Options options = parseCommandLine(args);

      if (options.askForHelp()) {
        printUsage(options);
        System.exit(ExitStatus.SUCCESS);
      }

      if (options.askForPropertiesHelp()) {
        printHelpProperties(options);
        System.exit(ExitStatus.SUCCESS);
      }

      if (options.askForEcjHelp()) {
        // ECJ help was already printed by Options.checkValidity()
        System.exit(ExitStatus.SUCCESS);
      }

      if (options.askForVersion()) {
        printVersion();
        System.exit(ExitStatus.SUCCESS);
      }

      // Compile
      runJackAndExitOnError(options);

      System.exit(ExitStatus.SUCCESS);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      CmdLineParser parser = e.getParser();
      if (parser != null) {
        printSubUsage(System.err);
      } else {
        System.err.println("Try --help for help.");
      }

      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (IOException e) {
      System.err.println(e.getMessage());

      System.exit(ExitStatus.FAILURE_INTERNAL);
    }
  }

  @Nonnull
  public static Options parseCommandLine(@Nonnull String[] args)
      throws CmdLineException {
    Options options = new Options();

    CmdLineParser parser = new CmdLineParser(options);
    parser.setUsageWidth(100);

    parser.parseArgument(args);
    parser.stopOptionParsing();

    return options;
  }
}
