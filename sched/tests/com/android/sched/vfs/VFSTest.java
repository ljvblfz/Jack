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

import com.android.sched.util.config.AsapConfigBuilder;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.ThreadConfig;
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
import java.security.Provider;
import java.security.Security;
import java.util.Collection;

import javax.annotation.Nonnull;

public class VFSTest {

  @Before
  public void setUp() throws ConfigurationException {
    VFSTest.class.getClassLoader().setDefaultAssertionStatus(true);
    ThreadConfig.setConfig(new AsapConfigBuilder().build());
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
    File file = null;
    DirectVFS directVFS = null;
    DirectVFS directVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());
      directVFS = new DirectVFS(new Directory(path, null, Existence.NOT_EXIST,
          Permission.READ | Permission.WRITE, ChangePermission.NOCHANGE));
      testOutputVFS(directVFS);
      testInputVFS(directVFS);
      directVFS.close();

      directVFS2 = new DirectVFS(new Directory(path, null, Existence.MUST_EXIST,
          Permission.READ | Permission.WRITE, ChangePermission.NOCHANGE));
      testInputVFS(directVFS2);
    } finally {
      if (directVFS != null) {
        directVFS.close();
      }
      if (directVFS2 != null) {
        directVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testDirectFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS directVFS = null;
    InputOutputVFS directVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      directVFS =
          new GenericInputOutputVFS(new DirectFS(new Directory(path, null, Existence.NOT_EXIST,
              Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE));

      testOutputVFS(directVFS);
      testInputVFS(directVFS);
      directVFS.close();

      directVFS2 =
          new GenericInputOutputVFS(new DirectFS(new Directory(path, null, Existence.MUST_EXIST,
              Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE));
      testInputVFS(directVFS2);

    } finally {
      if (directVFS != null) {
        directVFS.close();
      }
      if (directVFS2 != null) {
        directVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testDeflateFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS directVFS = null;
    InputOutputVFS directVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      directVFS =
          new GenericInputOutputVFS(new DeflateFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));

      testOutputVFS(directVFS);
      testInputVFS(directVFS);
      directVFS.close();

      directVFS2 =
          new GenericInputOutputVFS(new DeflateFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));
      testInputVFS(directVFS2);

    } finally {
      if (directVFS != null) {
        directVFS.close();
      }
      if (directVFS2 != null) {
        directVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testCaseInsensitiveFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS directVFS = null;
    InputOutputVFS directVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      directVFS =
          new GenericInputOutputVFS(new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));

      testOutputVFS(directVFS);
      testInputVFS(directVFS);
      directVFS.close();

      directVFS2 =
          new GenericInputOutputVFS(new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));
      testInputVFS(directVFS2);

    } finally {
      if (directVFS != null) {
        directVFS.close();
      }
      if (directVFS2 != null) {
        directVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testMessageDigestFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS directVFS = null;
    InputOutputVFS directVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      Provider.Service sha1 = null;
      for (Provider provider : Security.getProviders()) {
        for (Provider.Service service : provider.getServices()) {
          if (service.getType().equals("MessageDigest") && service.getAlgorithm().equals("SHA")) {
            sha1 = service;
          }
        }
      }
      Assert.assertNotNull(sha1);

      directVFS = new GenericInputOutputVFS(new MessageDigestFS(new DirectFS(new Directory(path,
          null, Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));

      testOutputVFS(directVFS);
      testInputVFS(directVFS);
      InputVFile fileAAB1 =
          directVFS.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      Assert.assertNotNull(((GenericInputVFile) fileAAB1).getDigest());
      directVFS.close();

      directVFS2 = new GenericInputOutputVFS(new MessageDigestFS(new DirectFS(new Directory(path,
          null, Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));
      testInputVFS(directVFS2);
      InputVFile fileAAB1b =
          directVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      Assert.assertNotNull(((GenericInputVFile) fileAAB1b).getDigest());

    } finally {
      if (directVFS != null) {
        directVFS.close();
      }
      if (directVFS2 != null) {
        directVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testPrefixFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS directVFS = null;
    InputOutputVFS directVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      directVFS =
          new GenericInputOutputVFS(new PrefixedFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE), new VPath("stuff", '/')));

      testOutputVFS(directVFS);
      testInputVFS(directVFS);
      directVFS.close();

      directVFS2 =
          new GenericInputOutputVFS(new PrefixedFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE), new VPath("stuff", '/')));
      testInputVFS(directVFS2);

    } finally {
      if (directVFS != null) {
        directVFS.close();
      }
      if (directVFS2 != null) {
        directVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testWriteZipVFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS outputZipVFS = null;
    InputZipVFS inputZipVFS = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      outputZipVFS = new GenericInputOutputVFS(new WriteZipFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE)));
      testOutputVFS(outputZipVFS);
      outputZipVFS.close();
      inputZipVFS = new InputZipVFS(
          new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE));
      testInputVFS(inputZipVFS);
    } finally {
      if (outputZipVFS != null) {
        outputZipVFS.close();
      }
      if (inputZipVFS != null) {
        inputZipVFS.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
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
    File file = null;
    InputOutputZipVFS zipVFS = null;
    InputZipVFS inputZipVFS = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      zipVFS = new InputOutputZipVFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE));
      testOutputVFS(zipVFS);
      testInputVFS(zipVFS);
      zipVFS.close();
      inputZipVFS = new InputZipVFS(
          new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE));
      testInputVFS(inputZipVFS);
    } finally {
      if (zipVFS != null) {
        zipVFS.close();
      }
      if (inputZipVFS != null) {
        inputZipVFS.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
  }

  @Test
  public void testReadZipVFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS outputZipVFS = null;
    InputVFS inputZipVFS = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      outputZipVFS = new InputOutputZipVFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE));
      testOutputVFS(outputZipVFS);
      outputZipVFS.close();
      inputZipVFS = new GenericInputVFS(new ReadZipFS(
          new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE)));
      testInputVFS(inputZipVFS);
    } finally {
      if (outputZipVFS != null) {
        outputZipVFS.close();
      }
      if (inputZipVFS != null) {
        inputZipVFS.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
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
    Assert.assertTrue(containsFile(dirAElements, "fileA1", "dirA/fileA1"));
    Assert.assertTrue(containsFile(dirAElements, "fileA2", "dirA/fileA2"));
    Assert.assertTrue(containsDir(dirAElements, "dirAA"));

    InputVFile fileAAB1 =
        inputVFS.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
    Assert.assertEquals("dirA/dirAA/dirAAB/fileAAB1", readFromFile(fileAAB1));

    InputVDir dirB = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirB", '/'));
    InputVDir dirBA = dirB.getInputVDir(new VPath("dirBA", '/'));
    InputVFile fileBA1 = dirBA.getInputVFile(new VPath("fileBA1", '/'));
    Assert.assertEquals("dirB/dirBA/fileBA1", readFromFile(fileBA1));

    InputVDir dirBB = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirB/dirBB", '/'));
    InputVFile fileBB1 = dirBB.getInputVFile(new VPath("fileBB1", '/'));
    Assert.assertEquals("dirB/dirBB/fileBB1", readFromFile(fileBB1));
  }

  @Test
  public void testReadWriteZipFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS zipVFS = null;
    InputZipVFS inputZipVFS = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      zipVFS = new GenericInputOutputVFS(new ReadWriteZipFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE)));
      testOutputVFS(zipVFS);
      testInputVFS(zipVFS);
      zipVFS.close();
      inputZipVFS = new InputZipVFS(
          new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE));
      testInputVFS(inputZipVFS);
    } finally {
      if (zipVFS != null) {
        zipVFS.close();
      }
      if (inputZipVFS != null) {
        inputZipVFS.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
  }

  private boolean containsFile(@Nonnull Collection<? extends InputVElement> elements,
      @Nonnull String fileSimpleName, @Nonnull String fileContent) throws IOException {
    for (VElement element : elements) {
      if (element.getName().equals(fileSimpleName)) {
        return !element.isVDir()
            && fileContent.equals(readFromFile((InputVFile) element));
      }
    }

    return false;
  }

  private boolean containsDir(@Nonnull Collection<? extends InputVElement> elements,
      @Nonnull String fileSimpleName) {
    for (VElement element : elements) {
      if (element.getName().equals(fileSimpleName)) {
        return element.isVDir();
      }
    }

    return false;
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
