/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.jayce.v0003.io;

import com.android.jack.IllegalOptionsException;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

public class EscapeStringTest {

  @Test
  public void test001() throws Exception {
    String string = "abcd";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test002() throws Exception {
    String string = "ab\"cd\"";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test003() throws Exception {
    String string = "ab'cd'";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test004() throws Exception {
    String string = "ab\tcd";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test005() throws Exception {
    String string = "ab\rcd";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test006() throws Exception {
    String string = "ab\ncd";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test007() throws Exception {
    String string = "ab\bcd";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test008() throws Exception {
    String string = "ab\fcd";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test009() throws Exception {
    String string = "\"'\\";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test010() throws Exception {
    String string = "[ab]\\b\\\\o5\\xF9\\u1E7B\\t\\n\\f\\r\\a\\e[yz]";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test011() throws Exception {
    String string = "\uD9A0\uDE81*abc";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Test
  public void test012() throws Exception {
    String string = "\uDE81|\uD9A0\uDE81|\uD9A0";
    Assert.assertEquals(string, writeStringAndReadItAfter(string));
  }

  @Nonnull
  private String writeStringAndReadItAfter(@Nonnull String stringToWrite) throws IOException,
      IllegalOptionsException, CannotCreateFileException, CannotChangePermissionException {
    File tmp = TestTools.createTempFile("tmp", "");
    RunnableHooks hooks = new RunnableHooks();
    try {
      Options options = new Options();
      options.checkValidity(hooks);
      options.getConfigBuilder(hooks).getCodecContext().setDebug();
      ThreadConfig.setConfig(options.getConfig());
      FileOutputStream fos = new FileOutputStream(tmp);
      JayceInternalWriterImpl jw = new JayceInternalWriterImpl(fos);
      jw.writeString(stringToWrite);
      jw.close();
      FileInputStream fis = new FileInputStream(tmp);
      Tokenizer t = new Tokenizer(fis);
      String result = t.readString();
      fis.close();
      assert result != null;
      return result;
    } catch (ConfigurationException e) {
      throw new AssertionError(e);
    } finally {
      ThreadConfig.unsetConfig();
      hooks.runHooks();
    }
  }
}
