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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import com.android.jack.plugin.v01.Plugin;
import com.android.sched.util.UncomparableVersion;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.log.LoggerFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A {@link PluginSelector} which selects the most recent plugin.
 */
@ImplementationName(iface = PluginSelector.class, name = "last")
public class Last implements PluginSelector {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Override
  @Nonnull
  public Plugin select(@Nonnull List<Plugin> plugins) {
    Plugin selected = plugins.get(0);

    for (Plugin plugin : plugins) {
      try {
        if (plugin.getVersion().isNewerThan(selected.getVersion())) {
          selected = plugin;
        }
      } catch (UncomparableVersion e) {
        // Continue
      }
    }

    if (logger.isLoggable(Level.INFO)) {
      if (plugins.size() > 1) {
        final Plugin one = selected;
        StringBuilder others = new StringBuilder();
        Joiner.on(", ").appendTo(others, Iterators
            .transform(Iterators.<Plugin>filter(plugins.iterator(), new Predicate<Plugin>() {
              @Override
              public boolean apply(Plugin plugin) {
                return plugin != one;
              }
            }), new Function<Plugin, String>() {
              @Override
              public String apply(Plugin plugin) {
                return plugin.getVersion().getVersion();
              }
            }));

        logger.log(Level.INFO, "For plugin ''{0}'', selected version {1} because newer than {2}",
            new Object[] {selected.getCanonicalName(), selected.getVersion().getVersion(), others});
      } else {
        logger.log(Level.INFO, "For plugin ''{0}'', selected version {1} because alone",
            new Object[] {selected.getCanonicalName(), selected.getVersion().getVersion()});
      }
    }

    return selected;
  }
}