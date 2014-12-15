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

import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An {@link InputOutputVFS} which generated a file with message digest of each file in a VFS.
 */
public class MessageDigestInputOutputVFS implements InputOutputVFS {
  private boolean closed = false;
  @Nonnull
  protected OutputVFS vfs;
  @Nonnull
  private final MessageDigestInputOutputVDir root;
  @Nonnull
  private final MessageDigestFactory mdFactory;

  private class MessageDigestOutputVFile implements OutputVFile {
    @Nonnull
    private final OutputVFile file;
    @Nonnull
    private final VPath path;
    @CheckForNull
    private byte[] digest;

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
    public OutputStream openWrite() throws IOException {
      assert !closed;

      return new DigestOutputStream(file.openWrite(),
          MessageDigestInputOutputVFS.this.mdFactory.create()) {
        @Override
        public void close() throws IOException {
          super.close();
          MessageDigestOutputVFile.this.digest = getMessageDigest().digest();
        }
      };
    }

    @CheckForNull
    public byte[] getDigest() {
      return digest;
    }

    @Nonnull
    public VPath getVPath() {
      return path;
    }
  }

  private class MessageDigestInputOutputVDir implements InputOutputVDir {
    @Nonnull
    private final InputOutputVDir dir;
    @Nonnull
    private final List<MessageDigestOutputVFile> files = new ArrayList<MessageDigestOutputVFile>();

    protected MessageDigestInputOutputVDir (@Nonnull InputOutputVDir dir) {
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
    public synchronized OutputVFile createOutputVFile(@Nonnull VPath path)
        throws CannotCreateFileException {
      assert !closed;

      MessageDigestOutputVFile file =
          new MessageDigestOutputVFile(dir.createOutputVFile(path), path);
      files.add(file);

      return file;
    }

    private synchronized void close() throws CannotCreateFileException, IOException {
      OutputStream os = dir.createOutputVFile(new VPath("jack.sha1", '/')).openWrite();
      PrintStream printer = new PrintStream(os);

      for (MessageDigestOutputVFile file : files) {
        byte[] digest = file.getDigest();
        if (digest != null) {
          printer.print(file.getVPath().getPathAsString('/'));
          printer.print(':');
          printer.print(encode(digest));
          printer.println();
        }
      }

      printer.close();
    }

    @Override
    @Nonnull
    public Collection<? extends InputVElement> list() {
      assert !closed;

      return dir.list();
    }

    @Override
    @Nonnull
    public InputVDir getInputVDir(@Nonnull VPath path) throws NotFileOrDirectoryException,
        NoSuchFileException {
      assert !closed;

      return dir.getInputVDir(path);
    }

    @Override
    @Nonnull
    public InputVFile getInputVFile(@Nonnull VPath path) throws NotFileOrDirectoryException,
        NoSuchFileException {
      assert !closed;

      return dir.getInputVFile(path);
    }

    @Override
    @Nonnull
    public void delete(@Nonnull VPath path) throws CannotDeleteFileException {
      assert !closed;

      dir.delete(path);
    }
  }

  public MessageDigestInputOutputVFS(@Nonnull InputOutputVFS vfs,
      @Nonnull MessageDigestFactory mdFactory) {
    this.root = new MessageDigestInputOutputVDir(vfs.getRootInputOutputVDir());
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
      root.close();
      vfs.close();
      closed = true;
    }
  }

  @Override
  @Nonnull
  public OutputVDir getRootOutputVDir() {
    assert !closed;

    return root;
  }

  @Override
  @Nonnull
  public InputVDir getRootInputVDir() {
    assert !closed;

    return root;
  }

  @Override
  @Nonnull
  public InputOutputVDir getRootInputOutputVDir() {
    assert !closed;

    return root;
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Nonnull
  private static final byte[] code = "0123456789ABCDEF".getBytes();
  @Nonnull
  private static char[] encode(@Nonnull byte[] bytes) {
    char[] array = new char[bytes.length * 2];

    for (int idx = 0; idx < bytes.length; idx++) {
      array[(idx << 1)    ] = (char) code[(bytes[idx] & 0xF0) >> 4];
      array[(idx << 1) + 1] = (char) code[(bytes[idx] & 0x0F)     ];
    }

    return array;
  }
}
