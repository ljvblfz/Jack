/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.string.concat001.dx;

import com.android.jack.string.concat001.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about string concatenation.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals("test0" + "test1", Data.test001("test0", "test1"));
    Assert.assertEquals(null + "test1", Data.test001(null, "test1"));
    Assert.assertEquals("test0" + null, Data.test001("test0", null));
  }

  @Test
  public void test2() {
    Object object = new String[] {"test2"};
    Assert.assertEquals("test0" + object, Data.test002("test0", object));
    Assert.assertEquals(((String)null) + object, Data.test002(null, object));
    Assert.assertEquals("test0" + null, Data.test002("test0", null));
    Assert.assertEquals(((String)null) + null, Data.test002(null, null));
  }

  @Test
  public void test3() {
    Assert.assertEquals("test0" + "literal1", Data.test003("test0"));
    Assert.assertEquals(null + "literal1", Data.test003(null));
  }

  @Test
  public void test4() {
    Object object = new String[] {"test2"};
    Assert.assertEquals(object + "literal2", Data.test004(object));
    Assert.assertEquals("test1" + "literal2", Data.test004("test1"));
    Assert.assertEquals(null + "literal2", Data.test004(null));
  }

  @Test
  public void test5() {
    Object object = new String[] {"test2"};
    Assert.assertEquals("test0" + object + "literal3", Data.test005("test0", object));
    Assert.assertEquals("test0" + null + "literal3", Data.test005("test0", null));
    Assert.assertEquals(((String)null) + object + "literal3", Data.test005(null, object));
    Assert.assertEquals(((String)null) + null + "literal3", Data.test005(null, null));
  }

  @Test
  public void test6() {
    Object a = new String[] {"test2"};
    Object b = new String[] {"5test5"};
    Assert.assertEquals("literal4" + a + b, Data.test006(a, b));
  }

  @Test
  public void test7() {
    Object a = new String[] {"test2"};
    Object b = new String[] {"5test5"};
    Assert.assertEquals( a + "literal5" + b, Data.test007(a, b));
  }

  @Test
  public void test8() {
    String a = "A";
    String b = "B";
    Assert.assertEquals( a + b, Data.test008(a, b));
  }

  @Test
  public void test9() {
    String a = "A";
    String b = "B";
    Assert.assertEquals( a + (b + "literal6"), Data.test009(a, b));
  }
    @Test
    public void test10() {
      String a = "A";
      String b = "B";
      Assert.assertEquals((a + b) + "literal7", Data.test010(a, b));
    }

  @Test
  public void test11() {
    String a = "A";
    String b = "B";
    Assert.assertEquals( a + b + "literal8", Data.test011(a, b));
  }

  @Test
  public void test12() {
    String a = "A";
    String b = "B";
    CharSequence c = "C";
    Assert.assertEquals( a + b + c, Data.test012(a, b, c));
  }
}
