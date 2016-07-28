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

package com.android.jack.ifstatement.shortCondition.jack;

public class If {

  public int shortCircuit1(boolean x) {
    // This requires SCCP.
    boolean y = true;
    y = !y;
    y = !y;
    if (x || y) {
      return 1;
    } else {
      return 2;
    }
  }

  public int shortCircuit2(boolean x1, boolean x2, boolean x3, boolean x4, boolean x5) {
    // This requires SCCP.
    boolean y = true;
    y = !y;
    y = !y;
    if (x1 || x2 || x3 || x4 || x5 || y) {
      return 1;
    } else {
      return 2;
    }
  }

  private class State {
    private int count;
    private int max;
    private State(int max) {
      this.max = max;
      }

    private boolean n() {
      count++;
      return count >= max;
    }
  }

  public int sideEffect(int x) {
    State s = new State(x);
    // This requires SCCP.
    boolean y = true;
    y = !y;
    y = !y;
    if (s.n() || s.n() || s.n() || s.n() || s.n() || y) {
      return s.count;
    } else {
      return -1;
    }
  }
}
