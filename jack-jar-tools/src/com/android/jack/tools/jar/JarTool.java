/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.tools.jar;

import com.android.sched.util.Version;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A command line for jar related tools.
 */
public class JarTool {

  /**
   * Success.
   */
  public static final int SUCCESS = 0;

  /**
   * Usage, syntax or configuration file error.
   */
  public static final int FAILURE_USAGE = 2;

  /**
   * Internal error.
   */
  public static final int FAILURE_INTERNAL = 3;

  /**
   * Processing error.
   */
  public static final int FAILURE_PROCESSING = 4;

  private static final String VERSION_FILE_SUFFIX = "-version.properties";

  @Option(name = "--version", usage = "display version")
  private boolean version;

  @Option(name = "--help", usage = "display help")
  private boolean help;

  @Option(name = "--list-version",
      usage = "display versions found in the given jar file")
  private boolean listVersion;

  @Argument()
  @CheckForNull
  private File input;

  public static void main(String[] args) {

    if (args.length == 0) {
      printUsage(System.err);
      System.exit(FAILURE_USAGE);
    }

    JarTool jarTool = new JarTool();

    CmdLineParser parser =
        new CmdLineParser(jarTool, ParserProperties.defaults().withUsageWidth(100));

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      printUsage(System.err);
      System.exit(FAILURE_USAGE);
    }

    parser.stopOptionParsing();

    System.exit(jarTool.run());
  }

  private int run() {
    if (version) {
      try {
        printVersion(System.out);
        return SUCCESS;
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println();
        System.err.println("Failed to read version");
        return FAILURE_INTERNAL;
      }
    } else if (help) {
      printUsage(System.out);
      return SUCCESS;
    } else {
      if (input == null) {
        System.err.println("A jar to process must be specified");
        printUsage(System.err);
        return  FAILURE_USAGE;
      } else {
        ZipFile zip = null;
        try {
          zip = new ZipFile(input);
        } catch (IOException e) {
          System.err.println("Failed to open zip '" + input + "'");
          return FAILURE_USAGE;
        }
        try {
          if (listVersion) {
            PrintStream printStream = System.out;
            for (Enumeration<? extends ZipEntry> entries = zip.entries();
                entries.hasMoreElements();) {
              ZipEntry entry = entries.nextElement();
              if (entry.getName().endsWith(VERSION_FILE_SUFFIX)) {
                InputStream inputStream = null;
                try {
                  inputStream = zip.getInputStream(entry);
                  Version version = new Version(inputStream);
                  printStream.println("'" + entry.getName() + "': "
                      + version.getVerboseVersion());
                } finally {
                  if (inputStream != null) {
                    try {
                      inputStream.close();
                    } catch (IOException e) {
                      // ignore
                    }
                  }
                }
              }
            }
            return SUCCESS;
          } else {
            printUsage(System.err);
            return  FAILURE_USAGE;
          }
        } catch (IOException e) {
          System.err.println("Something went wrong trying to read '" + input.getPath() + "': "
              + e.getMessage());
          return FAILURE_PROCESSING;
        } finally {
          try {
            zip.close();
          } catch (IOException e) {
            // ignore
          }
        }
      }
    }
  }

  private static void printVersion(@Nonnull PrintStream printStream) throws IOException {
    Version version = new Version("jack-jar-tools", JarTool.class.getClassLoader());
    printStream.println("Jack jar tools.");
    printStream.println("Version: " + version.getVerboseVersion() + '.');
  }

  private static void printUsage(@Nonnull PrintStream printStream) {
    CmdLineParser parser =
        new CmdLineParser(new JarTool(), ParserProperties.defaults().withUsageWidth(100));
    printStream.println("Usage: <options> <jar file>");
    printStream.println();
    printStream.println("Options:");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    parser.printUsage(outputStream);
    printStream.append(outputStream.toString());
 }

  private JarTool() {
  }
}
