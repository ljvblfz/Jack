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

package com.android.jack.util;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.config.ConfigurationError;

import junit.framework.Assert;

import org.junit.Test;

public class AndroidApiLevelCodecTest {

  @Test
  public void testDefault() {
    CodecContext context = new CodecContext();
    AndroidApiLevelCodec codec = new AndroidApiLevelCodec();
    AndroidApiLevel level;

    level = codec.parseString(context, "1");
    Assert.assertEquals(1, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    level = codec.parseString(context, "5");
    Assert.assertEquals(5, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    level = codec.parseString(context, "3080");
    Assert.assertEquals(3080, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());
  }

  @Test
  public void testMinApiLevel() {
    CodecContext context = new CodecContext();
    AndroidApiLevelCodec codec = new AndroidApiLevelCodec();
    codec.setMinReleasedApiLevel(2);
    AndroidApiLevel level;

    try {
      level = codec.parseString(context, "1");
      Assert.fail();
    } catch (ConfigurationError e) {
    }

    level = codec.parseString(context, "2");
    Assert.assertEquals(2, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());


    try {
      level = codec.parseString(context, "o-b1");
      Assert.fail();
    } catch (ConfigurationError e) {
    }
  }

  @Test
  public void testMaxApiLevel() {
    CodecContext context = new CodecContext();
    AndroidApiLevelCodec codec = new AndroidApiLevelCodec();
    codec.setMaxReleasedApiLevel(2);
    AndroidApiLevel level;

    level = codec.parseString(context, "1");
    Assert.assertEquals(1, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    level = codec.parseString(context, "2");
    Assert.assertEquals(2, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    try {
      level = codec.parseString(context, "3");
      Assert.fail();
    } catch (ConfigurationError e) {
    }

    try {
      level = codec.parseString(context, "o-b1");
      Assert.fail();
    } catch (ConfigurationError e) {
    }
  }

  @Test
  public void testNoProvisionalLevel() {
    CodecContext context = new CodecContext();
    AndroidApiLevelCodec codec = new AndroidApiLevelCodec();
    codec.forbidProvisionalLevel();
    AndroidApiLevel level;

    level = codec.parseString(context, "1");
    Assert.assertEquals(1, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    level = codec.parseString(context, "5");
    Assert.assertEquals(5, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    level = codec.parseString(context, "3080");
    Assert.assertEquals(3080, level.getReleasedLevel());
    Assert.assertTrue(level.isReleasedLevel());

    try {
      level = codec.parseString(context, "o-b1");
      Assert.fail();
    } catch (ConfigurationError e) {
    }
  }
}
