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

import com.android.jack.Jack;

import java.util.Properties;
import java.util.logging.Level;

import javax.annotation.Nonnull;


/**
 * Interface representing an input jack library.
 */
public abstract class InputJackLibrary  extends CommonJackLibrary implements InputLibrary {

   public InputJackLibrary(@Nonnull Properties libraryProperties) {
    super(libraryProperties);
  }

  protected void check() throws LibraryVersionException, LibraryFormatException {
    getProperty(JackLibrary.KEY_LIB_EMITTER);
    getProperty(JackLibrary.KEY_LIB_EMITTER_VERSION);
    getProperty(JackLibrary.KEY_LIB_MAJOR_VERSION);
    getProperty(JackLibrary.KEY_LIB_MINOR_VERSION);

    int majorVersion = getMajorVersion();
    int minorVersion = getMinorVersion();
    int supportedMinorMin = getSupportedMinorMin();
    int supportedMinor = getSupportedMinor();

    if (minorVersion < supportedMinorMin) {
      throw new LibraryVersionException("The version of the library file is not supported anymore."
          + "Library version: " + majorVersion + "." + minorVersion + " - Current version: "
          + majorVersion + "." + supportedMinor + " - Minimum compatible version: " + majorVersion
          + "." + supportedMinorMin);
    } else if (minorVersion > supportedMinor) {
      throw new LibraryVersionException("The version of the library file is too recent."
          + "Library version: " + majorVersion + "." + minorVersion + " - Current version: "
          + majorVersion + "." + supportedMinor);
    } else if (minorVersion < supportedMinor) {
      Jack.getSession().getUserLogger().log(Level.WARNING,
          "The version of the library is older than the current version but is "
          + "supported. File version: {0}.{1} - Current version: {2}.{3}", new Object[] {
          Integer.valueOf(majorVersion), Integer.valueOf(minorVersion),
          Integer.valueOf(majorVersion), Integer.valueOf(supportedMinor)});
    }

    for (FileType ft : getFileTypes()) {
      ft.check();
    }
  }

  public abstract int getSupportedMinor();

  public abstract int getSupportedMinorMin();
}
