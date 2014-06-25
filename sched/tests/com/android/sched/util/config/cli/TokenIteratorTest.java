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

package com.android.sched.util.config.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.io.Files;

import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.LineLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.location.StringLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

public class TokenIteratorTest {
  @Before
  public void setUp() throws Exception {
    TokenIteratorTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testTokenIteratorWithoutFile() {
    Location loc = new StringLocation("Default location");

    try {
      test(
          new TokenIterator(loc, "0-1", "0-2", "0-3"),
          new String[]   {"0-1", "0-2", "0-3"},
          new Location[] {loc,   loc,   loc});
    } catch (NoSuchElementException e) {
      fail();
    } catch (WrongPermissionException e) {
      fail();
    } catch (NoSuchFileException e) {
      fail();
    } catch (NotFileOrDirectoryException e) {
      fail();
    } catch (CannotReadException e) {
      fail();
    }
  }

  @Test
  public void testTokenIteratorWithSimpleFile() throws IOException {
    Location loc = new StringLocation("Default location");

    File file = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-1");
    file.deleteOnExit();
    Location floc = new FileLocation(file);

    String fileArg = "@" + file.getAbsolutePath();
    PrintStream printer = new PrintStream(file);
    printer.print("1-1\n1-2\r1-3\n\r1-4\r\n1-5 1-6");
    printer.close();

    try {
      test(
          new TokenIterator(loc, fileArg, "0-1", "0-2").disallowFileReferenceInArray(),
          new String[]   {fileArg, "0-1", "0-2"},
          new Location[] {loc,  loc,   loc});
      test(
          new TokenIterator(loc, fileArg, "0-1", "0-2"),
          new String[]   {"1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "0-1", "0-2"},
          new Location[] {floc,  floc,  floc,  floc,  floc,  floc,  loc,   loc});
      test(
          new TokenIterator(loc, "0-1", fileArg, "0-2"),
          new String[]   {"0-1", "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "0-2"},
          new Location[] {loc,   floc,  floc,  floc,  floc,  floc,  floc,  loc});
      test(
          new TokenIterator(loc, "0-1", "0-2", fileArg),
          new String[]   {"0-1", "0-2", "1-1", "1-2", "1-3", "1-4", "1-5", "1-6"},
          new Location[] {loc,   loc,   floc,  floc,  floc,  floc,  floc,  floc});
    } catch (NoSuchElementException e) {
      fail(e.getMessage());
    } catch (WrongPermissionException e) {
      fail(e.getMessage());
    } catch (NoSuchFileException e) {
      fail(e.getMessage());
    } catch (NotFileOrDirectoryException e) {
      fail(e.getMessage());
    } catch (CannotReadException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTokenIteratorWithSimpleFileWithPrefix() throws IOException {
    Location loc = new StringLocation("Default location");

    File file = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-1");
    file.deleteOnExit();
    Location floc = new FileLocation(file);

    String fileArg = "!" + file.getAbsolutePath();
    PrintStream printer = new PrintStream(file);
    printer.print("1-1");
    printer.close();

    try {
      test(
          new TokenIterator(loc, fileArg, "0-1", "0-2").withFilePrefix('!'),
          new String[]   {"1-1", "0-1", "0-2"},
          new Location[] {floc,  loc,   loc});
      test(
          new TokenIterator(loc, "0-1", fileArg, "0-2").withFilePrefix('!'),
          new String[]   {"0-1", "1-1", "0-2"},
          new Location[] {loc,   floc,  loc});
      test(
          new TokenIterator(loc, "0-1", "0-2", fileArg).withFilePrefix('!'),
          new String[]   {"0-1", "0-2", "1-1"},
          new Location[] {loc,   loc,   floc});
    } catch (NoSuchElementException e) {
      fail(e.getMessage());
    } catch (WrongPermissionException e) {
      fail(e.getMessage());
    } catch (NoSuchFileException e) {
      fail(e.getMessage());
    } catch (NotFileOrDirectoryException e) {
      fail(e.getMessage());
    } catch (CannotReadException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTokenIteratorNoSuchFile() {
    Location loc = new NoLocation();
    TokenIterator ti = null;

    try {
      test(ti = new TokenIterator(loc, "@<wrong-file>"),
          new String[] {""}, new Location[] {new NoLocation()});
      fail();
    } catch (NoSuchElementException e) {
      fail(e.getMessage());
    } catch (WrongPermissionException e) {
      fail(e.getMessage());
    } catch (NoSuchFileException e) {
      testInError(ti, e);
    } catch (NotFileOrDirectoryException e) {
      fail(e.getMessage());
    } catch (CannotReadException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTokenIteratorWrongPermission() throws IOException {
    Location loc = new NoLocation();
    TokenIterator ti = null;
    File file = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-2");
    file.deleteOnExit();
    file.setReadable(false);

    try {
      test(ti = new TokenIterator(loc, "@" + file.getAbsolutePath()),
          new String[] {""}, new Location[] {new NoLocation()});
      fail();
    } catch (NoSuchElementException e) {
      fail(e.getMessage());
    } catch (WrongPermissionException e) {
      testInError(ti, e);
    } catch (NoSuchFileException e) {
      fail(e.getMessage());
    } catch (NotFileOrDirectoryException e) {
      fail(e.getMessage());
    } catch (CannotReadException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTokenIteratorNotFile() {
    Location loc = new NoLocation();
    TokenIterator ti = null;
    File dir = Files.createTempDir();
    dir.deleteOnExit();

    try {
      test(ti = new TokenIterator(loc, "@" + dir.getAbsolutePath()),
          new String[] {""}, new Location[] {new NoLocation()});
      fail();
    } catch (NoSuchElementException e) {
      fail(e.getMessage());
    } catch (WrongPermissionException e) {
      fail(e.getMessage());
    } catch (NoSuchFileException e) {
      fail(e.getMessage());
    } catch (NotFileOrDirectoryException e) {
      testInError(ti, e);
    } catch (CannotReadException e) {
      fail(e.getMessage());
    }
  }


  @Test
  public void testTokenIteratorWithFiles() throws IOException {
    Location loc = new StringLocation("Default location");
    PrintStream printer;

    File file2 = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-3");
    file2.deleteOnExit();
    Location floc2 = new FileLocation(file2);
    printer = new PrintStream(file2);
    printer.print("2-1 2-2");
    printer.close();

    File file1_1 = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-4");
    file1_1.deleteOnExit();
    Location floc1_1 = new FileLocation(file1_1);
    printer = new PrintStream(file1_1);
    printer.print("@" + file2.getAbsolutePath() + " 1-1 1-2");
    printer.close();

    File file1_2 = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-5");
    file1_2.deleteOnExit();
    Location floc1_2 = new FileLocation(file1_2);
    printer = new PrintStream(file1_2);
    printer.print("1-1 @" + file2.getAbsolutePath() + " 1-2");
    printer.close();

    File file1_3 = File.createTempFile(TokenIteratorTest.class.getSimpleName(), "-6");
    file1_3.deleteOnExit();
    Location floc1_3 = new FileLocation(file1_3);
    printer = new PrintStream(file1_3);
    printer.print("1-1 1-2 @" + file2.getAbsolutePath());
    printer.close();

    try {
      test(
          new TokenIterator(loc, "@" + file1_1.getAbsolutePath()),
          new String[]   {"@" + file2.getAbsolutePath(),  "1-1",    "1-2"},
          new Location[] {floc1_1,                        floc1_1,  floc1_1});
      test(
          new TokenIterator(loc, "@" + file1_1.getAbsolutePath()).allowFileReferenceInFile(),
          new String[]   {"2-1",  "2-2",  "1-1",    "1-2"},
          new Location[] {floc2,  floc2,  floc1_1,  floc1_1});
      test(
          new TokenIterator(loc, "@" + file1_2.getAbsolutePath()).allowFileReferenceInFile(),
          new String[]   {"1-1",   "2-1",  "2-2", "1-2"},
          new Location[] {floc1_2, floc2,  floc2, floc1_2});
      test(
          new TokenIterator(loc, "@" + file1_3.getAbsolutePath()).allowFileReferenceInFile(),
          new String[]   {"1-1",   "1-2",   "2-1",  "2-2"},
          new Location[] {floc1_3, floc1_3, floc2,  floc2});
    } catch (NoSuchElementException e) {
      fail(e.getMessage());
    } catch (WrongPermissionException e) {
      fail(e.getMessage());
    } catch (NoSuchFileException e) {
      fail(e.getMessage());
    } catch (NotFileOrDirectoryException e) {
      fail(e.getMessage());
    } catch (CannotReadException e) {
      fail(e.getMessage());
    }
  }

  private void test(@Nonnull TokenIterator ti, @Nonnull String[] expectedArgs,
      @Nonnull Location[] expectedLocs) throws NoSuchElementException, WrongPermissionException,
      NoSuchFileException, NotFileOrDirectoryException, CannotReadException {
    assert expectedArgs.length == expectedLocs.length;

    assertNull(ti.getToken());
    assertNull(ti.getLocation());
    assertTrue(ti.hasNext());
    assertNull(ti.getToken());
    assertNull(ti.getLocation());

    for (int idx = 0; idx < expectedArgs.length; idx++) {
      assertTrue(ti.hasNext());

      assertEquals(expectedArgs[idx], ti.next());
      assertEquals(expectedArgs[idx], ti.getToken());
      assertEquals(expectedArgs[idx], ti.getToken());

      Location loc = ti.getLocation();
      if (loc instanceof LineLocation) {
        loc = ((LineLocation) loc).getSubLocation();
      }
      assertNotNull(loc);
      assertEquals(
          "for <" + expectedArgs[idx] + ">, expected: <" + expectedLocs[idx].getDescription()
              + "> but was: <" + loc.getDescription() + ">", expectedLocs[idx], loc);
    }
    assertFalse(ti.hasNext());
    assertFalse(ti.hasNext());
    assertEquals(expectedArgs[expectedArgs.length -  1], ti.getToken());

    try {
      ti.next();
      fail();
    } catch (NoSuchElementException e) {
    }

    assertFalse(ti.hasNext());
    assertNull(ti.getToken());
    assertNull(ti.getLocation());

    try {
      ti.next();
      fail();
    } catch (NoSuchElementException e) {
    }

    assertFalse(ti.hasNext());
    assertNull(ti.getToken());
    assertNull(ti.getLocation());
  }

  private void testInError(@Nonnull TokenIterator ti, @Nonnull Throwable expectedThrowable) {
    assertTrue(ti.hasNext());
    assertTrue(ti.hasNext());

    try {
      ti.getToken();
      fail();
    } catch (Exception e) {
      if (e.getClass() != expectedThrowable.getClass()) {
        fail();
      }
    }

    try {
      ti.next();
      fail();
    } catch (Exception e) {
      if (e.getClass() != expectedThrowable.getClass()) {
        fail();
      }
    }

    try {
      ti.getToken();
      fail();
    } catch (Exception e) {
      if (e.getClass() != expectedThrowable.getClass()) {
        fail();
      }
    }

    try {
      ti.next();
      fail();
    } catch (Exception e) {
      if (e.getClass() != expectedThrowable.getClass()) {
        fail();
      }
    }

    assertTrue(ti.hasNext());
  }
}
