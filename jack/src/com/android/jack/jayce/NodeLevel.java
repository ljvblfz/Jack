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

package com.android.jack.jayce;

import javax.annotation.Nonnull;

/**
 * Node level.
 */
public enum NodeLevel {

  TYPES {
    @Override
    public boolean keep(@Nonnull NodeLevel elementKind) {
      return elementKind == TYPES;
    }
  }, STRUCTURE {
    @Override
    public boolean keep(@Nonnull NodeLevel elementKind) {
      return elementKind != FULL;
    }
  }, FULL {
    @Override
    public boolean keep(@Nonnull NodeLevel elementKind) {
      return true;
    }
  };

  /**
   * For a model of {@code this} kind, returns if an element of kind {@code elementKind} should
   * be kept.
   */
  public abstract boolean keep(@Nonnull NodeLevel elementKind);

}
