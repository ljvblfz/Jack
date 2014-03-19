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

import javax.annotation.Nonnull;

/**
 * This checker is used to check the validity of an object.
 *
 * @param <T> Type of the object.
 */
public interface Checker<T> {
  /**
   * Checks the value for this data.
   * @throws CheckingException if this value is not valid.
   */
  public void checkValue(@Nonnull CodecContext context, @Nonnull T data)
      throws CheckingException;

  /**
   * @return a textual expression of the values that are suitable for an object.
   */
  @Nonnull
  public String getUsage();
}
