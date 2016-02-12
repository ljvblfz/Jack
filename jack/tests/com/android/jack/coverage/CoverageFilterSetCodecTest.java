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

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CoverageFilterSetCodecTest {
  private CoverageFilterSetCodec codec;
  private CodecContext codecContext;
  private List<PatternTest> patternTests;

  static class PatternTest {
    public PatternTest(String pattern, String[] matchStrings, String[] noMatchStrings) {
      this.pattern = pattern;
      this.matchStrings = matchStrings;
      this.noMatchStrings = noMatchStrings;
    }

    public String describe() {
      StringBuilder sb = new StringBuilder();
      sb.append("Pattern: \"");
      sb.append(pattern);
      sb.append("\", ");
      appendStrings(sb, "matchStrings", matchStrings);
      sb.append(", ");
      appendStrings(sb, "noMatchStrings", noMatchStrings);
      return sb.toString();
    }

    private static void appendStrings(StringBuilder sb, String name, String[] strings) {
      sb.append(name);
      sb.append("={");
      boolean first = true;
      for (String s : strings) {
        if (!first) {
          sb.append(',');
        }
        first = false;
        sb.append('\"');
        sb.append(s);
        sb.append('\"');
      }
      sb.append("}");
    }
    private final String pattern;
    private final String[] matchStrings;
    private final String[] noMatchStrings;
  }

  @Before
  public void setUp() {
    codec = new CoverageFilterSetCodec();
    codecContext = new CodecContext();
    patternTests = new ArrayList<CoverageFilterSetCodecTest.PatternTest>();
    // Single pattern testing.
    addPatternTest("", new String[0], new String[]{"a", "foo", "*", "???"});
    addPatternTest("*", new String[]{"foo", "bar", "foo.bar"}, new String[0]);
    addPatternTest("?", new String[]{"", "a"}, new String[]{"foo", "bar"});
    addPatternTest("???", new String[]{"foo", "bar", "", "a", "aa"}, new String[]{"aaaa"});
    addPatternTest("foo", new String[]{"foo"}, new String[]{"bar", "*", "???"});
    addPatternTest("bar", new String[]{"bar"}, new String[]{"foo", "*", "???"});
    addPatternTest("foo.bar", new String[]{"foo.bar"}, new String[]{"foo", "bar", "*", "???"});
    // Multiple patterns testing.
    addPatternTest("foo,bar", new String[]{"foo", "bar"}, new String[]{"foo.bar", "*", "???"});
    addPatternTest("foo, bar", new String[]{"foo", "bar"}, new String[]{"foo.bar", "*", "???"});
    addPatternTest(" foo, bar ", new String[]{"foo", "bar"}, new String[]{"foo.bar", "*", "???"});
    addPatternTest("foo,*,bar", new String[]{"foo", "bar", "foo.bar"}, new String[0]);
    addPatternTest("*,?", new String[]{"a", "aa"}, new String[0]);
  }

  private void addPatternTest(String pattern, String[] matchStrings, String[] noMatchStrings) {
    patternTests.add(new PatternTest(pattern, matchStrings, noMatchStrings));
  }

  @Test
  public void testCodec_checkString() throws ParsingException {
    for (PatternTest t : patternTests) {
      CoverageFilterSet cfs = codec.checkString(codecContext, t.pattern);
      Assert.assertNotNull(cfs);
      for (CoveragePattern p : cfs.getPatterns()) {
        Assert.assertNotNull(p);
      }
      for (String matchString : t.matchStrings) {
        Assert.assertTrue(t.describe(), cfs.matchesAny(matchString));
      }
      for (String noMatchString : t.noMatchStrings) {
        Assert.assertFalse(t.describe(), cfs.matchesAny(noMatchString));
      }
    }

    try {
      codec.checkString(codecContext, "foo/bar");
      Assert.fail();
    } catch (ParsingException expected) {
    }

    try {
      codec.checkString(codecContext, "foo.");
      Assert.fail();
    } catch (ParsingException expected) {
    }
  }

  @Test
  public void testParse() {
    for (PatternTest t : patternTests) {
      CoverageFilterSet cfs = codec.parseString(codecContext, t.pattern);
      Assert.assertNotNull(cfs);
      for (CoveragePattern p : cfs.getPatterns()) {
        Assert.assertNotNull(p);
      }
      for (String matchString : t.matchStrings) {
        Assert.assertTrue(t.describe(), cfs.matchesAny(matchString));
      }
      for (String noMatchString : t.noMatchStrings) {
        Assert.assertFalse(t.describe(), cfs.matchesAny(noMatchString));
      }
    }
  }

  @Test
  public void testFormatValue() {
    CoverageFilterSet filter = codec.parseString(codecContext, "a");
    Assert.assertEquals("a", codec.formatValue(filter));

    filter = codec.parseString(codecContext, "a,b");
    Assert.assertEquals("a,b", codec.formatValue(filter));
  }
}
