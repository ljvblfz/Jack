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

package com.android.sched.marker;

import com.android.sched.config.ConfigException;

import javax.annotation.Nonnull;

/**
 * Thrown when a {@link Marker} does not have a valid structure.
 */
public class MarkerNotConformException extends ConfigException {
  private static final long serialVersionUID = 1L;

  public MarkerNotConformException() {
    super();
  }

  public MarkerNotConformException(@Nonnull String message) {
    super(message);
  }

  public MarkerNotConformException(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
  }

  public MarkerNotConformException(@Nonnull Throwable cause) {
    super(cause);
  }
}
