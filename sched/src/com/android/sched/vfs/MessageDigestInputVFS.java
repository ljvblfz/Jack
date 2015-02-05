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

package com.android.sched.vfs;

import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An {@link InputVFS} which read a file with message digest of each file in the VFS.
 */
public class MessageDigestInputVFS extends MessageDigestVFS implements InputVFS {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  protected InputVFS vfs;
  @Nonnull
  private final MessageDigestInputVDir root;
  @Nonnull
  private final Map<VPath, String> digests = new HashMap<VPath, String>();
  @CheckForNull
  private String algorithm = null;
  @CheckForNull
  private String digest = null;

  /**
   * An {@link InputVFile} which have a message digest.
   */
  public static class MessageDigestInputVFile implements InputVFile {
    @Nonnull
    private final InputVFile file;
    @CheckForNull
    private final String digest;

    public MessageDigestInputVFile(@Nonnull InputVFile file, @CheckForNull String digest) {
      this.file = file;
      this.digest = digest;
    }

    @Override
    public boolean isVDir() {
      return false;
    }

    @Override
    @Nonnull
    public String getName() {
      return file.getName();
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return file.getLocation();
    }

    @Override
    @Nonnull
    public InputStream openRead() throws IOException {
      return file.openRead();
    }

    @CheckForNull
    public String getDigest() {
      return digest;
    }

    @Override
    public void delete() {
      // This implementation is obsolete anyway
      throw new UnsupportedOperationException();
    }
  }

  /**
   * An {@link InputVDir} which return {@link MessageDigestInputVFile}.
   */
  public class MessageDigestInputVDir implements InputVDir {
    @Nonnull
    private final InputVDir dir;
    @Nonnull
    private final VPath pathToRoot;

    private MessageDigestInputVDir (@Nonnull InputVDir dir, @Nonnull VPath pathToRoot) {
      this.dir = dir;
      this.pathToRoot = pathToRoot;
    }

    @Override
    public boolean isVDir() {
      return true;
    }

    @Override
    @Nonnull
    public String getName() {
      return dir.getName();
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return dir.getLocation();
    }

    @Override
    @Nonnull
    public Collection<? extends InputVElement> list() {
      return dir.list();
    }

    @Override
    @Nonnull
    public MessageDigestInputVDir getInputVDir(@Nonnull VPath path)
        throws NotDirectoryException, NoSuchFileException {
      VPath newPathToRoot = pathToRoot.clone();
      newPathToRoot.appendPath(path);
      return new MessageDigestInputVDir(dir.getInputVDir(path), newPathToRoot);
    }

    @Override
    @Nonnull
    public MessageDigestInputVFile getInputVFile(@Nonnull VPath path)
        throws NoSuchFileException, NotFileOrDirectoryException {
      VPath filePathToRoot = pathToRoot.clone();
      filePathToRoot.appendPath(path);
      return new MessageDigestInputVFile(dir.getInputVFile(path), digests.get(filePathToRoot));
    }
 }

  public MessageDigestInputVFS(@Nonnull InputVFS vfs) {
    this.root = new MessageDigestInputVDir(vfs.getRootInputVDir(), VPath.ROOT);
    this.vfs  = vfs;

    // Reading digest directory is best effort
    // If there is one error, let's skip that step
    BufferedReader in = null;
    InputVFile     file = null;
    try {
      try {
        file = root.getInputVFile(new VPath(DIGEST_DIRECTORY_NAME, '/'));
      } catch (NotFileOrDirectoryException e) {
        logger.log(Level.WARNING, "Cannot open '" + DIGEST_DIRECTORY_NAME + "' file in {0}", vfs
            .getLocation().getDescription());
        logger.log(Level.WARNING, "Stacktrace", e);
        return;
      } catch (NoSuchFileException e) {
        logger.log(Level.WARNING, "Cannot open '" + DIGEST_DIRECTORY_NAME + "' file in {0}", vfs
            .getLocation().getDescription());
        logger.log(Level.WARNING, "Stacktrace", e);
        return;
      }

      try {
        in = new BufferedReader(new InputStreamReader(file.openRead()));
      } catch (IOException e) {
        logger.log(Level.WARNING, "Cannot open {0}", file.getLocation().getDescription());
        logger.log(Level.WARNING, "Stacktrace", e);
        return;
      }

      try {
        algorithm = in.readLine();

        String line;
        while ((line = in.readLine()) != null) {
          int index = line.indexOf(':');
          if (index < 1) {
            logger.log(Level.WARNING, "Bad format in {0}", file.getLocation().getDescription());
            continue;
          }

          digests.put(new VPath(line.substring(index + 1), '/'), line.substring(0, index));
        }
      } catch (IOException e) {
        logger.log(Level.WARNING, "Error reading {0}", file.getLocation().getDescription());
        return;
      }

      digest = digests.get(new VPath(MessageDigestOutputVFS.DIGEST_DIRECTORY_NAME, '/'));
    } finally {
      if (in != null) {
        assert file != null;

        try {
          in.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Cannot close {0}", file.getLocation().getDescription());
        }
      }
    }
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getLocation();
  }

  @Override
  public synchronized void close() throws IOException {
    vfs.close();
  }

  @Override
  @Nonnull
  public MessageDigestInputVDir getRootInputVDir() {
    return root;
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @CheckForNull
  public String getDigestAlgorithm() {
    return algorithm;
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return digest;
  }
}
