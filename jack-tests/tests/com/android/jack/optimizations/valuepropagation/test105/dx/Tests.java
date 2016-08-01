package com.android.jack.optimizations.valuepropagation.test105;

import org.junit.Test;

/** Just touch all the classes */
public class Tests {
  void touch(Class clazz) {
  }

  @Test
  public void test00() {
    touch(A.class);
    touch(B.class);
    touch(C.class);
    touch(D.class);
  }
}
