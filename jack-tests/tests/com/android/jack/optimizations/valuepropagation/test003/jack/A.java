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

package com.android.jack.optimizations.valuepropagation.test003;

class A {
  byte byteField = 1;
  char charField = '*';
  short shortField = 3;
  long longField = 4;
  boolean boolField = true;
  double doubleField = 1.0;
  float floatField = (float)2.0;
  String strFieldA = "AAA";
  String strFieldB;
  Object objFieldA = null;
  Object objFieldB = "OOO";

  A(boolean b) {
    if (b) {
      byteField = 10 - 9;
      charField = '*';
      shortField = 3;
      strFieldB = "aaaaa";
      objFieldB = "OOO";
      doubleField = 1.0f;

    } else {
      strFieldB = "aaaaa";
      longField = 4;
      boolField = true;
      objFieldA = null;
      floatField = 2.0f;
    }
  }

  void check() {
    long accum = byteField + charField +
      shortField + longField + (long)doubleField + (long)floatField;
    accum += boolField ? 1001 : 9999;
    if (objFieldB.equals(objFieldA)) {
      String s = strFieldA + strFieldB;
    }
  }
}

class B {
  String str1 = "111";
  String str2 = "222";
  static String str3 = "333";
  static String str4 = "444";

  String check() {
    return str1 + str2 + str3 + str4;
  }
}

class C {
  String check() {
    return B.str3 + B.str4;
  }
}
