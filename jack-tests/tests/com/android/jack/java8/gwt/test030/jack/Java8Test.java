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

package com.android.jack.java8.gwt.test030.jack;

import org.junit.Test;

import junit.framework.Assert;

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

public class Java8Test implements DefaultInterface {

  static class VirtualUpRef {
    public int method2() {
      return 99;
    }

    public int redeclaredAsAbstract() {
      return 44;
    }
  }

  interface DefaultInterface2 {
    void method3();

    default int method4() {
      return 23;
    }

    default int redeclaredAsAbstract() {
      return 77;
    }
  }

  public void method1() {}

  static class DualImplementor extends Java8Test implements DefaultInterface2 {
    public void method3() {}

    public int redeclaredAsAbstract() {
      return DefaultInterface2.super.redeclaredAsAbstract();
    }
  }

  static class DualImplementorBoth extends VirtualUpRef
      implements DefaultInterface, DefaultInterface2 {
    public void method1() {}

    public void method3() {}
  }

  interface DefaultInterfaceSubType extends DefaultInterface {
    default int method2() {
      return 43;
    }

    default String print() {
      return "DefaultInterfaceSubType " + DefaultInterface.super.print();
    }
  }

  static class DefaultInterfaceImplVirtualUpRefTwoInterfaces extends VirtualUpRef
      implements DefaultInterfaceSubType {
    public void method1() {}

    public String print() {
      return "DefaultInterfaceImplVirtualUpRefTwoInterfaces";
    }
  }

  @Test
  public void testDefaultInterfaceMethodMultiple() {
    Assert.assertEquals(42, new DualImplementor().method2());
    Assert.assertEquals(23, new DualImplementor().method4());
    Assert.assertEquals(77, new DualImplementor().redeclaredAsAbstract());
    Assert.assertEquals(44, new DualImplementorBoth().redeclaredAsAbstract());
    DefaultInterfaceImplVirtualUpRefTwoInterfaces instanceImplementInterfaceSubType =
        new DefaultInterfaceImplVirtualUpRefTwoInterfaces();
    DefaultInterfaceSubType interfaceSubType1 = instanceImplementInterfaceSubType;
    Assert.assertEquals("DefaultInterfaceImplVirtualUpRefTwoInterfaces",
        instanceImplementInterfaceSubType.print());
    Assert.assertEquals("DefaultInterfaceImplVirtualUpRefTwoInterfaces", interfaceSubType1.print());
    DefaultInterfaceSubType interfaceSubType2 = new DefaultInterfaceSubType() {
      @Override
      public void method1() {}
    };
    Assert.assertEquals("DefaultInterfaceSubType DefaultInterface", interfaceSubType2.print());
    DefaultInterfaceSubType interfaceSubType3 = () -> {
    };
    Assert.assertEquals("DefaultInterfaceSubType DefaultInterface", interfaceSubType3.print());
  }
}
