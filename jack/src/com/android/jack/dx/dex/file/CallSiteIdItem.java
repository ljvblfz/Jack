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
import com.android.jack.dx.rop.cst.CstCallSiteRef;
import com.android.jack.dx.util.AnnotatedOutput;
import com.android.jack.dx.util.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Representation of a call site reference inside a Dex file.
 */
public final class CallSiteIdItem extends IndexedItem {

  @Nonnull
  private final CstCallSiteRef callSiteRef;
  @Nonnull
  private EncodedArrayItem encodedArray;

  public CallSiteIdItem(@Nonnull CstCallSiteRef callSiteRef) {
    this.callSiteRef = callSiteRef;
    encodedArray = new EncodedArrayItem(callSiteRef.getCstArray());
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public ItemType itemType() {
    return ItemType.TYPE_CALL_SITE_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnegative
  public int writeSize() {
    return SizeOf.CALL_SITE_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  public void addContents(@Nonnull DexFile file) {
    StringIdsSection stringIds = file.getStringIds();
    ProtoIdsSection protoIds = file.getProtoIds();
    MethodHandleIdsSection methodHandleIds = file.getMethodHandleIds();

    stringIds.intern(callSiteRef.getTargetMethodName());
    protoIds.intern(callSiteRef.getCallSitePrototype().getPrototype());
    methodHandleIds.intern(callSiteRef.getMethodHandle());

   encodedArray = file.getByteData().intern(encodedArray);
  }

  /** {@inheritDoc} */
  @Override
  public void writeTo(@Nonnull DexFile file, @Nonnull AnnotatedOutput out) {
    if (out.annotates()) {
      out.annotate(0, indexString());
      out.annotate(4, "  encoded_array_absolute_offset: " + encodedArray.getAbsoluteOffset());
      MethodHandleIdItem methodHandle =
          (MethodHandleIdItem) file.findItemOrNull(callSiteRef.getMethodHandle());
      out.annotate(0, "  method_handle_idx:             " + Hex.u2(methodHandle.getIndex()) + " // "
          + methodHandle.getCstMethodHandleRef().toHuman());
      out.annotate(0,
          "  target_method_name:            " + callSiteRef.getTargetMethodName().getString());
      ProtoIdItem methodType =
          (ProtoIdItem) file.findItemOrNull(callSiteRef.getCallSitePrototype());
      out.annotate(0, "  method_type_idx:               " + Hex.u2(methodType.getIndex()) + " // "
          + methodType.toHuman());
      out.annotate(0, "  extra_args:                    "
          + callSiteRef.getExtraArgs().getList().toHuman("{", ",", "}"));
    }

    out.writeInt(encodedArray.getAbsoluteOffset());
  }
}