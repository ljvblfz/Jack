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

package com.android.jack.synchronize.test001.jack;

/**
 * Synchronize test.
 */
public class Synchronize {

  public static Object getObject() {
    return new Object();
  }

  // Check that jlock/junlock use the same object, otherwise java.lang.IllegalMonitorStateException
  // will be trigger.
  public static int syncBlock() {
    synchronized(getObject()) {
      return 1;
    }
  }

  public static int waitFor() {
    Object exitValueMutex = new Object();
    Integer exitValue = null;
    synchronized (exitValueMutex) {
      while (exitValue != null) {
      }
      return 1;
    }
  }

  public static synchronized int exceptionRange(Integer i) throws IllegalArgumentException {
    if (i.intValue() > 0) {
      synchronized (i) {
        return i.intValue();
      }
    }
    throw new IllegalArgumentException();
  }

  public static int result = 0;

  public static synchronized void exceptionRange2() {
    try {
      result += 1;
    } finally {
      synchronized (Synchronize.class) {
        try {
          result += 1;
        } finally {
          result += 1;
        }
      }
    }
  }

  public static void synchronizeIntoACatch() {
    try {
      result += 1;
    } catch (Exception e) {
      synchronized (Synchronize.class) {
        result += 1;
      }
    }
  }
}
