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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.HasName;
import com.android.sched.util.location.LineLocation;

import javax.annotation.Nonnull;

/**
 * An {@link Exception} that occurs during the mapping phase of the obfuscation, when a collision is
 * detected.
 */
public class MappingCollisionException extends Exception {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final LineLocation location;

  @Nonnull
  private final HasName node;

  @Nonnull
  private final String newName;

  public MappingCollisionException(
      @Nonnull LineLocation location, @Nonnull HasName node, String newName) {
    this.location = location;
    this.node = node;
    this.newName = newName;
  }

  @Nonnull
  public HasName getNode() {
    return node;
  }

  @Nonnull
  public String getNewName() {
    return newName;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return location.getDescription() + ": '" + node.getName()
        + "' could not be renamed to '" + newName + "' since the name was already used";
  }

}
