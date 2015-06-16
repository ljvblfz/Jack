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

import junit.framework.Assert;

import org.junit.Test;

public class VersionTest {

  @Test
  public void testCompare() {
    Version v31a = new Version(3, 1, SubReleaseKind.ALPHA);
    Version v31e = new Version(3, 1, SubReleaseKind.ENGINEERING);
    try {
      Assert.assertTrue(v31a.compareTo(v31a) == 0);
    } catch (UncomparableVersion e) {
      Assert.fail();
    }
    Version v32e = new Version(3, 2, SubReleaseKind.ENGINEERING);
    try {
      Assert.assertTrue(v31a.compareTo(v32e) < 0);
      Assert.fail();
    } catch (UncomparableVersion e) {
      // Ok, versions are not comparable
    }
    try {
      Assert.assertTrue(v31e.compareTo(v32e) < 0);
    } catch (UncomparableVersion e) {
      // Two ENGINEERING version are comparable
      Assert.fail();
    }

    Version v33p = new Version(3, 3, SubReleaseKind.PRE_ALPHA);
    Version v41b = new Version(4, 1, SubReleaseKind.BETA);
    try {
      Assert.assertTrue(v33p.compareTo(v41b) < 0);
      Assert.assertTrue(v41b.compareTo(v33p) > 0);
    } catch (UncomparableVersion e) {
      Assert.fail();
    }

    Version v_11b = new Version(-1, 1, SubReleaseKind.BETA);
    try {
      Assert.assertTrue(v31e.compareTo(v_11b) > 0);
      Assert.fail();
    } catch (UncomparableVersion e) {
      // Ok, versions are not comparable
    }
    Version v1_1b = new Version(1, -1, SubReleaseKind.BETA);
    try {
      Assert.assertTrue(v31e.compareTo(v1_1b) > 0);
      Assert.fail();
    } catch (UncomparableVersion e) {
      // Ok, versions are not comparable
    }
  }

  @Test
  public void testNewerOlder() {
    Version v31a = new Version(3, 1, SubReleaseKind.ALPHA);
    Version v30a = new Version(3, 0, SubReleaseKind.ALPHA);
    Version v25a = new Version(2, 5, SubReleaseKind.ALPHA);
    Version v40a = new Version(4, 0, SubReleaseKind.ALPHA);
    Version v32a = new Version(3, 2, SubReleaseKind.ALPHA);

    try {
      Assert.assertTrue(v31a.isNewerOrEqualsThan(v31a));
      Assert.assertTrue(v31a.isNewerOrEqualsThan(v30a));
      Assert.assertTrue(v31a.isNewerOrEqualsThan(v25a));
      Assert.assertFalse(v31a.isNewerOrEqualsThan(v40a));
      Assert.assertFalse(v31a.isNewerOrEqualsThan(v32a));

      Assert.assertFalse(v31a.isNewerThan(v31a));
      Assert.assertTrue(v31a.isNewerThan(v30a));
      Assert.assertTrue(v31a.isNewerThan(v25a));
      Assert.assertFalse(v31a.isNewerThan(v40a));
      Assert.assertFalse(v31a.isNewerThan(v32a));
    } catch (UncomparableVersion e) {
      Assert.fail();
    }
  }
}
