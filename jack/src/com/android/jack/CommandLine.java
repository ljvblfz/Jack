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

import com.android.jack.config.id.Douarn;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.load.JackLoadingException;
import com.android.jack.plugin.v01.Plugin;
import com.android.sched.scheduler.ProcessException;
import com.android.sched.scheduler.ScheduleInstance;
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
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;
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
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Abstract command line to run the jack compiler.
 */
public abstract class CommandLine {

  @Nonnull
  protected static final String INTERRUPTED_COMPILATION_WARNING =
    "Warning: This may have produced partial or corrupted output.";

  @Nonnegative
  private static final int CONSOLE_STACK_OVERFLOW_TOP = 20;
  @Nonnegative
  private static final int CONSOLE_STACK_OVERFLOW_BOTTOM = 30;
  @Nonnegative
  private static final int LOG_STACK_OVERFLOW_TOP = 100;
  @Nonnegative
  private static final int LOG_STACK_OVERFLOW_BOTTOM = 100;

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
    } catch (UnrecoverableException e) {
      err.println("Unrecoverable error: " + e.getMessage());
      err.println(INTERRUPTED_COMPILATION_WARNING);
      logger.log(Level.SEVERE, "Unrecoverable exception:", e);
      return (ExitStatus.FAILURE_UNRECOVERABLE);
    } catch (JackAbortException e) {
      // Exception should already have been reported, do not print message.
      logger.log(Level.FINE, "Jack fatal exception:", e);
      return (ExitStatus.FAILURE_COMPILATION);
    } catch (OutOfMemoryError e) {
      String info =
          "Out of memory error (version " + Jack.getVersion().getVerboseVersion() + ")";
      // First, log
      logger.log(Level.SEVERE, info + ':', (pe != null) ? pe : e);
      // After, report on err
      err.println(info + '.');
      printExceptionMessage(err, e);
      err.println("Try increasing heap size with java option '-Xmx<size>'.");
      err.println(INTERRUPTED_COMPILATION_WARNING);
      return (ExitStatus.FAILURE_VM);
    } catch (StackOverflowError e) {
      String info =
          "Stack overflow error (version " + Jack.getVersion().getVerboseVersion() + ")";
      // First, log
      logger.log(Level.SEVERE, info);
      if (pe != null) {
        logger.log(Level.SEVERE, pe.getMessage() + ".");
      }
      printStackOverflow(logger, e, LOG_STACK_OVERFLOW_TOP, LOG_STACK_OVERFLOW_BOTTOM);
      // After, report on details err
      if (pe != null) {
        err.println(pe.getMessage() + ".");
      }
      printExceptionMessage(err, e);
      printStackOverflow(err, e, CONSOLE_STACK_OVERFLOW_TOP, CONSOLE_STACK_OVERFLOW_BOTTOM);
      // Report a summary at the end
      err.println();
      err.println(info + '.');
      err.println("Try increasing stack size with property '"
          + ScheduleInstance.DEFAULT_STACK_SIZE.getName() + "'.");
      err.println(INTERRUPTED_COMPILATION_WARNING);
      return (ExitStatus.FAILURE_VM);
    } catch (VirtualMachineError e) {
      String info =
          "Virtual machine error (version " + Jack.getVersion().getVerboseVersion() + ")";
      // First, log
      logger.log(Level.SEVERE, info + ':', (pe != null) ? pe : e);
      // After, report on err
      err.println(info + '.');
      printExceptionMessage(err, e);
      err.println(INTERRUPTED_COMPILATION_WARNING);
      return (ExitStatus.FAILURE_VM);
    } catch (Throwable e) {
      String info =
          "Internal compiler error (version " + Jack.getVersion().getVerboseVersion() + ")";
      // First log
      logger.log(Level.SEVERE, info + ':', (pe != null) ? pe : e);
      // After, report on err
      ((pe != null) ? pe : e).printStackTrace(err);
      // Report a summary at the end
      err.println();
      err.println(info + '.');
      printExceptionMessage(err, e);
      err.println(INTERRUPTED_COMPILATION_WARNING);
      return (ExitStatus.FAILURE_INTERNAL);
    }
  }

  public static void printVersion(@Nonnull PrintStream printStream, @Nonnull Options options)
      throws IllegalOptionsException {
    // STOPSHIP remove call
    options.ensurePluginManager();
    printStream.println("Jack compiler: " + Jack.getVersion().getVerboseVersion() + '.');
    for (Plugin plugin : options.getPluginManager().getPlugins()) {
      printVersion(printStream, plugin);
    }
  }

  public static void printVersion(@Nonnull PrintStream printStream, @Nonnull Plugin plugin) {
    printStream.println("Jack plugin:   " + plugin.getFriendlyName()
                                          + " (" + plugin.getCanonicalName() + ") "
                                          + plugin.getVersion().getVerboseVersion() + '.');
    printStream.println("               " + plugin.getDescription() + '.');
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
      throws IOException, IllegalOptionsException {
    GatherConfigBuilder builder = options.getDefaultConfigBuilder();

    printProperties(printStream, builder, Douarn.class);

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

        Location location = builder.getLocation(property);
        if (!location.equals(NoLocation.getInstance())) {
          sb.append(" (declared by ");
          sb.append(location.getDescription());
          sb.append(')');
        }

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

  public static void printPluginsList(@Nonnull PrintStream printStream, @Nonnull Options options)
      throws IllegalOptionsException {
    // STOPSHIP remove call
    options.ensurePluginManager();
    for (Plugin plugin : options.getPluginManager().getAvailablePlugins()) {
      printVersion(printStream, plugin);
    }
  }

  protected static void printExceptionMessage(@Nonnull PrintStream printer, @Nonnull Throwable t) {
    String exceptionMessage = t.getMessage();
    if (exceptionMessage != null) {
      printer.println(exceptionMessage + ".");
    }
  }

  protected static void printStackOverflow(@Nonnull PrintStream stream,
      @Nonnull StackOverflowError e, @Nonnegative int topCount, @Nonnegative int bottomCount) {
    StackTraceElement[] elts = e.getStackTrace();
    stream.println(elts.length + " calls reported in " + e.getClass().getCanonicalName() + ".");
    stream.println("(See -XX:MaxJavaStackTraceDepth to report more if necessary).");
    boolean ellipse = false;
    for (int idx = 0; idx < elts.length; idx++) {
      if (idx < topCount || idx > elts.length - 1 - bottomCount) {
        stream.println("    at "  + elts[idx].toString());
       } else if (!ellipse) {
        ellipse = true;
        stream.println("    ...");
      }
    }
  }

  protected static void printStackOverflow(@Nonnull Logger logger,
      @Nonnull StackOverflowError e, @Nonnegative int topCount, @Nonnegative int bottomCount) {
    StackTraceElement[] elts = e.getStackTrace();
    logger.log(Level.SEVERE,
        elts.length + " calls reported in " + e.getClass().getCanonicalName() + ".");
    boolean ellipse = false;
    for (int idx = 0; idx < elts.length; idx++) {
      if (idx < topCount || idx > elts.length - 1 - bottomCount) {
        logger.log(Level.SEVERE, "    at "  + elts[idx].toString());
       } else if (!ellipse) {
        ellipse = true;
        logger.log(Level.SEVERE, "    ...");
      }
    }
  }
}
