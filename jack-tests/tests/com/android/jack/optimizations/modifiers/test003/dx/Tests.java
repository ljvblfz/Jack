package com.android.jack.optimizations.modifiers.test003;

import org.junit.Test;

/** Just touch all the classes */
public class Tests {
  void touch(Class clazz) {
  }

  @Test
  public void test00() {
    touch(FldBase.class);
    touch(FldDerived.class);
    touch(Outer.class);
  }
}
