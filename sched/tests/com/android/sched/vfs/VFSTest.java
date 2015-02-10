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
import com.android.sched.util.file.CannotDeleteFileException;
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
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.ZipLocation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
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
  public void testDirectFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());
      VFS vfs1 = new DirectFS(new Directory(path, null, Existence.NOT_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);

      ioVFS1 = new GenericInputOutputVFS(vfs1);

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      VFS vfs2 = new DirectFS(new Directory(path, null, Existence.MUST_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);
      ioVFS2 = new GenericInputOutputVFS(vfs2);
      testInputVFS(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testCachedDirectFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());
      VFS vfs1 = new CachedDirectFS(new Directory(path, null,
          Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE);

      ioVFS1 = new GenericInputOutputVFS(vfs1);

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      checkUnicity(vfs1);
      ioVFS1.close();

      VFS vfs2 = new DirectFS(new Directory(path, null, Existence.MUST_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);

      ioVFS2 = new GenericInputOutputVFS(vfs2);
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
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
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      ioVFS1 =
          new GenericInputOutputVFS(new DeflateFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      ioVFS2 =
          new GenericInputOutputVFS(new DeflateFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
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
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      ioVFS1 =
          new GenericInputOutputVFS(new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      ioVFS2 =
          new GenericInputOutputVFS(new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));
      testInputVFS(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
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
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
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

      ioVFS1 = new GenericInputOutputVFS(new MessageDigestFS(new DirectFS(new Directory(path,
          null, Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputOutputVFS(new MessageDigestFS(new DirectFS(new Directory(path,
          null, Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  @Ignore //STOPSHIP: fix this
  public void testMessageDigestFSWithCaseInsensitiveFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
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

      ioVFS1 = new GenericInputOutputVFS(new MessageDigestFS(new CaseInsensitiveFS(
          new DirectFS(new Directory(path, null, Existence.NOT_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE)),
              new MessageDigestFactory(sha1)));
      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputOutputVFS(new MessageDigestFS(new CaseInsensitiveFS(
          new DirectFS(new Directory(path, null, Existence.MUST_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE)),
              new MessageDigestFactory(sha1)));
      testInputVFS(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testMessageDigestFSWithCachedDirectFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
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

      VFS vfs1 = new MessageDigestFS(new CachedDirectFS(
          new Directory(path, null, Existence.NOT_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE),
              new MessageDigestFactory(sha1));

      ioVFS1 = new GenericInputOutputVFS(vfs1);
      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputOutputVFS(new MessageDigestFS(
          new DirectFS(new Directory(path, null, Existence.MUST_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE),
              new MessageDigestFactory(sha1)));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
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
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      ioVFS1 =
          new GenericInputOutputVFS(new PrefixedFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE), new VPath("stuff", '/')));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      ioVFS2 =
          new GenericInputOutputVFS(new PrefixedFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE), new VPath("stuff", '/')));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
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
    InputOutputVFS ioVFS1 = null;
    InputVFS iVFS2 = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      ioVFS1 = new GenericInputOutputVFS(new WriteZipFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE)));
      testOutputVFS(ioVFS1);
      ioVFS1.close();
      iVFS2 = new GenericInputVFS(new ReadZipFS(
          new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE)));
      testInputVFS(iVFS2);
      checkZipLocations(iVFS2);
    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (iVFS2 != null) {
        iVFS2.close();
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
  public void testReadWriteZipFSAndReadZipFS()
      throws NotDirectoryException,
      CannotCreateFileException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      IOException {
    File file = null;
    InputOutputVFS zipVFS = null;
    InputVFS inputZipVFS = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      zipVFS = new GenericInputOutputVFS(new ReadWriteZipFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE)));
      testOutputVFS(zipVFS);
      testDelete(zipVFS);
      //STOPSHIP: should be a ZipLocation but is currently a FileOrDirLocation
//      checkZipLocations(zipVFS);
      testInputVFS(zipVFS);
      zipVFS.close();
      inputZipVFS = new GenericInputVFS(new ReadZipFS(
          new InputZipFile(path, null, Existence.MUST_EXIST, ChangePermission.NOCHANGE)));
      testInputVFS(inputZipVFS);
      checkZipLocations(inputZipVFS);
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

  private void testDelete(@Nonnull InputOutputVFS ioVFS)
      throws NoSuchFileException,
      CannotDeleteFileException,
      NotFileOrDirectoryException,
      CannotCreateFileException,
      IOException {

    // let's delete "dirA/dirAA/dirAAB/fileAAB1"
    InputOutputVDir dirA = ioVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirA", '/'));
    {
      InputOutputVFile fileAAB1 = dirA.getInputVFile(new VPath("dirAA/dirAAB/fileAAB1", '/'));
      fileAAB1.delete();
      try {
        ioVFS.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }
    }

    // let's delete "dirB/dirBB/fileBB1"
    InputOutputVDir dirBB =
        ioVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirB/dirBB", '/'));
    {
      InputOutputVFile fileBB1 = dirBB.getInputVFile(new VPath("fileBB1", '/'));
      fileBB1.delete();
      try {
        ioVFS.getRootInputVDir().getInputVFile(new VPath("dirB/dirBB/fileBB1", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }
    }

    // let's delete "dirC/fileC1"
    {
      InputOutputVFile fileC1 = ioVFS.getRootInputOutputVDir().getInputVFile(
          new VPath("dirC/fileC1", '/'));
      fileC1.delete();
      try {
        ioVFS.getRootInputVDir().getInputVFile(new VPath("dirC/fileC1", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }
    }

    // let's re-create the files we've deleted to leave the VFS in the same state as before.
    {
      OutputVFile fileAAB1 = dirA.createOutputVFile(new VPath("dirAA/dirAAB/fileAAB1", '/'));
      writeToFile(fileAAB1, "dirA/dirAA/dirAAB/fileAAB1");
      OutputVFile fileBB1 = dirBB.createOutputVFile(new VPath("fileBB1", '/'));
      writeToFile(fileBB1, "dirB/dirBB/fileBB1");
      OutputVFile fileC1 =
          ioVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirC/fileC1", '/'));
      writeToFile(fileC1, "dirC/fileC1");
    }
  }


  private void checkFileLocations(@Nonnull InputVFS inputVFS) throws NotFileOrDirectoryException,
      NoSuchFileException {
    VPath fileAAB1Path = new VPath("dirA/dirAA/dirAAB/fileAAB1", '/');
    InputVFile fileAAB1 = inputVFS.getRootInputVDir().getInputVFile(fileAAB1Path);
    FileLocation fileAAB1Location = (FileLocation) fileAAB1.getLocation();
    Assert.assertTrue(fileAAB1Location.getDescription().contains("file"));
    Assert.assertTrue(fileAAB1Location.getDescription().contains(
        fileAAB1Path.getPathAsString(File.separatorChar)));

    VPath dirBBPath = new VPath("dirB/dirBB", '/');
    InputVDir dirBB = inputVFS.getRootInputVDir().getInputVDir(dirBBPath);
    DirectoryLocation dirBBLocation = (DirectoryLocation) dirBB.getLocation();
    Assert.assertTrue(dirBBLocation.getDescription().contains("directory"));
    Assert.assertTrue(dirBBLocation.getDescription().contains(
        dirBBPath.getPathAsString(File.separatorChar)));
  }

  private void checkZipLocations(@Nonnull InputVFS inputVFS) throws NotFileOrDirectoryException,
      NoSuchFileException {
    VPath fileAAB1Path = new VPath("dirA/dirAA/dirAAB/fileAAB1", '/');
    InputVFile fileAAB1 = inputVFS.getRootInputVDir().getInputVFile(fileAAB1Path);
    ZipLocation fileAAB1Location = (ZipLocation) fileAAB1.getLocation();
    Assert.assertTrue(fileAAB1Location.getDescription().contains(".zip"));
    Assert.assertTrue(fileAAB1Location.getDescription().contains("entry '" + ZipUtils.ZIP_SEPARATOR
        + fileAAB1Path.getPathAsString(ZipUtils.ZIP_SEPARATOR) + '\''));

    VPath dirBBPath = new VPath("dirB/dirBB", '/');
    InputVDir dirBB = inputVFS.getRootInputVDir().getInputVDir(dirBBPath);
    ZipLocation dirBBLocation = (ZipLocation) dirBB.getLocation();
    Assert.assertTrue(dirBBLocation.getDescription().contains(".zip"));
    Assert.assertTrue(dirBBLocation.getDescription().contains("entry '" + ZipUtils.ZIP_SEPARATOR
        + dirBBPath.getPathAsString(ZipUtils.ZIP_SEPARATOR) + ZipUtils.ZIP_SEPARATOR + '\''));
  }

  private void checkUnicity(@Nonnull VFS vfs) throws NotDirectoryException, NoSuchFileException,
      NotFileException {
    // check file unicity
    VDir dirA = vfs.getRootDir().getVDir("dirA");
    VDir dirAAv1 = dirA.getVDir("dirAA");
    VDir dirAAv2 = vfs.getRootDir().getVDir(new VPath("dirA/dirAA", '/'));
    VDir dirAAv3 = null;
    for (VElement subElement : dirA.list()) {
      if (subElement.getName().equals("dirAA")) {
        dirAAv3 = (VDir) subElement;
        break;
      }
    }
    Assert.assertNotNull(dirAAv3);
    Assert.assertTrue(dirAAv1 == dirAAv2);
    Assert.assertTrue(dirAAv2 == dirAAv3);

    // check dir unicity
    VDir dirAAB = dirAAv1.getVDir("dirAAB");
    VFile fileAAB1v1 = dirAAB.getVFile("fileAAB1");
    VFile fileAAB1v2 = dirAAv2.getVFile(new VPath("dirAAB/fileAAB1", '/'));
    VFile fileAAB1v3 = vfs.getRootDir().getVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
    VFile fileAAB1v4 = null;
    for (VElement subElement : dirAAB.list()) {
      if (subElement.getName().equals("fileAAB1")) {
        fileAAB1v4 = (VFile) subElement;
        break;
      }
    }
    Assert.assertNotNull(fileAAB1v4);
    Assert.assertTrue(fileAAB1v1 == fileAAB1v2);
    Assert.assertTrue(fileAAB1v2 == fileAAB1v3);
    Assert.assertTrue(fileAAB1v3 == fileAAB1v4);
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
