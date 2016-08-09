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

package com.android.jack.optimizations.modifiers.test001.jack;

public abstract class A {
  private static void touch(Class clazz) {}
  public static void touch() {
    touch(A2final.class);
    touch(IFooA.class);
    touch(IFooB.class);
    touch(IFooAB.class);
    touch(IFooC.class);
    touch(B.class);
    touch(C2final.class);
    touch(D.class);
    touch(E.class);
    touch(F2final.class);
  }
}

class A2final {
}

interface IFooA {
}

interface IFooB {
}

interface IFooAB extends IFooA, IFooB {
}

interface IFooC {
}

class B {
}

class C2final extends B implements IFooA {
}

abstract class D extends B {
}

abstract class E extends B {
}

class F2final extends E {
}
