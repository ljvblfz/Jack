/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.util.print;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

/**
 * Based printer implementation for {@link DataModel}
 */
public abstract class AbstractPrinter implements Printer {
  @Nonnull
  protected final EnumMap<DataType, TypePrinter<?>> printers =
      new EnumMap<DataType, TypePrinter<?>>(DataType.class);
  @Nonnull
  private final TypePrinter<?> defaultPrinter = new MissingPrinter();
  @Nonnull
  private final PrintStream printer;
  @Nonnull
  private final ArrayList<ResourceBundle> bundles = new ArrayList<ResourceBundle>();

  public AbstractPrinter(@Nonnull PrintStream printer) {
    this.printer = printer;
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T> TypePrinter<T> getFormatter(@Nonnull DataType type) {
    assert printers.size() == DataType.values().length : "Missing printer in "
        + this.getClass().getCanonicalName();

    TypePrinter<?> printer = printers.get(type);
    if (printer == null) {
      printer = defaultPrinter;
    }

    return (TypePrinter<T>) printer;
  }

  @Override
  @Nonnull
  public Printer addResourceBundles(@Nonnull Collection<ResourceBundle> bundles) {
    this.bundles.addAll(bundles);

    return this;
  }

  @Override
  @Nonnull
  public Printer addResourceBundles(@Nonnull ResourceBundle ... bundles) {
    this.bundles.addAll(Arrays.asList(bundles));

    return this;
  }

  @Nonnull
  protected String getString(@Nonnull String name) {
    for (ResourceBundle bundle : bundles) {
      try {
        return bundle.getString(name);
      } catch (MissingResourceException e) {
        // Go to the next bundle
      }
    }

    return name;
  }

  @Override
  public boolean print(@Nonnull DataModel data) {
    TypePrinter<DataModel> formatter =
        this.<DataModel>getFormatter(data.getDataView().getDataType());
    return formatter.print(printer, data);
  }

  /**
   * Missing printer
   */
  private class MissingPrinter implements TypePrinter<Object> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintStream printer, @Nonnull Object object) {
      return AbstractPrinter.this.<String> getFormatter(DataType.STRING).print(
          printer, "<missing formatter for '" + object.getClass().getCanonicalName() + "'>");
    }
  }

  /**
   * Default bundle key printer
   */
  protected class BundlePrinter implements TypePrinter<String> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintStream printer, @Nonnull String key) {
      return AbstractPrinter.this.<String> getFormatter(DataType.STRING).print(
          printer, getString(key));
    }
  }

  /**
   * Default dummy printer
   */
  protected static class NothingPrinter implements TypePrinter<Object> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintStream printer, @Nonnull Object object) {
      return false;
    }
  }
}
