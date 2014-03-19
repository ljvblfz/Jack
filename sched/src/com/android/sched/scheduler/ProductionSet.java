/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.scheduler;

import com.android.sched.item.ItemManager;
import com.android.sched.item.ItemSet;
import com.android.sched.item.Production;

import javax.annotation.Nonnull;

/**
 * An {@link ItemSet} specialized in {@link Production} classes.
 */
public class ProductionSet extends ItemSet<Production> {
  public ProductionSet(@Nonnull ProductionSet initial) {
    super(initial);
  }

  public ProductionSet(@Nonnull ItemManager manager) {
    super(manager);
  }

  @Nonnull
  @Override
  public ProductionSet clone() {
    return (ProductionSet) super.clone();
  }
}
