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

package com.android.jack.server.type;

import com.android.sched.util.TextUtils;
import com.android.sched.util.stream.UncloseableOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Manager for writing a application/vnd.jack.command-out.
 */
public class CommandOut {

  @Nonnull
  private static final Logger logger = Logger.getLogger(CommandOut.class.getName());

  @Nonnull
  public static final String JACK_COMMAND_OUT_CONTENT_TYPE = "application/vnd.jack.command-out";

  @Nonnull
  public static final Charset DEFAULT_COMMAND_OUT_CHARSET = StandardCharsets.UTF_8;

  @Nonnull
  private static final String OUT_PREFIX = "O|";
  @Nonnull
  private static final String ERR_PREFIX = "E|";
  @Nonnull
  private static final String EXIT_PREFIX = "X|";

  @Nonnull
  private static final String EOL = "\n";

  @Nonnull
  private final PrintStream out;
  @Nonnull
  private final PrintStream err;
  @Nonnull
  private final PrintStream exit;
  @Nonnull
  private final OutputStream os;

  public CommandOut(@Nonnull WritableByteChannel channel,
      @Nonnull Charset charset) {
    os = Channels.newOutputStream(channel);
    OutputStream unclosable = new UncloseableOutputStream(os);
    try {
      out = new CommantOutPrintStream(unclosable, charset, OUT_PREFIX);
      err = new CommantOutPrintStream(unclosable, charset, ERR_PREFIX);
      exit = new CommantOutPrintStream(unclosable, charset, EXIT_PREFIX);
    } catch (UnsupportedEncodingException e) {
      try {
        os.close();
      } catch (IOException e1) {
        logger.log(Level.SEVERE, "Failed to close", e);
      }
      throw new AssertionError(charset);
    }
  }

  public PrintStream getOut() {
    return out;
  }

  public PrintStream getErr() {
    return err;
  }

  public void close(int exitCode) throws IOException {
    out.close();
    err.close();
    exit.println(exitCode);
    exit.close();
    os.close();
  }

  private static class CommantOutPrintStream extends PrintStream {

    @Nonnull
    private static final String INPUT_EOL = TextUtils.LINE_SEPARATOR;
    @Nonnull
    private final byte[] prefix;
    @Nonnull
    private final byte[] outputEol;
    @Nonnull
    private final StringBuilder builder = new StringBuilder();

    CommantOutPrintStream(@Nonnull OutputStream out,
        @Nonnull Charset charset,
        @Nonnull String prefix)
        throws UnsupportedEncodingException {
      super(out, false, charset.name());
      this.prefix = charset.encode(prefix).array();
      this.outputEol = charset.encode(EOL).array();
    }

    @Override
    public synchronized void print(@CheckForNull String string) {
      builder.append(string);
      int index = builder.indexOf(INPUT_EOL);
      while (index >= 0) {
        try {
          synchronized (out) {
            super.write(prefix);
            super.print(builder.subSequence(0, index));
            super.write(outputEol);
            super.flush();
          }
        } catch (IOException e) {
          setError();
        }
        builder.delete(0, index + INPUT_EOL.length());
        index = builder.indexOf(INPUT_EOL);
      }
    }

    @Override
    public void print(boolean value) {
      print(Boolean.toString(value));
    }

    @Override
    public void print(char value) {
      print(Character.toString(value));
    }

    @Override
    public void print(int value) {
      print(Integer.toString(value));
    }

    @Override
    public void print(long value) {
      print(Long.toString(value));
    }

    @Override
    public void print(float value) {
      print(Float.toString(value));
    }

    @Override
    public void print(double value) {
      print(Double.toString(value));
    }

    @Override
    public void print(@Nonnull char[] value) {
      print(new String(value));
    }

    @Override
    public void print(@CheckForNull Object value) {
      print(String.valueOf(value));
    }

    @Override
    public void println(@CheckForNull String value) {
      print(value);
      println();
    }

    @Override
    public void println(boolean value) {
      print(value);
      println();
    }

    @Override
    public void println(char value) {
      print(value);
      println();
    }

    @Override
    public void println(int value) {
      print(value);
      println();
    }

    @Override
    public void println(long value) {
      print(value);
      println();
    }

    @Override
    public void println(float value) {
      print(value);
      println();
    }

    @Override
    public void println(double value) {
      print(value);
      println();
    }

    @Override
    public void println(@Nonnull char[] value) {
      print(value);
      println();
    }

    @Override
    public void println(@CheckForNull Object value) {
      print(value);
      println();
    }

    @Override
    public void println() {
      print(INPUT_EOL);
    }

    @Override
    public PrintStream append(char value) {
      print(value);
      return this;
    }

    @Override
    public PrintStream append(@CheckForNull CharSequence value) {
      print(value);
      return this;
    }

    @Override
    public PrintStream append(@CheckForNull CharSequence value, int start, int end) {
      if (value == null) {
        value = String.valueOf((String) null);
      }
      print(value.subSequence(start, end));
      return this;
    }

    @Override
    public PrintStream format(@Nonnull String format, @CheckForNull Object... args) {
      return format(Locale.getDefault(), format, args);
    }

    @Override
    public PrintStream format(@CheckForNull Locale l,
        @Nonnull String format,
        @CheckForNull Object... args) {
      // The purpose of closing a formatter is to close its Appendable, which we don't want
      @SuppressWarnings("resource")
      Formatter formatter = new Formatter(this, l);
      formatter.format(format, args);
      return this;
    }

    @Override
    public PrintStream printf(@Nonnull String format, @CheckForNull Object... args) {
      return format(format, args);
    }

    @Override
    public PrintStream printf(@CheckForNull Locale l,
        @Nonnull String format,
        @CheckForNull Object... args) {
      return format(l, format, args);
    }

    @Override
    public synchronized void close() {
      if (builder.length() > 0) {
        // Last line was not terminated but protocol does not support non terminated line.
        // Add a line termination to flush the buffer.
        println();
      }
      super.close();
    }
  }

}
