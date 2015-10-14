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

import java.util.concurrent.TimeUnit;

public class DurationFormatterTest {
  @Test
  public void test() {
    DurationFormatter formatter = new DurationFormatter();

    Assert.assertEquals("3 ns", formatter.formatValue(Long.valueOf(3)));
    Assert.assertEquals("1 µs", formatter.formatValue(Long.valueOf(1000)));
    Assert.assertEquals("1.003 µs", formatter.formatValue(Long.valueOf(1000 + 3)));
    Assert.assertEquals("1 min", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60)));
    Assert.assertEquals("1 min", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 + 3)));
    Assert.assertEquals("1 h", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60)));
    Assert.assertEquals("1 h",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60 + 3)));
    Assert.assertEquals("1 d",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60 * 24)));
    Assert.assertEquals("1 d",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60 * 24 + 3)));

    Assert.assertEquals("-3 ns", formatter.formatValue(Long.valueOf(-3)));
    Assert.assertEquals("-1 µs", formatter.formatValue(Long.valueOf(-1000)));
    Assert.assertEquals("-1.003 µs", formatter.formatValue(Long.valueOf(-1000 - 3)));
    Assert.assertEquals("-1 min", formatter.formatValue(Long.valueOf(-1000L * 1000 * 1000 * 60)));
    Assert.assertEquals("-1 min",
        formatter.formatValue(Long.valueOf(-1000L * 1000 * 1000 * 60 - 3)));
    Assert.assertEquals("-1 h",
        formatter.formatValue(Long.valueOf(-1000L * 1000 * 1000 * 60 * 60)));
    Assert.assertEquals("-1 h",
        formatter.formatValue(Long.valueOf(-1000L * 1000 * 1000 * 60 * 60 - 3)));
    Assert.assertEquals("-1 d",
        formatter.formatValue(Long.valueOf(-1000L * 1000 * 1000 * 60 * 60 * 24)));
    Assert.assertEquals("-1 d",
        formatter.formatValue(Long.valueOf(-1000L * 1000 * 1000 * 60 * 60 * 24 - 3)));

    formatter = new DurationFormatter().setPrecise();

    Assert.assertEquals("3 ns", formatter.formatValue(Long.valueOf(3)));
    Assert.assertEquals("1 µs", formatter.formatValue(Long.valueOf(1000)));
    Assert.assertEquals("1.003 µs", formatter.formatValue(Long.valueOf(1000 + 3)));
    Assert.assertEquals("1 min", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60)));
    Assert.assertEquals("1.00000000005 min",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 + 3)));
    Assert.assertEquals("1 h", formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60)));
    Assert.assertEquals("1.0000000000008333 h",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60 + 3)));
    Assert.assertEquals("1 d",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60 * 24)));
    Assert.assertEquals("1.0000000000000346 d",
        formatter.formatValue(Long.valueOf(1000L * 1000 * 1000 * 60 * 60 * 24 + 3)));

    formatter = new DurationFormatter().setInputUnit(TimeUnit.SECONDS);

    Assert.assertEquals("3 s", formatter.formatValue(Long.valueOf(3)));
    Assert.assertEquals("1 min", formatter.formatValue(Long.valueOf(60)));
    Assert.assertEquals("1.05 min", formatter.formatValue(Long.valueOf(60 + 3)));
    Assert.assertEquals("1 h", formatter.formatValue(Long.valueOf(60 * 60)));
    Assert.assertEquals("1.0008 h", formatter.formatValue(Long.valueOf(60 * 60 + 3)));
    Assert.assertEquals("1 d", formatter.formatValue(Long.valueOf(60 * 60 * 24)));
    Assert.assertEquals("1 d", formatter.formatValue(Long.valueOf(60 * 60 * 24 + 3)));
  }
}
