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

package com.android.sched.util.codec;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class to format an {@link Object} to a {@link String} with the {@link Object#toString()}.
 */
public class ToStringFormatter implements Formatter<Object> {
  @Nonnull
  private String nullString = "n/a";

  public void setNull(@Nonnull String nullString) {
    this.nullString = nullString;
  }

  @Override
  @Nonnull
  public String formatValue(@CheckForNull Object object) {
    if (object == null) {
      return nullString;
    } else {
      return object.toString();
    }
  }
}
