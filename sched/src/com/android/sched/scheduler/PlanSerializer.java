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

package com.android.sched.scheduler;

import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.StreamFile;
import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Plan printer which prints the plan to a file
 */
@ImplementationName(iface = PlanPrinter.class, name = "serializer")
public class PlanSerializer implements PlanPrinter {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  StreamFile planFile = ThreadConfig.get(PlanPrinterFactory.PLAN_PRINTER_FILE);

  @Override
  public void printPlan(@Nonnull Plan<?> plan) {
    try {
      PrintStream printStream = null;
      try {
        printStream = planFile.getPrintStream();
        printSubPlan(plan, printStream);
      } finally {
        if (printStream != null) {
          printStream.close();
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error trying to write the schedulable plan to a file", e);
    }
  }

  private void printSubPlan(@Nonnull Plan<?> plan, @Nonnull PrintStream printStream)
      throws IOException {
    Iterator<PlanStep> iter = plan.iterator();
    while (iter.hasNext()) {
      PlanStep step = iter.next();
      ManagedSchedulable schedulable = step.getManagedSchedulable();
      printStream.println(schedulable.getSchedulable().getCanonicalName());
      if (step.isVisitor()) {
        printStream.println("{");
        printSubPlan(step.getSubPlan(), printStream);
        printStream.println("}");
      }
    }
  }
}
