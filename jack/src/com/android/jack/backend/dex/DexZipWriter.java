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
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.jack.scheduling.feature.DexZipOutput;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.jack.util.BytesStreamSucker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.Support;
import com.android.sched.vfs.VPath;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * Write dex into a zip.
 */
@Description("Write dex into a zip")
@Name("DexZipWriter")
@Constraint(need = {DexFileMarker.Prepared.class})
@Produce(DexFileProduct.class)
@Support(DexZipOutput.class)
public class DexZipWriter extends DexFileWriter {

  @Nonnull
  private static final String DEX_NAME = "classes.dex";

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    DexFile dexFile = getDexFile(session);

    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new FileOutputStream(outputFile));
      ZipEntry entry = new ZipEntry(DEX_NAME);
      zos.putNextEntry(entry);
      dexFile.writeTo(zos, null, false);
      zos.closeEntry();
      for (Resource resource : session.getResources()) {
        writeResource(resource, zos);
      }
    } catch (IOException e) {
      throw new JackFileException(
          "Could not write Dex archive to output '" + outputFile.getAbsolutePath() + "'", e);
    } finally {
      if (zos != null) {
        zos.close();
      }
    }
  }

  private void writeResource(@Nonnull Resource resource, @Nonnull ZipOutputStream zos)
      throws IOException {
    VPath path = resource.getPath();
    ZipEntry resourceEntry = new ZipEntry(path.getPathAsString('/'));
    zos.putNextEntry(resourceEntry);
    BytesStreamSucker sucker = new BytesStreamSucker(resource.getVFile().openRead(), zos);
    sucker.suck();
  }
}
