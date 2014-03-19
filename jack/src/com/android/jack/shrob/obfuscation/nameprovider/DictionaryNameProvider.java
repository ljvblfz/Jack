/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.nameprovider;

import com.android.jack.JackIOException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.annotation.Nonnull;

/**
 * A class that provides names using a dictionary
 */
public class DictionaryNameProvider implements NameProvider {

  @Nonnull
  private final NameProvider defaultNameProvider;

  @Nonnull
  private final Scanner scanner;

  public DictionaryNameProvider(@Nonnull File dictionary, @Nonnull NameProvider defaultNameProvider)
      throws JackIOException {
    this.defaultNameProvider = defaultNameProvider;
    try {
      scanner = new Scanner(dictionary);
    } catch (FileNotFoundException e) {
      throw new JackIOException("Dictionary " + dictionary.getAbsolutePath() + " not found", e);
    }
    scanner.useDelimiter("?|,|;|'|\"|-|(|)|{|}");
    scanner.skip("^#.*$");
  }

  @Override
  @Nonnull
  public String getNewName(@Nonnull String oldName) {
    if (scanner.hasNext()) {
      return scanner.next();
    }
    return defaultNameProvider.getNewName(oldName);
  }

}
