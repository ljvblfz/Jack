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
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.util.Reflect;
import com.android.sched.util.log.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Represents a {@link AdapterSchedulable} with all annotations and signatures extracted.
 */
public class ManagedVisitor extends ManagedSchedulable {
  // Original visitor
  @Nonnull
  private final Class<? extends AdapterSchedulable<? extends Component, ? extends Component>>
      visitor;

  // Class ... implements VisitorSchedulable<T,...>
  @Nonnull
  private final Class<? extends Component> schedulableOn;

  // Class ... implements VisitorSchedulable<...,U>
  @Nonnull
  private final Class<? extends Component> visitorTo;

  public ManagedVisitor(@Nonnull Class<
      ? extends AdapterSchedulable<? extends Component, ? extends Component>> visitor)
      throws SchedulableNotConformException {
    super(visitor);

    this.visitor = visitor;

    schedulableOn = extractSchedulableOn();
    visitorTo = extractVisitorTo();

    if (schedulableOn == visitorTo) {
      throw new SchedulableNotConformException("VisitorSchedulable '" + getName()
          + "' can not adapt from/to the same type '" + schedulableOn.getCanonicalName() + "'");
    }

    LoggerFactory.getLogger().log(Level.CONFIG, "{0}", this);
  }

  @Nonnull
  public <T> Class<? extends AdapterSchedulable<?
      extends Component, ? extends Component>> getVisitorSchedulable() {
    return this.visitor;
  }

  @Override
  public boolean isVisitor() {
    return true;
  }

  @Override
  public boolean isRunnable() {
    return false;
  }

  /**
   * Returns the type of data that this adapts to.
   */
  @Nonnull
  public Class<? extends Component> getRunOnAfter() {
    return visitorTo;
  }

  @Nonnull
  @Override
  public Class<? extends Component> getRunOn() {
    return schedulableOn;
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private Class<? extends Component> extractSchedulableOn() {
    for (Type intf : Reflect.getAllGenericInSuperClassOrInterface(visitor)) {
      if (intf instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) intf;

        if (AdapterSchedulable.class.isAssignableFrom(
            ((Class<? extends Component>) (pt.getRawType())))) {
          return (Class<? extends Component>) pt.getActualTypeArguments()[0];
        }
      }
    }

    throw new AssertionError();
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private Class<? extends Component> extractVisitorTo() {
    for (Type intf : Reflect.getAllGenericInSuperClassOrInterface(visitor)) {
      if (intf instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) intf;

        if (AdapterSchedulable.class.isAssignableFrom(
            ((Class<? extends Component>) (pt.getRawType())))) {
          return (Class<? extends Component>) pt.getActualTypeArguments()[1];
        }
      }
    }

    throw new AssertionError();
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Visitor '");
    sb.append(getName());
    sb.append('\'');

    return new String(sb);
  }
}
