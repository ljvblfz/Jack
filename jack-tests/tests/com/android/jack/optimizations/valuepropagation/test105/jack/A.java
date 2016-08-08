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

package com.android.jack.optimizations.valuepropagation.test105.jack;

interface LocalComparable {
  int compareTo2(Object o);
}

public class A {
  void s(Object s) {
  }

  public boolean equals(Object s) {
    s(s); // Tainted
    return false;
  }

  public boolean equals2(Object o) {
    s(o); // Propagated: "CCC"
    return false;
  }

  public int compareTo(Object o) {
    s(o); // Propagated: "AAA"
    return 0;
  }

  public int compareTo2(Object o) {
    s(o); // Propagated: "BBB"
    return 0;
  }

  private void test(Comparable<String> c, LocalComparable lc) {
    equals("DDD");
    equals2("CCC");
    c.compareTo("AAA");
    lc.compareTo2("BBB");
  }

  private static void touch(Class clazz) { }
  public static void touch() {
    touch(LocalComparable.class);
    touch(B.class);
    touch(C.class);
    touch(D.class);
  }
}

class B extends A {
  public boolean equals(Object s) {
    s(s); // Tainted
    return super.equals("DDD");
  }

  public boolean equals2(Object o) {
    s(o); // Propagated: "CCC"
    return super.equals2("CCC");
  }

  public int compareTo(Object o) {
    s(o); // Tainted
    return super.compareTo("AAA");
  }

  public int compareTo2(Object o) {
    s(o); // Propagated: "BBB"
    return super.compareTo2("BBB");
  }

  private void test(Comparable<String> c, LocalComparable lc) {
    equals("DDD");
    equals2("CCC");
    c.compareTo("AAA");
    lc.compareTo2("BBB");
  }
}

class C extends B implements Comparable<Object>, LocalComparable {
  private void test(Comparable<String> c, LocalComparable lc) {
    equals("DDD");
    equals2("CCC");
    c.compareTo("AAA");
    lc.compareTo2("BBB");
  }
}

class D extends C {
  public boolean equals(Object o) {
    s(o); // Tainted
    return super.equals("DDD");
  }

  public boolean equals2(Object o) {
    s(o);  // Propagated: "CCC"
    return super.equals2("CCC");
  }

  public int compareTo(Object o) {
    s(o); // Tainted
    return super.compareTo("AAA");
  }

  public int compareTo2(Object o) {
    s(o); // Propagated: "BBB"
    return super.compareTo2("BBB");
  }

  private void test(Comparable<String> c, LocalComparable lc) {
    equals("DDD");
    equals2("CCC");
    c.compareTo("AAA");
    lc.compareTo2("BBB");
  }
}
