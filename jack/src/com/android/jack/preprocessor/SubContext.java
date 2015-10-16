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

package com.android.jack.preprocessor;

import javax.annotation.Nonnull;

class SubContext extends Context {

  @Nonnull
  private final Context outerContext;

  public SubContext(@Nonnull Context outerContext) {
    super(outerContext.rule);
    this.outerContext = outerContext;
  }

  public void push() {
    outerContext.steps.addAll(steps);
  }
}
