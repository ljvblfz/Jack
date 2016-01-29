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

package com.android.jack.reflect.test001.dx;

import com.android.jack.reflect.test001.jack.Reflect001;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void simpleName001() {
    Assert.assertEquals("C",  new Reflect001().getSimpleNameInner1());
    Assert.assertEquals("C",  new Reflect001().getSimpleNameInner2());
    Assert.assertEquals("$A$B$C",  new Reflect001().getSimpleNameInner3());
    Assert.assertEquals("$A$B$C",  new Reflect001().getSimpleNameInner4());
    Assert.assertEquals("$1I",  new Reflect001().getSimpleNameInner5());
    Assert.assertEquals("$1I",  new Reflect001().getSimpleNameInner6());
    Assert.assertEquals("$1",  new Reflect001().getSimpleNameInner7());
    Assert.assertEquals("$",  new Reflect001().getSimpleNameInner8());
    Assert.assertEquals("",  new Reflect001().getSimpleNameAnonymous1());
    Assert.assertEquals("",  new Reflect001().getSimpleNameAnonymous2());
  }

  @Test
  public void simpleName002() {
    Assert.assertEquals("$1I",  new Reflect001().getSimpleNameLocal1());
    Assert.assertEquals("",  new Reflect001().getSimpleNameLocal2());
    Assert.assertEquals("I",  new Reflect001().getSimpleNameLocal3());
    Assert.assertEquals("",  new Reflect001().getSimpleNameLocal4());
  }

  @Test
  public void isAnonymous001() {
    Assert.assertFalse(new Reflect001().isAnonymousInner1());
    Assert.assertFalse(new Reflect001().isAnonymousInner2());
    Assert.assertFalse(new Reflect001().isAnonymousInner3());
    Assert.assertFalse(new Reflect001().isAnonymousInner4());
    Assert.assertTrue(new Reflect001().isAnonymousInner5());
    Assert.assertTrue(new Reflect001().isAnonymousInner6());
    Assert.assertTrue(new Reflect001().isAnonymousInner7());
  }

  @Test
  public void isAnonymous002() {
    Assert.assertFalse(new Reflect001().isAnonymousLocal1());
    Assert.assertTrue(new Reflect001().isAnonymousLocal2());
    Assert.assertFalse(new Reflect001().isAnonymousLocal3());
    Assert.assertTrue(new Reflect001().isAnonymousLocal4());
  }
}
