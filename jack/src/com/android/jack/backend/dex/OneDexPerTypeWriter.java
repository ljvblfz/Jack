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

package com.android.jack.backend.dex;

import com.android.jack.JackIOException;
import com.android.jack.Options;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.direct.OutputDirectDir;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Write one dex file per types.
 */
@Description("Write one dex file per types")
@Constraint(need = {DexCodeMarker.class, DexFileMarker.Complete.class})
@Produce(OneDexPerTypeProduct.class)
public class OneDexPerTypeWriter extends DexWriter implements RunnableSchedulable<JSession> {

  @Nonnull
  protected Directory outputDirectory = ThreadConfig.get(Options.DEX_FILE_FOLDER);

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    OutputDirectDir odd = new OutputDirectDir(outputDirectory);
    DexFileMarker dexFileMarker = session.getMarker(DexFileMarker.class);
    assert dexFileMarker != null;

    for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
      OutputVFile vFile = odd.createOutputVFile(getFilePath(type));
      OutputStream outStream = null;
      try {
        outStream = vFile.openWrite();
        dexFileMarker.getDexFileOfType(type).writeTo(outStream, null, false);
      } catch (IOException e) {
        throw new JackIOException("Could not write Dex file to output '" + vFile + "'", e);
      } finally {
        if (outStream != null) {
          outStream.close();
        }
      }
    }
  }
}
