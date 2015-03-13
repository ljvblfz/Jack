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

package com.android.jack.library;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.StringCodec;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} is used to create an instance of an {@link InvalidLibrary}.
 */
public class InvalidLibraryCodec implements StringCodec<InvalidLibrary> {

  @Override
  @Nonnull
  public InvalidLibrary parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return checkString(context, string);
  }

  @Override
  @CheckForNull
  public InvalidLibrary checkString(@Nonnull CodecContext context, @Nonnull String string) {
    return new InvalidLibrary(new File(string));
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to an unsupported library";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "lib";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InvalidLibrary data) {
    return data.getPath();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InvalidLibrary data) {
  }
}
