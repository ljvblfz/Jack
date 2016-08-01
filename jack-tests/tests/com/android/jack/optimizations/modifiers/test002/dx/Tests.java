package com.android.jack.optimizations.modifiers.test002;

import org.junit.Test;

/** Just touch all the classes */
public class Tests {
  void touch(Class clazz) {
  }

  @Test
  public void test00() {
    touch(Base.class);
    touch(D1.class);
    touch(D2.class);
    touch(Inter.class);
    touch(PreBase.class);
    touch(PrePreBase.class);
  }
}
