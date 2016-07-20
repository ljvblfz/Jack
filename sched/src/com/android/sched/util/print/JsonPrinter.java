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

import com.android.sched.util.codec.ImplementationName;

import java.io.PrintWriter;

import javax.annotation.Nonnull;

/**
 * JSON printer implementation for {@link DataModel}
 */
@ImplementationName(iface = Printer.class, name = "json")
public class JsonPrinter extends AbstractPrinter {
  public JsonPrinter(@Nonnull PrintWriter printer) {
    super(printer);
    printers.put(DataType.NOTHING, new NothingPrinter());
    printers.put(DataType.BOOLEAN, new BooleanFormatter());
    printers.put(DataType.DURATION, new NumberFormatter());
    printers.put(DataType.NUMBER, new NumberFormatter());
    printers.put(DataType.PERCENT, new NumberFormatter());
    printers.put(DataType.QUANTITY, new NumberFormatter());
    printers.put(DataType.STRING, new StringFormatter());
    printers.put(DataType.BUNDLE, new StringFormatter());
    printers.put(DataType.STRUCT, new StructureFormatter());
    printers.put(DataType.LIST, new ListFormatter());
  }

  private static class NumberFormatter implements TypePrinter<Number> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintWriter printer, @Nonnull Number value) {

      if (value instanceof Double) {
        if (((Double) value).isNaN() || ((Double) value).isInfinite()) {
          printer.print("null");
        } else {
          printer.print(value.toString());
        }
      } else if (value instanceof Float) {
        if (((Float) value).isNaN() || ((Float) value).isInfinite()) {
          printer.print("null");
        } else {
          printer.print(value.toString());
        }
      } else {
        printer.print(value.toString());
      }

      return true;
    }
  }

  private static class BooleanFormatter implements TypePrinter<Boolean> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintWriter printer, @Nonnull Boolean value) {
      printer.print(value.booleanValue() ? "true" : "false");

      return true;
    }
  }

  private static class StringFormatter implements TypePrinter<String> {
    @Override
    public boolean print(@Nonnull PrintWriter printer, @Nonnull String value) {
      printer.print("\"" + value.replace("\"", "\\\"") + "\"");

      return true;
    }
  }

  private class ListFormatter implements TypePrinter<DataModel> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintWriter printer, @Nonnull DataModel model) {
      TypePrinter<Object> formatter =
          JsonPrinter.this.<Object>getFormatter(model.getDataView().getDataTypes()[0]);
      boolean first = true;
      int idx = 0;

      printer.print('[');
      for (Object object : model) {
        if (!first) {
          printer.print(',');
        } else {
          first = false;
        }

        formatter.print(printer, object);
      }
      printer.print(']');

      return true;
    }
  }

  private class StructureFormatter implements TypePrinter<DataModel> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintWriter printer, @Nonnull DataModel model) {
      DataType[] types = model.getDataView().getDataTypes();
      String[]   names = model.getDataView().getDataNames();
      boolean first = true;
      int idx = 0;

      printer.print('{');
      for (Object object : model) {
        if (object != null) {
          if (!first) {
            printer.print(',');
          } else {
            first = false;
          }

          printer.print('"');
          printer.print(names[idx]);
          printer.print("\":");
          TypePrinter<Object> formatter =
              JsonPrinter.this.<Object>getFormatter(types[idx]);
          formatter.print(printer, object);
        }
        idx++;
      }
      printer.print('}');

      return true;
    }
  }
}

