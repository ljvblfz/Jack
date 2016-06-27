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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JSession;
import com.android.jack.reporting.ReportableIOException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.util.StructurePrinter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.WriterFileCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.WriterFilePropertyId;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.stream.CustomPrintWriter;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * A {link RunnableSchedulable} that lists all members and types.
 */
@HasKeyId
@Description("lists all members and types")
@Produce(StructurePrinting.class)
public class ShrinkStructurePrinter implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final BooleanPropertyId STRUCTURE_PRINTING = BooleanPropertyId.create(
      "jack.internal.structure.print",
      "List all types and members")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final WriterFilePropertyId STRUCTURE_PRINTING_FILE = WriterFilePropertyId.create(
      "jack.internal.structure.print.file", "File containing the list of all types and members",
      new WriterFileCodec(Existence.MAY_EXIST).allowStandardOutputOrError().allowCharset())
      .addDefaultValue("-").requiredIf(STRUCTURE_PRINTING.getValue().isTrue());

  @Nonnull
  private final CustomPrintWriter writer;

  public ShrinkStructurePrinter() {
    writer = ThreadConfig.get(STRUCTURE_PRINTING_FILE).getPrintWriter();
  }

  @Override
  public void run(@Nonnull JSession session) {
    try {
      StructurePrinter visitor = new StructurePrinter(writer);
      visitor.accept(session.getTypesToEmit());
    } finally {
      writer.close();
      try {
        writer.throwPendingException();
      } catch (IOException e) {
        session.getReporter().report(Severity.FATAL, new ReportableIOException("Structure",
            new CannotWriteException(ThreadConfig.get(STRUCTURE_PRINTING_FILE), e)));
        session.abortEventually();
      }
    }
  }
}
