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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A class that provides names using a dictionary
 */
public class DictionaryNameProvider implements NameProvider {

  @Nonnull
  private final NameProvider defaultNameProvider;

  @CheckForNull
  private BufferedReader br;

  public DictionaryNameProvider(@Nonnull File dictionary, @Nonnull NameProvider defaultNameProvider)
      throws JackIOException {
    this.defaultNameProvider = defaultNameProvider;
    try {
      br = new BufferedReader(new FileReader(dictionary));
    } catch (FileNotFoundException e) {
      throw new JackIOException("Dictionary " + dictionary.getPath() + " not found", e);
    }
  }

  @Override
  @Nonnull
  public String getNewName(@Nonnull String oldName) {
    if (br != null) {
      String nameFromDict = getNameFromDictionary();
      if (!nameFromDict.isEmpty()) {
        return nameFromDict;
      }
    }

    return defaultNameProvider.getNewName(oldName);
  }

  @Nonnull
  private String getNameFromDictionary() {
    assert br != null;

    StringBuffer name = new StringBuffer();
    int readCharAsInt;

    try {
      while ((readCharAsInt = br.read()) != -1) {
        char readChar = (char) readCharAsInt;

        if ((name.length() != 0 && Character.isJavaIdentifierPart(readChar))
            || (name.length() == 0 && Character.isJavaIdentifierStart(readChar))) {
          name.append(readChar);
        } else {
          if (readChar == '#') {
            br.readLine();
          }

          if (name.length() != 0) {
            return name.toString();
          }
        }
      }
    } catch (IOException e) {
      closeDictionary();
      throw new JackIOException("Failed to read obfuscation dictionary", e);
    }

    closeDictionary();

    return name.toString();
  }

  private void closeDictionary() {
    assert br != null;

    try {
      br.close();
    } catch (IOException e) {
      // nothing to handle for inputs
    } finally {
      br = null;
    }
  }
}
