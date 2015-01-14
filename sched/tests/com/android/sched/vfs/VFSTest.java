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

package com.android.sched.vfs;

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotSetPermissionException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.WrongPermissionException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;

import javax.annotation.Nonnull;

public class VFSTest {

  @Before
  public void setUp() {
    VFSTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testDirectVFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = File.createTempFile("vfs", "dir");
    String path = file.getAbsolutePath();
    Assert.assertTrue(file.delete());

    DirectVFS directVFS = new DirectVFS(new Directory(path, null, Existence.NOT_EXIST,
        Permission.WRITE, ChangePermission.NOCHANGE));
    testOutputVFS(directVFS);
    testInputVFS(directVFS);
    directVFS.close();

    FileUtils.deleteDir(file);
  }

  @Test
  public void testInputOutputZipVFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = File.createTempFile("vfs", ".zip");
    String path = file.getAbsolutePath();

    InputOutputZipVFS zipVFS = new InputOutputZipVFS(
        new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE));
    testOutputVFS(zipVFS);
    testInputVFS(zipVFS);
    zipVFS.close();

    InputZipVFS inputZipVFS = new InputZipVFS(
        new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE));
    testInputVFS(inputZipVFS);
    inputZipVFS.close();
    Assert.assertTrue(file.delete());
  }

  private void testOutputVFS(@Nonnull InputOutputVFS outputVFS) throws NotDirectoryException,
      CannotCreateFileException, IOException {

    // create stuff from root dir
    InputOutputVDir dirA = (InputOutputVDir) outputVFS.getRootInputOutputVDir().createOutputVDir(
        new VPath("dirA", '/'));
    outputVFS.getRootInputOutputVDir().createOutputVDir(
        new VPath("dirB/dirBA", '/'));
    OutputVFile file1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("file1", '/'));
    writeToFile(file1, "file1");
    OutputVFile fileA1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirA/fileA1", '/'));
    writeToFile(fileA1, "dirA/fileA1");
    OutputVFile fileB1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirB/fileB1", '/'));
    writeToFile(fileB1, "dirB/fileB1");
    OutputVFile fileBA1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirB/dirBA/fileBA1", '/'));
    writeToFile(fileBA1, "dirB/dirBA/fileBA1");
    OutputVFile fileBB1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirB/dirBB/fileBB1", '/'));
    writeToFile(fileBB1, "dirB/dirBB/fileBB1");
    OutputVFile fileC1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirC/fileC1", '/'));
    writeToFile(fileC1, "dirC/fileC1");

    // create stuff from dirA
    InputOutputVDir dirAA = (InputOutputVDir) dirA.createOutputVDir(new VPath("dirAA", '/'));
    OutputVFile fileA2 = dirA.createOutputVFile(new VPath("fileA2", '/'));
    writeToFile(fileA2, "dirA/fileA2");

    // create stuff from dirAA
    dirAA.createOutputVDir(new VPath("dirAAA/dirAAAA", '/'));
    OutputVFile fileAAB1 = dirAA.createOutputVFile(new VPath("dirAAB/fileAAB1", '/'));
    writeToFile(fileAAB1, "dirA/dirAA/dirAAB/fileAAB1");
  }

  private void testInputVFS(@Nonnull InputVFS inputVFS) throws NotFileException,
      NoSuchFileException, IOException {
    InputVFile file1 = inputVFS.getRootInputVDir().getInputVFile(new VPath("file1", '/'));
    Assert.assertEquals("file1", readFromFile(file1));

    InputVDir dirA = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirA", '/'));
    Collection<? extends InputVElement> dirAElements = dirA.list();
    Assert.assertEquals(3, dirAElements.size());

    InputVFile fileAAB1 =
        inputVFS.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
    Assert.assertEquals("dirA/dirAA/dirAAB/fileAAB1", readFromFile(fileAAB1));
  }

  private void writeToFile(@Nonnull OutputVFile file, @Nonnull String string) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(file.openWrite());
    writer.write(string);
    writer.close();
  }

  @Nonnull
  private String readFromFile(@Nonnull InputVFile file) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(file.openRead()));
    String string = reader.readLine();
    Assert.assertNull(reader.readLine());
    reader.close();
    return string;
  }

}
