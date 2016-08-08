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

package com.android.jack.optimizations.modifiers.test003.jack;

public abstract class A {
  private static void touch(Class clazz) { }
  public static void touch() {
    touch(FldBase.class);
    touch(FldDerived.class);
    touch(Outer.class);
  }
}

class FldBase {
  final int f0 = 0;
  int bf1_ok;
  int bf2_partially_assigned;
  int bf3_reassigned;
  int bf4_written_from_outside;
  int bf5_assigned_in_delegating_constr;
  int bf6_assigned_from_derived;
  int bf7_ok_but_initialized = sbf1_ok + 777;
  volatile int bf8_ok_volatile = 123;

  static int sbf1_ok = 123;
  static int sbf2_assigned_in_instance_constr;
  static int sbf3_assigned_in_static_constr;
  static int sbf4_reassigned_in_static_constr;
  static int sbf5_not_initialized;

  static {
    sbf3_assigned_in_static_constr = 777;
    sbf4_reassigned_in_static_constr = 777;
  }

  static {
    sbf4_reassigned_in_static_constr = 888;
  }

  FldBase(int foo) {
    this(foo > 0);
    bf5_assigned_in_delegating_constr = 100;
    sbf2_assigned_in_instance_constr = 100;
  }

  FldBase(boolean foo) {
    bf7_ok_but_initialized = -1;
    if (foo) {
      bf1_ok = 1;
      bf2_partially_assigned = 1;
      bf3_reassigned = 1;
    } else {
      bf1_ok = 2;
      bf3_reassigned = 2;
    }
    if (foo) {
      bf3_reassigned = sbf3_assigned_in_static_constr;
    }
    bf4_written_from_outside = 1;
    bf5_assigned_in_delegating_constr = 1;
    bf6_assigned_from_derived = 1;
  }

  void m() {
    bf4_written_from_outside = 4;
  }
}

final class FldDerived extends FldBase {
  int df0_not_assigned;

  FldDerived(boolean foo) {
    super(foo);
    bf6_assigned_from_derived = bf1_ok;
  }
}

final class Outer {
  private final static class Inner {
    static Object a = new Object();
  }

  public static void setA(Object newA) {
    if (newA == null) {
      throw new IllegalArgumentException("newA");
    }
    Inner.a = newA;
  }
}
