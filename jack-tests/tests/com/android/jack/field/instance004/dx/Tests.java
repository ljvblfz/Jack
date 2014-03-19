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

package com.android.jack.field.instance004.dx;

import com.android.jack.field.instance004.jack.Data;
import com.android.jack.field.instance004.jack.Data10;
import com.android.jack.field.instance004.jack.Data11;
import com.android.jack.field.instance004.jack.Data2;
import com.android.jack.field.instance004.jack.Data3;
import com.android.jack.field.instance004.jack.Data4;
import com.android.jack.field.instance004.jack.Data5;
import com.android.jack.field.instance004.jack.Data6;
import com.android.jack.field.instance004.jack.Data7;
import com.android.jack.field.instance004.jack.Data8;
import com.android.jack.field.instance004.jack.Data9;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    Data d = new Data();
    Assert.assertEquals(1, d.j);
  }

  @Test
  public void test2() {
    Data2 d = new Data2();
    Assert.assertEquals(1, d.j);
  }

  @Test
  public void test3() {
    Data3 d = new Data3();
    Assert.assertEquals(0, d.j);
  }

  @Test
  public void test4() {
    Data4 d = new Data4();
    Assert.assertEquals(1, d.j);
  }

  @Test
  public void test5() {
    Data5 d = new Data5();
    Assert.assertEquals(1, d.j);
  }

  @Test
  public void test6() {
    Data6 d = new Data6();
    Assert.assertEquals(1, d.j);
  }

  @Test
  public void test7() {
    Data7.Data7Bis d = new Data7.Data7Bis();
    Assert.assertEquals(1, d.k);
  }

  @SuppressWarnings("static-access")
  @Test
  public void test8() {
    try {
      new Data8().m();
      Assert.fail();
    } catch(NullPointerException npe) {
      // Ok
    }
  }

  @Test
  public void test9() {
    try {
      Data9.m();
      Assert.fail();
    } catch(NullPointerException npe) {
      // Ok
    }
  }

  @Test
  public void test10() {
    try {
      new Data10().m();
      Assert.fail();
    } catch(NullPointerException e) {
      // Ok
    }
  }

  @Test
  public void test11() {
    Assert.assertEquals(1, new Data11().m());
  }
}
