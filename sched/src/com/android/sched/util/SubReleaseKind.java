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

package com.android.sched.util;

import javax.annotation.Nonnull;

/**
 * The kind of sub-release.
 */
public enum SubReleaseKind {
  ENGINEERING,
  PRE_ALPHA,
  ALPHA,
  BETA,
  CANDIDATE,
  RELEASE;

  public boolean isMoreStableThan(@Nonnull SubReleaseKind other) throws UncomparableSubReleaseKind {
    if ((this == ENGINEERING && other != ENGINEERING)
        || (this != ENGINEERING && other == ENGINEERING)) {
      throw new UncomparableSubReleaseKind(
          this.toString() + " is not comparable with " + other.toString());
    }
    return ordinal() > other.ordinal();
  }

}