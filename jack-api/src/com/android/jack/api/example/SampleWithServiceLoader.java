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
import com.android.jack.api.JackConfigProvider;
import com.android.jack.api.v01.AbortException;
import com.android.jack.api.v01.Api01CompilationTask;
import com.android.jack.api.v01.Api01Config;
import com.android.jack.api.v01.ConfigurationException;
import com.android.jack.api.v01.UnrecoverableException;

import java.io.File;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Sample of Jack api usage based on a service provider.
 * This sample requires jack.jar on classpath.
 */
public class SampleWithServiceLoader {

  public static void main(String[] args) throws SecurityException, IllegalArgumentException {
    if (args.length != 3) {
      System.out.println(
          "Usage: <jack core library> <source files directory> <output dex files directory>");
      return;
    }

    ServiceLoader<JackConfigProvider> serviceLoader = ServiceLoader.load(JackConfigProvider.class);
    JackConfigProvider confProvider;
    try {
      confProvider = serviceLoader.iterator().next();
    } catch (NoSuchElementException e) {
      System.out.println("Check that jack.jar is on classpath");
      return;
    }

    Api01CompilationTask compilationTask;
    Api01Config config;

    // Get configuration object
    try {
      config = confProvider.getConfig(Api01Config.class);
    } catch (ConfigNotSupportedException e1) {
      System.err.println("Brest config not supported)");
      return;
    }

    // Configure the compiler
    try {
      config.setClasspath(Arrays.asList(new File(args[0])));

      config.setSourceEntries(Arrays.asList(new File(args[1])));

      config.setOutputDexDir(new File(args[2]));

      // Check and build compiler
      compilationTask = config.getTask();
    } catch (ConfigurationException e) {
      System.err.println(e.getMessage());
      return;
    }

    // Run the compilation
    try {
      compilationTask.run();
    } catch (AbortException e) {
      System.out.println("User error, see reporter");
      return;
    } catch (UnrecoverableException e) {
      System.out.println("Something out of Jack control has happen: " + e.getMessage());
      return;
    } catch (ConfigurationException e) {
      System.err.println(e.getMessage());
      return;
    }
  }
}
