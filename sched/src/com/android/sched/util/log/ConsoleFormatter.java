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

package com.android.sched.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.annotation.CheckForNull;

/**
 * A very simple formatter that produces one line messages, but also prints stack traces.
 */
public class ConsoleFormatter extends SimpleFormatter {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /* (non-Javadoc)
   * @see java.util.logging.SimpleFormatter#format(java.util.logging.LogRecord)
   */
  @Override
  public synchronized String format(@CheckForNull LogRecord record) {
    assert record != null;

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    printWriter.append(record.getLevel().toString());
    printWriter.append(": ");
    String n = record.getLoggerName();
    printWriter.append((n != null) ? n : "anonymous");
    printWriter.append(": ");
    printWriter.append(formatMessage(record));
    printWriter.append(LINE_SEPARATOR);

    Throwable t = record.getThrown();
    if (t != null) {
      t.printStackTrace(printWriter);
    }
    printWriter.flush();

    return stringWriter.toString();
  }

}
