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

package com.android.sched.util.stream;

import com.android.sched.util.LineSeparator;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link PrintWriter} which supports customized separator line and a way to retrieve
 * {@link IOException}.
 */
public class CustomPrintWriter extends PrintWriter {
  private boolean autoFlush = false;
  @CheckForNull
  private Formatter formatter = null;
  @Nonnull
  private final String newLine;
  @CheckForNull
  private IOException pendingFirstException = null;

  public CustomPrintWriter(@Nonnull Writer out) {
    this(out, LineSeparator.SYSTEM.getLineSeparator());
  }

  public CustomPrintWriter(@Nonnull Writer out, @Nonnull String lineSeparator) {
    this(out, lineSeparator, false);
  }

  public CustomPrintWriter(@Nonnull Writer out, @Nonnull String lineSeparator,
      boolean autoFlush) {
    super(out, autoFlush);
    this.autoFlush = autoFlush;
    this.newLine = lineSeparator;
  }

  private void manageException(@Nonnull IOException e) {
    if (pendingFirstException == null) {
      pendingFirstException = e;
      super.setError();
    }
  }

  //
  // flush & close
  //

  @Override
  public void flush() {
    synchronized (lock) {
      try {
        ensureOpen();
        out.flush();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  @Override
  public void close() {
    synchronized (lock) {
      try {
        if (out == null) {
          return;
        }
        out.close();
        out = null;
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  //
  // write
  //

  @Override
  public void write(int c) {
    synchronized (lock) {
      try {
        ensureOpen();
        out.write(c);
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  @Override
  public void write(char[] buf, int off, int len) {
    synchronized (lock) {
      try {
        ensureOpen();
        out.write(buf, off, len);
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  @Override
  public void write(String s, int off, int len) {
    synchronized (lock) {
      try {
        ensureOpen();
        out.write(s, off, len);
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  //
  // println
  //

  @Override
  public void println() {
    synchronized (lock) {
      try {
        ensureOpen();
        newLine();
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  @Override
  public void println(boolean b) {
    println(String.valueOf(b));
  }

  @Override
  public void println(char c) {
    println(String.valueOf(c));
  }

  @Override
  public void println(int i) {
    println(String.valueOf(i));
  }

  @Override
  public void println(long l) {
    println(String.valueOf(l));
  }

  @Override
  public void println(float f) {
    println(String.valueOf(f));
  }

  @Override
  public void println(double d) {
    println(String.valueOf(d));
  }

  @Override
  public void println(Object obj) {
    println(String.valueOf(obj));
  }

  @Override
  public void println(char[] ac) {
    synchronized (lock) {
      try {
        print(ac);
        newLine();
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  @Override
  public void println(String string) {
    synchronized (lock) {
      try {
        print(string);
        newLine();
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }
  }

  private void newLine() throws IOException {
    out.write(newLine);
    if (autoFlush) {
      out.flush();
    }
  }

  //
  // format
  //

  @Override
  @Nonnull
  public PrintWriter format(String format, Object... args) {
    format(Locale.getDefault(), format, args);

    return this;
  }

  @Override
  @Nonnull
  public PrintWriter format(Locale locale, String format, Object... args) {
    synchronized (lock) {
      try {
        ensureOpen();
        ensureFormatter(locale);
        assert formatter != null;
        formatter.format(locale, format, args);
        if (autoFlush) {
          out.flush();
        }
      } catch (InterruptedIOException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        manageException(e);
      }
    }

    return this;
  }

  private void ensureFormatter(@Nonnull Locale locale) {
    if (formatter == null || formatter.locale() != locale) {
      formatter = new Formatter(this, locale);
    }
  }

  //
  // Error management
  //

  @Override
  public boolean checkError() {
    synchronized (lock) {
      if (out != null) {
        flush();
      }

      return pendingFirstException != null;
    }
  }

  @Override
  public void clearError() {
    synchronized (lock) {
      pendingFirstException = null;
      super.clearError();
    }
  }

  public void throwPendingException() throws IOException {
    synchronized (lock) {
      if (pendingFirstException != null) {
        IOException pending = pendingFirstException;
        clearError();
        throw pending;
      }
    }
  }

  //
  // Utilities
  //

  private void ensureOpen() throws IOException {
    if (out == null) {
      throw new IOException("Writer already closed");
    }
  }
}
