/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.util.config;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * Class describing a line in another location.
 */
public class LineLocation extends Location {
  @Nonnegative
  private final int line;

  @Nonnull
  private final Location location;

  public LineLocation(@Nonnull Location location, @Nonnegative int line) {
    this.location = location;
    this.line = line;
  }

  @Override
  @Nonnull
  public String getDescription() {
    StringBuilder sb = new StringBuilder();

    if (!location.getDescription().isEmpty()) {
      sb.append(location.getDescription()).append(", ");
    }

    return sb.append("line ").append(line).toString();
  }

  @Nonnull
  public Location getSubLocation() {
    return location;
  }

  @Nonnegative
  public int getLine() {
    return line;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof LineLocation
        && ((LineLocation) obj).line == line
        && ((LineLocation) obj).location.equals(location);
  }

  @Override
  public final int hashCode() {
    return (line * 27) ^ location.hashCode();
  }
}
