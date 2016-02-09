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

import com.android.sched.schedulable.VisitorSchedulable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Exception to transport {@link Throwable} thrown by
 * {@link VisitorSchedulable}.
 */
public class VisitorProcessException extends ProcessException {
  private static final long serialVersionUID = 1L;

  public VisitorProcessException(@SuppressWarnings("rawtypes") @Nonnull VisitorSchedulable visitor,
      @CheckForNull ManagedSchedulable managedSchedulable, @Nonnull Object data,
      @Nonnull Throwable cause) {
    super(visitor, managedSchedulable, data, cause);
    setMessage("Error during '" + getName() + "' visitor on '" + getData().toString() + "'"
        + getAdditionalCauseMessage());
  }
}
