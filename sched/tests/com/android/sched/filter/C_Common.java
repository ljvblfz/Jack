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

package com.android.sched.filter;

import com.android.sched.item.Component;

import javax.annotation.Nonnull;

public abstract class C_Common implements Component {
  @Nonnull
  private final String string;

  public C_Common(@Nonnull String string) {
    this.string = string;
  }

  @Nonnull
  public String getString() {
    return string;
  }

  @Override
  public String toString() {
    return string;
  }
}
