/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.util;

import com.android.sched.util.findbugs.SuppressFBWarnings;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class VersionTest {
  @Nonnull
  private static final String NAME = "C";
  @Nonnull
  private static final String RELEASER = "android-jack-team@google.com";
  @Nonnull
  private static final String BUILD = "build";
  @Nonnull
  private static final String SHA = "sha";

  @Test
  public void testLoad() throws IOException {
    Version v;

    v = new Version(VersionTest.class.getClassLoader()
        .getResourceAsStream("com/android/sched/util/v2-version.properties"));
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(12, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ALPHA, v.getSubReleaseKind());
    Assert.assertEquals("android-jack-team@google.com", v.getReleaser());
    Assert.assertEquals("172300", v.getBuildId());
    Assert.assertEquals("4be26d618d50a1ff7313fded028dc099dd8e721d", v.getCodeBase());
    Assert.assertEquals("1.2-a8", v.getVersion());
    Assert.assertEquals(
        "1.2-a8 'Carnac' (172300 4be26d618d50a1ff7313fded028dc099dd8e721d by android-jack-team@google.com)",
        v.getVerboseVersion());

    v = new Version(VersionTest.class.getClassLoader()
        .getResourceAsStream("com/android/sched/util/v1-version.properties"));
    Assert.assertEquals(2, v.getReleaseCode());
    Assert.assertEquals(27, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.RELEASE, v.getSubReleaseKind());
    Assert.assertEquals("<unknown>", v.getReleaser());
    Assert.assertEquals("170000", v.getBuildId());
    Assert.assertEquals("4be26d618d50a1ff7313fabd028dc099dd8e721d", v.getCodeBase());
    Assert.assertEquals("1.1-a8", v.getVersion());
    Assert.assertEquals(
        "1.1-a8 'Brest' (170000 4be26d618d50a1ff7313fabd028dc099dd8e721d by <unknown>)",
        v.getVerboseVersion());

    v = new Version(VersionTest.class.getClassLoader()
        .getResourceAsStream("com/android/sched/util/v1-eng-version.properties"));
    Assert.assertEquals(2, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertNull(v.getReleaser());
    Assert.assertNull(v.getBuildId());
    Assert.assertNull(v.getCodeBase());
    Assert.assertEquals("1.1-eng", v.getVersion());
    Assert.assertEquals("1.1-eng 'Brest'", v.getVerboseVersion());
  }

  @Test
  public void testBuild() throws IOException {
    Version v;

    v = new Version(NAME, "3.1-a", 3, 1, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(1, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ALPHA, v.getSubReleaseKind());
    Assert.assertEquals(RELEASER, v.getReleaser());
    Assert.assertEquals(BUILD, v.getBuildId());
    Assert.assertEquals(SHA, v.getCodeBase());
    Assert.assertEquals("3.1-a", v.getVersion());
    Assert.assertEquals("3.1-a 'C' (build sha by android-jack-team@google.com)",
        v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1", 3, 1, SubReleaseKind.ALPHA, null, BUILD, SHA);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertNull(v.getReleaser());
    Assert.assertNull(v.getBuildId());
    Assert.assertNull(v.getCodeBase());
    Assert.assertEquals("3.1-eng", v.getVersion());
    Assert.assertEquals("3.1-eng 'C'", v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1-a", 3, 1, SubReleaseKind.ALPHA, RELEASER, null, SHA);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertEquals(RELEASER, v.getReleaser());
    Assert.assertNull(v.getBuildId());
    Assert.assertEquals(SHA, v.getCodeBase());
    Assert.assertEquals("3.1-a-eng", v.getVersion());
    Assert.assertEquals("3.1-a-eng 'C' (sha by android-jack-team@google.com)",
        v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1-a", 3, 1, SubReleaseKind.ALPHA, RELEASER, BUILD, null);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertEquals(RELEASER, v.getReleaser());
    Assert.assertEquals(BUILD, v.getBuildId());
    Assert.assertNull(v.getCodeBase());
    Assert.assertEquals("3.1-a-eng", v.getVersion());
    Assert.assertEquals("3.1-a-eng 'C' (build by android-jack-team@google.com)",
        v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1-a", 3, 1, SubReleaseKind.ALPHA, RELEASER, null, null);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertNull(v.getReleaser());
    Assert.assertNull(v.getBuildId());
    Assert.assertNull(v.getCodeBase());
    Assert.assertEquals("3.1-a-eng", v.getVersion());
    Assert.assertEquals("3.1-a-eng 'C'", v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1-a", 3, 1, SubReleaseKind.ALPHA, null, null, null);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertNull(v.getReleaser());
    Assert.assertNull(v.getBuildId());
    Assert.assertNull(v.getCodeBase());
    Assert.assertEquals("3.1-a-eng", v.getVersion());
    Assert.assertEquals("3.1-a-eng 'C'", v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1-a", 3, 1, SubReleaseKind.ALPHA);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertNull(v.getReleaser());
    Assert.assertNull(v.getBuildId());
    Assert.assertNull(v.getCodeBase());
    Assert.assertEquals("3.1-a-eng", v.getVersion());
    Assert.assertEquals("3.1-a-eng 'C'", v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1", 3, 1, SubReleaseKind.ENGINEERING, RELEASER, BUILD, SHA);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertEquals(RELEASER, v.getReleaser());
    Assert.assertEquals(BUILD, v.getBuildId());
    Assert.assertEquals(SHA, v.getCodeBase());
    Assert.assertEquals("3.1-eng", v.getVersion());
    Assert.assertEquals("3.1-eng 'C' (build sha by android-jack-team@google.com)",
        v.getVerboseVersion());
    testStoreLoad(v);

    v = new Version(NAME, "3.1-a", 3, 0, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA);
    Assert.assertEquals(3, v.getReleaseCode());
    Assert.assertEquals(0, v.getSubReleaseCode());
    Assert.assertEquals(SubReleaseKind.ENGINEERING, v.getSubReleaseKind());
    Assert.assertEquals(RELEASER, v.getReleaser());
    Assert.assertEquals(BUILD, v.getBuildId());
    Assert.assertEquals(SHA, v.getCodeBase());
    Assert.assertEquals("3.1-a-eng", v.getVersion());
    Assert.assertEquals("3.1-a-eng 'C' (build sha by android-jack-team@google.com)",
        v.getVerboseVersion());
    testStoreLoad(v);
  }

  // FINDBUGS Best effort on delete
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  private void testStoreLoad(Version v1) throws IOException {
    File file = null;

    try {
      file = com.android.sched.util.file.Files.createTempFile("sched-test");
      FileOutputStream os = new FileOutputStream(file);
      v1.store(os);
      os.close();

      FileInputStream is = new FileInputStream(file);
      Version v2 = new Version(is);

      Assert.assertEquals(v1.getBuildId(), v2.getBuildId());
      Assert.assertEquals(v1.getCodeBase(), v2.getCodeBase());
      Assert.assertEquals(v1.getReleaseCode(), v2.getReleaseCode());
      Assert.assertEquals(v1.getReleaseName(), v2.getReleaseName());
      Assert.assertEquals(v1.getReleaser(), v2.getReleaser());
      Assert.assertEquals(v1.getSubReleaseCode(), v2.getSubReleaseCode());
      Assert.assertEquals(v1.getSubReleaseKind(), v2.getSubReleaseKind());
      Assert.assertEquals(v1.getVersion(), v2.getVersion());
      Assert.assertEquals(v1.getVerboseVersion(), v2.getVerboseVersion());
    } finally {
      if (file != null) {
        file.delete();
      }
    }
  }

  @Test
  public void testComparison() {
    Version uncomparable[] = {
        new Version(NAME, "-1.-1", -1, -1, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "-1.0", -1, 0, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "-1.1", -1, 1, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "0.-1", 0, -1, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "0.0", 0, 0, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "0.4", 0, 4, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "3.-1", 3, -1, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "3.0", 3, 0, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "3.4", 3, 4, SubReleaseKind.ENGINEERING, RELEASER, BUILD, SHA),
    };

    Version comparable[] = {
        new Version(NAME, "2.1-a", 2, 1, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "2.3-a", 2, 3, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "2.5-a", 2, 5, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "3.2-a", 3, 2, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
        new Version(NAME, "3.4-a", 3, 4, SubReleaseKind.ALPHA, RELEASER, BUILD, SHA),
    };

    for (int i = 0; i < uncomparable.length; i++) {
      Assert.assertFalse(uncomparable[i].isComparable());
    }

    for (int i = 0; i < comparable.length; i++) {
      Assert.assertTrue(comparable[i].isComparable());
    }

    for (int i = 0; i < uncomparable.length; i++) {
      for (int j = 0; j < uncomparable.length; j++) {
        compare(uncomparable[i], i, uncomparable[j], j, false);
      }
    }

    for (int i = 0; i < uncomparable.length; i++) {
      for (int j = 0; j < comparable.length; j++) {
        compare(uncomparable[i], i, comparable[j], j, false);
      }
    }

    for (int i = 0; i < comparable.length; i++) {
      for (int j = 0; j < uncomparable.length; j++) {
        compare(comparable[i], i, uncomparable[j], j, false);

      }
    }

    for (int i = 0; i < comparable.length; i++) {
      for (int j = 0; j < comparable.length; j++) {
        compare(comparable[i], i, comparable[j], j, true);
      }
    }
  }

  private void compare(@Nonnull Version v1, int i, @Nonnull Version v2, int j,
      boolean isComparable) {
    String message = "Fail to compare " + v1 + " with " + v2 + " <" + i + "," + j + ">";

    try {
      Assert.assertEquals(message, (i < j) ? -1 : ((i > j) ? 1 : 0), v1.compareTo(v2));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i > j, v1.isNewerThan(v2));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i >= j, v1.isNewerOrEqualThan(v2));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i == j, v1.isSame(v2));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i <= j, v1.isOlderOrEqualThan(v2));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i < j, v1.isOlderThan(v2));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    int releaseCode = v2.getReleaseCode();
    int subReleaseCode = v2.getSubReleaseCode();

    try {
      Assert.assertEquals(message, (i < j) ? -1 : (i > j) ? 1 : 0,
          v1.compareTo(releaseCode, subReleaseCode));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i > j, v1.isNewerThan(releaseCode, subReleaseCode));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i >= j, v1.isNewerOrEqualThan(releaseCode, subReleaseCode));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i == j, v1.isSame(releaseCode, subReleaseCode));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i <= j, v1.isOlderOrEqualThan(releaseCode, subReleaseCode));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }

    try {
      Assert.assertEquals(message, i < j, v1.isOlderThan(releaseCode, subReleaseCode));
      Assert.assertTrue(message, isComparable);
    } catch (UncomparableVersion e) {
      Assert.assertFalse(message, isComparable);
    }
  }
}
