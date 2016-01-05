/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.analysis.dfa.reachingdefs;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * The tag {@link ReachingDefsMarker} allows to keep which definitions are alive at the begin of
 * blocks.
 */
@Description("Reaching definitions.")
@ValidOn(BasicBlock.class)
public final class ReachingDefsMarker implements Marker {

  @Nonnull
  private final Set<DefinitionMarker> reachingDefs;

  public ReachingDefsMarker(@Nonnull Set<DefinitionMarker> reachingDefs) {
    this.reachingDefs = Collections.unmodifiableSet(reachingDefs);
  }

  @Nonnull
  public Collection<DefinitionMarker> getReachingDefs() {
    return reachingDefs;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}