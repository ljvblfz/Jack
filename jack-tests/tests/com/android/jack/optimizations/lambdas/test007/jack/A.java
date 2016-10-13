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

package com.android.jack.optimizations.lambdas.test007.jack;

interface Ia {
}

interface Iaa extends Ia {
}

interface Ib {
}

interface Ic {
  String m();
}

public class A {
  public static String test() {
    return
        ((Ia & Ic & Ib) () -> "Ia&Ic&Ib ").m() +
        ((Ib & Ic & Ia) () -> "Ib&Ic&Ia ").m() +
        ((Iaa & Ic & Ia) () -> "Iaa&Ic&Ia ").m() +
        ((Ib & Ic) () -> "Ib&Ic").m();
  }
}
