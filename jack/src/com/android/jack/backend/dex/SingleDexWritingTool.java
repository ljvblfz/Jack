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
import com.android.jack.tools.merger.ConstantManager;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.codec.ImplementationName;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, to a single
 * dex.
 */
@ImplementationName(iface = DexWritingTool.class, name = "single-dex",
    description = "only emit one dex file")
public class SingleDexWritingTool extends DexWritingTool {

  @Nonnull
  private final JackMerger merger =
    manager.getIterator().next(ConstantManager.FIRST_DETERMINISTIC_MODE_INDEX);

  @Override
  public void merge(@Nonnull JDefinedClassOrInterface type) throws DexWritingException {
    try {
      manager.mergeDex(merger, type);
    } catch (MergingOverflowException e) {
      throw new DexWritingException(new SingleDexOverflowException(e));
    }
  }

  @Override
  @Nonnull
  public Iterator<JDefinedClassOrInterface> sortAndPrepare(
      Collection<JDefinedClassOrInterface> types) {
    return types.iterator();
  }
}
