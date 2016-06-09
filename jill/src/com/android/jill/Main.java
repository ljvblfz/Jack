/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jill;

import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.SchedIOException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.NoLocation;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

/**
 *  Main class for Jill tool.
 */
public class Main {

  public static void main(@Nonnull String[] args) {
    Options options = null;

    try {
      options = getOptions(args);

      if (options.askForHelp()) {
        printUsage(System.out);
        System.exit(ExitStatus.SUCCESS);
      }

      if (options.askForVersion()) {
        System.out.println("Jill");
        System.out.println("Version: " + Jill.getVersion().getVerboseVersion() + '.');
        System.exit(ExitStatus.SUCCESS);
      }

      System.exit(runJill(System.err, options));

    } catch (CmdLineException e) {
      if (e.getMessage() != null) {
        System.err.println(e.getMessage());
      }
      CmdLineParser parser = e.getParser();
      if (parser != null) {
        printUsage(System.out);
      } else {
        System.err.println("Try --help for help");
      }
      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (IOException | SchedIOException e) {
      System.err.println(e.getMessage());

      System.exit(ExitStatus.FAILURE_USAGE);
    }

    System.exit(ExitStatus.FAILURE_UNKNOWN);
  }

  protected static int runJill(@Nonnull PrintStream err, @Nonnull Options options) {
    try {
      Jill.process(options);

    } catch (JillException e) {
        err.println("Binary transformation of " + options.getBinaryFile().getPath()
            + " failed: " + e.getMessage());
        if (options.isVerbose()) {
          e.printStackTrace(err);
        }

      return ExitStatus.FAILURE_INTERNAL;
    }
    return ExitStatus.SUCCESS;
  }

  @Nonnull
  public static Options getOptions(@Nonnull String[] args)
      throws CmdLineException, IOException, NoSuchElementException, WrongPermissionException,
      NoSuchFileException, NotFileOrDirectoryException, CannotReadException {
    Options options = new Options();


    CmdLineParser parser =
        new CmdLineParser(options, ParserProperties.defaults().withUsageWidth(100));

    TokenIterator iterator = new TokenIterator(new NoLocation(), args);
    List<String> list = new ArrayList<String>();
    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    parser.parseArgument(list);
    parser.stopOptionParsing();

    try {
      options.checkValidity();
    } catch (IllegalOptionsException e) {
      throw new CmdLineException(parser, e.getMessage(), e);
    }

    return options;
  }

  protected static void printUsage(@Nonnull PrintStream printStream) {
    CmdLineParser parser =
        new CmdLineParser(new Options(), ParserProperties.defaults().withUsageWidth(100));
    printStream.println("Usage: <options> <class files to be transformed contained recursively"
        + " in directories or in a zip/jar file>");
    printStream.println();
    printStream.println("Options:");
    parser.printUsage(printStream);
  }
}
