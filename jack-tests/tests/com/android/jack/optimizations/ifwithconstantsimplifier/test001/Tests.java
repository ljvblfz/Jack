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

package com.android.jack.optimizations.ifwithconstantsimplifier.test001;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {
  private static final String STRING1 = "foo";
  private static final String STRING2 = "bar";

  private static final StringProvider NULL_PROVIDER = new StringProvider(null, null);

  private static final StringProvider LEFT_NULL_PROVIDER =
      new StringProvider(null, STRING2);

  private static final StringProvider RIGHT_NULL_PROVIDER =
      new StringProvider(STRING1, null);

  private static final StringProvider SAME_STRINGS_PROVIDER =
      new StringProvider(STRING1, STRING1);

  private static final StringProvider EQUAL_STRINGS_PROVIDER =
      new StringProvider(new String(STRING1), new String(STRING1));

  private static final StringProvider DIFFERENT_STRINGS_PROVIDER =
      new StringProvider(STRING1, STRING2);

  @Test
  public void test001() {
    Assert.assertFalse(NestedAssign.ifWithNestedAssigns(NULL_PROVIDER));
    Assert.assertFalse(NestedAssign.ifWithNestedAssigns(LEFT_NULL_PROVIDER));
    Assert.assertFalse(NestedAssign.ifWithNestedAssigns(RIGHT_NULL_PROVIDER));
    Assert.assertFalse(NestedAssign.ifWithNestedAssigns(DIFFERENT_STRINGS_PROVIDER));

    Assert.assertTrue(NestedAssign.ifWithNestedAssigns(SAME_STRINGS_PROVIDER));
    Assert.assertTrue(NestedAssign.ifWithNestedAssigns(EQUAL_STRINGS_PROVIDER));

    // Try in a loop
    Assert.assertFalse(NestedAssign.ifWithNestedAssignsInLoop(
        SAME_STRINGS_PROVIDER,
        NULL_PROVIDER));

    Assert.assertTrue(NestedAssign.ifWithNestedAssignsInLoop(
            SAME_STRINGS_PROVIDER,
            SAME_STRINGS_PROVIDER));
  }
}
