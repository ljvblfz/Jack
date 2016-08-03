package com.android.jack.optimizations.modifiers.test001;

import org.junit.Test;

/** Just touch all the classes */
public class Tests {
  void touch(Class clazz) {
  }

  @Test
  public void test00() {
    touch(A.class);
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
