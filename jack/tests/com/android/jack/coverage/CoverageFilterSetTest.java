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

/**
 * JUnit test for CoverageFilterSet class.
 */
public class CoverageFilterSetTest {
  @Test
  public void testConstructor() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    Assert.assertTrue(filterSet.isEmpty());
    Assert.assertNotNull(filterSet.getPatterns());
    Assert.assertTrue(filterSet.getPatterns().isEmpty());
  }

  @Test
  public void testAdd() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    Assert.assertTrue(filterSet.isEmpty());

    filterSet.addPattern(new CoveragePattern("a"));
    Assert.assertFalse(filterSet.isEmpty());
    Assert.assertEquals(1, filterSet.getPatterns().size());

    filterSet.addPattern(new CoveragePattern("b"));
    Assert.assertFalse(filterSet.isEmpty());
    Assert.assertEquals(2, filterSet.getPatterns().size());

    // Check we ignore duplicates.
    filterSet.addPattern(new CoveragePattern("a"));
    filterSet.addPattern(new CoveragePattern("b"));
    Assert.assertFalse(filterSet.isEmpty());
    Assert.assertEquals(2, filterSet.getPatterns().size());

    Assert.assertEquals("a", filterSet.getPatterns().get(0).getString());
    Assert.assertEquals("b", filterSet.getPatterns().get(1).getString());
  }

  @Test
  public void testFilterSet_empty() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    Assert.assertTrue(filterSet.isEmpty());
    Assert.assertFalse(filterSet.matchesAny(""));
    Assert.assertFalse(filterSet.matchesAny("a"));
    Assert.assertFalse(filterSet.matchesAny("*"));
    Assert.assertFalse(filterSet.matchesAny("."));
    Assert.assertFalse(filterSet.matchesAny("?"));

    filterSet.addPattern(new CoveragePattern("foo"));
    Assert.assertFalse(filterSet.isEmpty());
  }

  @Test
  public void testFilterSet_onePattern() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("foo"));
    Assert.assertTrue(filterSet.matchesAny("foo"));
    Assert.assertFalse(filterSet.matchesAny("bar"));
  }

  @Test
  public void testFilterSet_twoPatterns() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("foo"));
    filterSet.addPattern(new CoveragePattern("bar"));
    Assert.assertTrue(filterSet.matchesAny("foo"));
    Assert.assertTrue(filterSet.matchesAny("bar"));
  }

  @Test
  public void testFilterSet_dot() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("."));

    // Test the '.' is not used as a special character.
    Assert.assertTrue(filterSet.matchesAny("."));
    Assert.assertFalse(filterSet.matchesAny("a"));
    Assert.assertFalse(filterSet.matchesAny(""));
    Assert.assertFalse(filterSet.matchesAny("aa"));
  }

  @Test
  public void testFilterSet_dollar() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("$"));

    // Test the '.' is not used as a special character.
    Assert.assertFalse(filterSet.matchesAny(""));
    Assert.assertTrue(filterSet.matchesAny("$"));
  }

  @Test
  public void testFilterSet_wildcardOneCharacter() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("?"));

    // Test we match one character only.
    Assert.assertTrue(filterSet.matchesAny("a"));
    Assert.assertTrue(filterSet.matchesAny("."));
    Assert.assertTrue(filterSet.matchesAny("*"));
    Assert.assertTrue(filterSet.matchesAny(""));
    Assert.assertFalse(filterSet.matchesAny("aa"));
  }

  @Test
  public void testFilterSet_wilcardMultipleCharacters() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("*"));

    Assert.assertTrue(filterSet.matchesAny("a"));
    Assert.assertTrue(filterSet.matchesAny("."));
    Assert.assertTrue(filterSet.matchesAny("*"));
    Assert.assertTrue(filterSet.matchesAny(""));
    Assert.assertTrue(filterSet.matchesAny("aa"));
  }

  @Test
  public void testFilterSet_dot2() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern(".*"));

    Assert.assertTrue(filterSet.matchesAny("."));
    Assert.assertTrue(filterSet.matchesAny(".a"));
    Assert.assertTrue(filterSet.matchesAny(".aa"));

    // Test the '.' is not used as a special character, even with '*'.
    Assert.assertFalse(filterSet.matchesAny("a"));
    Assert.assertFalse(filterSet.matchesAny("aa"));
  }

  @Test
  public void testFilterSet_wildcards() {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("foo.bar.?"));

    // Test we match one character only.
    Assert.assertTrue(filterSet.matchesAny("foo.bar.a"));
    Assert.assertTrue(filterSet.matchesAny("foo.bar.b"));
    Assert.assertFalse(filterSet.matchesAny("foo.bar.ab"));
    Assert.assertFalse(filterSet.matchesAny("aa"));

    filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("java.*"));

    Assert.assertTrue(filterSet.matchesAny("java.lang.Object"));
    Assert.assertFalse(filterSet.matchesAny("javax.foo"));

    filterSet = new CoverageFilterSet();
    filterSet.addPattern(new CoveragePattern("*Test*"));

    Assert.assertTrue(filterSet.matchesAny("Test"));
    Assert.assertTrue(filterSet.matchesAny("OneTest"));
    Assert.assertTrue(filterSet.matchesAny("TestCoverage"));
    Assert.assertTrue(filterSet.matchesAny("OneTestCoverage"));
    Assert.assertTrue(filterSet.matchesAny("a.Test.b"));
    Assert.assertTrue(filterSet.matchesAny("Test$1"));
  }
}
