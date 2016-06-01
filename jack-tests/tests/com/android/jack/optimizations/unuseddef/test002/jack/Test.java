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

package com.android.jack.optimizations.unuseddef.test002.jack;

public class Test {

  public int getInt() {
    return 7;
  }

  @SuppressWarnings("unused")
  public void changeCursor() {

    boolean hasProfile = getInt() == 1;

    boolean skipProfile = !hasProfile || (hasProfile && true);

    if (false && skipProfile) {
    }
  }
}
