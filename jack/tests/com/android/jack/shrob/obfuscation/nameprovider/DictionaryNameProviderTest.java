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

package com.android.jack.shrob.obfuscation.nameprovider;

import com.android.jack.shrob.obfuscation.key.TypeKey;
import com.android.jack.test.TestsProperties;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;

public class DictionaryNameProviderTest {

  @Test
  public void dictionaryNameProviderTest() throws Exception {
    TypeKey oldKey = new TypeKey("old");
    File dictionaryFile = new File(TestsProperties.getJackRootDir(),
        "jack/tests/com/android/jack/shrob/obfuscation/nameprovider/dictionary.txt");
    DictionaryNameProvider dnp =
        new DictionaryNameProvider(dictionaryFile, new LowerCaseAlphabeticalNameProvider());
    Assert.assertEquals("abc", dnp.getNewName(oldKey));
    Assert.assertEquals("def", dnp.getNewName(oldKey));
    Assert.assertEquals("ghi", dnp.getNewName(oldKey));
    Assert.assertEquals("jkl", dnp.getNewName(oldKey));
    Assert.assertEquals("mno", dnp.getNewName(oldKey));
    Assert.assertEquals("pqr", dnp.getNewName(oldKey));
    Assert.assertEquals("stu", dnp.getNewName(oldKey));
    Assert.assertEquals("vw$", dnp.getNewName(oldKey));
    Assert.assertEquals("xyz_", dnp.getNewName(oldKey));
    Assert.assertEquals("éà", dnp.getNewName(oldKey));
    Assert.assertEquals("abc", dnp.getNewName(oldKey));
    Assert.assertEquals("a", dnp.getNewName(oldKey));
  }
}
