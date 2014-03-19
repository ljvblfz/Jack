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

package com.android.sched.util.codec;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This parser is used to create the object from a string.
 *
 * @param <T> Type created by the parser.
 */
public interface Parser<T> {

  /**
   * Parse a string to instantiate an object. The string must be well formed. The string can be
   * checked with {@link #checkString(CodecContext, String)}.
   *
   * @return The object instance build according to configuration.
   */
  @Nonnull
  public T parseString(@Nonnull CodecContext context, @Nonnull String string);

  /**
   * Checks the value for this configuration property.
   * @return The object instance build according to configuration or null if not possible.
   * @throws ParsingException if this value is not suitable for this property.
   */
  @CheckForNull
  public T checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException;

  /**
   * @return a textual expression of the values that are suitable for this configurationn property.
   */
  @Nonnull
  public String getUsage();
}
