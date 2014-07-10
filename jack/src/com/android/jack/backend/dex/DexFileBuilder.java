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

import com.android.jack.Options;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Builds a {@code DexFile} instance from a {@code Session}.
 *
 * <p>This builder only creates an empty {@code DexFile} instance. This instance
 * is then filled with {@code ClassDefItem}s by the {@code ClassDefItemBuilder}.
 *
 * @see ClassDefItemBuilder
 */
@Description("Builds a DexFile instance from a Session.")
@Name("DexFileBuilder")
@Transform(add = DexFileMarker.class)
public class DexFileBuilder implements RunnableSchedulable<JSession> {

  private final boolean emitOneDexPerType = ThreadConfig.get(Options.GENERATE_ONE_DEX_PER_TYPE)
      .booleanValue();

  @Nonnull
  private final DexFile dexFile = new DexFile(new DexOptions());

  /**
   * Attaches the {@code DexFile} instance to build to the given {@code session}
   * in a {@code DexFileMarker}. This {@code DexFile} instance is then accessible
   * in {@code ClassDefItemBuilder} schedulable.
   */
  @Override
  public void run(@Nonnull JSession session) throws Exception {
    DexFileMarker dexFileMarker = new DexFileMarker(dexFile);

    if (emitOneDexPerType) {
      for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
        DexFile file = new DexFile(new DexOptions());
        dexFileMarker.addDexFilePerType(type, file);
      }
    }

    session.addMarker(dexFileMarker);
  }
}
