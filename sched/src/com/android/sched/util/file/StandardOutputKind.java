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

package com.android.sched.util.file;

import com.android.sched.util.location.Location;

import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Standard output stream kinds
 */
public enum StandardOutputKind {

  STANDARD_OUTPUT {
    @Override
    @Nonnull
    public OutputStream getOutputStream() {
      return System.out;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return WriterFile.STANDARD_OUTPUT_LOCATION;
    }
  },

  STANDARD_ERROR {
    @Override
    @Nonnull
    public OutputStream getOutputStream() {
      return System.err;
    }


    @Override
    @Nonnull
    public Location getLocation() {
      return WriterFile.STANDARD_ERROR_LOCATION;
    }
  };

  @Nonnull
  public abstract OutputStream getOutputStream();

  @Nonnull
  public abstract Location getLocation();
}