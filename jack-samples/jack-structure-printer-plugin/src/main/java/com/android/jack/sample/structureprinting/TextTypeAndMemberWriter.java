/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.sample.structureprinting;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.formatter.UserFriendlyFormatter;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.WriterFile;

import java.io.PrintWriter;

import javax.annotation.Nonnull;

/**
 * Lists all types and members in text format.
 */
public class TextTypeAndMemberWriter {

  @Nonnull
  private static final TypeAndMethodFormatter formatter = UserFriendlyFormatter.getFormatter();

  @Nonnull
  // We store the writer file and not the PrintWriter to avoid creating an empty file in case
  // the schedulable is not executed.
  private final WriterFile writerFile;

  public TextTypeAndMemberWriter() {
    writerFile = ThreadConfig.get(StructurePrinter.STRUCTURE_PRINTING_FILE);
  }

  public void write(@Nonnull JDefinedClassOrInterface type) {
    PrintWriter writer = writerFile.getPrintWriter();
    writer.print(formatter.getName(type));
    writer.println(":");
  }

  public void write(@Nonnull JField field) {
    PrintWriter writer = writerFile.getPrintWriter();
    writer.print(formatter.getName(field.getType()));
    writer.print(" ");
    writer.println(field.getName());
  }

  public void write(@Nonnull JMethod method) {
    PrintWriter writer = writerFile.getPrintWriter();
    writer.println(formatter.getName(method));
  }
}
