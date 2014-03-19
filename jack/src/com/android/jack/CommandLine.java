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
      logger.log(Level.INFO, "Jack user exception:", e);
      System.exit(ExitStatus.FAILURE_COMPILATION);
    } catch (VirtualMachineError e) {
      System.err.println(e.getMessage());
      if (e instanceof OutOfMemoryError) {
        System.err.println("Try increasing heap size with java option '-Xmx<size>'");
      } else if (e instanceof StackOverflowError) {
        System.err.println("Try increasing stack size with java option '-Xss<size>'");
      }
      logger.log(Level.CONFIG, "Virtual machine error:", e);
      System.exit(ExitStatus.FAILURE_VM);
    } catch (Throwable e) {
      System.err.println("Internal compiler error (see log)");
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
}
