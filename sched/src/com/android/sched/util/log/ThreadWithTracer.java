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

package com.android.sched.util.log;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link Thread} supporting tracer. This will automatically take care of parent thread creation.
 */
public class ThreadWithTracer extends Thread {
  @CheckForNull
  private ThreadTracerState state;
  @Nonnull
  private final Tracer tracer;

  public ThreadWithTracer(@Nonnull Runnable target, @Nonnull String name) {
    super(target, name);
    tracer = TracerFactory.getTracer();
  }

  public ThreadWithTracer(@Nonnull Runnable target) {
    super(target);
    tracer = TracerFactory.getTracer();
  }

  public ThreadWithTracer(@CheckForNull ThreadGroup group, @Nonnull Runnable target,
      @Nonnull String name, @Nonnegative long stackSize) {
    super(group, target, name, stackSize);
    tracer = TracerFactory.getTracer();
  }

  public ThreadWithTracer(@CheckForNull ThreadGroup group, @Nonnull Runnable target,
      @Nonnull String name) {
    super(group, target, name);
    tracer = TracerFactory.getTracer();
  }

  public ThreadWithTracer(@CheckForNull ThreadGroup group, @Nonnull Runnable target) {
    super(group, target);
    tracer = TracerFactory.getTracer();
  }

  @Override
  public void start() {
    state = tracer.getThreadState();
    super.start();
  }

  @Override
  public void run() {
    assert state != null;
    tracer.pushThreadState(state);
    try {
      super.run();
    } finally {
      assert state != null;
      tracer.popThreadState(state);
    }
  }
}
