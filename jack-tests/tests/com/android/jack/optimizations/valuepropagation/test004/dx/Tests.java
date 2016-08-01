package com.android.jack.optimizations.valuepropagation.test004;

import org.junit.Test;

/** Just touch all the classes */
public class Tests {
  void touch(Class clazz) {
  }

  @Test
  public void test00() {
    touch(A.class);
  }
}
