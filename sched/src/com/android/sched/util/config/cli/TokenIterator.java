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

import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.SchedIOException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.ColumnAndLineLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Iterator which tokenizes a list of {@link String}.
 *
 * This iterator manages implicitly references to files by automatically tokenizing the content of
 * these files.
 */
public class TokenIterator {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private static final Entry NULL = new Entry();
  private static final char  DEFAULT_FILE_PREFIX = '@';

  private char    filePrefix = DEFAULT_FILE_PREFIX;
  private boolean allowFileRefInArray = true;
  private boolean allowFileRefInFile  = false;

  @CheckForNull
  private Directory baseDirectory = null;

  @Nonnull
  private final String[] args;
  private       int      index = 0;

  @Nonnull
  private Entry next = NULL;
  @Nonnull
  private Entry current = NULL;
  @CheckForNull
  private SchedIOException pending = null;

  private class Sources {
    private class Source {
      @CheckForNull
      private final StreamTokenizer tokenizer;
      @Nonnull
      private final Location location;
      @CheckForNull
      private final InputStreamFile file;

      public Source(@Nonnull Location location) {
        this.tokenizer = null;
        this.file = null;
        this.location = location;
      }

      public Source(@Nonnull InputStreamFile file, @Nonnull StreamTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.file = file;
        this.location = file.getLocation();
      }
    }

    @Nonnull
    private final Stack<Source> stack = new Stack<Source>();

    public void push(@Nonnull Location location) {
      stack.push(new Source(location));
    }

    public void push(@Nonnull String fileName) throws WrongPermissionException,
        NoSuchFileException, NotFileException {
      InputStreamFile file = new InputStreamFile(baseDirectory, fileName);

      stack.push(new Source(file, getTokenizer(file)));
    }

    public void pop() {
      InputStreamFile file = stack.pop().file;

      if (file != null) {
        try {
          file.getInputStream().close();
        } catch (IOException e) {
          logger.log(Level.FINE, "Cannot close " + file.getLocation().getDescription());
        }
      }
    }

    public void clear() {
      while (!stack.isEmpty()) {
        pop();
      }
    }

    @Nonnull
    public Location getCurrentLocation() {
      return stack.peek().location;
    }

    @CheckForNull
    public StreamTokenizer getCurrentTokenizer() {
      return stack.peek().tokenizer;
    }
  }

  private final Sources sources = new Sources();

  public TokenIterator (@Nonnull Location location, @Nonnull String... args) {
    this.args = args.clone();
    sources.push(location);
  }

  @Nonnull
  public TokenIterator withFilePrefix(char filePrefix) {
    this.filePrefix = filePrefix;

    return this;
  }

  @Nonnull
  public TokenIterator allowFileReferenceInFile() {
    this.allowFileRefInFile = true;

    return this;
  }

  @Nonnull
  public TokenIterator withFileRelativeTo(@Nonnull File directory) throws NotDirectoryException,
      WrongPermissionException, NoSuchFileException {
    try {
      this.baseDirectory = new Directory(directory.getPath(), null, Existence.MUST_EXIST,
          Permission.EXECUTE, ChangePermission.NOCHANGE);
    } catch (CannotChangePermissionException e) {
      // we're not changing the permissions
      throw new AssertionError(e);
    } catch (FileAlreadyExistsException e) {
      // we're not creating the directory
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      // we're not creating the directory
      throw new AssertionError(e);
    }

    return this;
  }

  @Nonnull
  public TokenIterator disallowFileReferenceInArray() {
    this.allowFileRefInArray = false;

    return this;
  }

  /**
   * @return {@code true} if there is more tokens, {@code false} otherwise.
   */
  public boolean hasNext() {
    if (next == NULL) {
      try {
        next = getNext();
      } catch (NoSuchElementException e) {
        return false;
      } catch (SchedIOException e) {
        pending = e;
      }
    }

    return true;
  }

  /**
   * Return the next Token. It will become the current token.
   * In case of exceptions, the current token is discarded.
   *
   * @return the next token.
   */
  @Nonnull
  public String next()
      throws NoSuchElementException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException,
      CannotReadException {
    throwIfPending();

    if (next == NULL) {
      try {
        current = getNext();
      } catch (NoSuchElementException e) {
        current = NULL;
        throw e;
      } catch (SchedIOException e) {
        current = NULL;
        pending = e;
        throwIfPending();
        throw new AssertionError();
      }
    } else {
      current = next;
      next = NULL;
    }

    assert current.value != null;
    return current.value;
  }

