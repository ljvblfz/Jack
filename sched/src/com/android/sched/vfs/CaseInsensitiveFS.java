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

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.expression.LongExpression;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.IntegerPropertyId;
import com.android.sched.util.config.id.MessageDigestPropertyId;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.LineLocation;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.CaseInsensitiveFS.CaseInsensitiveVDir;
import com.android.sched.vfs.CaseInsensitiveFS.CaseInsensitiveVFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A filter implementation of a {@link VFS} which take a {@link VFS}, case insensitive or not, and
 * store in it files and directories with their name encoded through a digest algorithm. With that
 * filter, even if the real {@link VFS} is case insensitive, the filtered {@link VFS} acts as a case
 * sensitive {@link VFS}.
 */
@HasKeyId
public class CaseInsensitiveFS extends BaseVFS<CaseInsensitiveVDir, CaseInsensitiveVFile> implements
    VFS {
  static final String INDEX_NAME = "index";
  static final String DEBUG_NAME = "index.dbg";

  public static final IntegerPropertyId NB_GROUP = IntegerPropertyId
      .create("sched.vfs.case-insensitive.group.count",
          "Number of directory used to encode a path name").withMin(0).addDefaultValue(2);

  public static final IntegerPropertyId SZ_GROUP = IntegerPropertyId
      .create("sched.vfs.case-insensitive.group.size",
          "Number of letters in directory name used to encode a path name")
      .requiredIf(NB_GROUP.getValue().isGreater(LongExpression.getConstant(0))).withMin(0)
      .addDefaultValue(2);

  @Nonnull
  public static final MessageDigestPropertyId ALGO = MessageDigestPropertyId.create(
      "sched.vfs.case-insensitive.algo", "Algorithm used to encode a path name").addDefaultValue(
      "SHA");

  @Nonnull
  public static final BooleanPropertyId DEBUG = BooleanPropertyId.create(
      "sched.vfs.case-insensitive.debug",
      "generate an index file '" + DEBUG_NAME + "' for debugging purpose").addDefaultValue(false);

  private final int nbGroup = ThreadConfig.get(NB_GROUP).intValue();
  private final int szGroup = ThreadConfig.get(SZ_GROUP).intValue();
  private final MessageDigestFactory mdf = ThreadConfig.get(ALGO);
  private final boolean debug = ThreadConfig.get(DEBUG).booleanValue();

  @Nonnull
  private final CaseInsensitiveVDir root = new CaseInsensitiveVDir(this, null, "");

  @Override
  @Nonnull
  public String getDescription() {
    return "case insensitive wrapper";
  }

  static class CaseInsensitiveVDir extends InMemoryVDir {
    @CheckForNull
    protected final VDir parent;

    CaseInsensitiveVDir(
        @Nonnull BaseVFS<? extends InMemoryVDir, ? extends CaseInsensitiveVFile> vfs,
        @CheckForNull VDir parent, @Nonnull String name) {
      super(vfs, name);
      this.parent = parent;
    }

    @Override
    @Nonnull
    public VPath getPath() {
      if (parent != null) {
        return parent.getPath().clone().appendPath(new VPath(name, '/'));
      } else {
        return VPath.ROOT;
      }
    }
  }

  static class CaseInsensitiveVFile extends ParentVFile {
    @CheckForNull
    private BaseVFile encodedFile;

    CaseInsensitiveVFile(
        @Nonnull BaseVFS<? extends InMemoryVDir, ? extends CaseInsensitiveVFile> vfs,
        @Nonnull VDir parent, @Nonnull String name) {
      super(vfs, parent, name);
    }

    void setEncodedFile(@Nonnull BaseVFile encodedFile) {
      this.encodedFile = encodedFile;
    }

    @Nonnull
    BaseVFile getEncodedFile() {
      assert encodedFile != null;

      return encodedFile;
    }
  }

  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;

  @SuppressWarnings("unchecked")
  public CaseInsensitiveFS(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs)
      throws WrongVFSFormatException {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;

    LineNumberReader reader = null;
    VFile file = null;
    try {
      try {
        file = vfs.getRootDir().getVFile(INDEX_NAME);
      } catch (NoSuchFileException e) {
        if (!vfs.getRootDir().isEmpty()) {
          // If VFS is not empty, index file is missing
          throw new WrongVFSFormatException(this, vfs.getLocation(), e);
        }

        return;
      } catch (NotFileException e) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), e);
      }

      try {
        reader = new LineNumberReader(new InputStreamReader(file.openRead()));
      } catch (WrongPermissionException e) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), e);
      }

      String line;
      try {
        while ((line = reader.readLine()) != null) {
          if (line.charAt(1) != ':') {
            throw new WrongVFSFormatException(this, vfs.getLocation(),
                new WrongFileFormatException(new LineLocation(file.getLocation(),
                    reader.getLineNumber())));
          }

          char type = line.charAt(0);
          switch (type) {
            case 'd':
              root.createVDir(new VPath(line.substring(3), '/'));
              break;
            case 'f':
              root.createVFile(new VPath(line.substring(3), '/'));
              break;
            default:
              throw new WrongVFSFormatException(this, vfs.getLocation(),
                  new WrongFileFormatException(new LineLocation(file.getLocation(),
                      reader.getLineNumber())));
          }
        }
      } catch (CannotCreateFileException e) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), e);
      } catch (IOException e) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), e);
      }
    } finally {
      if (reader != null) {
        assert file != null;

        try {
          reader.close();
        } catch (IOException e) {
          // Ignore
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
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Override
  @Nonnull
  public CaseInsensitiveVDir getRootDir() {
    return root;
  }

  @Override
  public synchronized void close() throws IOException {
    if (!closed) {
      PrintStream printer = new PrintStream(vfs.getRootDir().createVFile(INDEX_NAME).openWrite());

      printIndex(printer, getRootDir());
      printer.flush();
      printer.close();

      if (debug) {
        printer = new PrintStream(vfs.getRootDir().createVFile(DEBUG_NAME).openWrite());

        printDebug(printer, getRootDir());
        printer.flush();
        printer.close();
      }

      vfs.close();
      closed = true;
    }
  }

  private void printIndex(@Nonnull PrintStream printer, @Nonnull InMemoryVDir dir) {
    Collection<? extends BaseVElement> elements = dir.list();
    if (elements.size() > 0) {
      for (BaseVElement element : elements) {
        if (element.isVDir()) {
          printIndex(printer, (InMemoryVDir) element);
        } else {
          CaseInsensitiveVFile file = (CaseInsensitiveVFile) element;

          printer.print("f:");
          printer.print(file.getPath().getPathAsString('/'));
          printer.println();
        }
      }
    } else {
      printer.print("d:");
      printer.print(dir.getPath().getPathAsString('/'));
      printer.println();
    }
  }

  private void printDebug(@Nonnull PrintStream printer, @Nonnull InMemoryVDir dir) {
    Collection<? extends BaseVElement> elements = dir.list();

    printer.print("d:");
    printer.print(dir.getPath().getPathAsString(File.separatorChar));
    printer.println();

    for (BaseVElement element : elements) {
      if (element.isVDir()) {
        printDebug(printer, (InMemoryVDir) element);
      } else {
        CaseInsensitiveVFile file = (CaseInsensitiveVFile) element;

        printer.print("f:");
        printer.print(file.getEncodedFile().getPath().getPathAsString(File.separatorChar));
        printer.print(":");
        printer.print(file.getPath().getPathAsString(File.separatorChar));
        printer.println();
      }
    }
  }

  //
  // Stream
  //

  @Override
  @Nonnull
  InputStream openRead(@Nonnull CaseInsensitiveVFile file) throws WrongPermissionException {
    assert !isClosed();

    return file.getEncodedFile().openRead();
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull CaseInsensitiveVFile file) throws WrongPermissionException {
    assert !isClosed();

    return file.getEncodedFile().openWrite();
  }

  //
  // VElement
  //

  @Override
  @Nonnull
  CaseInsensitiveVDir getVDir(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  CaseInsensitiveVFile getVFile(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  CaseInsensitiveVDir createVDir(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name) {
    assert !isClosed();

    return new CaseInsensitiveVDir(this, parent, name);
  }

  @Override
  @Nonnull
  CaseInsensitiveVFile createVFile(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    assert !isClosed();

    CaseInsensitiveVFile original = new CaseInsensitiveVFile(this, parent, name);
    BaseVFile encoded = vfs.getRootDir().createVFile(encode(original.getPath()));
    original.setEncodedFile(encoded);

    return original;
  }

  @Override
  @Nonnull
  void delete(@Nonnull CaseInsensitiveVFile file) throws CannotDeleteFileException {
    assert !isClosed();

    try {
      BaseVFile encoded = vfs.getRootDir().getVFile(encode(file.getPath()));
      vfs.delete(encoded);
    } catch (NotDirectoryException e) {
      throw new CannotDeleteFileException(file.getLocation());
    } catch (NotFileException e) {
      throw new CannotDeleteFileException(file.getLocation());
    } catch (NoSuchFileException e) {
      throw new CannotDeleteFileException(file.getLocation());
    }
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull CaseInsensitiveVDir dir) {
    throw new UnsupportedOperationException();
  }

  @Override
  boolean isEmpty(@Nonnull CaseInsensitiveVDir dir) {
    throw new UnsupportedOperationException();
  }

  //
  // Location
  //

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull CaseInsensitiveVFile file) {
    return vfs.getVFileLocation(file.getEncodedFile());
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name) {
    return vfs.getRootDir().getVFileLocation(
        encode(parent.getPath().clone().appendPath(new VPath(name, '/'))));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull CaseInsensitiveVDir dir) {
    return vfs.getRootDir().getVDirLocation(encode(dir.getPath()));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name) {
    return vfs.getRootDir().getVDirLocation(
        encode(parent.getPath().clone().appendPath(new VPath(name, '/'))));
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull CaseInsensitiveVDir parent, @Nonnull VPath path) {
    return vfs.getRootDir().getVFileLocation(encode(parent.getPath().clone().appendPath(path)));
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull CaseInsensitiveVDir parent, @Nonnull VPath path) {
    return vfs.getRootDir().getVDirLocation(encode(parent.getPath().clone().appendPath(path)));
  }

  //
  // Misc
  //

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }

  //
  // Encode / Decode
  //

  @Nonnull
  private VPath encode(@Nonnull VPath path) {
    char[] digest = encode(mdf.create().digest(path.getPathAsString('/').getBytes()));

    StringBuffer sb = new StringBuffer();
    int idx = 0;
    try {
      for (int groupIdx = 0; groupIdx < nbGroup; groupIdx++) {
        for (int letterIdx = 0; letterIdx < szGroup; letterIdx++) {
          sb.append(digest[idx++]);
        }
        sb.append('/');
      }

      if (idx < digest.length) {
        sb.append(digest, idx, digest.length - idx);
      } else {
        // Remove the last /, it is not a directory here
        sb.setLength(sb.length() - 1);
      }
    } catch (IndexOutOfBoundsException e) {
    }


    return new VPath(sb.toString(), '/');
  }

  @Nonnull
  private static final byte[] code = "0123456789ABCDEF".getBytes();

  @Nonnull
  static char[] encode(@Nonnull byte[] bytes) {
    char[] array = new char[bytes.length * 2];

    for (int idx = 0; idx < bytes.length; idx++) {
      array[(idx << 1)] = (char) code[(bytes[idx] & 0xF0) >> 4];
      array[(idx << 1) + 1] = (char) code[(bytes[idx] & 0x0F)];
    }

    return array;
  }
}
