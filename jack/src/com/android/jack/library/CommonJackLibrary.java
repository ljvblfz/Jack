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

package com.android.jack.library;

import com.android.sched.util.log.LoggerFactory;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Common part of {@link InputLibrary} and {@link OutputLibrary}
 */
public abstract class CommonJackLibrary implements JackLibrary {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  protected final Properties libraryProperties;

  public CommonJackLibrary(@Nonnull Properties libraryProperties) {
    this.libraryProperties = libraryProperties;
  }

  @Override
  @Nonnull
  public boolean containsProperty(@Nonnull String key) {
    return libraryProperties.containsKey(key);
  }

  @Nonnull
  @Override
  public String getProperty(@Nonnull String key) throws LibraryFormatException {
    if (!libraryProperties.containsKey(key)) {
      logger.log(Level.SEVERE, "Property " + key + " from the library "
          + getLocation().getDescription() + " does not exist");
      throw new LibraryFormatException(getLocation());
    }
    return (String) libraryProperties.get(key);
  }

  public void putProperty(@Nonnull String key, @Nonnull String value) {
    libraryProperties.put(key, value);
  }
}
