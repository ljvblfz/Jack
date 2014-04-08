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

package com.android.jack;

import com.android.jack.frontend.FrontendCompilationException;
import com.android.sched.util.TextUtils;
import com.android.sched.util.UnrecoverableException;
import com.android.sched.util.config.ChainedException;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.GatherConfigBuilder;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.LoggerFactory;

import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Abstract command line to run the jack compiler.
 */
public abstract class CommandLine {

  @Nonnull
  private static final String INTERRUPTED_COMPILATION_WARNING =
    "Warning: This may have produced partial or corrupted output.";

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  protected static void runJackAndExitOnError(@Nonnull Options options) {
    try {
      Jack.run(options);
    } catch (NothingToDoException e1) {
      // End normally since there is nothing to do
    } catch (ConfigurationException exceptions) {
      System.err.println(exceptions.getNextExceptionCount() + " error"
          + (exceptions.getNextExceptionCount() > 1 ? "s" : "")
          + " during configuration. Try --help-properties for help.");
      for (ChainedException exception : exceptions) {
        System.err.println("  " + exception.getMessage());
      }

      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (IllegalOptionsException e) {
      System.err.println(e.getMessage());
      System.err.println("Try --help for help.");

      System.exit(ExitStatus.FAILURE_USAGE);
    } catch (FrontendCompilationException e) {
      // Cause exception has already been logged
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (JackUserException e) {
      System.err.println(e.getMessage());
      logger.log(Level.FINE, "Jack user exception:", e);
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (OutOfMemoryError e) {
      printExceptionMessage(e, "Out of memory error.");
      System.err.println("Try increasing heap size with java option '-Xmx<size>'");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Out of memory error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (StackOverflowError e) {
      printExceptionMessage(e, "Stack overflow error.");
      System.err.println("Try increasing stack size with java option '-Xss<size>'");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Stack overflow error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (VirtualMachineError e) {
      printExceptionMessage(e, "Virtual machine error: " + e.getClass() + ".");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Virtual machine error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (UnrecoverableException e) {
      System.err.println("Unrecoverable error: " + e.getMessage());
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.FINE, "Unrecoverable exception:", e);
      System.exit(ExitStatus.FAILURE_UNRECOVERABLE);
    } catch (Throwable e) {
      System.err.println("Internal compiler error.");
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Internal compiler error:", e);

      System.exit(ExitStatus.FAILURE_INTERNAL);
    }
  }

  public static void printVersion() {
    String version = Jack.getVersionString();

    System.out.println("Jack compiler.");
    System.out.println("Version: " + version + '.');
  }

  public static void printUsage(@Nonnull Options options) {
    CmdLineParser parser = new CmdLineParser(options);

    // TODO(jplesot) Rework because single line usage is false
    System.out.print("Main: ");
    parser.printSingleLineUsage(System.out);
    System.out.println();
    parser.printUsage(System.out);
  }

  public static void printHelpProperties (@Nonnull Options options) throws IOException {
    GatherConfigBuilder builder = options.getDefaultConfigBuilder();

    // Get and sort properties
    Collection<PropertyId<?>>  collec = builder.getPropertyIds();
    PropertyId<?>[] properties = collec.toArray(new PropertyId<?>[collec.size()]);
    Arrays.sort(properties, new Comparator<PropertyId<?>>() {
      @Override
      public int compare(PropertyId<?> o1, PropertyId<?> o2) {
        return o1.getName().compareTo(o2.getName());
      }});

    // Print properties
    for (PropertyId<?> property : properties) {
      StringBuilder sb = new StringBuilder();

      sb.append(property.getName());
      sb.append(':');

      // Description and default value
      sb.append(TextUtils.LINE_SEPARATOR);
      sb.append("     ");
      sb.append(property.getDescription());
      String value = builder.getDefaultValue(property);
      if (value != null) {
        sb.append(" (default is '");
        sb.append(value);
        sb.append("')");
      }

      // Constraints
      BooleanExpression constraints = property.getRequiredExpression();
      if (constraints != null) {
        sb.append(TextUtils.LINE_SEPARATOR);
        sb.append("     required if ");
        sb.append(constraints.getDescription());
      }

      // Usage
      sb.append(TextUtils.LINE_SEPARATOR);
      sb.append("     ");
      sb.append(property.getCodec().getUsage());

      System.out.println(sb);
    }
  }

  private static void printExceptionMessage(@Nonnull Throwable t, @Nonnull String defaultMessage) {
    String exceptionMessage = t.getMessage();
    if (exceptionMessage == null) {
      exceptionMessage = defaultMessage;
    }
    System.err.println(exceptionMessage);
  }
}
