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

// This class includes content from Apache Ant 1.9.6
// Copyright 1999-2015 The Apache Software Foundation

package com.android.jack.test.junit;

import junit.framework.AssertionFailedError;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This launcher calls JUnitCore with a RunListener which generates an XML report.
 */
public class JackJunitLauncherXml {

  /**
   * Exit statuses codes for this JUnit launcher
   */
  public static class ExitStatus {
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
    public static final int FAILURE = 2;
    /**
     * Other.
     */
    public static final int OTHER = 3;
  }

  private static class XMLRunListener extends RunListener implements XMLConstants {

    @Nonnegative
    private static final double ONE_SECOND = 1000.0;

    @Nonnull
    private static final String UNKNOWN = "unknown";

    @Nonnull
    private Document doc;

    @Nonnull
    private Element rootElement;

    @Nonnull
    private final Hashtable<Description, Element> testElements =
        new Hashtable<Description, Element>();

    @Nonnull
    private final Set<Description> failedTests = new HashSet<Description>();

    @Nonnull
    private final Set<Description> errorTests = new HashSet<Description>();

    @Nonnull
    private final Set<Description> skippedTests = new HashSet<Description>();

    @Nonnull
    private final Set<Description> ignoredTests = new HashSet<Description>();

    @Nonnull
    private final Hashtable<Description, Long> testStarts = new Hashtable<Description, Long>();

    @Nonnull
    private OutputStream out;

    @Nonnegative
    private long startTime;

    @Nonnull
    private static DocumentBuilder getDocumentBuilder() {
      try {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
      } catch (final Exception exc) {
        throw new ExceptionInInitializerError(exc);
      }
    }

    public void setSystemOutput(@Nonnull String out) {
      formatOutput(SYSTEM_OUT, out);
    }

    public void setSystemError(@Nonnull String out) {
      formatOutput(SYSTEM_ERR, out);
    }

    private void formatOutput(@Nonnull String type, @Nonnull String output) {
      Element nested = doc.createElement(type);
      rootElement.appendChild(nested);
      nested.appendChild(doc.createCDATASection(output));
    }

    public XMLRunListener(@Nonnull OutputStream out) {
      doc = getDocumentBuilder().newDocument();
      rootElement = doc.createElement(TESTSUITE);
      this.out = out;
    }

    public void startTestSuite(@Nonnull Class<?> suite) {

      rootElement.setAttribute(ATTR_NAME, suite.getName());

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      String timestamp = simpleDateFormat.format(new Date());
      rootElement.setAttribute(TIMESTAMP, timestamp);

      rootElement.setAttribute(HOSTNAME, getHostname());

      Element propsElement = doc.createElement(PROPERTIES);
      rootElement.appendChild(propsElement);
      startTime = System.currentTimeMillis();
      final Properties props = System.getProperties();
      if (props != null) {
        final Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
          String name = (String) e.nextElement();
          Element propElement = doc.createElement(PROPERTY);
          propElement.setAttribute(ATTR_NAME, name);
          propElement.setAttribute(ATTR_VALUE, props.getProperty(name));
          propsElement.appendChild(propElement);
        }
      }
    }

    @Nonnull
    private String getHostname() {
      String hostname = "localhost";
      try {
        InetAddress localHost = InetAddress.getLocalHost();
        if (localHost != null) {
          hostname = localHost.getHostName();
        }
      } catch (UnknownHostException e) {
        // fall back to default 'localhost'
      }
      return hostname;
    }

