/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.jack.java8.gwt.test029.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface DefaultInterface {
    void method1();

    default int method2() {
      return 42;
    }

    default int redeclaredAsAbstract() {
      return 88;
    }

    default Integer addInts(int x, int y) {
      return x + y;
    }

    default String print() {
      return "DefaultInterface";
    }
  }

  static class VirtualUpRef {
    public int method2() {
      return 99;
    }

    public int redeclaredAsAbstract() {
      return 44;
    }
  }

  interface DefaultInterfaceSubType extends DefaultInterface {
    default int method2() {
      return 43;
    }

    default String print() {
      return "DefaultInterfaceSubType " + DefaultInterface.super.print();
    }
  }

  static class DefaultInterfaceImplVirtualUpRef extends VirtualUpRef implements DefaultInterface {
    public void method1() {}
  }

  static class DefaultInterfaceImplVirtualUpRefTwoInterfaces extends VirtualUpRef
      implements DefaultInterfaceSubType {
    public void method1() {}

    public String print() {
      return "DefaultInterfaceImplVirtualUpRefTwoInterfaces";
    }
  }

  @Test
  public void testDefaultInterfaceMethodVirtualUpRef() {
    Assert.assertEquals(99, new DefaultInterfaceImplVirtualUpRef().method2());
    Assert.assertEquals(99, new DefaultInterfaceImplVirtualUpRefTwoInterfaces().method2());
    Assert.assertEquals("SimpleB", new com.android.jack.java8.gwt.test029.jack.package3.SimpleC().m());
    Assert.assertEquals("SimpleASimpleB", new com.android.jack.java8.gwt.test029.jack.package1.SimpleD().m());
  }

}
