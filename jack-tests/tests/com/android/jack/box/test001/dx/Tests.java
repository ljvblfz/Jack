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

package com.android.jack.box.test001.dx;

import com.android.jack.box.test001.jack.Box001;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(Long.valueOf(15), Box001.get1(Long.valueOf(11), 4));
  }

  @Test
  public void test2() {
    Assert.assertEquals(Long.valueOf(15), Box001.get2(Long.valueOf(11), 4));
  }

  @Test
  public void test3() {
    Assert.assertTrue(Box001.get3(new Boolean(true), new Boolean(true)).booleanValue());
    Assert.assertFalse(Box001.get3(new Boolean(true), new Boolean(false)).booleanValue());
  }

  @Test
  public void test4() {
    Assert.assertTrue(Box001.get4(new Boolean(true), new Boolean(true)).booleanValue());
    Assert.assertFalse(Box001.get4(new Boolean(true), new Boolean(false)).booleanValue());
  }

  @Test
  public void test5() {
    Assert.assertTrue(Box001.get5(new Boolean(true)));
    Assert.assertFalse(Box001.get5(new Boolean(false)));
  }

  @Test
  public void test6() {
    Assert.assertEquals(2L << 2, Box001.get6(new Long(2), 2).longValue());
  }

  @Test
  public void test7() {
    Assert.assertTrue(Box001.get7(new Boolean(true), new Boolean(true)).booleanValue());
    Assert.assertFalse(Box001.get7(new Boolean(true), new Boolean(false)).booleanValue());
  }

  @Test
  public void test8() {
    Assert.assertTrue(Box001.get8());
  }

  @Test
  public void test9() {
    Assert.assertTrue(Box001.get9().booleanValue());
  }

  @Test
  public void test10() {
    Assert.assertTrue(Box001.get10());
  }

  @Test
  public void test11() {
    int[] a = new int[]{1,2,3};
    Assert.assertEquals(1, Box001.get11(a).intValue());
  }

  @Test
  public void test12() {
    Integer[] a = new Integer[] {new Integer(1), new Integer(2), new Integer(3)};
    Assert.assertEquals(1, Box001.get12(a));
  }

  @Test
  public void test13() {
    int[] a = new int[]{1,2,3};
    Assert.assertEquals(3, Box001.get13(a, new Integer(2)));
  }

  @Test
  public void test14() {
    Assert.assertEquals(1, Box001.get14(new Integer(1), new Integer(2)).length);
    Assert.assertEquals(2, Box001.get14(new Integer(1), new Integer(2))[0].length);
  }

  @Test
  public void test15() {
    Assert.assertEquals(3, Box001.get15(1,2.0f));
  }

  @Test
  public void test16() {
    Assert.assertEquals(3, Box001.get16(new Integer(1), new Float(2.0f)));
  }

  @Test
  public void test17() {
    Assert.assertEquals(-1, Box001.get17(new Integer(1)));
  }

  @Test
  public void test18() {
    Assert.assertEquals(1, Box001.get18(new Integer(2)));
  }

  @Test
  public void test19() {
    Assert.assertFalse(Box001.get19(new Boolean(true)));
  }

  @Test
  public void test20() {
    Assert.assertEquals(2, Box001.get20(new Integer(2)));
  }

  @Test
  public void test21() {
    short val = 2;
    Assert.assertEquals(2, Box001.get21(new Short(val)));
  }

  @Test
  public void test22() {
    Assert.assertEquals(~16, Box001.get22(new Integer(16)));
  }

  @Test
  public void test23() {
    Assert.assertTrue(Box001.get23(new Integer(16)));
  }

  @Test
  public void test24() {
    Assert.assertTrue(Box001.get24(new Boolean(true)));
    Assert.assertFalse(Box001.get24(new Boolean(false)));
  }

  @Test
  public void test25() {
    Assert.assertTrue(Box001.get25(new Boolean(true)));
    Assert.assertFalse(Box001.get25(new Boolean(false)));
  }

  @Test
  public void test26() {
    Assert.assertEquals(16, ((Integer)Box001.get26(16l)).intValue());
  }

  @Test
  public void test27() {
    Assert.assertEquals(16, Box001.get27(new Integer(16)));
  }

  @Test
  public void test28() {
    Assert.assertEquals("2", Box001.get28(new short[]{1,2,3}, true));
  }

  @Test
  public void test29() {
    Assert.assertEquals("1,true", Box001.get29(1,true));
  }

  @Test
  public void test30() {
    Assert.assertEquals(1, Box001.get30()[0].intValue());
    Assert.assertEquals(2, Box001.get30()[1].intValue());
  }

  @Test
  public void test31() {
    Assert.assertEquals(1, Box001.get31()[0]);
    Assert.assertEquals(2, Box001.get31()[1]);
  }

  @Test
  public void test32() {
    Assert.assertEquals(1, Box001.get32(new Integer(1)));
    Assert.assertEquals(2, Box001.get32(new Integer(2)));
  }

  @Test
  public void test33() {
    Assert.assertEquals(1, Box001.get33(new Integer(1)));
    Assert.assertEquals(0, Box001.get33(new Integer(2)));
  }

  @Test
  public void test34() {
    Assert.assertEquals(1, Box001.get34(new Boolean(true)));
    Assert.assertEquals(0, Box001.get34(new Boolean(false)));
  }

  @Test
  public void test35() {
    Assert.assertEquals(1, Box001.get35(new Boolean(true)));
    Assert.assertEquals(0, Box001.get35(new Boolean(false)));
  }

  @Test
  public void test36() {
    Assert.assertEquals(1, Box001.get36(new Boolean(true)));
    Assert.assertEquals(0, Box001.get36(new Boolean(false)));
  }

  @Test
  public void test37() {
    Assert.assertEquals(2, Box001.get37(new Boolean(true)));
    Assert.assertEquals(1, Box001.get37(new Boolean(false)));
  }

  @Test
  public void test38() {
    Assert.assertEquals(2, Box001.get38(new Boolean(true)));
    Assert.assertEquals(0, Box001.get38(new Boolean(false)));
  }

  @Test
  public void test39() {
    Assert.assertFalse(Box001.get39("ab"));
  }

  @Test
  public void test41() {
    Assert.assertEquals(16 + 16, Box001.get41(new Integer(16), new Integer(16)));
  }

  @Test
  public void test42() {
    Assert.assertTrue(Box001.get42(new Double(0.5), new Double(0.5)) == (0.5 + 0.5));
  }

  @Test
  public void test43() {
    Assert.assertEquals(1, Box001.get43());
  }

  @Test
  public void test44() {
    Assert.assertEquals(10, Box001.get44());
  }

  @Test
  public void test45() {
    Assert.assertEquals(2 & 4, Box001.get45(new Integer(2), new Integer(4)).intValue());
  }

  @Test
  public void test46() {
    Assert.assertEquals(1, Box001.get46(true, new Boolean(true)));
    Assert.assertEquals(0, Box001.get46(false, new Boolean(true)));
  }

  @Test
  public void test47() {
    Assert.assertEquals(2, Box001.get47(1));
  }

  @Test
  public void test48() {
    try {
      Box001.get48();
      Assert.fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void test49() {
    Assert.assertEquals(7, Box001.get49());
  }

  @Test
  public void test50() {
    Assert.assertEquals(14, Box001.get50());
  }
}
