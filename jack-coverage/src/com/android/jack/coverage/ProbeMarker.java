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

package com.android.jack.coverage;

import com.android.jack.cfg.BasicBlock;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A marker representing a unique code coverage probe on a {@link BasicBlock}.
 */
@Description("A probe assigned to a basic block.")
@ValidOn(BasicBlock.class)
public class ProbeMarker implements Marker {

  @Nonnull
  private final ProbeDescription probe;

  /**
   * Indicates whether the corresponding {@link BasicBlock} contains instrumentation code for
   * this probe.
   */
  @CheckForNull
  private BasicBlock insertionBlock = null;

  public ProbeMarker(@Nonnull ProbeDescription probe) {
    this.probe = probe;
  }

  @Nonnull
  public ProbeDescription getProbe() {
    return probe;
  }

  @CheckForNull
  public BasicBlock getInsertionBlock() {
    return insertionBlock;
  }

  public void setInsertionBlock(@Nonnull BasicBlock insertionBlock) {
    assert this.insertionBlock == null;
    this.insertionBlock = insertionBlock;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

}

