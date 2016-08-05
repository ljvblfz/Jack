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

package com.android.jack;

import com.android.sched.util.log.EventType;

import javax.annotation.Nonnull;

/**
 * Jack event type.
 */
public enum JackEventType implements EventType {

  NNODE_READING_FOR_IMPORT("NNode reading for import"),
  NNODE_READING_FOR_CLASSPATH("NNode reading for classpath"),
  NNODE_TO_JNODE_CONVERSION_FOR_IMPORT("NNode to JNode conversion for import"),
  NNODE_TO_JNODE_CONVERSION_FOR_CLASSPATH("NNode to JNode conversion for classpath"),
  JNODE_TO_NNODE_CONVERSION("JNode to NNode conversion"),
  NNODE_WRITING("NNode writing"),
  LOOKUP_TRANSFER("Lookup transfer"),
  METHOD_ID_MERGER("Method id merger"),
  PRELOOKUP("Pre-lookup"),
  ECJ_COMPILATION("ECJ compilation"),
  JACK_IR_BUILDER("Jack IR Builder"),
  J_AST_BUILDER("JAstBuilder"),
  DX_OPTIMIZATION("Dx optimizations on RopMethod"),
  REMOVE_DEAD_CODE("Remove dead code"),
  DOP_CREATION("Dop creation"),
  ZIP_JACK_LIBRARY_IN_INCREMENTAL("Zip jack library in incremental mode"),
  JACK_RUN("Jack run"),
  ALL_JACK_SCHEDULABLES("All Jack schedulables"),
  DX_BACKEND("Dx backend"),
  DEX_MERGER("Dex merger"),
  DEX_MERGER_FINISH("Dex merger finish");

  @Nonnull
  private final String name;

  JackEventType(@Nonnull String name) {
    this.name = name;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  @Nonnull
  public String toString() {
    return name + " (" + super.toString() + ")";
  }
}
