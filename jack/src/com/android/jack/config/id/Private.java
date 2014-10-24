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

package com.android.jack.config.id;

import com.android.sched.util.config.category.Category;

import javax.annotation.Nonnull;

/**
 * Define a category for private properties.
 */
public class Private implements Category {
  @Nonnull
  private static final Private INSTANCE = new Private();

  @Override
  public boolean isPublic() {
    return false;
  }

  @Nonnull
  public static Private get() {
    return INSTANCE;
  }
}
