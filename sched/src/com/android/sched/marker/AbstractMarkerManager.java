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

package com.android.sched.marker;

import com.android.sched.SchedProperties;
import com.android.sched.item.Items;
import com.android.sched.item.onlyfor.Default;
import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.reflections.ReflectionFactory;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.scheduler.ManagedVisitor;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides {@link Marker}-managing abilities to its subclasses.
 * <p>This is a skeletal implementation. Two full implementations are available:
 * {@link StaticMarkerManager} and {@link LocalMarkerManager}.
 */
abstract class AbstractMarkerManager implements MarkerManager {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @CheckForNull
  private static Map<Class<? extends Marker>, ManagedMarker> map = null;

  @Nonnull
  private static Class<? extends OnlyForType> onlyFor = ThreadConfig.get(SchedProperties.ONLY_FOR);

  private static final boolean FAILED_STOP =
      ThreadConfig.get(SchedProperties.FAILED_STOP).booleanValue();

  protected AbstractMarkerManager() {
    ensureScan();
    assert map != null;
  }

  @Override
  @CheckForNull
  public abstract <T extends Marker> T removeMarker(@Nonnull Class<T> c);

  @Override
  public abstract <T extends Marker> boolean containsMarker(@Nonnull Class<T> c);

  @Override
  public abstract void addMarker(@Nonnull Marker m);

  @Override
  @Nonnull
  public abstract Collection<Marker> getAllMarkers();

  @Override
  @CheckForNull
  public abstract <T extends Marker> T getMarker(@Nonnull Class<T> c);

  protected boolean isValidMarker(@Nonnull Class<? extends Marker> marker) {
    assert map != null;

    return map.get(marker).isValidMarker(this);
  }

  protected boolean isValidMarker(@Nonnull Marker marker) {
    assert map != null;

    return map.get(marker.getClass()).isValidMarker(this, marker);
  }

  protected boolean checkRemoveAccess(@Nonnull Class<? extends Marker> marker) {
    ManagedRunnable runner = getCurrentSchedulable();

    if (runner != null &&
        !runner.getRemovedTags().contains(marker)) {
      throw new AssertionError("SchedulableRunnable '" + runner.getName()
          + "' try to remove marker '" + Items.getName(marker)
          + "' without 'remove' specification on it.");
    }

    return true;
  }

  protected boolean checkAddAccess (@Nonnull Class<? extends Marker> marker) {
    ManagedRunnable runner = getCurrentSchedulable();

    if (runner != null &&
        !runner.getAddedTags().contains(marker)) {
      throw new AssertionError("SchedulableRunnable '" + runner.getName() + "' try to add marker '"
          + Items.getName(marker) + "' without 'add' specification on it.");
    }

    return true;
  }

  protected boolean checkGetAccess (@Nonnull Class<? extends Marker> marker) {
    ManagedRunnable runner = getCurrentSchedulable();

    if (runner != null) {
      FeatureSet features = ScheduleInstance.getCurrentFeatures();

      if (features != null) {
        // TODO(mikaelpeltier) Some cases does not require need constraint but could access markers.
//        if (!runner.getNeededTags(features).contains(marker)) {
//          throw new AssertionError("SchedulableRunnable '" + runner.getName()
//              + "' try to get marker '" + Items.getName(marker)
//              + "' without 'need' specification on it.");
//        }

        if (runner.getUnsupportedTags(features).contains(marker)) {
          throw new AssertionError("SchedulableRunnable '" + runner.getName()
              + "' try to get marker '" + Items.getName(marker)
              + "' with a 'no' specification on it.");
        }
      }
    }

    return true;
  }

  @CheckForNull
  private static ManagedRunnable getCurrentSchedulable() {
    ManagedSchedulable schedulable = null;

    ensureScan();
    assert map != null;

    try {
      schedulable = ScheduleInstance.getCurrentSchedulable();
    } catch (EmptyStackException e) {
      // If we are not in a context of a run, there is no thing to check.
    }

    if (schedulable != null && schedulable instanceof ManagedVisitor) {
      throw new AssertionError("Schedulable '" + schedulable.getName()
          + "' try use marker but it is not a SchedulableRunnable.");
    }

    return (ManagedRunnable) schedulable;
  }

  private static void registerMarker(@Nonnull Class<? extends Marker> marker)
      throws MarkerNotConformException {
    assert map != null;

    try {
      map.put(marker, new ManagedMarker(marker));
      logger.log(Level.INFO, "Register marker ''{0}'' ({1})", new Object[] {
          marker.getName(), marker.getCanonicalName()});
    } catch (MarkerNotConformException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      throw e;
    }
  }

  private static synchronized void ensureScan() {
    if (map == null) {
      map = new HashMap<Class<? extends Marker>, ManagedMarker>();

      ReflectionManager reflectionManager = ReflectionFactory.getManager();
      for (Class<? extends Marker> marker : reflectionManager.getSubTypesOf(Marker.class)) {
        try {
          Class<? extends OnlyForType> onlyFor = Items.getOnlyForType(marker);

          if (AbstractMarkerManager.onlyFor == null || onlyFor == Default.class
              || onlyFor == AbstractMarkerManager.onlyFor) {
            if (!Modifier.isAbstract(marker.getModifiers()) && !marker.isInterface()) {
              registerMarker(marker);
            }
          } else {
            logger.log(Level.INFO, "Marker ''{0}'' ({1}) is ignored because only for {2}",
                new Object[] {
                    Items.getName(marker), marker.getCanonicalName(), onlyFor.getSimpleName()});
          }
        } catch (MarkerNotConformException e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          if (FAILED_STOP) {
            throw e;
          }
        }
      }
    }
  }
}
