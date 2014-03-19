/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.util.table;

import com.google.common.base.Joiner;

import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.StreamFile;
import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * {@link ReportPrinter} implementation which dumps tables in human readable format.
 */
@ImplementationName(iface = ReportPrinter.class, name = "text")
public class TextReportPrinter implements ReportPrinter {
  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final StreamFile reportFile = ThreadConfig.get(ReportPrinterFactory.REPORT_PRINTER_FILE);

  @Override
  public void printReport(@Nonnull Report report) {
    try {
      PrintStream printStream = null;

      try {
        printStream = reportFile.getPrintStream();
        printStream.println("Report: " + report.getName());
        if (!report.getDescription().isEmpty()) {
          printStream.println("        " + report.getDescription());
        }
        printStream.println();
        for (Table table : report) {
          printStream.println("Table: " + table.getName());
          if (!table.getDescription().isEmpty()) {
            printStream.println("       " + table.getDescription());
          }
          printStream.println(Joiner.on(", ").join(table.getHeader()));
          for (Iterable<String> row : table) {
            printStream.println(Joiner.on(", ").join(row));
          }
          printStream.println();
        }
      } finally {
        if (printStream != null) {
          printStream.close();
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error trying to write the report to a file", e);
    }
  }
}
