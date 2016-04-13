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

package com.android.sched.scheduler;

import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WriterFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.CustomPrintWriter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Plan printer which prints a simple textual description
 */
@ImplementationName(iface = PlanPrinter.class, name = "simple-txt")
public class SimpleTextPlanPrinter implements PlanPrinter {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final WriterFile planFile = ThreadConfig.get(PlanPrinterFactory.PLAN_PRINTER_FILE);

  @Override
  public void printPlan(@Nonnull Plan<?> plan) throws CannotWriteException {
    CustomPrintWriter writer = planFile.getPrintWriter();
    try {
      writer.println(plan.getDescription());
    } finally {
      writer.close();
      try {
        writer.throwPendingException();
      } catch (IOException e) {
        throw new CannotWriteException(planFile.getLocation(), e);
      }
    }
  }
}
