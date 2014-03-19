/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.jack.Main;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * JUnit tests for class {@link FileUtils}.
 */
public class FileUtilsTest {

  @BeforeClass
  public static void setUpClass() {
    // Enable assertions
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Test method for {@link com.android.jack.util.FileUtils#getFileSeparator()}.
   */
  public void testGetFileSeparator() {
    String fileSeparator = FileUtils.getFileSeparator();
    Assert.assertNotNull(fileSeparator);
    Assert.assertFalse(fileSeparator.isEmpty());
  }

  /**
   * Test method for {@link com.android.jack.util.FileUtils#getWorkingDirectory()}.
   */
  @Test
  public void testGetWorkingDirectory() {
    File workingDir = FileUtils.getWorkingDirectory();
    Assert.assertNotNull(workingDir);
    Assert.assertTrue(workingDir.isDirectory());
  }

  /**
   * Test method for {@link com.android.jack.util.FileUtils#createIfNotExists(java.io.File)}.
   */
  @Test
  public void testCreateIfNotExists() throws IOException {

    // Test creation of a directory (in tmp)
    String tmpDirPath = System.getProperty("java.io.tmpdir");
    File tempDir = new File(tmpDirPath);
    File newDir = new File(tempDir, "testDir_FileUtilTest");
    newDir.deleteOnExit();
    if (newDir.exists()) {
      if (!newDir.delete()) {
        Assert.fail("Unable to delete folder " + newDir.getAbsolutePath());
      }
    }

    FileUtils.createIfNotExists(newDir);
    Assert.assertTrue(newDir.exists());
  }

}
