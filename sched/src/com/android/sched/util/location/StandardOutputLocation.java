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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class describing a standard output.
 */
public class StandardOutputLocation implements Location {
  @Override
  @Nonnull
  public String getDescription() {
    return "standard output";
  }

  @Override
  public final boolean equals(@CheckForNull Object obj) {
    return obj instanceof StandardOutputLocation;
  }

  @Override
  public final int hashCode() {
    return StandardOutputLocation.class.hashCode();
  }
}
