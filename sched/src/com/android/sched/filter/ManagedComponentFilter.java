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

import com.android.sched.item.Component;
import com.android.sched.item.ManagedConcreteItem;
import com.android.sched.schedulable.ComponentFilter;
import com.android.sched.util.HasDescription;
import com.android.sched.util.Reflect;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Represents a {@link ComponentFilter} with all annotations extracted.
 */
public class ManagedComponentFilter extends ManagedConcreteItem implements HasDescription {
  private static final Logger logger = LoggerFactory.getLogger();
  // Source component filter
  @Nonnull
  private final Class<? extends ComponentFilter<? extends Component>> filter;

  // Class ... implements ComponentFilter<T>
  @Nonnull
  private Class<? extends Component> filterOn;

  // Nonnull field is actually initialized during construction, in a private method
  @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public ManagedComponentFilter(
      @Nonnull Class<? extends ComponentFilter<? extends Component>> filter,
      @Nonnull ComponentFilterManager manager) throws ComponentFilterNotConformException {
    super(filter, manager);

    this.filter = filter;
    extractFilterOn(filter);

    checkValidity();
  }

  @Nonnull
  public Class<? extends Component> getFilterOn() {
    return filterOn;
  }

  @Nonnull
  public Class<? extends ComponentFilter<? extends Component>> getComponentFilter() {
    return filter;
  }

  private void checkValidity() throws ComponentFilterNotConformException {
  }

  @SuppressWarnings("unchecked")
  private void extractFilterOn(
      @Nonnull Class<? extends ComponentFilter<? extends Component>> cls) {
    for (Type intf : Reflect.getAllGenericInSuperClassOrInterface(cls)) {
      if (intf instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) intf;

        if (ComponentFilter.class.isAssignableFrom((Class<?>) (pt.getRawType()))) {
          filterOn = (Class<? extends Component>) pt.getActualTypeArguments()[0];
          return;
        }
      }
    }

    throw new AssertionError();
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(super.toString());
    sb.append(": component filter on ");
    sb.append(filterOn.getCanonicalName());

    return new String(sb);
  }
}
