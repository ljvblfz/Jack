/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.dx.dex.file;

import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstCallSiteRef;

import java.util.Collection;
import java.util.LinkedHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Call sites list section of a {@code .dex} file.
 */
public final class CallSiteIdsSection extends UniformItemSection {

  @Nonnegative
  private static final int ALIGMENT = 4;
  @Nonnull
  private final LinkedHashMap<CstCallSiteRef, CallSiteIdItem> callSiteIds;

  public CallSiteIdsSection(@Nonnull DexFile file) {
   super("call_site_ids", file, ALIGMENT);
   callSiteIds = new LinkedHashMap<CstCallSiteRef, CallSiteIdItem>();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Collection<? extends Item> items() {
    return callSiteIds.values();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IndexedItem get(@Nonnull Constant cst) {
    throwIfNotPrepared();

    IndexedItem result = callSiteIds.get(cst);
    assert result != null;

    return result;
  }

  public void add(@Nonnull CstCallSiteRef cstCallSiteRef) {
    throwIfPrepared();

    callSiteIds.put(cstCallSiteRef, new CallSiteIdItem(cstCallSiteRef));
  }

  /** {@inheritDoc} */
  @Override
  protected void orderItems() {
    int idx = 0;

    for (Object i : items()) {
      ((CallSiteIdItem) i).setIndex(idx);
      idx++;
    }
  }
}