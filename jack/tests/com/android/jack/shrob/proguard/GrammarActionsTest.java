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

package com.android.jack.shrob.proguard;

import com.android.jack.shrob.proguard.GrammarActions.FilterSeparator;
import com.android.jack.shrob.spec.NameSpecification;

import junit.framework.Assert;

import org.junit.Test;

public class GrammarActionsTest {

  @Test
  public void testNamePatternClass() {
    NameSpecification spec = GrammarActions.name("a.b.*.d", FilterSeparator.CLASS);
    Assert.assertTrue(spec.matches("a.b.c.d"));
    Assert.assertFalse(spec.matches("a.b.c.e.d"));
    Assert.assertTrue(spec.matches("a.b.c/e.d"));
    spec = GrammarActions.name("a.b.**.d", FilterSeparator.CLASS);
    Assert.assertTrue(spec.matches("a.b.c.d"));
    Assert.assertTrue(spec.matches("a.b.c.e.d"));
    spec = GrammarActions.name("a.b.?.d", FilterSeparator.CLASS);
    Assert.assertTrue(spec.matches("a.b.c.d"));
    Assert.assertFalse(spec.matches("a.b.ce.d"));
    Assert.assertFalse(spec.matches("a.b...d"));
    Assert.assertTrue(spec.matches("a.b./.d"));
  }

  @Test
  public void testNamePatternFile() {
    NameSpecification spec = GrammarActions.name("a/b/*/d.txt", FilterSeparator.FILE);
    Assert.assertTrue(spec.matches("a/b/c/d.txt"));
    Assert.assertFalse(spec.matches("a/b/c/e/d.txt"));
    Assert.assertTrue(spec.matches("a/b/c.e/d.txt"));
    spec = GrammarActions.name("a/b/**/d.txt", FilterSeparator.FILE);
    Assert.assertTrue(spec.matches("a/b/c/d.txt"));
    Assert.assertTrue(spec.matches("a/b/c/e/d.txt"));
    spec = GrammarActions.name("a/b/?/d.txt", FilterSeparator.FILE);
    Assert.assertTrue(spec.matches("a/b/c/d.txt"));
    Assert.assertFalse(spec.matches("a/b/ce/d.txt"));
    Assert.assertFalse(spec.matches("a/b///d.txt"));
    Assert.assertTrue(spec.matches("a/b/./d.txt"));
  }

  @Test
  public void testNamePatternGENERAL() {
    NameSpecification spec = GrammarActions.name("a/b/*/d.txt", FilterSeparator.GENERAL);
    Assert.assertTrue(spec.matches("a/b/c/d.txt"));
    Assert.assertTrue(spec.matches("a/b/c/e/d.txt"));
    Assert.assertTrue(spec.matches("a/b/c.e/d.txt"));
    spec = GrammarActions.name("a/b/**/d.txt", FilterSeparator.GENERAL);
    Assert.assertTrue(spec.matches("a/b/c/d.txt"));
    Assert.assertTrue(spec.matches("a/b/c/e/d.txt"));
    spec = GrammarActions.name("a/b/?/d.txt", FilterSeparator.GENERAL);
    Assert.assertTrue(spec.matches("a/b/c/d.txt"));
    Assert.assertFalse(spec.matches("a/b/ce/d.txt"));
    Assert.assertTrue(spec.matches("a/b///d.txt"));
    Assert.assertTrue(spec.matches("a/b/./d.txt"));
  }
}
