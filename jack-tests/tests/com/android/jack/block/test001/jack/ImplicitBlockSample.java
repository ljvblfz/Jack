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

package com.android.jack.block.test001.jack;

public class ImplicitBlockSample {

  public int ifThen(int val) {
    if (val == 5)
      return 1;
    return 2;
  }

  public int ifElse(int val) {
    if (val == 5) {
      return 1;
    }
    else return 2;
  }

  @SuppressWarnings("unused")
  public int labelStmt001(int val) {
    label:
      return val;
  }

  @SuppressWarnings("unused")
  public int labelStmt002(int val) {
    int a = 0;
    label:
      a = 5;
    return a;
  }

  @SuppressWarnings("unused")
  public int labelStmt003(int val) {
    int a = 0;
    label:
      a = 5;
    label:
      a = 10;
    return a;
  }

  public int forBody001(int val) {
    int sum = 0;
    for (int i = 0; i < val; i++)
      sum = sum + 1;
    return val;
  }

  public int forBody002(int val) {
    int sum = 0;
    for (int i = 0; i < val; i++)
      for (int j = 0; j < val; j++)
        sum = sum + 1;
    return val;
  }

  public int whileBody(int val) {
    int sum = 0;
    while(sum < val)
      sum = sum + 1;
    return val;
  }

  public int caseStmt001(int val) {
    switch(val) {
      case 1:
        return 1;
      case 2:
        int ret = 1;
      case 3:
        ret = 0;
        return ret;
    }
    return val;
  }

  public int caseStmt002(int val) {
    switch(val) {
      case 1: {
        return 1;
      }
      case 2:
        int ret = 1;
      case 3:
        ret = 0;
        return ret;
    }
    return val;
  }
}
