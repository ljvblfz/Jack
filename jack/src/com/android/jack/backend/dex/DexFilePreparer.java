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

import com.android.jack.Options;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Prepare dex file to be written later.
 */
@Description("Prepare dex file to be written later")
@Name("DexFilePreparer")
@Transform(add = {DexFileMarker.Prepared.class})
@Constraint(need = {DexCodeMarker.class, DexFileMarker.Complete.class})
public class DexFilePreparer implements RunnableSchedulable<JSession> {

  private final boolean emitOneDexPerType = ThreadConfig.get(Options.GENERATE_ONE_DEX_PER_TYPE)
      .booleanValue();

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    DexFileMarker dexFileMarker = session.getMarker(DexFileMarker.class);
    assert dexFileMarker != null;

    if (emitOneDexPerType) {
      for (DexFile dexFile : dexFileMarker.getAllDexPerType()) {
        dexFile.prepare(null);
      }
    } else {
      DexFile dexFile = dexFileMarker.getFinalDexFile();
      assert dexFile != null;
      dexFile.prepare(null);
    }
  }
}