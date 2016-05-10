package com.android.jack.java8.bridges.test003.jack;

import org.junit.Assert;
import org.junit.Test;

interface A<T> {
  T m(T t);
}

interface B extends A<String> {
  String m(String s);
}

/**
 * Test to check that bridges are correctly generated into the inner class implementing a lambda.
 */
public class Tests {

  @Test
  public void test001() {
    B b = (s) -> { return "Hello " + s; };
    A a = b;

    try {
        a.m(new Object());
        Assert.fail();
    } catch (ClassCastException e) {

    }
  }
}
