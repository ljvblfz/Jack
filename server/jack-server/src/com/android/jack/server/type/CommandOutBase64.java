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

import com.google.common.io.BaseEncoding;

import com.android.sched.util.log.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Manager for writing a application/vnd.jack.command-out-base64.
 */
public class CommandOutBase64 implements CommandOut {

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static final String JACK_COMMAND_OUT_CONTENT_TYPE =
    "application/vnd.jack.command-out-base64";

  @Nonnull
  private static final String OUT_PREFIX = "O|";
  @Nonnull
  private static final String ERR_PREFIX = "E|";
  @Nonnull
  private static final String EXIT_PREFIX = "X|";

  @Nonnull
  private static final String EOL = "\n";

  @Nonnull
  private final CommantOutStream out;
  @Nonnull
  private final CommantOutStream err;
  @Nonnull
  private final PrintStream printErr;
  @Nonnull
  private final PrintStream backend;
  @Nonnull
  private final Charset outputCharset;

  public CommandOutBase64(@Nonnull PrintStream backend,
      @Nonnull Charset outputCharset) {
    this.backend = backend;
    this.outputCharset = outputCharset;
    out = new CommantOutStream(backend, OUT_PREFIX);
    err = new CommantOutStream(backend, ERR_PREFIX);
    try {
      this.printErr = new PrintStream(err, /* autoFulsh = */ false, outputCharset.name());
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(outputCharset.name(), e);
    }
  }

  @Nonnull
  public OutputStream getOut() {
    return out;
  }

  @Nonnull
  public OutputStream getErr() {
    return err;
  }

  @Override
  @Nonnull
  public PrintStream getErrPrintStream() {
    return printErr;
  }

  @Override
  public void close(int exitCode) {
    out.close();
    printErr.close(); // also closes err
    backend.print(EXIT_PREFIX);
    backend.print(Integer.toString(exitCode));
    backend.print(EOL);

    backend.close();
  }

  private static class CommantOutStream extends OutputStream {

    @Nonnull
    private final String prefix;
    @Nonnull
    private final String outputEol;
    @Nonnull
    private final byte[] buffer = new byte[1];
    @Nonnull
    private final PrintStream out;

    CommantOutStream(@Nonnull PrintStream out,
        @Nonnull String prefix) {
      this.out = out;
      this.prefix = prefix;
      this.outputEol = EOL;
    }

    @Override
    public synchronized void close() {
      flush();
      // do not close out, let CommandOut handle closing of the shared output
    }

    @Override
    public synchronized void write(int b) {
      buffer[0] = (byte) b;
      write(buffer, 0, 1);
    }

    @Override
    public synchronized void write(@Nonnull byte[] bytes) {
      write(bytes, 0, bytes.length);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len) {
      String encoded = BaseEncoding.base64().encode(buf, off, len);
      synchronized (out) {
        out.print(prefix);
        out.print(encoded);
        out.print(EOL);
      }
    }

    @Override
    public synchronized void flush() {
      out.flush();
    }
  }

}
