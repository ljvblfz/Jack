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

import com.android.jack.api.JackConfigProvider;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Sample of Jack api usage based on a service provider.
 * This sample requires jack.jar on classpath.
 */
public class SampleWithServiceLoader {

  public static void main(String[] args) throws SecurityException, IllegalArgumentException {
    ServiceLoader<JackConfigProvider> serviceLoader = ServiceLoader.load(JackConfigProvider.class);
    try {
      JackConfigProvider confProvider = serviceLoader.iterator().next();
      System.out.println("Jack version: " + confProvider.getCompilerVersion());
    } catch (NoSuchElementException e) {
      System.out.println("Check that jack.jar is on classpath");
    }
  }
}
