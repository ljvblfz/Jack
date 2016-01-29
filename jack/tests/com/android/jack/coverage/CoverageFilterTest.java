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

import javax.annotation.Nonnull;

public class CoverageFilterTest {
  private static CoverageFilterSet create(@Nonnull String... string) {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    for (String s : string) {
      filterSet.addPattern(new CoveragePattern(s));
    }
    return filterSet;
  }

  private static CoverageFilter createIncludeFilter(@Nonnull String... includes) {
    return new CoverageFilter(create(includes), new CoverageFilterSet());
  }

  private static CoverageFilter createExcludeFilter(@Nonnull String... excludes) {
    return new CoverageFilter(new CoverageFilterSet(),create(excludes));
  }

  @Test
  public void testDefault() {
    CoverageFilter filter = new CoverageFilter();

    Assert.assertTrue(filter.matches("foo"));
    Assert.assertTrue(filter.matches("bar"));
  }

  @Test
  public void testSingleInclude() {
    CoverageFilter filter = createIncludeFilter("foo");
    Assert.assertTrue(filter.matches("foo"));
    Assert.assertFalse(filter.matches("bar"));
  }

  @Test
  public void testMultipleIncludes() {
    CoverageFilter filter = createIncludeFilter("foo", "bar");

    Assert.assertTrue(filter.matches("foo"));
    Assert.assertTrue(filter.matches("bar"));
  }

  @Test
  public void testSingleExclude() {
    CoverageFilter filter = createExcludeFilter("foo");

    Assert.assertFalse(filter.matches("foo"));
    Assert.assertTrue(filter.matches("bar"));
  }

  @Test
  public void testMultipleExclude() {
    CoverageFilter filter = createExcludeFilter("foo", "bar");

    Assert.assertFalse(filter.matches("foo"));
    Assert.assertFalse(filter.matches("bar"));
  }

  @Test
  public void testIncludeExclude() {
    CoverageFilterSet includeSet = create("foo");
    CoverageFilterSet excludeSet = create("foo");
    CoverageFilter filter = new CoverageFilter(includeSet, excludeSet);

    // "foo" is excluded.
    Assert.assertFalse(filter.matches("foo"));

    // only "foo" is included.
    Assert.assertFalse(filter.matches("bar"));
  }

  @Test
  public void testAllIncludes() {
    CoverageFilter filter = createIncludeFilter("*");

    Assert.assertTrue(filter.matches("foo"));
    Assert.assertTrue(filter.matches("bar"));
    Assert.assertTrue(filter.matches("foo.bar"));
  }

  @Test
  public void testAllExcludes() {
    CoverageFilter filter = createExcludeFilter("*");

    Assert.assertFalse(filter.matches("foo"));
    Assert.assertFalse(filter.matches("bar"));
    Assert.assertFalse(filter.matches("foo.bar"));
  }

  @Test
  public void testPackageSubset() {
    CoverageFilterSet includeSet = create("foo.*");
    CoverageFilterSet excludeSet = create("foo.bar.*");
    CoverageFilter filter = new CoverageFilter(includeSet, excludeSet);

    Assert.assertFalse(filter.matches("foo"));
    Assert.assertTrue(filter.matches("foo.foo"));
    Assert.assertFalse(filter.matches("foo.bar.foo"));
  }

  @Test
  public void testPackageSubset2() {
    CoverageFilter filter = createExcludeFilter("foo.a?c");

    Assert.assertFalse(filter.matches("foo.abc"));
    Assert.assertFalse(filter.matches("foo.a.c"));
    Assert.assertTrue(filter.matches("foo.abbc"));
  }
}
