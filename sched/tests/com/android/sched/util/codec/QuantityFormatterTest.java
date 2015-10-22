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

package com.android.sched.util.codec;

import junit.framework.Assert;

import org.junit.Test;

public class QuantityFormatterTest {
  @Test
  public void test() {
    QuantityFormatter formatter = new QuantityFormatter().setSI();

    Assert.assertEquals("3", formatter.formatValue(Long.valueOf(3)));
    Assert.assertEquals("1 k", formatter.formatValue(Long.valueOf(1000)));
    Assert.assertEquals("1.003 k", formatter.formatValue(Long.valueOf(1000 + 3)));
    Assert.assertEquals("1 G", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000)));
    Assert.assertEquals("1 G", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 + 3)));
    Assert.assertEquals("1 T", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000)));
    Assert.assertEquals("1 T", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000 + 3)));
    Assert.assertEquals("1 P",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000 * 1000)));
    Assert.assertEquals("1 P",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000 * 1000 + 3)));

    formatter = new QuantityFormatter().setIEC();

    Assert.assertEquals("3", formatter.formatValue(Long.valueOf(3)));
    Assert.assertEquals("1000", formatter.formatValue(Long.valueOf(1000)));
    Assert.assertEquals("1 Ki", formatter.formatValue(Long.valueOf(1024)));
    Assert.assertEquals("1003", formatter.formatValue(Long.valueOf(1000 + 3)));
    Assert.assertEquals("1.0029 Ki", formatter.formatValue(Long.valueOf(1024 + 3)));
    Assert.assertEquals("1 Gi", formatter.formatValue(Long.valueOf(1024L * 1024 * 1024)));
    Assert.assertEquals("1 Gi", formatter.formatValue(Long.valueOf(1024L * 1024 * 1024 + 3)));
    Assert.assertEquals("1 Ti", formatter.formatValue(Long.valueOf(1024L * 1024 * 1024 * 1024)));
    Assert.assertEquals("1 Ti",
        formatter.formatValue(Long.valueOf(1024L * 1024 * 1024 * 1024 + 3)));
    Assert.assertEquals("1 Pi",
        formatter.formatValue(Long.valueOf(1024L * 1024 * 1024 * 1024 * 1024)));
    Assert.assertEquals("1 Pi",
        formatter.formatValue(Long.valueOf(1024L * 1024 * 1024 * 1024 * 1024 + 3)));

    formatter = new QuantityFormatter().setUnit("B");

    Assert.assertEquals("3 B", formatter.formatValue(Long.valueOf(3)));

    formatter = new QuantityFormatter().setSI().setPrecise();

    Assert.assertEquals("3", formatter.formatValue(Long.valueOf(3)));
    Assert.assertEquals("1 k", formatter.formatValue(Long.valueOf(1000)));
    Assert.assertEquals("1.003 k", formatter.formatValue(Long.valueOf(1000 + 3)));
    Assert.assertEquals("1 G", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000)));
    Assert.assertEquals("1.000000003 G",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 + 3)));
    Assert.assertEquals("1 T", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000)));
    Assert.assertEquals("1.000000000003 T",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000 + 3)));
    Assert.assertEquals("1 P",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000 * 1000)));
    Assert.assertEquals("1.000000000000003 P",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 1000 * 1000 + 3)));
  }
}
