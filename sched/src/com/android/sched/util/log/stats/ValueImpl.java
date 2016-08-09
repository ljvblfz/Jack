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

package com.android.sched.util.log.stats;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Simple statistic storing a {@link String}.
 */
public class ValueImpl extends Value {
  @CheckForNull
  private String value;

  protected ValueImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void set(@Nonnull String value) {
    this.value = value;
  }

  @Override
  @CheckForNull
  public String getValue() {
    return value;
  }

  @Override
  public  void merge(@Nonnull Statistic statistic) {
    ValueImpl value = (ValueImpl) statistic;

    if (value.value != null) {
      assert this.value != null : "Wrong merge of value, target=" + this.value + ", source="
          + value.value;

      this.value = value.value;
    }
  }
}
