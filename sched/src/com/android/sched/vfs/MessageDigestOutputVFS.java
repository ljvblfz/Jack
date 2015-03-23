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

import com.android.sched.util.collect.Lists;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An {@link OutputVFS} which generates a file with message digest of each file in a VFS.
 */
public class MessageDigestOutputVFS extends MessageDigestVFS implements OutputVFS {
  private boolean closed = false;
  @Nonnull
  protected OutputVFS vfs;
  @Nonnull
  private final MessageDigestOutputVDir root;
  @Nonnull
  private final MessageDigestFactory mdFactory;
  @Nonnull
  private final List<MessageDigestOutputVFile> files = new ArrayList<MessageDigestOutputVFile>();

  private class MessageDigestOutputVFile implements OutputVFile,
      Comparable<MessageDigestOutputVFile> {
    @Nonnull
    private final OutputVFile file;
    @Nonnull
    private final VPath path;
    @CheckForNull
    private String digest = null;

    protected MessageDigestOutputVFile(@Nonnull OutputVFile file, @Nonnull VPath path) {
      this.file = file;
      this.path = path;
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
    public OutputStream getOutputStream() throws WrongPermissionException {
      assert !isClosed();

      return new DigestOutputStream(file.getOutputStream(),
          MessageDigestOutputVFS.this.mdFactory.create()) {
        @Override
        public void close() throws IOException {
          super.close();
          MessageDigestOutputVFile.this.digest =
              new String(encode(getMessageDigest().digest()));
        }
      };
    }

    @Override
    @Nonnull
    public PrintStream getPrintStream() throws WrongPermissionException {
      return new PrintStream(getOutputStream());
    }

    @CheckForNull
    public String getDigest() {
      return digest;
    }

    @Nonnull
    public VPath getVPath() {
      return path;
    }

    @Override
    public int compareTo(MessageDigestOutputVFile other) {
      return path.getPathAsString('/').compareTo(other.getVPath().getPathAsString('/'));
    }

    @Override
    public final boolean equals(Object obj) {
      if (!(obj instanceof MessageDigestOutputVFile)) {
        return false;
      }

      return path.getPathAsString('/').equals(
          ((MessageDigestOutputVFile) obj).getVPath().getPathAsString('/'));
    }

    @Override
    public final int hashCode() {
      return path.getPathAsString('.').hashCode();
    }
  }

  private class MessageDigestOutputVDir implements OutputVDir {
    @Nonnull
    private final OutputVDir dir;

    protected MessageDigestOutputVDir (@Nonnull OutputVDir dir) {
      this.dir = dir;
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
    public synchronized MessageDigestOutputVFile createOutputVFile(@Nonnull VPath path)
        throws CannotCreateFileException {
      assert !isClosed();

      MessageDigestOutputVFile file =
          new MessageDigestOutputVFile(dir.createOutputVFile(path), path);
      addFile(file);

      return file;
    }
  }

  public MessageDigestOutputVFS(@Nonnull OutputVFS vfs,
      @Nonnull MessageDigestFactory mdFactory) {
    this.root = new MessageDigestOutputVDir(vfs.getRootOutputVDir());
    this.vfs = vfs;
    this.mdFactory = mdFactory;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getLocation();
  }

  @Override
  public synchronized void close() throws IOException {
    if (!closed) {
      List<MessageDigestOutputVFile> sortedFiles = Lists.<MessageDigestOutputVFile> sort(files);

      DigestOutputStream os =
          new DigestOutputStream(root.createOutputVFile(new VPath(DIGEST_DIRECTORY_NAME, '/'))
              .getOutputStream(), MessageDigestOutputVFS.this.mdFactory.create());
      PrintStream printer = new PrintStream(os);

      printer.println(mdFactory.getService().getAlgorithm());

      for (MessageDigestOutputVFile file : sortedFiles) {
        String digest = file.getDigest();
        if (digest != null) {
          printer.print(digest);
          printer.print(':');
          printer.print(file.getVPath().getPathAsString('/'));
          printer.println();
        }
      }

      printer.flush();

      String digest = new String(encode(os.getMessageDigest().digest()));
      printer.print(digest);
      printer.print(':' + DIGEST_DIRECTORY_NAME);
      printer.println();

      printer.close();

      vfs.close();
      closed = true;
    }
  }

  private synchronized boolean isClosed() {
    return closed;
  }

  @Override
  @Nonnull
  public MessageDigestOutputVDir getRootOutputVDir() {
    assert !isClosed();

    return root;
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  private synchronized void addFile(@Nonnull MessageDigestOutputVFile file) {
    files.add(file);
  }

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }
}
