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

package com.android.jack.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

public class TestsProperties {

  @Nonnull
  private static final File JACK_ROOT_DIR;

  @Nonnull
  private static final String JACK_TESTS_FOLDER = "jack-tests";

  @Nonnull
  private static final Properties testsProperties;

  static {
    testsProperties = new Properties();
    String filePath = System.getProperty("tests.config");

    if (filePath == null) {
      throw new TestConfigurationException(
          "Configuration file not specified. It must be passed with -Dtests.config on command"
          + "line.");
    }
    File propertyFile;
    propertyFile = new File(filePath);
    if (!propertyFile.isAbsolute()) {
      propertyFile =
          new File(System.getenv("user.dir") , filePath);
    }

    if (!propertyFile.exists()) {
      throw new TestConfigurationException("Configuration file not found: '" + filePath + "'");
    }

    FileInputStream is = null;
    try {
      is = new FileInputStream(propertyFile);
      testsProperties.load(is);
    } catch (FileNotFoundException e) {
      throw new TestConfigurationException(e);
    } catch (IOException e) {
      throw new TestConfigurationException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // No need to manage failure when closing input stream
        }
      }
    }

    String jackHome = testsProperties.getProperty("jack.home");

    if (jackHome == null) {
      throw new TestConfigurationException("'jack.home' property is not set");
    }
    JACK_ROOT_DIR = new File(jackHome);
  }

  @Nonnull
  public static final File getJackRootDir() {
    return JACK_ROOT_DIR;
  }

  @Nonnull
  public static final File getAndroidRootDir() {
    return new File(getProperty("android.home"));
  }

  @Nonnull
  public static String getProperty(@Nonnull String key) {
    String value = testsProperties.getProperty(key);
    if (value == null) {
      throw new TestConfigurationException("Undefined property : '" + key + "'");
    }
    return value;
  }

  @Nonnull
  public static Properties getGlobalProperties() {
    return testsProperties;
  }


}
