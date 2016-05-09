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

package com.android.sched.util.location;

import javax.annotation.Nonnull;

/**
 * Class without location.
 */
public class NoLocation implements Location {
  @Nonnull
  private static final NoLocation NO_LOCATION = new NoLocation();

  @Override
  @Nonnull
  public String getDescription() {
    return "";
  }

  @Override
  public final boolean equals(Object obj) {
    return (obj == this) || obj instanceof NoLocation;
  }

  @Override
  public final int hashCode() {
    return NoLocation.class.hashCode();
  }

  @Nonnull
  public static NoLocation getInstance() {
    return NO_LOCATION;
  }
}
