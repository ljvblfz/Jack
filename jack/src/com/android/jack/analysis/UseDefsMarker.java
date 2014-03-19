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

package com.android.jack.analysis;

import com.android.jack.ir.ast.JVariableRef;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * {@link UseDefsMarker} allows to keep the chain between a use and theirs used definitions.
 */
@Description("Keep the chain between a use and theirs used definitions.")
@ValidOn(JVariableRef.class)
public final class UseDefsMarker implements Marker {

  @Nonnull
  final List<DefinitionMarker> defs = new ArrayList<DefinitionMarker>();

  public boolean isWithoutDefinition() {
    return defs.isEmpty();
  }

  public boolean isUsingOnlyOneDefinition() {
    return defs.size() == 1;
  }

  @Nonnull
  public List<DefinitionMarker> getDefs() {
    // Used definitions must return a new list to support concurrent modifications.
    return new ArrayList<DefinitionMarker>(defs);
  }

  public void addUsedDefinitions(
      @Nonnull List<DefinitionMarker> usedDefinitions, @Nonnull JVariableRef by) {
    for (DefinitionMarker usedDefinition : usedDefinitions) {
      addUsedDefinition(usedDefinition, by);
    }
  }

  public void addUsedDefinition(
      @Nonnull DefinitionMarker usedDefinition, @Nonnull JVariableRef by) {
    // Add used definitions
    assert !defs.contains(usedDefinition);
    defs.add(usedDefinition);

    // Update def/uses chains
    usedDefinition.uses.add(by);
  }

  public void removeAllUsedDefinitions(@Nonnull JVariableRef usedBy) {
    for (DefinitionMarker usedDef : defs) {
      usedDef.uses.remove(usedBy);
    }
    defs.clear();
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError("It is not valid to use cloneIfNeeded, create a new marker.");
  }
}