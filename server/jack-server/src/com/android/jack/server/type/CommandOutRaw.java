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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Manager for writing a application/vnd.jack.command-out.
 */
public class CommandOutRaw implements CommandOut {

  @Nonnull
  private static final Logger logger = Logger.getLogger(CommandOutRaw.class.getName());

  @Nonnull
  public static final String JACK_COMMAND_OUT_CONTENT_TYPE = "application/vnd.jack.command-out";

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

  public CommandOutRaw(@Nonnull WritableByteChannel channel,
      @Nonnull Charset inputBinaryCharset,
      @Nonnull Charset outputCharset) {
    os = Channels.newOutputStream(channel);
    OutputStream unclosable = new UncloseableOutputStream(os);
    try {
      out = new CommantOutPrintStream(unclosable, inputBinaryCharset, outputCharset, OUT_PREFIX);
      err = new CommantOutPrintStream(unclosable, inputBinaryCharset, outputCharset, ERR_PREFIX);
      exit = new CommantOutPrintStream(unclosable, inputBinaryCharset, outputCharset, EXIT_PREFIX);
    } catch (UnsupportedEncodingException e) {
      try {
        os.close();
      } catch (IOException e1) {
        logger.log(Level.SEVERE, "Failed to close", e);
      }
      throw new AssertionError(outputCharset);
    }
  }

  @Nonnull
  public PrintStream getOutPrintStream() {
    return out;
  }

  @Override
  @Nonnull
  public PrintStream getErrPrintStream() {
    return err;
  }

  @Override
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
    @Nonnull
    private final ByteArrayOutputStream outputStreamBuffer = new ByteArrayOutputStream();
    @Nonnull
    private final Charset outputCharset;
    @Nonnull
    private final Charset inputBinaryCharset;

    CommantOutPrintStream(@Nonnull OutputStream out,
        @Nonnull Charset inputBinaryCharset,
        @Nonnull Charset outputCharset,
        @Nonnull String prefix)
        throws UnsupportedEncodingException {
      super(out, false, outputCharset.name());
      this.inputBinaryCharset = inputBinaryCharset;

      this.outputCharset = outputCharset;
      this.prefix = outputCharset.encode(prefix).array();
      this.outputEol = outputCharset.encode(EOL).array();
    }

    @Override
    public synchronized void print(@CheckForNull String string) {
      if (outputStreamBuffer.size() != 0) {
        String toPrint =
            inputBinaryCharset.decode(ByteBuffer.wrap(outputStreamBuffer.toByteArray())).toString();
        outputStreamBuffer.reset();
        print(toPrint);
      }
      builder.append(string);
      int index = builder.indexOf(INPUT_EOL);
      while (index >= 0) {
        synchronized (out) {
          super.write(prefix, 0, prefix.length);
          byte[] line = outputCharset.encode(builder.substring(0, index)).array();
          super.write(line, 0, line.length);
          super.write(outputEol, 0, outputEol.length);
          super.flush();
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
      if (outputStreamBuffer.size() > 0) {
        // Flush outputStreamBuffer
        print("");
      }
      assert outputStreamBuffer.size() == 0;
      if (builder.length() > 0) {
        // Last line was not terminated but protocol does not support non terminated line.
        // Add a line termination to flush the buffer.
        println();
      }
      super.close();
    }

    @Override
    public synchronized void write(int b) {

      outputStreamBuffer.write(b);
    }

    @Override
    public synchronized void write(@Nonnull byte[] bytes) throws IOException {
      outputStreamBuffer.write(bytes);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len) {
      outputStreamBuffer.write(buf, off, len);
    }
  }

}
