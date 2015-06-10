/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFS;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * A deterministic version of {@link MergingManager}.
 */
public class DeterministicMergingManager extends MergingManager {

  @Override
  @Nonnull
  public JackMerger getNewJackMerger(int firstTypeIndex) {
    return new JackMerger(createDexFile(),
        ThreadConfig.get(Options.BEST_MERGING_ACCURACY).booleanValue(), firstTypeIndex);
  }

  @Override
  public void mergeDex(@Nonnull JackMerger merger, @Nonnull JDefinedClassOrInterface type)
      throws MergingOverflowException, DexWritingException {
    InputVFile inputDex = getDexInputVFileOfType(jackOutputLibrary, type);
    try {
      NumberMarker marker = type.getMarker(NumberMarker.class);
      assert marker != null;
      merger.addDexFile(new DexBuffer(inputDex.getInputStream()), marker.getNumber());
    } catch (IOException e) {
      throw new DexWritingException(new CannotReadException(inputDex, e));
    }
  }

  @Override
  public void finishMerge(@Nonnull OutputVFS outputVDir) throws DexWritingException {
    super.finishMerge(outputVDir);
    for (JDefinedClassOrInterface type : Jack.getSession().getTypesToEmit()) {
      type.removeMarker(NumberMarker.class);
    }
  }

}