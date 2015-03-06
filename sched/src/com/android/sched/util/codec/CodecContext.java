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

package com.android.sched.util.codec;

import com.android.sched.util.RunnableHooks;

import java.io.InputStream;
import java.io.PrintStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Object containing context local to a {@code Config} for {@link StringCodec} based parser.
 */
public class CodecContext {
  private boolean debug = false;

  @CheckForNull
  private RunnableHooks hooks;

  @Nonnull
  private InputStream standardInput = System.in;

  @Nonnull
  private PrintStream standardOutput = System.out;

  @Nonnull
  private PrintStream standardError = System.err;

  @Nonnull
  public CodecContext setDebug() {
    this.debug = true;

    return this;
  }

  @Nonnull
  public CodecContext setHooks(@Nonnull RunnableHooks hooks) {
    this.hooks = hooks;

    return this;
  }

  public boolean isDebug() {
    return debug;
  }

  @CheckForNull
  public RunnableHooks getRunnableHooks() {
    return hooks;
  }

  @Nonnull
  public InputStream getStandardInput() {
    return standardInput;
  }

  public void setStandardInput(@Nonnull InputStream standardInput) {
    this.standardInput = standardInput;
  }

  @Nonnull
  public PrintStream getStandardOutput() {
    return standardOutput;
  }

  public void setStandardOutput(@Nonnull PrintStream standardOutput) {
    this.standardOutput = standardOutput;
  }

  @Nonnull
  public PrintStream getStandardError() {
    return standardError;
  }

  public void setStandardError(@Nonnull PrintStream standardError) {
    this.standardError = standardError;
  }
}
