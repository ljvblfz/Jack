/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.shrob.test016.dx;

import com.android.jack.shrob.test016.jack.KeepClass;

import junit.framework.Assert;

import org.junit.Test;

public class Tests2 {

  @Test
  public void test() {
    Assert.assertEquals("com.android.jack.shrob.test016.jack.A", KeepClass.value());
    Assert.assertEquals("Lcom.android.jack.shrob.test016.jack.A;", KeepClass.value2());
    Assert.assertEquals("com/android/jack/shrob/test016/jack/A", KeepClass.value3());
    Assert.assertEquals("Lcom/android/jack/shrob/test016/jack/A;", KeepClass.value4());
    Assert.assertEquals("A", KeepClass.value5());
    Assert.assertEquals("[com.android.jack.shrob.test016.jack.A", KeepClass.value6());
    Assert.assertEquals("com.android.jack.shrob.test016.jack.A[]", KeepClass.value7());
    Assert.assertEquals("dfgdgcom.android.jack.shrob.test016.jack.A", KeepClass.value8());
    Assert.assertEquals("com.android.jack.shrob.test016.jack.Afgdg", KeepClass.value9());
  }
}
