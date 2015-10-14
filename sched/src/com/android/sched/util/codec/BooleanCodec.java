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

package com.android.sched.util.codec;
import javax.annotation.Nonnull;

/**
 * This {@link BooleanCodec} is used to return instance of {@link Boolean}
 */
public class BooleanCodec extends KeyValueCodec<Boolean> {

  @SuppressWarnings("unchecked")
  @Nonnull
  static final Entry<Boolean>[] elements =
      new Entry[] {
        new Entry<Boolean>("true",  Boolean.TRUE),
        new Entry<Boolean>("yes",   Boolean.TRUE),
        new Entry<Boolean>("on",    Boolean.TRUE),
        new Entry<Boolean>("1",     Boolean.TRUE),
        new Entry<Boolean>("false", Boolean.FALSE),
        new Entry<Boolean>("no",    Boolean.FALSE),
        new Entry<Boolean>("off",   Boolean.FALSE),
        new Entry<Boolean>("0",     Boolean.FALSE)
    };

  public BooleanCodec() {
    super("bool", elements);
    ignoreCase();
  }
}
