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

package com.android.jack.switchstatement.test028.jack;

public class RedundantSwitch {

  public int switch001(int x) {
    switch (x) {
      case 1:
        break;
      case 2:
        break;
    }
    return 10;
  }

  public int switch002(int x) {
    switch (x) {
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        break;
      case 5:
        break;
      case 6:
        break;
      case 7:
        break;
    }
    return 10;
  }

  public int switch003(int x) {
    switch (x) {
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        break;
      case 5:
        break;
      case 6:
        break;
      case 7:
        break;
      default:
        break;
    }
    return 10;
  }

  public int switch004(int x) {
    switch (x) {
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        return 4;
      case 5:
        break;
      case 6:
        break;
      case 7:
        break;
      default:
        break;
    }
    return 10;
  }
}
