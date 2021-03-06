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

package com.android.jack.shrob.obfuscation.resource;

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.LocationByteStreamSucker;
import com.android.sched.vfs.VFile;
import com.android.sched.vfs.VPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link VFile} wrapping another {@link VFile} with refining data.
 */
public class RefinedVFile implements VFile {

  @Nonnull
  private final VFile file;

  @Nonnull
  private final TreeSet<RefinedEntry> refinedEntries =
      new TreeSet<RefinedVFile.RefinedEntry>(new Comparator<RefinedEntry>() {

        @Override
        public int compare(@Nonnull RefinedEntry e1, @Nonnull RefinedEntry e2) {
          return e1.startPosition > e2.startPosition ? 1
              : (e1.endPosition < e2.startPosition ? -1 : 0);
        }

      });

  public RefinedVFile(@Nonnull VFile file) {
    this.file = file;
  }

  public void addRefinedEntry(@Nonnegative int startPosition, @Nonnegative int endPosition,
      @Nonnull CharSequence content) {
    refinedEntries.add(new RefinedEntry(startPosition, endPosition, content));
  }

  @Nonnull
  @Override
  public InputStream getInputStream() throws WrongPermissionException {
    InputStream inputStream = file.getInputStream();
    if (refinedEntries.isEmpty()) {
      return inputStream;
    }
    return new RefinedInputStream(inputStream);
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return file.getLocation();
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  private static class RefinedEntry {
    @Nonnegative
    private final int startPosition;

    @Nonnegative
    private final int endPosition;

    @Nonnull
    private final CharSequence content;

    private RefinedEntry(@Nonnegative int startPosition, @Nonnegative int endPosition,
        @Nonnull CharSequence content) {
      this.startPosition = startPosition;
      this.endPosition = endPosition;
      this.content = content;
    }

    @Nonnull
    public InputStream openRead() {
      return new ByteArrayInputStream(content.toString().getBytes());
    }

    @Override
    public String toString() {
      return startPosition + ":" + endPosition;
    }
  }

  private class RefinedInputStream extends InputStream {

    /**
     * The unrefined inputstream
     */
    @Nonnull
    private final InputStream baseInputStream;

    /**
     * The position in the baseInputStream
     */
    private int position = -1;

    /**
     * The current refined part of the stream
     */
    @CheckForNull
    private InputStream currentStream;

    /**
     * Current or next refined entry that we are reading or will be reading.
     */
    @CheckForNull
    private RefinedEntry currentRefinedEntry;

    /**
     * Iterator representing the position in the refinedEntries. Entries before this iterator have
     * already been read.
     */
    @Nonnull
    private final Iterator<RefinedEntry> refinedEntryIterator;

    public RefinedInputStream(@Nonnull InputStream baseInputStream) {
      this.baseInputStream = baseInputStream;
      refinedEntryIterator = RefinedVFile.this.refinedEntries.iterator();
      if (refinedEntryIterator.hasNext()) {
        currentRefinedEntry = refinedEntryIterator.next();
      }
    }

    @Override
    public int read() throws IOException {
      int inputData;
      if (currentStream != null) {
        // We are reading a refined entry
        inputData = currentStream.read();
        if (inputData == -1) {
          // The entry has already been completely read
          closeCurrentRefinedEntry();
        } else {
          return inputData;
        }
      }
      if (openNextRefinedEntryIfNecessary()) {
        assert currentStream != null;
        return currentStream.read();
      }
      // No refined entry was found, read the base stream normally.
      position++;
      return baseInputStream.read();
    }

    private boolean openNextRefinedEntryIfNecessary() {
      // Check that previous refined entry was completely read
      assert currentStream == null;
      if (currentRefinedEntry != null) {
        if (currentRefinedEntry.startPosition <= position + 1) {
          assert currentRefinedEntry.endPosition >= position;
          // A refined entry should be read
          currentStream = currentRefinedEntry.openRead();
          return true;
        }
      }
      return false;
    }

    private void closeCurrentRefinedEntry() throws IOException {
      // Reset current stream
      assert currentStream != null;
      currentStream.close();
      currentStream = null;

      assert currentRefinedEntry != null;
      // Set the base inputstream position to the refined entry end position
      int toSkip = currentRefinedEntry.endPosition - position;
      while (toSkip > 0) {
        assert baseInputStream.available() != 0;
        toSkip -= baseInputStream.skip(toSkip);
      }
      position = currentRefinedEntry.endPosition;

      // Next refined entry
      if (refinedEntryIterator.hasNext()) {
        currentRefinedEntry = refinedEntryIterator.next();
      } else {
        currentRefinedEntry = null;
      }
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (available() == 0) {
        return -1;
      }

      int totalRead = 0;
      while (available() != 0 && totalRead < len) {
        // Read current refined entry if any
        if (currentStream != null) {
          int read = currentStream.read(b, off + totalRead, len - totalRead);
          if (read > 0) {
            totalRead += read;
          }
          if (currentStream.available() == 0) {
            closeCurrentRefinedEntry();
          }
        }

        // Read until next refined entry
        // Calculate the length of bytes that can be read without refining
        int baseLength;
        if (currentRefinedEntry != null) {
          baseLength =
              Math.min(currentRefinedEntry.startPosition - (position + 1), len - totalRead);
        } else {
          baseLength = len - totalRead;
        }
        int read = baseInputStream.read(b, off + totalRead, baseLength);
        if (read > 0) {
          totalRead += read;
          position += read;
        }

        if (totalRead < len) {
          openNextRefinedEntryIfNecessary();
        }
      }

      return totalRead;
    }

    @Override
    public int available() throws IOException {
      int available = 0;
      if (currentStream != null) {
        available += currentStream.available();
      }
      if (baseInputStream.available() > 0) {
        available += 1;
      }
      return available;
    }

    @Override
    public void close() throws IOException {
      super.close();
      baseInputStream.close();
    }
  }

  @Override
  public void delete() throws CannotDeleteFileException {
    file.delete();
  }

  @Override
  @Nonnull
  public VPath getPath() {
    return file.getPath();
  }

  @Override
  @Nonnull
  public VPath getPathFromRoot() {
    return file.getPathFromRoot();
  }

  @Override
  @Nonnull
  public FileTime getLastModified() throws CannotGetModificationTimeException {
    return file.getLastModified();
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream(boolean append) {
    throw new UnsupportedOperationException();
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return null;
  }

  @Override
  public void copy(@Nonnull VFile vFile) throws WrongPermissionException,
      CannotCloseException, CannotReadException, CannotWriteException {

    try (InputStream is = getInputStream()) {
      try (OutputStream os = vFile.getOutputStream()) {
        new LocationByteStreamSucker(is, os, this, vFile).suck();
      } catch (IOException e) {
        throw new CannotCloseException(vFile, e);
      }
    } catch (IOException e) {
      throw new CannotCloseException(this, e);
    }
  }
}
