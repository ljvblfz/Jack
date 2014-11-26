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

package com.android.jack.backend.jayce;

import com.android.jack.Jack;
import com.android.jack.ir.JackFormatIr;
import com.android.jack.ir.NonJackFormatIr;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.jayce.JayceWriterFactory;
import com.android.jack.library.FileType;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.scheduling.feature.JayceFileOutput;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Writer of Jayce files in a folder organized according to package names.
 */
@Description("Writer of Jayce files in a folder organized according to package names")
@Name("JayceSingleTypeWriter")
@Constraint(need = {JackFormatIr.class}, no = {NonJackFormatIr.class})
@Produce(JayceFormatProduct.class)
@Support(JayceFileOutput.class)
public class JayceSingleTypeWriter implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final OutputJackLibrary outputJackLibrary;

  {
    OutputJackLibrary ojl = Jack.getSession().getJackOutputLibrary();
    assert ojl != null;
    this.outputJackLibrary = ojl;
  }

  @Synchronized
  public boolean needsSynchronization() {
    return outputJackLibrary.needsSequentialWriting();
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OutputVFile vFile = outputJackLibrary.createFile(FileType.JAYCE,
        new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));

    try {
      OutputStream out = new BufferedOutputStream(vFile.openWrite());
      try {
        JayceWriterFactory.get(outputJackLibrary, out).write(type);
      } finally {
        out.close();
      }
    } catch (IOException e) {
      throw new LibraryIOException(outputJackLibrary.getLocation(), e);
    }
  }

  @Nonnull
  protected VPath getFilePath(@Nonnull JDefinedClassOrInterface type) {
    return new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type)
        + JayceFileImporter.JAYCE_FILE_EXTENSION, '/');
  }
}
