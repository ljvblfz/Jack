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

import com.android.jack.JackFileException;
import com.android.jack.Options;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.naming.CompositeName;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.scheduling.feature.DexNonZipOutput;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.jack.scheduling.tags.OneDexPerTypeProduct;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;
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
@Support(DexNonZipOutput.class)
public class OneDexPerTypeWriter implements RunnableSchedulable<JSession> {

  @Nonnull
  protected Directory outputDirectory = ThreadConfig.get(Options.DEX_FILE_FOLDER);

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    OutputDirectDir odd = new OutputDirectDir(outputDirectory);

    for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
      OutputVFile vFile = odd.createOutputVFile(getFilePath(type));
      try {
        ClassDefItemMarker marker = type.getMarker(ClassDefItemMarker.class);
        if (marker != null) {
          OutputStream outStream = vFile.openWrite();
          try {
            ClassDefItem cdi = marker.getClassDefItem();
            DexFile file = new DexFile(new DexOptions());
            file.add(cdi);
            file.prepare(null);
            file.writeTo(outStream, null, false);
          } finally {
            outStream.close();
          }
        }
      } catch (IOException e) {
        throw new JackFileException("Could not write Dex file to output '" + vFile + "'", e);
      }
    }
  }

  @Nonnull
  protected static VPath getFilePath(@Nonnull JDefinedClassOrInterface type) {
    return new VPath(new CompositeName(new TypeName(Kind.BINARY_QN, type),
        ".dex"), '/');
  }
}
