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
import com.android.sched.item.Description;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.WriterFileCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.WriterFilePropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;

import javax.annotation.Nonnull;

/**
 * A schedulable that prints all types and members.
 */
// Needed because this class contains PropertyIds
@HasKeyId
// A schedulable must describe itself.
@Description("Prints all types and members.")
// We produce the printing of the structure (on a file, stdout or stderr), therefore we need to
// declare that production otherwise we will not be scheduled during the compilation
@Produce(StructurePrinting.class)
public class StructurePrinter implements RunnableSchedulable<JDefinedClassOrInterface> {

  /**
   * A property containing the file (or stdout, stderr) where the printing will be done.
   */
  @Nonnull
  public static final WriterFilePropertyId STRUCTURE_PRINTING_FILE =
      WriterFilePropertyId.create(
              "jack.samples.structure-printer.file",
              "File containing the list of all types and members",
              new WriterFileCodec(Existence.MAY_EXIST).allowStandardOutputOrError().allowCharset())
          // "-" represents stdout
          .addDefaultValue("-")
          .withAutoClose();

  /**
   * This method will process the type and write it and every field and method to a file (or
   * stdout, stderr).
   */
  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    TextTypeAndMemberWriter writer = new TextTypeAndMemberWriter();
    writer.write(type);

    for (JField field : type.getFields()) {
      writer.write(field);
    }

    for (JMethod method : type.getMethods()) {
      writer.write(method);
    }
  }
}
