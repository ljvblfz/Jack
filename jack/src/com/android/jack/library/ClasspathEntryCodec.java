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

import com.android.sched.util.codec.OrCodec;
import com.android.sched.util.codec.StringCodec;

import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} is used to create an instance of an {@link InputLibrary} representing a
 * classpath entry.
 */
public class ClasspathEntryCodec extends OrCodec<InputLibrary> {

  @SuppressWarnings("unchecked")
  public ClasspathEntryCodec() {
    super(new InputJackLibraryCodec(), new JarLibraryCodec(), new InvalidLibraryCodec());
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a Jack library, or a Jar for the annotation processor";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "jack-or-jar";
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputLibrary data) {
    return data.getPath();
  }

}
