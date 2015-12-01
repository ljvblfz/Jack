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

package com.android.jack.plugin;

import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Class describing a Jack plugin.
 */
public class PluginLocation implements Location {
  @Nonnull
  private final Plugin plugin;

  public PluginLocation(@Nonnull Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "'" + plugin.getName() + "' plugin";
  }

  @Nonnull
  public Plugin getPlugin() {
    return plugin;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PluginLocation &&
        plugin.getName().equals(((PluginLocation) obj).plugin.getName());
  }

  @Override
  public int hashCode() {
    return plugin.getName().hashCode();
  }
}
