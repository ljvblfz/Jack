/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.coverage;

import junit.framework.Assert;

import org.junit.Test;

public class CoveragePatternTest {

  @Test
  public void testConstructor() {
    CoveragePattern p = new CoveragePattern("foo");
    Assert.assertNotNull(p.getString());
    Assert.assertEquals("foo", p.getString());
    Assert.assertNotNull(p.getPattern());
    Assert.assertFalse(p.getString().equals(p.getPattern().pattern()));
  }

  @Test
  public void testEquals() {
    CoveragePattern p = new CoveragePattern("foo");
    Assert.assertEquals(p, p);
    Assert.assertEquals(p, new CoveragePattern("foo"));
    Assert.assertFalse(p.equals(null));
    Assert.assertFalse(p.equals(new Object()));
    Assert.assertFalse(p.equals(new CoveragePattern("*")));
    Assert.assertFalse(p.equals(new CoveragePattern("???")));

    checkEquals("*", "*");
    checkEquals(".", ".");
    checkEquals("?", "?");
    checkEquals("$", "$");
  }

  @Test
  public void testHashcode() {
    CoveragePattern p = new CoveragePattern("foo");
    Assert.assertEquals(p.hashCode(), p.hashCode());
    Assert.assertEquals(p.hashCode(), new CoveragePattern("foo").hashCode());
    Assert.assertTrue(p.hashCode() != new Object().hashCode());
    Assert.assertTrue(p.hashCode() != new CoveragePattern("*").hashCode());
    Assert.assertTrue(p.hashCode() != new CoveragePattern("???").hashCode());
  }

  @Test
  public void testToString() {
    checkToString("foo");
    checkToString("*");
    checkToString(".");
    checkToString("?");
    checkToString("$");
  }

  private void checkToString(String string) {
    CoveragePattern p = new CoveragePattern(string);
    Assert.assertEquals(string, p.toString());
  }

  private void checkEquals(String string1, String string2) {
    CoveragePattern p1 = new CoveragePattern(string1);
    CoveragePattern p2 = new CoveragePattern(string2);
    Assert.assertEquals(p1, p2);
  }
}
