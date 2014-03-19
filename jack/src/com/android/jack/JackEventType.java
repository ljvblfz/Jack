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

  NNODE_READING_FOR_IMPORT("NNode reading for import", "Blue"),
  NNODE_READING_FOR_CLASSPATH("NNode reading for classpath", "Teal"),
  NNODE_TO_JNODE_CONVERSION_FOR_IMPORT("NNode to JNode conversion for import", "Purple"),
  NNODE_TO_JNODE_CONVERSION_FOR_CLASSPATH("NNode to JNode conversion for classpath", "Green"),
  JNODE_TO_NNODE_CONVERSION("JNode to NNode conversion", "Red"),
  NNODE_WRITING("NNode writing", "Orange"),
  LOOKUP_TRANSFER("Lookup transfer", "Yellow"),
  METHOD_ID_MERGER("Method id merger", "Beige"),
  PRELOOKUP("Pre-lookup", "Pink"),
  ECJ_COMPILATION("ECJ compilation", "Black"),
  GWT_AST_BUILDER("GwtAstBuilder", "LightSkyBlue"),
  J_AST_BUILDER("JAstBuilder", "LightSeaGreen"),
  DX_OPTIMIZATION("Dx optimizations on RopMethod", "Brown"),
  REMOVE_DEAD_CODE("Remove dead code", "Chocolate"),
  DOP_CREATION("Dop creation", "Cyan"),
  JACK_RUN("Jack run", "BlueBerry");

  @Nonnull
  private final String cssColor;
  @Nonnull
  private final String name;

  JackEventType(@Nonnull String name, @Nonnull String cssColor) {
    this.name = name;
    this.cssColor = cssColor;
  }

  @Override
  @Nonnull
  public String getColor() {
    return cssColor;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

}
