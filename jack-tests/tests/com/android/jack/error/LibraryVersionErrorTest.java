/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.error;

import com.android.jack.comparator.util.BytesStreamSucker;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.InputLibraryCodec;
import com.android.jack.library.JackLibrary;
import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * Test checking what happens with Jack Libraries that are too old or too recent.
 */
public class LibraryVersionErrorTest {

  @Test
  public void testTooRecentJackLib() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    // Build lib
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File lib = AbstractTestTools.createTempFile("lib", ".jack");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.srcToLib(lib, /* zipFile = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.error.library.jack"));

    // Get current Minor version
    InputLibraryCodec codec = new InputLibraryCodec();
    InputLibrary inputLib = codec.parseString(new CodecContext(), lib.getPath());
    try {
      int minorVersion = inputLib.getMinorVersion();

      // Create new lib with hacked version
      File hackedLib = hackLib(lib, Collections.singletonMap(JackLibrary.KEY_LIB_MINOR_VERSION,
          String.valueOf(minorVersion + 1)));

      try {
        File output = AbstractTestTools.createTempDir();
        toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(hackedLib)
            .srcToExe(output, /* zipFile= */ false,
                AbstractTestTools.getTestRootDir("com.android.jack.error.source.jack"));
        Assert.fail();

      } catch (PropertyIdException e) {
        String message = e.getMessage();
        Assert.assertTrue(message.contains("The version of the library"));
        Assert.assertTrue(message.contains("is too recent."));
      }
    } finally {
      inputLib.close();
    }
  }

  @Test
  public void testTooOldJackLib() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    // Build lib
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File lib = AbstractTestTools.createTempFile("lib", ".jack");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.srcToLib(lib, /* zipFile = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.error.library.jack"));

    // Let's use a negative version number to be sure it's not supported anymore
    int newMinorVersion = -1;

    // Create new lib with hacked version
    File hackedLib = hackLib(lib, Collections.singletonMap(JackLibrary.KEY_LIB_MINOR_VERSION,
        String.valueOf(newMinorVersion)));

    try {
      File output = AbstractTestTools.createTempDir();
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addStaticLibs(hackedLib)
          .srcToExe(output, /* zipFile= */ false,
              AbstractTestTools.getTestRootDir("com.android.jack.error.source.jack"));
    } catch (PropertyIdException e) {
      String message = e.getMessage();
      Assert.assertTrue(message.contains("The version of the library"));
      Assert.assertTrue(message.contains("is not supported anymore."));
    }
  }

  @Nonnull
  private static File hackLib(@Nonnull File lib, @Nonnull Map<String,String> customProp)
      throws IOException, CannotCreateFileException, CannotChangePermissionException {
    File hackedLib = AbstractTestTools.createTempFile("hackedlib", ".jack");

    try (ZipFile libZip = new ZipFile(lib);
        ZipOutputStream zos =
            new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(hackedLib)));) {
      Enumeration<? extends ZipEntry> entries = libZip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String entryName = entry.getName();
        zos.putNextEntry(new ZipEntry(entryName));

        if (entryName.equals(JackLibrary.LIBRARY_PROPERTIES)) {
          Properties properties = new Properties();
          properties.load(libZip.getInputStream(entry));
          properties.putAll(customProp);
          properties.store(zos, null);
        } else {
          InputStream is = libZip.getInputStream(entry);
          new BytesStreamSucker(is, zos).suck();
        }
      }
      return hackedLib;
    }
  }

}
