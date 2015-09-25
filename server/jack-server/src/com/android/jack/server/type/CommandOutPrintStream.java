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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

/**
 * PrintStream for writing a application/vnd.jack.command-out.
 */
public class CommandOutPrintStream extends PrintStream {

  @Nonnull
  public static final String JACK_COMMAND_OUT_CONTENT_TYPE = "application/vnd.jack.command-out";

  @Nonnull
  public static final String COMMAND_OUT_CHARSET = StandardCharsets.UTF_8.name();

  private static class CommandOutOutputStream extends OutputStream {

    private final WritableByteChannel out;
    private final ByteBuffer prefix;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public CommandOutOutputStream(WritableByteChannel out, String prefix) {
      this.out = out;
      try {
        this.prefix = ByteBuffer.wrap(prefix.getBytes(COMMAND_OUT_CHARSET));
      } catch (UnsupportedEncodingException e) {
        throw new AssertionError("", e);
      }
    }

    @Override
    public synchronized void write(int b) throws IOException {
      buffer.put((byte) b);
      if (b == '\n') {
        synchronized (out) {
          try {
            writeBuffer(prefix, out);
            buffer.flip();
            writeBuffer(buffer, out);
          } finally {
            buffer.clear();
          }
        }
      }
      if (buffer.remaining() == 1) {
        write('\n');
      }
    }

    private static void writeBuffer(ByteBuffer buffer, WritableByteChannel out) throws IOException {
      int length = buffer.limit();
      int written = 0;
      while (written < length) {
        buffer.position(written);
        written += out.write(buffer);
      }
    }

    @Override
    public void close() throws IOException {
      if (buffer.position() != 0) {
        write('\n');
      }
      super.close();
    }
  }

  @Nonnull
  public static CommandOutPrintStream newInstance(WritableByteChannel out, String prefix) {
    try {
      return new CommandOutPrintStream(out, prefix);
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("", e);
    }
  }

  private CommandOutPrintStream(WritableByteChannel out, String prefix)
      throws UnsupportedEncodingException {
    super(new CommandOutOutputStream(out, prefix), /* autoFlush =  */ true, COMMAND_OUT_CHARSET);
  }
}