  /**
   * @return the current token, or {@code null} if no current token exists.
   */
  @CheckForNull
  public String getToken() throws WrongPermissionException, NoSuchFileException,
      NotFileOrDirectoryException, CannotReadException {
    throwIfPending();

    return current.value;
  }

  /**
   * @return the location of the current token, or {@code null} if no current token exists.
   */
  @CheckForNull
  public Location getLocation() throws WrongPermissionException, NoSuchFileException,
      NotFileOrDirectoryException, CannotReadException {
    throwIfPending();

    return current.location;
  }

  private void throwIfPending() throws WrongPermissionException, NoSuchFileException,
      NotFileOrDirectoryException, CannotReadException {
    if (pending != null) {
      if (pending instanceof WrongPermissionException) {
        throw (WrongPermissionException) pending;
      } else if (pending instanceof NoSuchFileException) {
        throw (NoSuchFileException) pending;
      } else if (pending instanceof NotFileOrDirectoryException) {
        throw (NotFileOrDirectoryException) pending;
      } else if (pending instanceof CannotReadException) {
        throw (CannotReadException) pending;
      } else {
        throw new AssertionError();
      }
    }
  }

  @Nonnull
  private Entry getNext()
      throws NoSuchElementException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException,
      CannotReadException {

    while (true) {
      StreamTokenizer tokenizer = sources.getCurrentTokenizer();

      while (tokenizer != null) {
        try {
          if (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            if (allowFileRefInFile && (!tokenizer.sval.isEmpty())
                && tokenizer.sval.charAt(0) == filePrefix) {
              // If it is a @<file_name>, create a tokenizer with this file, and set it to current
              sources.push(tokenizer.sval.substring(1));
              tokenizer = sources.getCurrentTokenizer();
              continue;
            } else {
              // If the current tokenizer has a next, return it
              return new Entry(tokenizer.sval,
                  new ColumnAndLineLocation(sources.getCurrentLocation(), tokenizer.lineno()));
            }
          }
        } catch (IOException e) {
          try {
            throw new CannotReadException(sources.getCurrentLocation());
          } finally {
            // Stop the iterator
            sources.clear();
            sources.push(new NoLocation());
            index = args.length;
          }
        }

        // Else, go to the next one
        sources.pop();
        tokenizer = sources.getCurrentTokenizer();
      }

      // If the is no current tokenizer, switch to arg
      if (index >= args.length) {
        // If all args have been already returned, it is the end
        throw new NoSuchElementException();
      }

      // Else, analyze the next arg
      if (allowFileRefInArray && (!args[index].isEmpty()) && args[index].charAt(0) == filePrefix) {
        // If it is a @<file_name>, create a tokenizer with this file, and set it to current
        sources.push(args[index].substring(1));
        index++;
      } else {
        // Else, return the arg
        return new Entry(args[index++], sources.getCurrentLocation());
      }
    }
  }

  @Nonnull
  protected StreamTokenizer getTokenizer(@Nonnull InputStreamFile file) {
    StreamTokenizer tokenizer;

    Reader reader = new InputStreamReader(file.getInputStream());
    tokenizer = new StreamTokenizer(reader);

    tokenizer.resetSyntax();
    tokenizer.wordChars(0, 255);
    tokenizer.whitespaceChars(' ', ' ');
    tokenizer.whitespaceChars('\t', '\t');
    tokenizer.whitespaceChars('\n', '\n');
    tokenizer.whitespaceChars('\r', '\r');
    tokenizer.quoteChar('\'');
    tokenizer.quoteChar('\"');
    tokenizer.commentChar('#');
    tokenizer.eolIsSignificant(false);
    tokenizer.slashSlashComments(false);
    tokenizer.slashStarComments(false);
    tokenizer.lowerCaseMode(false);

    return tokenizer;
  }

  private static class Entry {
    @CheckForNull
    private final String value;
    @CheckForNull
    private final Location location;

    private Entry() {
      this(null, null);
    }

    private Entry(@CheckForNull String value, @CheckForNull Location location) {
      this.value = value;
      this.location = location;
    }
  }
}
