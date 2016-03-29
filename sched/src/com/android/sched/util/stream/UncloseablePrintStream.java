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

package com.android.sched.util.stream;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * {@link PrintStream} which silently ignore close
 */
public class UncloseablePrintStream extends PrintStream {
  @Nonnull
  private final PrintStream stream;

  public UncloseablePrintStream(@Nonnull PrintStream stream) {
    super(ByteStreams.nullOutputStream());
    this.stream = stream;
  }

  @Override
  public void close() {
    stream.flush();
  }

  //
  // Following methods are delegated to stream
  //

  @Override
  public int hashCode() {
    return stream.hashCode();
  }

  @Override
  public void write(byte[] b) throws IOException {
    stream.write(b);
  }

  @Override
  public boolean equals(Object obj) {
    return stream.equals(obj);
  }

  @Override
  public String toString() {
    return stream.toString();
  }

  @Override
  public void flush() {
    stream.flush();
  }

  @Override
  public boolean checkError() {
    return stream.checkError();
  }

  @Override
  public void write(int b) {
    stream.write(b);
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    stream.write(buf, off, len);
  }

  @Override
  public void print(boolean b) {
    stream.print(b);
  }

  @Override
  public void print(char c) {
    stream.print(c);
  }

  @Override
  public void print(int i) {
    stream.print(i);
  }

  @Override
  public void print(long l) {
    stream.print(l);
  }

  @Override
  public void print(float f) {
    stream.print(f);
  }

  @Override
  public void print(double d) {
    stream.print(d);
  }

  @Override
  public void print(char[] s) {
    stream.print(s);
  }

  @Override
  public void print(String s) {
    stream.print(s);
  }

  @Override
  public void print(Object obj) {
    stream.print(obj);
  }

  @Override
  public void println() {
    stream.println();
  }

  @Override
  public void println(boolean x) {
    stream.println(x);
  }

  @Override
  public void println(char x) {
    stream.println(x);
  }

  @Override
  public void println(int x) {
    stream.println(x);
  }

  @Override
  public void println(long x) {
    stream.println(x);
  }

  @Override
  public void println(float x) {
    stream.println(x);
  }

  @Override
  public void println(double x) {
    stream.println(x);
  }

  @Override
  public void println(char[] x) {
    stream.println(x);
  }

  @Override
  public void println(String x) {
    stream.println(x);
  }

  @Override
  public void println(Object x) {
    stream.println(x);
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    return stream.printf(format, args);
  }

  @Override
  public PrintStream printf(Locale l, String format, Object... args) {
    return stream.printf(l, format, args);
  }

  @Override
  public PrintStream format(String format, Object... args) {
    return stream.format(format, args);
  }

  @Override
  public PrintStream format(Locale l, String format, Object... args) {
    return stream.format(l, format, args);
  }

  @Override
  public PrintStream append(CharSequence csq) {
    return stream.append(csq);
  }

  @Override
  public PrintStream append(CharSequence csq, int start, int end) {
    return stream.append(csq, start, end);
  }

  @Override
  public PrintStream append(char c) {
    return stream.append(c);
  }
}
