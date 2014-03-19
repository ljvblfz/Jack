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

package com.android.jack.constant.test002.jack;

public class ConstantReuse {

  public void intConstantReuse() {
    int a = 0;
    int b = 0;
    int c = 1;
    int d = 1;
  }

  public void byteConstantReuse() {
    byte a = (byte) 0;
    byte b = (byte) 0;
    byte c = (byte) 1;
    byte d = (byte) 1;
  }

  public void shortConstantReuse() {
    short a = (short) 0;
    short b = (short) 0;
    short c = (short) 1;
    short d = (short) 1;
  }

  public void charConstantReuse() {
    char a = (char) 0;
    char b = (char) 0;
    char c = (char) 1;
    char d = (char) 1;
  }

  public void floatConstantReuse() {
    float a = (float) 0.0;
    float b = (float) 0.0;
    float c = (float) 1.2;
    float d = (float) 1.2;
  }

  public void longConstantReuse() {
    long a = 0L;
    long b = 0L;
    long c = 1L;
    long d = 1L;
  }

  public void doubleConstantReuse() {
    double a = 0.0;
    double b = 0.0;
    double c = 1.2;
    double d = 1.2;
  }

  public void booleanConstantReuse() {
    boolean a = true;
    boolean b = true;
    boolean c = false;
    boolean d = false;
  }

  public void nullConstantReuse() {
    Object a = null;
    Object b = null;
  }
}
