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

package com.android.jack.shrob.test001.jack;

public class Reflect2 {
  public volatile int fieldPublic;

  private volatile int fieldPrivate;

  public volatile long fieldLong;

  private volatile long fieldLong2;

  volatile long fieldLong3;

  protected volatile long fieldLong4;

  public volatile A a;

  public volatile B b;

  private volatile Object c;

  private void calledMethod() {

  }

  public void m(A a) {

  }

  private void privateMethod(B b) {

  }

  class A {

  }

  class B {

  }
}
