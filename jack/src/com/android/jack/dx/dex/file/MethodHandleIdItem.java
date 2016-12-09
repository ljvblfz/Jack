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

import com.android.jack.dx.dex.SizeOf;
import com.android.jack.dx.rop.cst.CstMethodHandleRef;
import com.android.jack.dx.util.AnnotatedOutput;
import com.android.jack.dx.util.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Representation of a method handle reference inside a Dex file.
 */
public final class MethodHandleIdItem extends IndexedItem {

  @Nonnull
  private final CstMethodHandleRef cstMethodHandleRef;

  /**
   * Constructs an instance.
   *
   * @param cstMethodHandleRef {@code non-null;} the constant for the method handle
   */
  public MethodHandleIdItem(@Nonnull CstMethodHandleRef cstMethodHandleRef) {
    assert cstMethodHandleRef != null;
    this.cstMethodHandleRef = cstMethodHandleRef;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public ItemType itemType() {
    return ItemType.TYPE_METHOD_HANDLE_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnegative
  public int writeSize() {
    return SizeOf.METHOD_HANDLE_ID_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  public void addContents(@Nonnull DexFile file) {
    file.internIfAppropriate(cstMethodHandleRef.getMemberRef());
  }

  @Nonnull
  public CstMethodHandleRef getCstMethodHandleRef() {
    return cstMethodHandleRef;
  }

  /** {@inheritDoc} */
  @Override
  public void writeTo(@Nonnull DexFile file, @Nonnull AnnotatedOutput out) {
    MemberIdItem memberItem = (MemberIdItem) file.findItemOrNull(cstMethodHandleRef.getMemberRef());

    if (out.annotates()) {
      out.annotate(0, indexString());
      out.annotate(2, "  method_handle_kind:  " + cstMethodHandleRef.getKind());
      out.annotate(2, "  reserved");
      out.annotate(2, "  field_or_method_idx: " + Hex.u2(memberItem.getIndex()) + " // "
          + memberItem.getRef().toHuman());
      out.annotate(2, "  reserved");
   }

    out.writeShort(cstMethodHandleRef.getKind().getValue());
    out.writeShort(0);
    out.writeShort(memberItem.getIndex());
    out.writeShort(0);
  }
}