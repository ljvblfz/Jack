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

package com.android.sched.filter;

import com.android.sched.SchedProperties;
import com.android.sched.item.Component;
import com.android.sched.item.Item;
import com.android.sched.item.AbstractItemManager;
import com.android.sched.item.ItemSet;
import com.android.sched.item.Items;
import com.android.sched.item.ManagedItem;
import com.android.sched.item.onlyfor.Default;
import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.marker.Marker;
import com.android.sched.marker.MarkerNotConformException;
import com.android.sched.marker.StaticMarkerManager;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.schedulable.ComponentFilter;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.sched.ManagedDataListener;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Provides {@link Marker}-managing abilities to its subclasses.
 * <p>This is a skeletal implementation. Two full implementations are available:
 * {@link StaticMarkerManager} and {@link LocalMarkerManager}.
 */
public class ComponentFilterManager extends AbstractItemManager {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Class<? extends OnlyForType> onlyFor = ThreadConfig.get(SchedProperties.ONLY_FOR);

  private final boolean failedStop = ThreadConfig.get(SchedProperties.FAILED_STOP).booleanValue();

  @Nonnull
  private final ManagedDataListener listener = ManagedDataListenerFactory.getManagedDataListener();

  @Nonnull
  public static ComponentFilterManager createComponentFilterManager(
      @Nonnull ReflectionManager reflectionManager) {
    return new ComponentFilterManager(reflectionManager);
  }

  protected ComponentFilterManager(@Nonnull ReflectionManager reflectionManager) {
    ensureScan(reflectionManager);
  }

  private void registerComponentFilter(
      @Nonnull Class<? extends ComponentFilter<? extends Component>> filter)
          throws MarkerNotConformException {
    assert map != null;

    try {
      ManagedComponentFilter mcf = new ManagedComponentFilter(filter, this);
      registerManagedItem(mcf);
      logger.log(Level.INFO, "Register {0}", mcf);
    } catch (ComponentFilterNotConformException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  private synchronized void ensureScan(@Nonnull ReflectionManager reflectionManager) {
    for (@SuppressWarnings("rawtypes")
         Class<? extends ComponentFilter> filter : reflectionManager
        .getSubTypesOf(ComponentFilter.class)) {
      try {
        Class<? extends OnlyForType> filterOnlyFor = Items.getOnlyForType(filter);
        if (filterOnlyFor == null || filterOnlyFor == Default.class || filterOnlyFor == onlyFor) {
          if (!Modifier.isAbstract(filter.getModifiers()) && !filter.isInterface()) {
            registerComponentFilter((Class<? extends ComponentFilter<? extends Component>>) filter);
          }
        } else {
          logger.log(Level.INFO, "Filter ''{0}'' ({1}) is ignored because only for {2}",
              new Object[] {Items.getName(filter), filter.getCanonicalName(),
                  filterOnlyFor.getSimpleName()});
        }
      } catch (MarkerNotConformException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        if (failedStop) {
          throw e;
        }
      }
    }


    logger.log(Level.INFO, "Register {0} item(s) in {1} integer(s)",
        new Object[] {Integer.valueOf(getItemsCount()), Integer.valueOf(getIntegersCount())});

    for (ManagedItem item : map.values()) {
      Class<? extends Item> cls = item.getItem();

      for (Class<?> sup : reflectionManager.getSuperTypesOf(cls)) {
        ManagedItem managedSup = map.get(sup);
        if (managedSup != null) {
          managedSup.addComposedOf(item);
        }
      }
    }

    // Add missing @ComposedOf
    for (ManagedItem item : getManagedItems()) {
      item.addComposedOf();
    }

    // Notify & log items
    for (ManagedItem item : map.values()) {
      listener.notifyNewManagedItem(item);

      if (logger.isLoggable(Level.FINER)) {
        ItemSet<Item> set = new ItemSet<Item>(this);
        set.add(item.getItem());
        logger.log(Level.FINER, "Item {0} is {1}", new Object[] {item, set});
      }
    }

    listener.notifyNoMoreManagedItem(getType());
  }

  @Override
  @Nonnull
  public Class<? extends Item> getType() {
    return ComponentFilter.class;
  }
}
