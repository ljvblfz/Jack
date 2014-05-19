/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.annotation.Nonnull;

/**
 * Class that continuously read an {@link InputStream} and optionally could print the input in a
 * {@link PrintStream}.
 */
public class CharactersStreamSucker {

  @Nonnull
  private final InputStream is;
  @Nonnull
  private final PrintStream os;

  private final boolean toBeClose;

  public CharactersStreamSucker(
      @Nonnull InputStream is, @Nonnull PrintStream os, boolean toBeClose) {
    this.is = is;
    this.os = os;
    this.toBeClose = toBeClose;
  }

  public CharactersStreamSucker(@Nonnull InputStream is, @Nonnull PrintStream os) {
    this(is, os, false);
  }

  public CharactersStreamSucker(@Nonnull InputStream is) {
    this(is, new NullPrintStream(), false);
  }

  public void suck() throws IOException {
    int readChar;
    try {
      while ((readChar = is.read()) != -1) {
        os.write(readChar);
      }
    } finally {
      if (toBeClose) {
        os.close();
      }
    }
  }
}