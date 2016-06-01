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

package com.android.sched.util.stream;

import com.google.common.io.ByteStreams;

import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class that continuously read an {@link InputStream} and optionally could write the input to an
 * {@link OutputStream}. A {@link Location} must be provided for each stream.
 */
public class LocationByteStreamSucker {

  private static final int BUFFER_SIZE = 4096;

  @Nonnull
  private final byte[] buffer = new byte[BUFFER_SIZE];

  @Nonnull
  private final InputStream is;
  @Nonnull
  private final OutputStream os;
  @CheckForNull
  private Location inputLocation;
  @CheckForNull
  private Location outputLocation;
  @CheckForNull
  private HasLocation inputLocationProvider;
  @CheckForNull
  private HasLocation outputLocationProvider;

  public LocationByteStreamSucker(@Nonnull InputStream is, @Nonnull OutputStream os,
      @Nonnull Location inputLocation, @Nonnull Location outputLocation) {
    this.is = is;
    this.os = os;
    this.inputLocation = inputLocation;
    this.outputLocation = outputLocation;
  }

  public LocationByteStreamSucker(@Nonnull InputStream is, @Nonnull OutputStream os,
      @Nonnull HasLocation inputLocationProvider, @Nonnull HasLocation outputLocationProvider) {
    this.is = is;
    this.os = os;
    this.inputLocationProvider = inputLocationProvider;
    this.outputLocationProvider = outputLocationProvider;
  }

  public LocationByteStreamSucker(@Nonnull InputStream is,
      @Nonnull HasLocation inputLocationProvider) {
    this.is = is;
    this.os = ByteStreams.nullOutputStream();
    this.inputLocationProvider = inputLocationProvider;
    this.outputLocation = NoLocation.getInstance();
  }

  public LocationByteStreamSucker(@Nonnull InputStream is, @Nonnull Location inputLocation) {
    this(is, ByteStreams.nullOutputStream(), inputLocation, NoLocation.getInstance());
  }

  public void suck() throws CannotReadException, CannotWriteException {
    int bytesRead;
    while ((bytesRead = readToBuffer()) >= 0) {
      try {
        os.write(buffer, 0, bytesRead);
        os.flush();
      } catch (IOException e) {
        throw new CannotWriteException(getOutputLocation(), e);
      }
    }
  }

  private int readToBuffer() throws CannotReadException {
    try {
      return is.read(buffer);
    } catch (IOException e) {
      throw new CannotReadException(getInputLocation(), e);
    }
  }

  @Nonnull
  private Location getInputLocation() {
    if (inputLocation != null) {
      return inputLocation;
    }

    if (inputLocationProvider != null) {
      return inputLocationProvider.getLocation();
    }

    throw new AssertionError();
  }

  @Nonnull
  private Location getOutputLocation() {
    if (outputLocation != null) {
      return outputLocation;
    }

    if (outputLocationProvider != null) {
      return outputLocationProvider.getLocation();
    }

    throw new AssertionError();
  }
}