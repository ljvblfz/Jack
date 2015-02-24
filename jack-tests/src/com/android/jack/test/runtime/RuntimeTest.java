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

package com.android.jack.test.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@code RuntimeTest}s must extends this class if they are to be passed as
 * regression tests.
 */
public abstract class RuntimeTest {

  @CheckForNull
  protected List<RuntimeTestInfo> rtTestInfos = null;

  @Nonnull
  public final List<RuntimeTestInfo> getRuntimeTestInfos() {
    if (rtTestInfos == null) {
      rtTestInfos = new ArrayList<RuntimeTestInfo>();
      fillRtTestInfos();
    }
    assert rtTestInfos != null;
    return rtTestInfos;
  }

  protected abstract void fillRtTestInfos();
}