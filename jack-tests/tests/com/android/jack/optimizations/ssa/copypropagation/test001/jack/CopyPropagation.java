/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.optimizations.ssa.copypropagation.test001.jack;

public class CopyPropagation {

  /**
   * Linked List like object
   */
  public static class Testing {
    public final int value;
    public Testing next = null;

    public Testing(int value) {
      this.value = value;
    }

    public Testing getNext() {
      return next;
    }
  }

  /**
   * Sum the list, ignoring the head value.
   */
  public int sum(Testing head) {
    Testing e = head.getNext();
    int sum = 0;
    while (e != head) {
      sum += e.value;
      Testing next = e.getNext();
      e = next;
    }
    return sum;
  }
}
