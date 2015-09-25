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

package com.android.jack.server;

/**
 * Storage for some server info.
 */
public class ServerInfo implements Cloneable {

  int currentLocal = 0;
  long totalLocal = 0;
  int maxLocal = 0;

  int currentForward = 0;
  long totalForward = 0;
  int maxForward = 0;

  public ServerInfo() {
  }

  public int getCurrentLocal() {
    return currentLocal;
  }

  public long getTotalLocal() {
    return totalLocal;
  }

  public int getMaxLocal() {
    return maxLocal;
  }

  public int getCurrentForward() {
    return currentForward;
  }

  public long getTotalForward() {
    return totalForward;
  }

  public int getMaxForward() {
    return maxForward;
  }

  @Override
  public ServerInfo clone() {
    try {
      return (ServerInfo) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
