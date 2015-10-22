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

public class SubReleaseKindTest {

  @Test
  public void testIsMoreStable() {
    try {
      Assert.assertFalse(SubReleaseKind.ENGINEERING.isMoreStableThan(SubReleaseKind.ENGINEERING));
    } catch (UncomparableSubReleaseKind e) {
      Assert.fail();
    }
    try {
      Assert.assertFalse(SubReleaseKind.ENGINEERING.isMoreStableThan(SubReleaseKind.PRE_ALPHA));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertFalse(SubReleaseKind.ENGINEERING.isMoreStableThan(SubReleaseKind.ALPHA));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertFalse(SubReleaseKind.ENGINEERING.isMoreStableThan(SubReleaseKind.BETA));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertFalse(SubReleaseKind.ENGINEERING.isMoreStableThan(SubReleaseKind.CANDIDATE));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertFalse(SubReleaseKind.ENGINEERING.isMoreStableThan(SubReleaseKind.RELEASE));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }

    try {
      Assert.assertTrue(SubReleaseKind.PRE_ALPHA.isMoreStableThan(SubReleaseKind.ENGINEERING));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertFalse(SubReleaseKind.PRE_ALPHA.isMoreStableThan(SubReleaseKind.PRE_ALPHA));
      Assert.assertFalse(SubReleaseKind.PRE_ALPHA.isMoreStableThan(SubReleaseKind.ALPHA));
      Assert.assertFalse(SubReleaseKind.PRE_ALPHA.isMoreStableThan(SubReleaseKind.BETA));
      Assert.assertFalse(SubReleaseKind.PRE_ALPHA.isMoreStableThan(SubReleaseKind.CANDIDATE));
      Assert.assertFalse(SubReleaseKind.PRE_ALPHA.isMoreStableThan(SubReleaseKind.RELEASE));
    } catch (UncomparableSubReleaseKind e) {
      Assert.fail();
    }

    try {
      Assert.assertTrue(SubReleaseKind.ALPHA.isMoreStableThan(SubReleaseKind.ENGINEERING));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertTrue(SubReleaseKind.ALPHA.isMoreStableThan(SubReleaseKind.PRE_ALPHA));
      Assert.assertFalse(SubReleaseKind.ALPHA.isMoreStableThan(SubReleaseKind.ALPHA));
      Assert.assertFalse(SubReleaseKind.ALPHA.isMoreStableThan(SubReleaseKind.BETA));
      Assert.assertFalse(SubReleaseKind.ALPHA.isMoreStableThan(SubReleaseKind.CANDIDATE));
      Assert.assertFalse(SubReleaseKind.ALPHA.isMoreStableThan(SubReleaseKind.RELEASE));
    } catch (UncomparableSubReleaseKind e) {
      Assert.fail();
    }

    try {
      Assert.assertTrue(SubReleaseKind.BETA.isMoreStableThan(SubReleaseKind.ENGINEERING));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertTrue(SubReleaseKind.BETA.isMoreStableThan(SubReleaseKind.PRE_ALPHA));
      Assert.assertTrue(SubReleaseKind.BETA.isMoreStableThan(SubReleaseKind.ALPHA));
      Assert.assertFalse(SubReleaseKind.BETA.isMoreStableThan(SubReleaseKind.BETA));
      Assert.assertFalse(SubReleaseKind.BETA.isMoreStableThan(SubReleaseKind.CANDIDATE));
      Assert.assertFalse(SubReleaseKind.BETA.isMoreStableThan(SubReleaseKind.RELEASE));
    } catch (UncomparableSubReleaseKind e) {
      Assert.fail();
    }

    try {
      Assert.assertTrue(SubReleaseKind.CANDIDATE.isMoreStableThan(SubReleaseKind.ENGINEERING));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertTrue(SubReleaseKind.CANDIDATE.isMoreStableThan(SubReleaseKind.PRE_ALPHA));
      Assert.assertTrue(SubReleaseKind.CANDIDATE.isMoreStableThan(SubReleaseKind.ALPHA));
      Assert.assertTrue(SubReleaseKind.CANDIDATE.isMoreStableThan(SubReleaseKind.BETA));
      Assert.assertFalse(SubReleaseKind.CANDIDATE.isMoreStableThan(SubReleaseKind.CANDIDATE));
      Assert.assertFalse(SubReleaseKind.CANDIDATE.isMoreStableThan(SubReleaseKind.RELEASE));
    } catch (UncomparableSubReleaseKind e) {
      Assert.fail();
    }

    try {
      Assert.assertTrue(SubReleaseKind.RELEASE.isMoreStableThan(SubReleaseKind.ENGINEERING));
      Assert.fail();
    } catch (UncomparableSubReleaseKind e) {
      // Ok, sub release kind are not comparable
    }
    try {
      Assert.assertTrue(SubReleaseKind.RELEASE.isMoreStableThan(SubReleaseKind.PRE_ALPHA));
      Assert.assertTrue(SubReleaseKind.RELEASE.isMoreStableThan(SubReleaseKind.ALPHA));
      Assert.assertTrue(SubReleaseKind.RELEASE.isMoreStableThan(SubReleaseKind.BETA));
      Assert.assertTrue(SubReleaseKind.RELEASE.isMoreStableThan(SubReleaseKind.CANDIDATE));
      Assert.assertFalse(SubReleaseKind.RELEASE.isMoreStableThan(SubReleaseKind.RELEASE));
    } catch (UncomparableSubReleaseKind e) {
      Assert.fail();
    }
  }

}
