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

package com.android.jack.scheduling.marker;

import com.android.sched.item.Description;
import com.android.sched.marker.Marker;

import javax.annotation.Nonnull;

/**
 * An abstract immutable {@link Marker} which contains an {@code Object}.
 *
 * @param <T> the type of the {@code Object} it contains
 */
@Description("Abstract immutable marker")
public abstract class ContainerMarker<T> implements Marker {
  @Nonnull
  private final T content;

  public ContainerMarker(@Nonnull T content) {
    this.content = content;
  }

  /**
   * Returns the {@code Object} contained in the {@code Marker}.
   */
  @Nonnull
  public T getContent() {
    return content;
  }
}
