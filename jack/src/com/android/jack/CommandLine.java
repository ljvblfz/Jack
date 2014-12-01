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

import com.android.jack.config.id.Arzon;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.load.JackLoadingException;
import com.android.sched.util.TextUtils;
import com.android.sched.util.UnrecoverableException;
import com.android.sched.util.codec.Parser.ValueDescription;
import com.android.sched.util.config.ChainedException;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.GatherConfigBuilder;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.category.DefaultCategory;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.CharacterStreamSucker;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    } catch (JackLoadingException e) {
      System.err.println(e.getMessage());
      logger.log(Level.FINE, "Jack loading exception:", e);
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
    } catch (JackAbortException e) {
      // Exception should already have been reported, do not print message.
      logger.log(Level.FINE, "Jack fatal exception:", e);
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (Throwable e) {
      String info = "Internal compiler error (version " + Jack.getVersionString() + ")";
      logger.log(Level.SEVERE, info + ':', e);
      e.printStackTrace();
      System.err.println();
      System.err.println(info + '.');
      if (e.getMessage() != null) {
        System.err.println(e.getMessage() + '.');
      }
      System.err.println(INTERRUPTED_COMPILATION_WARNING);
      System.exit(ExitStatus.FAILURE_INTERNAL);
    }
  }

  public static void printVersion() {
    String version = Jack.getVersionString();

    System.out.println("Jack compiler.");
    System.out.println("Version: " + version + '.');
  }

  protected static void printUsage(@Nonnull PrintStream printStream) {
    InputStream is = Main.class.getResourceAsStream("/help.txt");
    if (is == null) {
      throw new AssertionError();
    }
    CharacterStreamSucker css = new CharacterStreamSucker(is, printStream);
    try {
      css.suck();
    } catch (IOException e) {
      throw new AssertionError(e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  public static void printHelpProperties (@Nonnull Options options) throws IOException {
    GatherConfigBuilder builder = options.getDefaultConfigBuilder();

    printProperties(builder, Arzon.class);

    System.out.println();
    System.out.println("Provisional properties (subject to change):");
    System.out.println();
    printProperties(builder, DefaultCategory.class);
  }

  private static void printProperties(@Nonnull GatherConfigBuilder builder,
      @Nonnull Class<? extends Category> category) {
    // Get and sort properties
    Collection<PropertyId<?>>  collec = builder.getPropertyIds(category);
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

      System.out.println(sb);
    }
  }

  protected static void printExceptionMessage(@Nonnull Throwable t,
      @Nonnull String defaultMessage) {
    String exceptionMessage = t.getMessage();
    if (exceptionMessage == null) {
      exceptionMessage = defaultMessage;
    }
    System.err.println(exceptionMessage);
  }
}
