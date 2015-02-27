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

package com.android.jack.api.example;

import com.android.jack.api.ConfigNotSupportedException;
import com.android.jack.api.JackConfig;
import com.android.jack.api.JackConfigProvider;
import com.android.jack.api.brest.AbortException;
import com.android.jack.api.brest.BrestCompiler;
import com.android.jack.api.brest.BrestConfig;
import com.android.jack.api.brest.ConfigurationException;
import com.android.jack.api.brest.UnrecoverableException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * STOPSHIP
 */
public class Main {
  public static void main(String[] args) throws MalformedURLException, ClassNotFoundException,
      SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    ClassLoader loader =
        URLClassLoader.newInstance(new URL[] {new File(
            "/Users/jplesot/Android/ub-jack/toolchain/jack/jack/dist/jack.jar").toURI().toURL()},
            Main.class.getClassLoader());

    Class<? extends JackConfigProvider> confProviderClass =
        Class.forName(JackConfigProvider.CLASS_NAME, true, loader).asSubclass(
            JackConfigProvider.class);

    JackConfigProvider confProvider = confProviderClass.getConstructor().newInstance();

    System.out.println("Jack version: " + confProvider.getCompilerVersion() + " '"
        + confProvider.getCompilerCodeName() + "' (" + confProvider.getCompilerBuildId() + " "
        + confProvider.getCompilerCodeBase() + ")");

    System.out.println("Supported configs: ");
    for (Class<? extends JackConfig> cls : confProvider.getSupportedConfigs()) {
      System.out.print(cls.getSimpleName() + " ");
    }
    System.out.println();

    BrestCompiler brestCompiler;
    BrestConfig brestConfig;

    // Get configuration object
    try {
      brestConfig = confProvider.getConfig(BrestConfig.class);
    } catch (ConfigNotSupportedException e1) {
      System.err.println("Brest config not supported)");
      return;
    }

    // Configure the compiler
    try {
      brestConfig.setProperty(BrestConfig.PROPERTY_REPORTER, "sdk");
      brestCompiler = brestConfig.build();
    } catch (ConfigurationException e) {
      System.err.println(e.getMessage());
      return;
    }

    // Run the compilation
    try {
      // First
      brestCompiler.run();
      // Same compilation
      brestCompiler.run();
    } catch (AbortException e) {
      System.out.println("User error, see reporter");
      return;
    } catch (UnrecoverableException e) {
      System.out.println("Something out of Jack control has happen: " + e.getMessage());
      return;
    }
  }
}
