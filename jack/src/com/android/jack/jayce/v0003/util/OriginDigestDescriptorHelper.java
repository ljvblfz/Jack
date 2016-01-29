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

package com.android.jack.jayce.v0003.util;

import com.android.jack.digest.OriginDigestElement;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A helper class to encode a {@link Set}&lt;{@link OriginDigestElement}&gt; values in
 * Jayce format.
 */
public class OriginDigestDescriptorHelper {

  @Nonnull
  private static OriginDigestElement[] values;
  @Nonnull
  private static int[] ids;

  // Very trivial cache just to avoid memory consumption
  private static int lastId = 0;
  @Nonnull
  private static Set<OriginDigestElement> lastSet = EnumSet.noneOf(OriginDigestElement.class);

  static {
    assert OriginDigestElement.values().length == 9;

    values = new OriginDigestElement[9];
    values[0] = OriginDigestElement.SOURCE;
    values[1] = OriginDigestElement.BINARY;
    values[2] = OriginDigestElement.LOCAL_NAME;
    values[3] = OriginDigestElement.PRIVATE_NAME;
    values[4] = OriginDigestElement.PACKAGE_NAME;
    values[5] = OriginDigestElement.PROTECTED_NAME;
    values[6] = OriginDigestElement.PUBLIC_NAME;
    values[7] = OriginDigestElement.COMMENT;
    values[8] = OriginDigestElement.FORMAT;

    ids = new int[9];
    ids[OriginDigestElement.SOURCE.ordinal()]         = 1 << 0;
    ids[OriginDigestElement.BINARY.ordinal()]         = 1 << 1;
    ids[OriginDigestElement.LOCAL_NAME.ordinal()]     = 1 << 2;
    ids[OriginDigestElement.PRIVATE_NAME.ordinal()]   = 1 << 3;
    ids[OriginDigestElement.PACKAGE_NAME.ordinal()]   = 1 << 4;
    ids[OriginDigestElement.PROTECTED_NAME.ordinal()] = 1 << 5;
    ids[OriginDigestElement.PUBLIC_NAME.ordinal()]    = 1 << 6;
    ids[OriginDigestElement.COMMENT.ordinal()]        = 1 << 7;
    ids[OriginDigestElement.FORMAT.ordinal()]         = 1 << 8;
  }

  @Nonnegative
  public static synchronized int getInt(@Nonnull Set<OriginDigestElement> set) {
    if (lastSet.equals(set)) {
      return lastId;
    }

    int id = 0;

    for (OriginDigestElement element : set) {
      id |= ids[element.ordinal()];
    }

    lastSet = set;
    lastId = id;

    return id;
  }

  @Nonnull
  public static synchronized Set<OriginDigestElement> getValue(@Nonnegative int id) {
    if (id == lastId) {
      return lastSet;
    }

    EnumSet<OriginDigestElement> set = EnumSet.noneOf(OriginDigestElement.class);

    for (int idx = 0; idx < values.length; idx++) {
      if ((id & (1 << idx)) != 0) {
        set.add(values[idx]);
      }
    }

    lastSet = set;
    lastId = id;

    return set;
  }
}
