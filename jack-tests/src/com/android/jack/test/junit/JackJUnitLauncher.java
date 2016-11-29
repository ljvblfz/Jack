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

package com.android.jack.test.junit;

import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to run JUnit tests on command line with a per method granularity
 */
public class JackJUnitLauncher {

  private static class ExitStatus {
    /**
     * Success.
     */
    public static final int SUCCESS = 0;
    /**
     * Passed arguments are erroneous.
     */
    public static final int BAD_ARGUMENTS = 1;
    /**
     * Test suites have failures.
     */
    public static final int TEST_FAILURE = 2;
    /**
     * Other.
     */
    public static final int OTHER = 3;
  }

  public static void main(String[] args) {
    JUnitCore core = new JUnitCore();

    List<Request> requests = new ArrayList<Request>(args.length);

    boolean dumpTests = Boolean.parseBoolean(System.getProperty("tests.dump", "false"));

    if (dumpTests) {
      System.out.println('{');
    }

    // build all requests at once to check validity of args before running any tests
    for (String test : args) {
      String[] classMethodPair = test.split("#");

      Class<?> testClass = null;

      try {
        testClass = Class.forName(classMethodPair[0]);
      } catch (ClassNotFoundException e) {
        System.err.println("Error: test class '" + classMethodPair[0] + "' not found");
        System.exit(ExitStatus.BAD_ARGUMENTS);
      }

      Request req = null;
      if (classMethodPair.length == 2) {
        try {
          testClass.getMethod(classMethodPair[1], (Class<?>[]) null);
        } catch (SecurityException e) {
          System.err.println("Error: " + e.getMessage());
          System.exit(ExitStatus.OTHER);
        } catch (NoSuchMethodException e) {
          System.err.println("Error: method '" + classMethodPair[1] + "()' not found in class '"
              + testClass.getName() + "'");
          System.exit(ExitStatus.BAD_ARGUMENTS);
        }

        requests.add(Request.method(testClass, classMethodPair[1]));
      } else if (classMethodPair.length == 1) {
        requests.add(Request.aClass(testClass));
      } else {
        System.err.println("Error: wrong format, '#' found more than once in '" + test + "'");
        System.exit(ExitStatus.BAD_ARGUMENTS);
      }
    }

    // tests dump mode => don't print anything else
    if (!dumpTests) {
      core.addListener(new TextListener(new RealSystem()));
    }

    int failureCount = 0;
    for (Request req : requests) {
      Result result = core.run(req);
      failureCount += result.getFailureCount();
      for (Failure f : result.getFailures()) {
        f.getException().printStackTrace();
      }
    }

    if (dumpTests) {
      System.out.println("  \"empty\": {}");
      System.out.println("}");
    }

    System.exit((failureCount == 0) ? ExitStatus.SUCCESS : ExitStatus.TEST_FAILURE);
  }
}

