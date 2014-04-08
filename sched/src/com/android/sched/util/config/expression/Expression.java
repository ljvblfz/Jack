/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.util.config.expression;

import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;


/**
 * Abstract class representing an expression.
 */
public abstract class Expression {
  @Nonnull
  public abstract String getDescription();
  @Nonnull
  public abstract String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException;

  @Nonnull
  protected String formatPropertyName(@Nonnull PropertyId<?> propertyId) {
    StringBuilder sb = new StringBuilder();

    sb.append('\'');
    sb.append(propertyId.getName());
    sb.append('\'');

    return sb.toString();
  }

  @Nonnull
  protected String formatPropertyName(
      @Nonnull ConfigChecker checker, @Nonnull PropertyId<?> propertyId) {
    StringBuilder sb = new StringBuilder();

    sb.append('\'');
    sb.append(propertyId.getName());
    sb.append('\'');

    Location location = checker.getLocation(propertyId);
    String details = location.getDescription();

    if (!details.isEmpty()) {
      sb.append(" (defined in ");
      sb.append(details);
      sb.append(')');
    }

    return sb.toString();
  }
}