    public void endTestSuite() throws IOException {
      rootElement.setAttribute(ATTR_TESTS, "" + testStarts.size());
      rootElement.setAttribute(ATTR_FAILURES, "" + failedTests.size());
      rootElement.setAttribute(ATTR_ERRORS, "" + errorTests.size());
      rootElement.setAttribute(ATTR_SKIPPED, "" + skippedTests.size());

      rootElement.setAttribute(
          ATTR_TIME, "" + ((System.currentTimeMillis() - startTime) / ONE_SECOND));
      if (out != null) {
        Writer wri = null;
        try {
          wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));

          Transformer transformer;

          transformer = TransformerFactory.newInstance().newTransformer();
          Result output = new StreamResult(wri);
          Source input = new DOMSource(rootElement);
          transformer.setOutputProperty(OutputKeys.INDENT, "yes");
          transformer.transform(input, output);

        } catch (final IOException exc) {
          throw new IOException("Unable to write log file", exc);
        } catch (TransformerException e) {
          throw new IOException("Unable to write log file", e);
        } finally {
          if (wri != null) {
            try {
              wri.flush();
            } catch (final IOException ex) {
              // ignore
            }
          }
          if (out != System.out && out != System.err) {
            wri.close();
          }
        }
      }
    }

    @Override
    public void testFailure(@Nonnull Failure failure) throws Exception {
      Throwable t = failure.getException();

      String failureType =
          (t instanceof AssertionError || t instanceof AssertionFailedError) ? FAILURE : ERROR;

      Description description = failure.getDescription();

      if (description != null) {
        testFinished(description);
        if (failureType == FAILURE) {
          failedTests.add(description);
        } else {
          errorTests.add(description);
        }
      }

      formatError(failureType, failure);
    }

    private void formatError(@Nonnull String type, @Nonnull Failure failure) throws Exception {

      final Element nested = doc.createElement(type);
      Element currentTest;
      if (failure.getDescription() != null) {
        currentTest = testElements.get(failure.getDescription());
      } else {
        currentTest = rootElement;
      }

      currentTest.appendChild(nested);

      final String message = failure.getMessage();
      if (message != null && message.length() > 0) {
        nested.setAttribute(ATTR_MESSAGE, failure.getMessage());
      }
      nested.setAttribute(ATTR_TYPE, failure.getDescription().getClassName());

      final String strace = failure.getTrace();
      final Text trace = doc.createTextNode(strace);
      nested.appendChild(trace);
    }

    @Override
    public void testFinished(@Nonnull Description description) throws Exception {

      if (!testStarts.containsKey(description)) {
        testStarted(description);
      }

      Element currentTest;
      if (!failedTests.contains(description) && !errorTests.contains(description)
          && !skippedTests.contains(description) && !ignoredTests.contains(description)) {
        currentTest = doc.createElement(TESTCASE);
        final String n = description.getDisplayName();
        currentTest.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);
        // a TestSuite can contain Tests from multiple classes,
        // even tests with the same name - disambiguate them.
        currentTest.setAttribute(ATTR_CLASSNAME, description.getClassName());
        rootElement.appendChild(currentTest);
        testElements.put(description, currentTest);

      } else {
        currentTest = testElements.get(description);
      }

      final Long l = testStarts.get(description);
      currentTest.setAttribute(
          ATTR_TIME, "" + ((System.currentTimeMillis() - l.longValue()) / ONE_SECOND));
    }

    @Override
    public void testStarted(@Nonnull Description description) throws Exception {
      testStarts.put(description, Long.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void testIgnored(@Nonnull Description description) throws Exception {
      formatSkip(description);
      if (description != null) {
        ignoredTests.add(description);
      }
    }

    @Override
    public void testAssumptionFailure(@Nonnull Failure failure) {
      try {
        formatSkip(failure.getDescription());
      } catch (Exception e) {
        e.printStackTrace();
      }
      skippedTests.add(failure.getDescription());
    }

    public void formatSkip(@Nonnull Description description) throws Exception {
      testFinished(description);

      Method testMethod =
          Class.forName(description.getClassName())
              .getMethod(description.getMethodName(), (Class<?>[]) null);

      Class<?> ignoreAnnotation = Class.forName("org.junit.Ignore");

      @SuppressWarnings("unchecked")
      Annotation annotation = testMethod.getAnnotation((Class<Annotation>) ignoreAnnotation);

      String message = null;
      if (annotation != null) {
        Method valueMethod = annotation.getClass().getMethod("value");
        String value = (String) valueMethod.invoke(annotation);
        if (value != null && value.length() > 0) {
          message = value;
        }
      }

      final Element nested = doc.createElement("skipped");

      if (message != null) {
        nested.setAttribute("message", message);
      }

      Element currentTest = testElements.get(description);

      currentTest.appendChild(nested);
    }
  }

  /**
   * Entry point
   * @param args <output file>  <test class>
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static void main(@Nonnull String[] args) throws ClassNotFoundException, IOException {
    if (args.length != 2) {
      System.err.println("Usage: <test> <output-file>");
      System.exit(ExitStatus.BAD_ARGUMENTS);
    }

    String fileName = args[0];
    Class<?> testClass = Class.forName(args[1]);

    XMLRunListener listener = new XMLRunListener(new FileOutputStream(fileName));

    JUnitCore core = new JUnitCore();
    core.addListener(listener);

    PrintStream stdOut = System.out;
    PrintStream stdErr = System.err;

    ByteArrayOutputStream outputStreamByteArray = new ByteArrayOutputStream();
    ByteArrayOutputStream errorStreamByteArray = new ByteArrayOutputStream();

    PrintStream outputStream = new PrintStream(outputStreamByteArray);
    PrintStream errorStream = new PrintStream(errorStreamByteArray);

    System.setOut(outputStream);
    System.setErr(errorStream);

    org.junit.runner.Result result = null;
    try {
      listener.startTestSuite(testClass);
      result = core.run(testClass);
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
      outputStream.close();
      errorStream.close();
    }

    listener.setSystemOutput(new String(outputStreamByteArray.toByteArray()));
    listener.setSystemError(new String(errorStreamByteArray.toByteArray()));
    listener.endTestSuite();

    System.exit((result.getFailureCount() == 0) ? ExitStatus.SUCCESS : ExitStatus.FAILURE);
  }
}

