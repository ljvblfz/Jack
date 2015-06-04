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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.sched.util.codec.VariableName;
import com.android.sched.vfs.OutputVFS;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * A helper to write dex files.
 */
@VariableName("writer")
public abstract class DexWritingTool {

  @Nonnull
  private static final TypePackageAndMethodFormatter formatter = Jack.getLookupFormatter();

  @Nonnull
  protected static final Comparator<JDefinedClassOrInterface> nameComp =
    new Comparator<JDefinedClassOrInterface>() {
      @Override
      public int compare(@Nonnull JDefinedClassOrInterface first,
          @Nonnull JDefinedClassOrInterface second) {
        return formatter.getName(first).compareTo(formatter.getName(second));
      }
    };

  @Nonnull
  protected final MergingManager manager;

  public DexWritingTool() {
    this.manager = getManager();
  }

  public abstract void merge(@Nonnull JDefinedClassOrInterface type) throws DexWritingException;

  @Nonnull
  public abstract Iterator<JDefinedClassOrInterface> sortAndPrepare(
      @Nonnull Collection<JDefinedClassOrInterface> collection);

  @Nonnull
  protected MergingManager getManager() {
    return new MergingManager();
  }

  public void finishMerge(@Nonnull OutputVFS outputVDir) throws DexWritingException {
    manager.finishMerge(outputVDir);
  }

}
