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

package com.android.jack.transformations.finallyblock;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JTryStatement;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The tag {@link InlinedFinallyMarker} allows to know that the {@link JBlock} represents an inlined
 * finally block.
 */
@Description("Inlined finally block.")
@ValidOn(JBlock.class)
public final class InlinedFinallyMarker implements Marker {

  @CheckForNull
  private JTryStatement tryStmt;

  private final boolean catchIntoFinally;

  public InlinedFinallyMarker(@CheckForNull JTryStatement tryStmt, boolean catchIntoFinally) {
    this.tryStmt = tryStmt;
    this.catchIntoFinally = catchIntoFinally;
  }

  public boolean isCatchIntoFinally() {
    return catchIntoFinally;
  }

  @CheckForNull
  public JTryStatement getTryStmt() {
    return tryStmt;
  }

  public void setTryStmt(@Nonnull JTryStatement tryStmt) {
    this.tryStmt = tryStmt;
  }

  @Override
  public Marker cloneIfNeeded() {
    return new InlinedFinallyMarker(tryStmt, catchIntoFinally);
  }
}