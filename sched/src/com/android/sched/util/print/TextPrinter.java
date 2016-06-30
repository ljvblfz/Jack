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

import com.google.common.base.Strings;

import com.android.sched.util.codec.BooleanCodec;
import com.android.sched.util.codec.DurationFormatter;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.codec.NumberFormatter;
import com.android.sched.util.codec.PercentFormatter;
import com.android.sched.util.codec.QuantityFormatter;
import com.android.sched.util.codec.ToStringFormatter;

import java.io.PrintWriter;

import javax.annotation.Nonnull;

/**
 * Human readable printer implementation for {@link DataModel}
 */
@ImplementationName(iface = Printer.class, name = "text")
public class TextPrinter extends AbstractPrinter {
  @Nonnull
  private final String prefixFirst = "";
  @Nonnull
  private final String prefixFollowing = "";
  @Nonnull
  private final TextIndenter f = new TextIndenter(0);

  public TextPrinter(@Nonnull PrintWriter printer) {
    super(printer);
    printers.put(DataType.NOTHING, new NothingPrinter());
    printers.put(DataType.BOOLEAN, new FormatterAdapter<Boolean>(new BooleanCodec()));
    printers.put(DataType.DURATION, new FormatterAdapter<Long>(new DurationFormatter()));
    printers.put(DataType.NUMBER, new FormatterAdapter<Number>(new NumberFormatter()));
    printers.put(DataType.PERCENT, new FormatterAdapter<Double>(new PercentFormatter()));
    printers.put(DataType.QUANTITY, new FormatterAdapter<Long>(new QuantityFormatter()));
    printers.put(DataType.STRING, new FormatterAdapter<Object>(new ToStringFormatter()));
    printers.put(DataType.BUNDLE, new BundlePrinter());
    printers.put(DataType.STRUCT, new StructurePrinter());
    printers.put(DataType.LIST, new ListPrinter());
  }

  private class ListPrinter implements TypePrinter<DataModel> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintWriter printer, @Nonnull DataModel model) {
      TypePrinter<Object> formatter =
          TextPrinter.this.<Object>getFormatter(model.getDataView().getDataTypes()[0]);
      boolean notEmpty = false;

      f.println(printer);
      for (Object object : model) {
        f.bullet();
        notEmpty |= formatter.print(printer, object);
      }

      return notEmpty;
    }
  }

  private class StructurePrinter implements TypePrinter<DataModel> {
    @Override
    @Nonnull
    public boolean print(@Nonnull PrintWriter printer, @Nonnull DataModel model) {
      boolean notEmpty = false;
      DataType[] types = model.getDataView().getDataTypes();
      String[]   names = model.getDataView().getDataNames();
      int idx = 0;

      f.println(printer);
      f.push();
      f.bullet();
      for (Object object : model) {
        if (object != null) {
          f.print(printer, getString(names[idx]));
          f.print(printer, ": ");

          TypePrinter<Object> formatter = TextPrinter.this.<Object> getFormatter(types[idx]);
          formatter.print(printer, object);
          f.println(printer);
        }
        idx++;
      }
      f.pop();

      return notEmpty;
    }
  }

  private static class TextIndenter {
    private int indent = 0;
    @Nonnull
    private String blank = "  ";
    @Nonnull
    private String bullet = "- ";

    @Nonnull
    private String currentBlank = "";
    @Nonnull
    private String currentBullet = "";

    private boolean needBullet;
    private boolean newLineDone = true;
    private boolean prefixDone = false;

    public TextIndenter(@Nonnull int indent) {
      this.indent = indent;
    }

    @SuppressWarnings("unused")
    public TextIndenter setBullet(String bullet) {
      this.bullet = bullet;
      if (this.indent > 1) {
        this.currentBullet = Strings.repeat(blank, indent - 1) + bullet;
      }

      return this;
    }

    @SuppressWarnings("unused")
    public TextIndenter setIndent(@Nonnull String indent) {
      this.blank = indent;
      if (this.indent > 0) {
        this.currentBlank = Strings.repeat(blank, this.indent);
      }

      if (this.indent > 1) {
        this.currentBullet = Strings.repeat(blank, this.indent - 1) + bullet;
      }

      return this;
    }

    @SuppressWarnings("unused")
    public int getIndent() {
      return indent;
    }

    public void push() {
      indent++;
      currentBullet = currentBlank + bullet;
      currentBlank  = currentBlank + blank;
    }

    public void bullet() {
      needBullet = true;
    }

    public void pop() {
      indent--;
      currentBlank  = currentBlank.substring(blank.length());
      currentBullet = currentBullet.substring(blank.length());
    }

    public void print(@Nonnull PrintWriter printer, @Nonnull String str) {
      if (!prefixDone) {
        if (needBullet) {
          printer.print(currentBullet);
          needBullet = false;
        } else {
          printer.print(currentBlank);
        }
        prefixDone = true;
      }

      printer.print(str);
      newLineDone = false;
    }

    public void println(@Nonnull PrintWriter printer) {
      prefixDone = false;
      if (!newLineDone) {
        printer.println();
        newLineDone = true;
      }
    }
  }
}
