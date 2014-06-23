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

package com.android.jack.backend.dex;

import com.android.jack.JackFileException;
import com.android.jack.Options;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.feature.DexNonZipOutput;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.OutputStreamFile;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Write dex into a file.
 */
@Description("Write dex into a file")
@Name("DexFileWriter")
@Constraint(need = {DexFileMarker.Prepared.class})
@Produce(DexFileProduct.class)
@Support(DexNonZipOutput.class)
public class DexFileWriter implements RunnableSchedulable<JSession> {

  @Nonnull
  private final OutputStreamFile outputFile = ThreadConfig.get(Options.DEX_FILE_OUTPUT);

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    DexFile dexFile = getDexFile(session);

    OutputStream outputStream = outputFile.getOutputStream();
    try {
      dexFile.writeTo(outputStream, null, false);
    } catch (IOException e) {
      throw new JackFileException(
          "Could not write Dex file to output '" + outputFile.getLocation() + "'", e);
    } finally {
      outputStream.close();
    }
  }

  @Nonnull
  private DexFile getDexFile(@Nonnull JSession session) {
    DexFileMarker dexFileMarker = session.getMarker(DexFileMarker.class);
    assert dexFileMarker != null;
    DexFile dexFile = dexFileMarker.getDexFile();
    assert dexFile != null;
    return dexFile;
  }
}
