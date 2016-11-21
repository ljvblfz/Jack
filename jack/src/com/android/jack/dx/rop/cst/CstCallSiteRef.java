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

package com.android.jack.dx.rop.cst;

import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.rop.cst.CstArray.List;
import com.android.jack.dx.rop.type.Type;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Constants of type reference to a call site.
 */
public final class CstCallSiteRef extends TypedConstant {

  @Nonnegative
  private static final int METHOD_HANDLE_IDX = 0;
  @Nonnegative
  private static final int TARGET_METHOD_NAME_IDX = 1;
  @Nonnegative
  private static final int CALL_SITE_IDX = 2;
  @Nonnegative
  private static final int EXTRA_ARGS_IDX = 3;
  @Nonnull
  private final CstArray callSite;

  public CstCallSiteRef(@Nonnull CstArray callSite) {
    this.callSite = callSite;
  }

  public CstCallSiteRef(@Nonnull CstMethodHandleRef methodHandle, @Nonnull String targetMethodName,
      @Nonnull CstPrototypeRef callSitePrototype, @CheckForNull CstArray extraArgs) {
    CstArray.List list =
        new CstArray.List(3 + (extraArgs != null ? extraArgs.getList().size() : 0));
    list.set(METHOD_HANDLE_IDX, methodHandle);
    list.set(TARGET_METHOD_NAME_IDX, new CstString(targetMethodName));
    list.set(CALL_SITE_IDX, callSitePrototype);
    if (extraArgs != null) {
      List extraArgList = extraArgs.getList();
      for (int idx = 0; idx < extraArgList.size(); idx++) {
        list.set(EXTRA_ARGS_IDX + idx, extraArgList.get(idx));
      }
    }
    list.setImmutable();
    callSite = new CstArray(list);
  }

  /** {@inheritDoc} */
  @Override
  protected int compareTo0(@Nonnull Constant other) {
    assert other instanceof CstCallSiteRef;

    int cmp = getTargetMethodName().compareTo(((CstCallSiteRef) other).getTargetMethodName());

    if (cmp != 0) {
      return cmp;
    }

    return getCallSitePrototype().compareTo(((CstCallSiteRef) other).getCallSitePrototype());
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCategory2() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Type getType() {
    return getCallSitePrototype().getType();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String typeName() {
    return "call-site";
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String toString() {
    return typeName() + '{' + toHuman() + '}';
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String toHuman() {
    return getMethodHandle().toHuman() + ", " + getTargetMethodName() + ", " +
         getCallSitePrototype().toHuman();
  }

  @Nonnull
  public CstMethodHandleRef getMethodHandle() {
    return (CstMethodHandleRef) callSite.getList().get(METHOD_HANDLE_IDX);
  }

  @Nonnull
  public CstString getTargetMethodName() {
    return (CstString) callSite.getList().get(TARGET_METHOD_NAME_IDX);
  }

  @Nonnull
  public CstPrototypeRef getCallSitePrototype() {
    return (CstPrototypeRef) callSite.getList().get(CALL_SITE_IDX);
  }

  @Nonnull
  public CstArray getExtraArgs() {
    List callSiteList = callSite.getList();
    CstArray.List list =  new CstArray.List(callSiteList.size() - EXTRA_ARGS_IDX);
    for (int idx = EXTRA_ARGS_IDX; idx < callSiteList.size(); idx++) {
      list.set(idx - EXTRA_ARGS_IDX, callSiteList.get(idx));
    }
    list.setImmutable();
    return new CstArray(list);
  }

  @Nonnull
  public CstArray getCstArray() {
    return callSite;
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    throw new UnsupportedOperationException();
  }
}