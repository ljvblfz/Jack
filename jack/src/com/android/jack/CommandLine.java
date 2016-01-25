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

import com.android.jack.config.id.Carnac;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.load.JackLoadingException;
import com.android.sched.scheduler.ProcessException;
import com.android.sched.util.TextUtils;
import com.android.sched.util.UnrecoverableException;
import com.android.sched.util.codec.Parser.ValueDescription;
import com.android.sched.util.config.ChainedException;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.GatherConfigBuilder;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.category.Version;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.LoggerFactory;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Abstract command line to run the jack compiler.
 */
public abstract class CommandLine {

  @Nonnull
  protected static final String INTERRUPTED_COMPILATION_WARNING =
    "Warning: This may have produced partial or corrupted output.";

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  protected static int runJack(@Nonnull PrintStream err, @Nonnull Options options) {
    ProcessException pe = null;

    try {
      try {
        Jack.checkAndRun(options);
        return (ExitStatus.SUCCESS);
      } catch (ProcessException e) {
        // Handle the cause, but keep the ProcessException in case of
        // Internal Compiler Error only
        pe = e;
        throw e.getCause();
      }
    } catch (ConfigurationException exceptions) {
      err.println(exceptions.getNextExceptionCount() + " error"
          + (exceptions.getNextExceptionCount() > 1 ? "s" : "")
          + " during configuration. Try --help-properties for help.");
      for (ChainedException exception : exceptions) {
        err.println("  " + exception.getMessage());
      }

     return (ExitStatus.FAILURE_USAGE);
    } catch (IllegalOptionsException e) {
      err.println(e.getMessage());
      err.println("Try --help for help.");

      return (ExitStatus.FAILURE_USAGE);
    } catch (FrontendCompilationException e) {
      // Cause exception has already been logged
      return (ExitStatus.FAILURE_COMPILATION);
    } catch (JackUserException e) {
      err.println(e.getMessage());
      logger.log(Level.FINE, "Jack user exception:", e);
      return (ExitStatus.FAILURE_COMPILATION);
    } catch (JackLoadingException e) {
      err.println(e.getMessage());
      logger.log(Level.FINE, "Jack loading exception:", e);
      return (ExitStatus.FAILURE_COMPILATION);
    } catch (OutOfMemoryError e) {
      printExceptionMessage(err, e, "Out of memory error.");
      err.println("Try increasing heap size with java option '-Xmx<size>'");
      err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Out of memory error:", e);
      return (ExitStatus.FAILURE_VM);
    } catch (StackOverflowError e) {
      printExceptionMessage(err, e, "Stack overflow error.");
      err.println("Try increasing stack size with java option '-Xss<size>'");
      err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Stack overflow error:", e);
      return (ExitStatus.FAILURE_VM);
    } catch (VirtualMachineError e) {
      printExceptionMessage(err, e, "Virtual machine error: " + e.getClass() + ".");
      err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Virtual machine error:", e);
      return (ExitStatus.FAILURE_VM);
    } catch (UnrecoverableException e) {
      err.println("Unrecoverable error: " + e.getMessage());
      err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Unrecoverable exception:", e);
      return (ExitStatus.FAILURE_UNRECOVERABLE);
    } catch (JackAbortException e) {
      // Exception should already have been reported, do not print message.
      logger.log(Level.FINE, "Jack fatal exception:", e);
      return (ExitStatus.FAILURE_COMPILATION);
    } catch (Throwable e) {
      // Internal Compiler Error here
      // If the exception come from a ProcessException, we want
      // to report ProcessException instead of the cause
      if (pe != null) {
        e = pe;
      }

      String info =
          "Internal compiler error (version " + Jack.getVersion().getVerboseVersion() + ")";
      logger.log(Level.SEVERE, info + ':', e);
      e.printStackTrace(err);
      err.println();
      err.println(info + '.');
      if (e.getMessage() != null) {
        err.println(e.getMessage() + '.');
      }
      err.println(INTERRUPTED_COMPILATION_WARNING);
      return (ExitStatus.FAILURE_INTERNAL);
    }
  }

  public static void printVersion(@Nonnull PrintStream printStream) {
    String version = Jack.getVersion().getVerboseVersion();

    printStream.println("Jack compiler.");
    printStream.println("Version: " + version + '.');
  }

  protected static void printUsage(@Nonnull PrintStream printStream) {
    CmdLineParser parser =
        new CmdLineParser(new Options(), ParserProperties.defaults().withUsageWidth(100));
    printStream.println("Usage: <options> <source files>");
    printStream.println();
    printStream.println("Options:");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    parser.printUsage(outputStream);
    printStream.append(outputStream.toString());
  }

  public static void printHelpProperties(@Nonnull PrintStream printStream, @Nonnull Options options)
      throws IOException {
    GatherConfigBuilder builder = options.getDefaultConfigBuilder();

    printProperties(printStream, builder, Carnac.class);

    printStream.println();
    printStream.println("Provisional properties (subject to change):");
    printStream.println();
    printProperties(printStream, builder, null);
  }

  private static void printProperties(@Nonnull PrintStream printStream,
      @Nonnull GatherConfigBuilder builder,
      @CheckForNull Class<? extends Category> category) {
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
      if ((category != null &&  property.hasCategory(category))
       || (category == null && !property.hasCategory(Version.class))) {
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

        // Value descriptions
        List<ValueDescription> descriptions = property.getCodec().getValueDescriptions();
        if (descriptions.size() != 0) {
          sb.append(" where");
          for (ValueDescription entry : descriptions) {
            sb.append(TextUtils.LINE_SEPARATOR);
            sb.append("          ");
            sb.append(entry.getValue());
            sb.append(": ");
            sb.append(entry.getDescription());
          }
        }

        printStream.println(sb);
      }
    }
  }

  protected static void printExceptionMessage(@Nonnull PrintStream err, @Nonnull Throwable t,
      @Nonnull String defaultMessage) {
    String exceptionMessage = t.getMessage();
    if (exceptionMessage == null) {
      exceptionMessage = defaultMessage;
    }
    err.println(exceptionMessage);
  }
}
