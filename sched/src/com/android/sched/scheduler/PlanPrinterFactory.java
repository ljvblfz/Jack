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

import com.android.sched.util.codec.WriterFileCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.WriterFilePropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;

import javax.annotation.Nonnull;


/**
 * Factory class to manage {@link PlanPrinter}
 */
@HasKeyId
public class PlanPrinterFactory {
  @Nonnull
  private static final
      ImplementationPropertyId<PlanPrinter> PLAN_PRINTER = ImplementationPropertyId.create(
          "sched.plan.printer", "Define which plan printer to use", PlanPrinter.class)
          .addDefaultValue("none");

  @Nonnull
  public static final WriterFilePropertyId PLAN_PRINTER_FILE = WriterFilePropertyId.create(
      "sched.plan.printer.file", "The file where to print the plan",
      new WriterFileCodec(Existence.MAY_EXIST).allowStandardOutputOrError().allowCharset())
      .requiredIf(PLAN_PRINTER.getClazz().isSubClassOf(DetailedTextPlanPrinter.class)
              .or(PLAN_PRINTER.getClazz().isSubClassOf(PlanSerializer.class))
              .or(PLAN_PRINTER.getClazz().isSubClassOf(SimpleTextPlanPrinter.class)))
      .addDefaultValue("-");

  public static PlanPrinter getPlanPrinter() {
    return ThreadConfig.get(PLAN_PRINTER);
  }
}
