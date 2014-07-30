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
import com.android.jack.util.StructurePrinter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;

import java.io.PrintStream;

import javax.annotation.CheckForNull;
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
  public static final PropertyId<OutputStreamFile> STRUCTURE_PRINTING_FILE = PropertyId.create(
      "jack.internal.structure.print.file", "File containing the list of all types and members",
      new OutputStreamCodec(Existence.MAY_EXIST).allowStandard())
      .addDefaultValue("-").requiredIf(STRUCTURE_PRINTING.getValue().isTrue());

  static class WriteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WriteException() {
    }

    public WriteException(@CheckForNull String message) {
      super(message);
    }

    public WriteException(@CheckForNull Throwable cause) {
      super(cause);
    }

    public WriteException(@CheckForNull String message, @CheckForNull Throwable cause) {
      super(message, cause);
    }

  }

  @Nonnull
  private final PrintStream stream;

  public ShrinkStructurePrinter() {
    stream = ThreadConfig.get(STRUCTURE_PRINTING_FILE).getPrintStream();
  }

  @Override
  public void run(@Nonnull JSession t) throws Exception {
    try {
      StructurePrinter visitor = new StructurePrinter(stream);
      visitor.accept(t.getTypesToEmit());
    } finally {
      stream.close();
    }
  }

}
