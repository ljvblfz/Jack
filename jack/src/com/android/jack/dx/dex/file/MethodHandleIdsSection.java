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
import com.android.jack.dx.rop.cst.CstMethodHandleRef;
import com.android.jack.dx.util.AnnotatedOutput;
import com.android.jack.dx.util.Hex;

import java.util.Collection;
import java.util.TreeMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Method handle list section of a dex file.
 */
public final class MethodHandleIdsSection extends UniformItemSection {
  @Nonnegative
  private static final int ALIGNMENT = 4;

  @Nonnull
  private final TreeMap<CstMethodHandleRef, MethodHandleIdItem> methodHandleIds;

  public MethodHandleIdsSection(@Nonnull DexFile file) {
    super("method_handle_ids", file, ALIGNMENT);
    methodHandleIds = new TreeMap<CstMethodHandleRef, MethodHandleIdItem>();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Collection<? extends Item> items() {
    return methodHandleIds.values();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IndexedItem get(@Nonnull Constant cst) {
    throwIfNotPrepared();

    IndexedItem result = methodHandleIds.get(cst);
    assert result != null;

    return result;
  }

  /**
   * Writes the portion of the file header that refers to this instance.
   *
   * @param out {@code non-null;} where to write
   */
  public void writeHeaderPart(@Nonnull AnnotatedOutput out) {
    throwIfNotPrepared();

    int sz = methodHandleIds.size();
    int offset = (sz == 0) ? 0 : getFileOffset();

    if (sz > 65536) {
      throw new UnsupportedOperationException("too many method handle ids");
    }

    if (out.annotates()) {
      out.annotate(4, "method_handle_ids_size:  " + Hex.u4(sz));
      out.annotate(4, "method_handle_ids_off:   " + Hex.u4(offset));
    }

    out.writeInt(sz);
    out.writeInt(offset);
  }

  /**
   * Interns an element into this instance.
   *
   * @param cstMethodHandleRef {@code non-null;} the method handle to intern
   * @return {@code non-null;} the interned reference
   */
  @Nonnull
  public MethodHandleIdItem intern(@Nonnull CstMethodHandleRef cstMethodHandleRef) {
    assert cstMethodHandleRef != null;
    throwIfPrepared();

    MethodHandleIdItem result = methodHandleIds.get(cstMethodHandleRef);

    if (result == null) {
      result = new MethodHandleIdItem(cstMethodHandleRef);
      methodHandleIds.put(cstMethodHandleRef, result);
    }

    return result;
  }

  /**
   * Gets the index of the given method handle, which must have
   * been added to this instance.
   *
   * @param methodHandleRef {@code non-null;} the method handle to look up
   * @return {@code >= 0;} the reference's index
   */
  @Nonnegative
  public int indexOf(@Nonnull CstMethodHandleRef methodHandleRef) {
    assert methodHandleRef != null;

    throwIfNotPrepared();

    MethodHandleIdItem item = methodHandleIds.get(methodHandleRef);
    assert item != null;

    return item.getIndex();
  }

  /** {@inheritDoc} */
  @Override
  protected void orderItems() {
    int idx = 0;

    for (Object i : items()) {
      ((MethodHandleIdItem) i).setIndex(idx);
      idx++;
    }
  }
}