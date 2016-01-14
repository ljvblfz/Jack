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

package com.android.jack.digest;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.SerializableMarker;
import com.android.sched.marker.ValidOn;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Marker which contains a digest of the origin of the type
 */
@Description("Contains digest of the origine")
@ValidOn(JDefinedClassOrInterface.class)
public class OriginDigestMarker implements SerializableMarker {
  @Nonnull
  private final Set<OriginDigestElement> descriptor;
  @Nonnull
  private final String algo;
  @Nonnull
  private final byte[] digest;
  @Nonnull
  private final String emitter;
  private final int major;
  private final int minor;


  public OriginDigestMarker(@Nonnull Set<OriginDigestElement> descriptor, @Nonnull String algo,
      @Nonnull byte[] digest, @Nonnull String emitter, int major, int minor) {
    this.algo = algo;
    this.digest = digest.clone();
    this.descriptor = descriptor;
    this.emitter = emitter;
    this.major = major;
    this.minor = minor;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  @Nonnull
  public byte[] getDigest() {
    return digest.clone();
  }

  @Nonnull
  public Set<OriginDigestElement> getDescriptor() {
    return descriptor;
  }

  @Nonnull
  public String getAlgo() {
    return algo;
  }

  @Nonnull
  public String getEmitterId() {
    return emitter;
  }

  public int getMajorCode() {
    return major;
  }

  public int getMinorCode() {
    return minor;
  }

  @Override
  public String toString() {
    return "<" + descriptor + "," + algo + "," + new String(encode(digest)) + ">";
  }

  @Nonnull
  private static final byte[] code = "0123456789ABCDEF".getBytes();

  @Nonnull
  private static char[] encode(@Nonnull byte[] bytes) {
    char[] array = new char[bytes.length * 2];

    for (int idx = 0; idx < bytes.length; idx++) {
      array[(idx << 1)] = (char) code[(bytes[idx] & 0xF0) >> 4];
      array[(idx << 1) + 1] = (char) code[(bytes[idx] & 0x0F)];
    }

    return array;
  }

  @Override
  public final boolean equals(Object obj) {
    if (!(obj instanceof OriginDigestMarker)) {
      return false;
    }

    OriginDigestMarker marker = (OriginDigestMarker) obj;
    return (algo.equals(marker.algo) &&
        descriptor.equals(marker.descriptor) &&
        Arrays.equals(digest, marker.digest));
  }

  @Override
  public int hashCode() {
    return algo.hashCode() ^ descriptor.hashCode() ^ Arrays.hashCode(digest);
  }
}
