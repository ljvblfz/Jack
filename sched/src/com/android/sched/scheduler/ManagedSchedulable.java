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

import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.item.Items;
import com.android.sched.item.Synchronized;
import com.android.sched.marker.MarkerNotConformException;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.util.HasDescription;
import com.android.sched.util.log.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a {@link Schedulable} with all annotations and signatures extracted.
 */
public abstract class ManagedSchedulable implements HasDescription {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Class<? extends Schedulable> schedulable;

  // @Name
  @Nonnull
  private final String name;

  // @Description
  @Nonnull
  private final String description;

  // @Synchronized
  private boolean isSynchronized = false;
  @CheckForNull
  private Method  dynamicIsSynchronized = null;

  /**
   * Creates a new instance of {@link ManagedSchedulable} from a {@link Schedulable}.
   *
   * @throws SchedulableNotConformException if the structure of the {@code Schedulable} is not
   * valid.
   */
  protected ManagedSchedulable (@Nonnull Class<? extends Schedulable> schedulable)
      throws SchedulableNotConformException {
    this.schedulable = schedulable;

    name = Items.getName(schedulable);

    // FINDBUGS
    String description = Items.getDescription(schedulable);
    if (description == null) {
      throw new SchedulableNotConformException("Schedulable '" + schedulable.getCanonicalName()
          + "' must have a @" + Description.class.getSimpleName());
    }
    this.description = description;

    extractSynchronized(schedulable);
  }

  @Nonnull
  public <T> Class<? extends Schedulable> getSchedulable() {
    return this.schedulable;
  }

  public abstract boolean isVisitor();

  public abstract boolean isRunnable();

  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * @return if the schedulable is synchronized
   */
  public boolean isSynchronized(@Nonnull Schedulable schedulable) {
    if (isSynchronized) {
      return true;
    }

    if (dynamicIsSynchronized != null) {
      try {
        return ((Boolean) dynamicIsSynchronized.invoke(schedulable)).booleanValue();
      } catch (IllegalArgumentException e) {
        throw new AssertionError(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      } catch (InvocationTargetException e) {
        logger.log(Level.WARNING, "Method '" + dynamicIsSynchronized + "' threw an exception",
            e.getCause());

        return false;
      }
    }

    return false;
  }

  public boolean isStaticallySynchronized() {
    return isSynchronized;
  }

  @CheckForNull
  public Method getDynamicallySynchronizedMethod() {
    return dynamicIsSynchronized;
  }

  /**
   * Returns the type of data this is applied to.
   */
  @Nonnull
  public abstract Class<? extends Component> getRunOn();

  @Override
  @Nonnull
  public String getDescription() {
    return description;
  }

  @Nonnull
  @Override
  public abstract String toString();


  private void extractSynchronized(
      @Nonnull Class<? extends Schedulable> cls) {
    Synchronized sync = cls.getAnnotation(Synchronized.class);

    isSynchronized = sync != null;

    for (Method method : cls.getMethods()) {
      Synchronized dynamicSynchronizedOnAnnotation = method.getAnnotation(Synchronized.class);

      if (dynamicSynchronizedOnAnnotation != null) {
        if (!method.getReturnType().equals(Boolean.TYPE)) {
          throw new SchedulableNotConformException("Annotated method '" + method + "' with @"
              + Synchronized.class.getSimpleName() + " must have a 'boolean' return type");
        }

        if (method.getParameterTypes().length != 0) {
          throw new SchedulableNotConformException("Annotated method '" + method + "' with @"
              + Synchronized.class.getSimpleName() + " must have no parameter");
        }

        if (isSynchronized) {
          throw new SchedulableNotConformException("Schedulable '" + name
              + "' cannot have both a static and a dynamic @" + Synchronized.class.getName()
              + " (on class '" + cls.getCanonicalName() + "')");
        }

        if (dynamicIsSynchronized != null) {
          throw new MarkerNotConformException("Schedulable '" + name + "' cannot have two @"
              + Synchronized.class.getName() + " ('" + method + "' and '" + dynamicIsSynchronized
              + "')");
        }

        dynamicIsSynchronized = method;
      }
    }
  }
}
