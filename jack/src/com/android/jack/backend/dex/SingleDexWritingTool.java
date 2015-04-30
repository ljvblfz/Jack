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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.vfs.InputVFile;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, to a single
 * dex.
 */
@ImplementationName(iface = DexWritingTool.class, name = "single-dex",
    description = "only emit one dex file")
public class SingleDexWritingTool extends DexWritingTool {

  @Nonnull
  private final JackMerger merger = (new AvailableMergerIterator()).next();

  @Override
  public void merge(@Nonnull JDefinedClassOrInterface type) throws DexWritingException {
    InputVFile vFile = getDexInputVFileOfType(jackOutputLibrary, type);
    try {
      mergeDex(merger, vFile);
    } catch (MergingOverflowException e) {
      throw new DexWritingException(new SingleDexOverflowException(e));
    }
  }
}
