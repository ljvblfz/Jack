/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.api.v02.impl;

import com.android.jack.Options;
import com.android.jack.api.v01.ConfigurationException;
import com.android.jack.api.v01.impl.Api01ConfigImpl;
import com.android.jack.api.v02.Api02Config;
import com.android.jack.api.v02.JavaSourceVersion;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * A configuration implementation for API level 02 of the Jack compiler.
 */
public class Api02ConfigImpl extends Api01ConfigImpl implements Api02Config {
  public Api02ConfigImpl() {
    super();
  }

  @Override
  public void setJavaSourceVersion(@Nonnull JavaSourceVersion javaSourceVersion)
      throws ConfigurationException {
    JavaVersion javaSourceVersionWrapped = null;

    switch (javaSourceVersion) {
      case JAVA_3: {
        javaSourceVersionWrapped = JavaVersion.JAVA_3;
        break;
      }
      case JAVA_4: {
        javaSourceVersionWrapped = JavaVersion.JAVA_4;
        break;
      }
      case JAVA_5: {
        javaSourceVersionWrapped = JavaVersion.JAVA_5;
        break;
      }
      case JAVA_6: {
        javaSourceVersionWrapped = JavaVersion.JAVA_6;
        break;
      }
      case JAVA_7: {
        javaSourceVersionWrapped = JavaVersion.JAVA_7;
        break;
      }
      case JAVA_8: {
        javaSourceVersionWrapped = JavaVersion.JAVA_8;
        break;
      }
      default: {
        throw new ConfigurationException(
            "Java source version '" + javaSourceVersion.toString() + "' is unsupported");
      }
    }

    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), javaSourceVersionWrapped.toString());
  }

  @Override
  public void setBaseDirectory(@Nonnull File baseDir) {
    options.setWorkingDirectory(baseDir);
  }
}
