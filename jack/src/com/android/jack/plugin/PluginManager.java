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

import com.android.jack.Jack;
import com.android.jack.plugin.v01.Plugin;
import com.android.sched.reflections.CompositeReflectionManager;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A plugin manager to search, find and select a Jack plugin.
 */
// STOPSHIP Use properties here
// @HasKeyId
public class PluginManager {
  @Nonnull
  private static final ImplementationPropertyId<PluginSelector> PLUGIN_SELECTOR =
      ImplementationPropertyId
          .create("jack.plugin.selector", "Plugin selection policy", PluginSelector.class)
          .addDefaultValue("last");

  @Nonnull
  private static final ImplementationPropertyId<PluginFilter> PLUGIN_FILTER =
      ImplementationPropertyId
          .create("jack.plugin.filter", "Plugin filter policy", PluginFilter.class)
          .addDefaultValue("stable-only");

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final ServiceLoader<Plugin> serviceLoader;

  public PluginManager(@Nonnull ServiceLoader<Plugin> serviceLoader) {
    this.serviceLoader = serviceLoader;
  }

  public PluginManager(@Nonnull ClassLoader classLoader) {
    this.serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
  }

  @SuppressFBWarnings("DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
  public PluginManager(@Nonnull List<URL> urls) {
    this(new URLClassLoader(urls.toArray(new URL[urls.size()]),
        PluginManager.class.getClassLoader()));
  }

  // STOPSHIP Use property
  @Nonnull
  private final PluginFilter filter = new AcceptAll();
  // STOPSHIP Use property
  @Nonnull
  private final PluginSelector selector = new Last();

  @Nonnull
  private final ConcurrentMap<String, Plugin> map = new ConcurrentHashMap<String, Plugin>();

  @Nonnull
  public Plugin getPlugin(@Nonnull String name) throws PluginNotFoundException {
    Plugin plugin = map.get(name);
    if (plugin == null) {
      List<Plugin> plugins = new ArrayList<Plugin>();
      for (Plugin candidate : serviceLoader) {
        if (candidate.getCanonicalName().equals(name)) {
          if (candidate.isCompatibileWithJack(Jack.getVersion())) {
            if (filter.accept(candidate)) {
              plugins.add(candidate);
            }
          } else {
            logger.log(Level.INFO, "For plugin ''{0}'', "
                + "rejected version {1} because not compatible with Jack version {2}",
                new Object[] {candidate.getCanonicalName(), candidate.getVersion().getVersion(),
                    Jack.getVersion().getVersion()});
          }
        }
      }

      if (plugins.isEmpty()) {
        throw new PluginNotFoundException(name);
      }

      Plugin newPlugin = selector.select(plugins);
      plugin = map.putIfAbsent(name, newPlugin);
      if (plugin == null) {
        plugin = newPlugin;
      }
    }

    assert plugin != null;
    return plugin;
  }

  @Nonnull
  public Collection<Plugin> getPlugins() {
    return map.values();
  }

  public boolean hasPlugins() {
    return !map.isEmpty();
  }

  @Nonnull
  public ReflectionManager getReflectionManager(@Nonnull ReflectionManager primary) {
    if (getPlugins().size() == 0) {
      return primary;
    } else {
      CompositeReflectionManager composite = new CompositeReflectionManager();
      composite.addReflectionManager(primary);
      for (Plugin plugin : getPlugins()) {
        composite.addReflectionManager(plugin.getReflectionManager());
      }

      return composite;
    }
  }

  @Nonnull
  public Collection<Plugin> getAvailablePlugins() {
    List<Plugin> plugins = new ArrayList<Plugin>();
    for (Plugin plugin : serviceLoader) {
      plugins.add(plugin);
    }

    return plugins;
  }
}
