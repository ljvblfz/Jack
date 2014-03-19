/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.scheduler.genetic;

import javax.annotation.Nonnull;

class State {
  @Nonnull
  private ThreeState state = ThreeState.UNDEFINED;

  enum ThreeState {
    SATISFIED,
    UNSATISFIED,
    UNDEFINED;
  }

  void setSatisfied() {
    if (state != ThreeState.UNSATISFIED) {
      state = ThreeState.SATISFIED;
    }
  }

  void setUnsatisfied() {
    state = ThreeState.UNSATISFIED;
  }

  boolean isStatisfied() {
    assert state != ThreeState.UNDEFINED;

    return state == ThreeState.SATISFIED;
  }
}
