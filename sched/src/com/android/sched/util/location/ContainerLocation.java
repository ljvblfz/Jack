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

package com.android.sched.util.location;

import javax.annotation.Nonnull;

/**
 * Container location.
 */
public class ContainerLocation implements Location {
  @Nonnull
  private final Location container;
  @Nonnull
  private final Location element;

  public ContainerLocation(@Nonnull Location container, @Nonnull Location element) {
    this.container = container;
    this.element = element;
  }

  @Override
  @Nonnull
  public String getDescription() {
    StringBuilder sb = new StringBuilder();

    if (!container.getDescription().isEmpty()) {
      sb.append(container.getDescription()).append(", ");
    }

    return sb.append(element.getDescription()).toString();
  }

  @Nonnull
  public Location getElementLocation() {
    return element;
  }

  @Nonnull
  public Location getContainerLocation() {
    return container;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof ContainerLocation
        && ((ContainerLocation) obj).container.equals(container)
        && ((ContainerLocation) obj).element.equals(element);
  }

  @Override
  public final int hashCode() {
    return container.hashCode() ^ element.hashCode();
  }
}
