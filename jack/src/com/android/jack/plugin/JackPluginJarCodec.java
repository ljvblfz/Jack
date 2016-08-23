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

import com.android.jack.plugin.v01.Plugin;
import com.android.sched.util.codec.ServiceJarCodec;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.location.Location;

import java.util.ServiceLoader;

import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} is used to create an instance of a {@link ServiceLoader} for Jack
 * {@link Plugin}.
 */
public class JackPluginJarCodec extends ServiceJarCodec<Plugin> {
  public JackPluginJarCodec() {
    super(Plugin.class);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a Jack plugin jar file";
  }

  @Override
  @Nonnull
  protected void throwException(@Nonnull Location location) throws NotJackPluginFileException {
    throw new NotJackPluginFileException(location);
  }
}
