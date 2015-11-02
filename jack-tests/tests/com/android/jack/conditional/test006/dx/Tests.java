/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.conditional.test006.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.conditional.test006.jack.ConditionalTest006;

public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(null, ConditionalTest006.test001());
  }

  @Test
  public void test002() {
    try {
      ConditionalTest006.test002();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test003() {
    try {
      ConditionalTest006.test003();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test004() {
    try {
      ConditionalTest006.test004();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test005() {
    try {
      ConditionalTest006.test005();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test006() {
    try {
      ConditionalTest006.test006();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test007() {
    try {
      ConditionalTest006.test007();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test008() {
    try {
      ConditionalTest006.test008();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test009() {
    try {
      ConditionalTest006.test009();
      Assert.fail("Npe expected");
    } catch (NullPointerException npe) {
      // OK
    }
  }
}
