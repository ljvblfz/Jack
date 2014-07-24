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

package com.android.sched.vfs;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;


/**
 * {@link OutputVDir} that contains {@link OutputVFile}s that must be written to and closed
 * sequentially.
 */
abstract class SequentialOutputVDir extends AbstractVElement implements OutputVDir {

  @Nonnull
  private final AtomicBoolean lastVFileOpen = new AtomicBoolean(false);

  void notifyVFileClosed() {
    boolean previousState = lastVFileOpen.getAndSet(false);
    assert previousState;
  }

  boolean notifyVFileOpenAndReturnPreviousState() {
    return lastVFileOpen.getAndSet(true);
  }

}
