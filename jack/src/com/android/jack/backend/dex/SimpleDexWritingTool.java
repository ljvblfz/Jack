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
import com.android.jack.dx.dex.file.DexFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that simply writes a {@link DexFile} to a physical file.
 */
public class SimpleDexWritingTool extends DexWritingTool {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public SimpleDexWritingTool(@Nonnull OutputVDir outputVDir) {
    super(outputVDir);
  }

  @Override
  public void write() throws IOException {
    DexFile dexFile = getDexFile();
    OutputVFile dexVFile = outputVDir.createOutputVFile(new VPath(DexFileWriter.DEX_FILENAME, '/'));
    OutputStream osDex = null;
    try {
      osDex = new BufferedOutputStream(dexVFile.openWrite());
      dexFile.prepare();
      dexFile.writeTo(osDex, null, false);
    } catch (IOException e) {
      throw new JackIOException("Could not write Dex file to output '" + dexVFile + "'", e);
    } finally {
      try {
        if (osDex != null) {
          osDex.close();
        }
      } catch (IOException e) {
        throw new JackIOException("Failed to close output stream on '" + dexVFile + "'", e);
      }
    }
  }

}
