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

package com.android.jill.api.example;

import com.android.jill.api.ConfigNotSupportedException;
import com.android.jill.api.JillConfig;
import com.android.jill.api.JillProvider;
import com.android.jill.api.v01.Api01Config;
import com.android.jill.api.v01.Api01TranslationTask;
import com.android.jill.api.v01.ConfigurationException;
import com.android.jill.api.v01.TranslationException;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Sample of Jill api usage based on a service provider.
 * This sample requires jill.jar on classpath.
 */
public class WithServiceLoader {
  public static void main(String[] args) throws SecurityException, IllegalArgumentException {
    if (args.length != 2) {
      System.out.println(
          "Usage: <jill input archive> <jill output archive>");
      return;
    }

    ServiceLoader<JillProvider> serviceLoader = ServiceLoader.load(JillProvider.class);
    JillProvider provider;
    try {
      provider = serviceLoader.iterator().next();
    } catch (NoSuchElementException e) {
      System.out.println("Check that jill.jar is on classpath");
      return;
    }

    System.out.println("Translator version: " + provider.getTranslatorVersion());
    System.out.println("Translator release name: " + provider.getTranslatorReleaseName());
    System.out.println("Translator release code: " + provider.getTranslatorReleaseCode());
    System.out.println("Translator sub-release kind: " + provider.getTranslatorSubReleaseKind());
    System.out.println("Translator sub-release code: " + provider.getTranslatorSubReleaseCode());
    String str;
    str = provider.getTranslatorBuildId();
    System.out.println("Translator build id: " + ((str != null) ? str : "Unknown"));
    str = provider.getTranslatorSourceCodeBase();
    System.out.println("Translator souce code base: " + ((str != null) ? str : "Unknown"));
    System.out.print("Supported configurations: ");

    for (Class<? extends JillConfig> config : provider.getSupportedConfigs()) {
      System.out.print(config.getSimpleName());
      assert provider.isConfigSupported(config);
    }
    System.out.println();

    Api01TranslationTask translationTask;
    Api01Config config;

    // Get configuration object
    try {
      config = provider.createConfig(Api01Config.class);
    } catch (ConfigNotSupportedException e1) {
      System.err.println("Brest config not supported)");
      return;
    }

    // Configure
    try {
      config.setInputJavaBinaryFile(new File(args[0]));

      config.setOutputJackFile(new File(args[1]));

      // Check and build
      translationTask = config.getTask();

    } catch (ConfigurationException e) {
      System.err.println(e.getMessage());
      return;
    }

    // Run the translation
    try {
      translationTask.run();
    } catch (TranslationException e) {
      System.out.println("User error, see reporter");
      return;
    }
  }
}
