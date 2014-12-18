/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.scheduler;

import com.android.sched.schedulable.Schedulable;
import com.android.sched.util.config.ChainedException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Exception to transport {@link Throwable} during {@link Schedulable} execution.
 */
public abstract class ProcessException extends ChainedException {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private final String name;
  @Nonnull
  private final Object data;
  @Nonnull
  private final Throwable cause;

  public ProcessException(@Nonnull Schedulable schedulable,
      @CheckForNull ManagedSchedulable managedSchedulable, @Nonnull Object data,
      @Nonnull Throwable cause) {
    super("");
    this.name =
        (managedSchedulable != null) ? managedSchedulable.getName() : ("<"
            + schedulable.getClass().getSimpleName() + ">");
    this.data = data;
    this.cause = cause;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public Object getData() {
    return data;
  }

  @Override
  @Nonnull
  public Throwable getCause() {
    return cause;
  }

  @Nonnull
  protected String getAdditionalCauseMessage() {
    String msg = cause.getMessage();

    if (msg != null) {
      return ": " + msg;
    } else {
      return "";
    }
  }
}
