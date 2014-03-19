package com.android.jack.inner.test026.jack;

import com.android.jack.inner.test026.jack.pkg.C;

public class D extends C {
  public static int Dvalue = new D().f();
  public int Evalue = getE();
  public int Fvalue = getF();
  public static int Gvalue = new G().f();
  public static int G2value = new G().getE();
  public static int G3value = new G().getF();

  int f() {
    return m();
  }

  class E {
    int f() {
      return D.super.m();
    }
  }

  class F {
    int f() {
      return m();
    }
  }

  public int getE() {
    return new E().f();
  }

  public int getF() {
    return new F().f();
  }
}

class G extends D {
  @Override
  protected int m() {
    return 2;
  }

  @Override
  int f() {
    return m();
  }
}
