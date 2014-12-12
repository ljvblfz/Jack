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

package com.android.jack.shrob;

import com.android.sched.util.log.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public abstract class AbstractListingComparator {
  protected static interface Readable {
    @Nonnull
    BufferedReader openReader() throws IOException;
  }

  protected static class FileReadable implements Readable {

    @Nonnull
    private final File input;

    protected FileReadable(@Nonnull File input) {
      this.input = input;
    }

    @Nonnull
    @Override
    public BufferedReader openReader() throws IOException {
      return new BufferedReader(new InputStreamReader(new FileInputStream(input)));
    }

  }

  protected static class StringReadable implements Readable {

    @Nonnull
    private final String input;

    protected StringReadable(@Nonnull String input) {
      this.input = input;
    }

    @Nonnull
    @Override
    public BufferedReader openReader() {
      return new BufferedReader(new StringReader(input));
    }

  }

  protected static class ParseException extends IOException {

    private static final long serialVersionUID = 1L;

    public ParseException() {
    }

    public ParseException(@CheckForNull String message) {
      super(message);
    }

    public ParseException(@CheckForNull Throwable cause) {
      super(cause);
    }

    public ParseException(@CheckForNull String message, @CheckForNull Throwable cause) {
      super(message, cause);
    }

  }

  private boolean differenceFound = false;

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  protected boolean differenceFound() {
    return differenceFound;
  }

  @Nonnull
  protected BufferedReader createStreamReader(@Nonnull File file) throws FileNotFoundException {
    return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
  }

  protected static boolean findLine(@Nonnull String searchedLine, @Nonnull BufferedReader reader)
      throws IOException {
    String line = reader.readLine();
    while (line != null) {
      if (line.equals(searchedLine)) {
        return true;
      }
      line = reader.readLine();
    }
    return false;
  }

  protected void missingType(@Nonnull String typeName, boolean missingInReference) {
    differenceFound = true;
    String location = missingInReference ? "reference" : "candidate";
    logger.log(Level.SEVERE, "Type {0} not found in {1}", new Object[] {typeName, location});
  }

  protected void missingMember(@Nonnull String memberName, @Nonnull String enclosingTypeName,
      boolean missingInReference) {
    differenceFound = true;
    String location = missingInReference ? "reference" : "candidate";
    logger.log(Level.SEVERE, "Member {0} in {1} not found in {2}",
        new Object[] {memberName, enclosingTypeName, location});
  }
}
