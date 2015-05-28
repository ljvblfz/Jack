/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.server.type;

import com.android.jack.server.HasVersion;
import com.android.jack.server.VersionFinder;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
import com.android.sched.util.codec.ParsingException;

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Version finder searching for a given release code and and exact sub-release code.
 */
public class ExactCodeVersionFinder implements VersionFinder {

  @Nonnull
  public static final String SELECT_EXACT_VERSION_CONTENT_TYPE =
    "application/vnd.jack.select-exact";

  @Nonnegative
  private final int release;
  @Nonnegative
  private final int subRelease;
  @Nonnull
  private final SubReleaseKind minKind;

  public ExactCodeVersionFinder(@Nonnegative int major, @Nonnegative int minor,
      @Nonnull SubReleaseKind minKind) {
    this.release = major;
    this.subRelease = minor;
    this.minKind = minKind;
  }

  @Nonnull
  public static ExactCodeVersionFinder parse(@Nonnull String versionString)
      throws ParsingException {
    int codeSeparatorIndex = versionString.indexOf('.');
    if (codeSeparatorIndex == -1) {
      throw new ParsingException("Failed to parse version from '" + versionString + "'");
    }
    int kindSeparatorIndex = versionString.indexOf('.', codeSeparatorIndex + 1);
    if (kindSeparatorIndex == -1) {
      throw new ParsingException("Failed to parse version from '" + versionString + "'");
    }
    try {
      int major = Integer.parseInt(versionString.substring(0, codeSeparatorIndex));
      int minor = Integer.parseInt(versionString.substring(codeSeparatorIndex + 1,
          kindSeparatorIndex));
      SubReleaseKind minKind = SubReleaseKind.valueOf(
          versionString.substring(kindSeparatorIndex + 1));
      return new ExactCodeVersionFinder(major, minor, minKind);
    } catch (NumberFormatException e) {
      throw new ParsingException("Failed to parse version from '" + versionString + "'", e);
    } catch (IllegalArgumentException e) {
      throw new ParsingException("Failed to parse version from '" + versionString + "'", e);
    }
  }

  @Override
  @CheckForNull
  public <T extends HasVersion> T select(@Nonnull Collection<T> collection) {
    for (T t : collection) {
      Version v = t.getVersion();
      if (v.getReleaseCode() == release
          && v.getSubReleaseCode() == subRelease
          && v.getSubReleaseKind().compareTo(minKind) >= 0) {
        return t;
      }
    }
    return null;
  }
}
