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

import javax.annotation.Nonnull;

/**
 * Exception when a plugin is not found.
 */
public class PluginNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private final String name;

  public PluginNotFoundException(@Nonnull String name) {
    super("Plugin '" + name + "' not found");
    this.name = name;
  }

  public PluginNotFoundException(@Nonnull String name, @Nonnull String message) {
    super(message);
    this.name = name;
  }

  public PluginNotFoundException(@Nonnull String name, @Nonnull Throwable cause) {
    super("Plugin '" + name + "' not found", cause);
    this.name = name;
  }

  @Nonnull
  public String getPluginName() {
    return name;
  }
}
