/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.reflections;

import com.google.common.base.CharMatcher;

import com.android.sched.util.log.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link ReflectionManager} that uses files.
 */
public class FileReflectionManager extends CommonReflectionManager implements ReflectionManager {
  @Nonnull
  private static final String RESOURCE_DIR = "/reflection";

  @Nonnull
  public static final String SUBTYPES_FILE_SUFFIX = "-sub.txt";

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls) {
    Set<Class<? extends T>> result = null;

    result = new HashSet<Class<? extends T>>();

    StringBuilder sb = new StringBuilder(RESOURCE_DIR);
    sb.append('/');
    sb.append(cls.getName().replace('.', '/'));
    sb.append(SUBTYPES_FILE_SUFFIX);

    String resourceFilePath = sb.toString();

    InputStream is = cls.getResourceAsStream(resourceFilePath);

    if (is != null) {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String typeName = null;
      try {
        typeName = br.readLine();
        while (typeName != null) {
          if (typeName.length() > 0 && typeName.charAt(0) != '#'
              && !CharMatcher.WHITESPACE.matchesAllOf(typeName)) {
            Class<?> subType = Class.forName(typeName.trim());
            if (!cls.isAssignableFrom(subType)) {
              // Maybe files are not up to date
              LoggerFactory.getLogger().log(Level.SEVERE, "Type {0}  is not a subtype of {1}",
                  new Object[] {typeName, cls.getName()});
              throw new ReflectionException(
                  "Type " + typeName + " is not a subtype of " + cls.getName());
            }
            result.add((Class<? extends T>) subType);
          }
          typeName = br.readLine();
        }
      } catch (IOException e) {
        LoggerFactory.getLogger()
            .log(Level.SEVERE, "An error occured while reading file " + resourceFilePath, e);
        throw new ReflectionException("An error occured while reading file " + resourceFilePath);
      } catch (ClassNotFoundException e) {
        // Maybe files are not up to date
        LoggerFactory.getLogger().log(Level.SEVERE, "Type {0} couldn't be found", typeName);
        throw new ReflectionException("Type " + typeName + " couldn't be found");
      } finally {
        try {
          br.close();
        } catch (IOException e) {
          // Nothing more to be done.
        }
      }
    } else {
      LoggerFactory.getLogger().log(Level.INFO, "Failed to find resource file {0}.",
          resourceFilePath);
      throw new ReflectionException("Resource file " + resourceFilePath + " couldn't be found.");
    }

    return result;
  }
}
