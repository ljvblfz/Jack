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

import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class CoveragePatternCodecTest {

  private CoveragePatternCodec codec;
  private CodecContext codecContext;

  @Before
  public void setUp() {
    codec = new CoveragePatternCodec();
    codecContext = new CodecContext();
  }

  @Test
  public void testCodec_checkString() throws ParsingException {
    checkString("foo");
    checkString("foo.bar");
    checkString("foo.bar$inner");
    checkString("foo.bar$8");

    checkString("*");
    checkString("foo.bar.*");
    checkString("*foo*");

    checkString("?");
    checkString("foo.bar.?");
    checkString("?foo?");

    try {
      checkString("foo/bar");
      Assert.fail();
    } catch (ParsingException expected) {
    }

    try {
      checkString("foo.");
      Assert.fail();
    } catch (ParsingException expected) {
    }
  }

  @Test
  public void testParse() {
    CoveragePattern p = checkParseString("a");
    Assert.assertFalse(p.matchesAny("ab"));

    p = checkParseString("ab");
    Assert.assertFalse(p.matchesAny("a"));

    p = checkParseString("?");
    Assert.assertTrue(p.matchesAny("a"));
    Assert.assertFalse(p.matchesAny("ab"));

    p = checkParseString("??");
    Assert.assertTrue(p.matchesAny("a"));
    Assert.assertTrue(p.matchesAny("ab"));

    p = checkParseString("*");
    Assert.assertTrue(p.matchesAny("a"));
    Assert.assertTrue(p.matchesAny("ab"));
    Assert.assertTrue(p.matchesAny("a.b"));
  }

  @Test
  public void testFormatValue() {
    checkFormatValue("a");
    checkFormatValue("*");
    checkFormatValue("?");
    checkFormatValue(".");
    checkFormatValue("$");
    checkFormatValue("foo.bar");
  }

  @Test
  public void testCheckValue() throws CheckingException {
    checkCheckValue("a");
    checkCheckValue("*");
    checkCheckValue("?");
    checkCheckValue("foo.bar");

    try {
      // Class name "." is not valid.
      checkCheckValue(".");
      Assert.fail();
    } catch (CheckingException expected) {
    }

    try {
      checkCheckValue("foo/bar");
      Assert.fail();
    } catch (CheckingException expected) {
    }
  }

  @Test
  public void testUsage() {
    Assert.assertNotNull(codec.getUsage());
    Assert.assertFalse(codec.getUsage().isEmpty());
  }

  @Test
  public void testValueDescriptions() {
    Assert.assertNotNull(codec.getValueDescriptions());
  }

  @Test
  public void testVariableName() {
    Assert.assertNotNull(codec.getVariableName());
    Assert.assertFalse(codec.getVariableName().isEmpty());
  }

  private void checkString(String string) throws ParsingException {
    codec.checkString(codecContext, string);
  }

  private CoveragePattern checkParseString(String string) {
    CoveragePattern p = codec.parseString(codecContext, string);
    Assert.assertNotNull(p);
    Assert.assertEquals(string, p.getString());
    Assert.assertTrue(p.matchesAny(string));
    return p;
  }

  private void checkFormatValue(String string) {
    CoveragePattern p = checkParseString(string);
    Assert.assertEquals(string, codec.formatValue(p));
  }

  private void checkCheckValue(String string) throws CheckingException {
    codec.checkValue(codecContext, checkParseString(string));
  }
}
