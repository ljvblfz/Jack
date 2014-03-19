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

package com.android.jack.shrob.test006.jack;

public abstract class A {
  void m() {}
  public void m1() {}
  protected void m2() {}
  static public void m3() {}
  public abstract void m4();
  public abstract void m5(String... args);
  static void m6() {}
  @SuppressWarnings("unused")
  private static void m7() {}
  int m8() { return 0;}
}
