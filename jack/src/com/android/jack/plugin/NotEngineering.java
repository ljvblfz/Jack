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

import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A {@link PluginFilter} which accepts every non engineering plugin.
 */
@ImplementationName(iface = PluginFilter.class, name = "not-engineering")
public class NotEngineering implements PluginFilter {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Override
  public boolean accept(@Nonnull Plugin plugin) {
    boolean accepted = plugin.getVersion().getSubReleaseKind() != SubReleaseKind.ENGINEERING;

    if (accepted) {
      logger.log(Level.INFO, "For plugin ''{0}'', accepted version {1} because not engineering",
          new Object[] {plugin.getName(), plugin.getVersion().getVersion()});
    } else {
      logger.log(Level.INFO, "For plugin ''{0}'', rejected version {1} because engineering",
          new Object[] {plugin.getName(), plugin.getVersion().getVersion()});
    }

    return accepted;
  }
}