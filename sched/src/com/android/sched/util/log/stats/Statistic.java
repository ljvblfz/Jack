/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.log.stats;

import com.google.common.collect.Iterators;

import com.android.sched.util.HasDescription;
import com.android.sched.util.print.DataModel;
import com.android.sched.util.print.TextPrinter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic.
 */
public abstract class Statistic implements DataModel, HasDescription {
  @Nonnull
  private final StatisticId<? extends Statistic> id;

  public abstract void merge (@Nonnull Statistic statistic);

  protected Statistic(@Nonnull StatisticId<? extends Statistic> id) {
    this.id = id;
  }

  @Nonnull
  public StatisticId<? extends Statistic> getId() {
    return id;
  }

  @Override
  @Nonnull
  public String toString() {
    return id.getName();
  }

  public boolean isEnabled() {
    return false;
  }

  //
  // Adapter for deprecated API
  //

  @Nonnull
  @Deprecated
  public final String getDescription(@Nonnegative int columnIdx) {
    return getDataView().getDataNames()[columnIdx];
  }

  @Nonnull
  @Deprecated
  public final String getType(@Nonnegative int columnIdx) {
    switch (getDataView().getDataTypes()[columnIdx]) {
      case STRING:
      case BUNDLE:
        return "string";
      case BOOLEAN:
      case DURATION:
      case NUMBER:
      case PERCENT:
      case QUANTITY:
        return "number";
      case LIST:
      case NOTHING:
      case STRUCT:
        throw new AssertionError(getDataView().getDataTypes()[columnIdx].toString());
    }

    throw new AssertionError(getDataView().getDataTypes()[columnIdx].toString());
  }

  @Nonnull
  @Deprecated
  public final Object getValue(@Nonnegative int columnIdx) {
    return Iterators.get(iterator(), columnIdx);
  }

  @Nonnull
  @Deprecated
  public final String getHumanReadableValue(@Nonnegative int columnIdx) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter printer = new PrintWriter(new PrintStream(baos));
    TextPrinter provider = new TextPrinter(printer);
    provider.getFormatter(getDataView().getDataTypes()[columnIdx]).print(printer,
        getValue(columnIdx));
    printer.close();

    return baos.toString();
  }
}
