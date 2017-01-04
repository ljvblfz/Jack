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

import com.google.common.base.Splitter;

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.expression.LongExpression;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.IntegerPropertyId;
import com.android.sched.util.config.id.MessageDigestPropertyId;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.ColumnAndLineLocation;
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
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
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
          "Number of directory used to encode a path name").withMin(0).addDefaultValue(1);

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

  @Nonnull
  private static final char INDEX_SEPARATOR = '/';

  @Nonnull
  private static final Splitter splitter = Splitter.on(INDEX_SEPARATOR);

  @Nonnegative
  private final int numGroups;
  @Nonnegative
  private final int groupSize;
  @Nonnull
  private final MessageDigestFactory mdf;
  private final boolean debug;

  @Nonnull
  private final CaseInsensitiveVDir root = new CaseInsensitiveVDir(this, null, "");

  @Nonnull
  private final Set<Capabilities> capabilities;

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

    @Override
    @Nonnull
    public BaseVFile getVFile(@Nonnull String name) throws NoSuchFileException,
        NotFileException {
      return vfs.getVFile(this, name);
    }

    @Override
    @Nonnull
    public BaseVDir getVDir(@Nonnull String name) throws NotDirectoryException,
        NoSuchFileException {
      return vfs.getVDir(this, name);
    }

    @Override
    @Nonnull
    public BaseVFile createVFile(@Nonnull String name) throws CannotCreateFileException {
      return vfs.createVFile(this, name);
    }

    @Override
    @Nonnull
    public BaseVDir createVDir(@Nonnull String name) throws CannotCreateFileException {
      return vfs.createVDir(this, name);
    }

    @Override
    @Nonnull
    public Collection<? extends BaseVElement> list() {
      return vfs.list(this);
    }

    @CheckForNull
    public VDir getParent() {
      return parent;
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

    @Override
    public void delete() throws CannotDeleteFileException {
      vfs.delete(this);
    }

    public void deleteFromCache() {
      ((InMemoryVDir) parent).internalDelete(name);
    }
  }

  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;

  public CaseInsensitiveFS(@Nonnull VFS vfs) throws BadVFSFormatException {
    this(vfs, ThreadConfig.get(NB_GROUP).intValue(), ThreadConfig.get(SZ_GROUP).intValue(),
        ThreadConfig.get(ALGO), ThreadConfig.get(DEBUG).booleanValue());
  }

  @SuppressWarnings("unchecked")
  public CaseInsensitiveFS(@Nonnull VFS vfs, int numGroups, int groupSize,
      @Nonnull MessageDigestFactory mdf, boolean debug)
      throws BadVFSFormatException {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;

    Set<Capabilities> capabilities = EnumSet.copyOf(vfs.getCapabilities());
    capabilities.add(Capabilities.CASE_SENSITIVE);
    capabilities.add(Capabilities.UNIQUE_ELEMENT);
    this.capabilities = Collections.unmodifiableSet(capabilities);

    this.numGroups = numGroups;
    this.groupSize = groupSize;
    this.mdf = mdf;
    this.debug = debug;

    initVFS();
  }

  private void initVFS() throws BadVFSFormatException {
    LineNumberReader reader = null;
    VFile file = null;
    try {
      try {
        file = vfs.getRootDir().getVFile(INDEX_NAME);
      } catch (NoSuchFileException e) {
        if (!vfs.getRootDir().isEmpty()) {
          // If VFS is not empty, index file is missing
          throw new BadVFSFormatException(this, vfs.getLocation(), e);
        }

        return;
      } catch (NotFileException e) {
        throw new BadVFSFormatException(this, vfs.getLocation(), e);
      }

      try {
        reader = new LineNumberReader(new InputStreamReader(file.getInputStream()));
      } catch (WrongPermissionException e) {
        throw new BadVFSFormatException(this, vfs.getLocation(), e);
      }

      String line;
      try {
        while ((line = reader.readLine()) != null) {
          if (line.charAt(1) != ':') {
            throw new BadVFSFormatException(this, vfs.getLocation(),
                new WrongFileFormatException(new ColumnAndLineLocation(file.getLocation(),
                    reader.getLineNumber())));
          }

          char type = line.charAt(0);
          switch (type) {
            case 'd':
              loadVDir(line.substring(2));
              break;
            case 'f':
              loadVFile(line.substring(2));
              break;
            default:
              throw new BadVFSFormatException(this, vfs.getLocation(),
                  new WrongFileFormatException(new ColumnAndLineLocation(file.getLocation(),
                      reader.getLineNumber())));
          }
        }
      } catch (NotDirectoryException | NotFileException | NoSuchFileException e) {
        throw new BadVFSFormatException(this, vfs.getLocation(), e);
      } catch (IOException e) {
        throw new BadVFSFormatException(this, vfs.getLocation(), e);
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

  private void loadVDir(@Nonnull String path) throws NotDirectoryException {
    CaseInsensitiveVDir currentDir = getRootDir();
    Iterator<String> pathElementIterator = splitter.split(path).iterator();
    String pathElement = null;
    while (pathElementIterator.hasNext()) {
      pathElement = pathElementIterator.next();
      assert !pathElement.isEmpty();
      currentDir = loadVDir(currentDir, pathElement);
    }
  }

  private void loadVFile(@Nonnull String path)
      throws NotDirectoryException, NotFileException, NoSuchFileException {
    CaseInsensitiveVDir currentDir = getRootDir();
    Iterator<String> pathElementIterator = splitter.split(path).iterator();
    String pathElement = null;
    while (pathElementIterator.hasNext()) {
      pathElement = pathElementIterator.next();
      assert !pathElement.isEmpty();
      if (pathElementIterator.hasNext()) {
        // simpleName is a dir name
        currentDir = loadVDir(currentDir, pathElement);
      }
    }
    loadVFile(currentDir, pathElement);
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return capabilities;
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
  public synchronized void close() throws CannotCloseException {
    if (!closed) {
      try {
        PrintStream printer =
            new PrintStream(vfs.getRootDir().createVFile(INDEX_NAME).getOutputStream());

        printIndex(printer, getRootDir());
        printer.flush();
        printer.close();

        if (debug) {
          printer = new PrintStream(vfs.getRootDir().createVFile(DEBUG_NAME).getOutputStream());

          printDebug(printer, getRootDir());
          printer.flush();
          printer.close();
        }
      } catch (WrongPermissionException | CannotCreateFileException e) {
        throw new CannotCloseException(this, e);
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
          printer.print(file.getPath().getPathAsString(INDEX_SEPARATOR));
          printer.println();
        }
      }
    } else {
      printer.print("d:");
      printer.print(dir.getPath().getPathAsString(INDEX_SEPARATOR));
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

    return file.getEncodedFile().getInputStream();
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull CaseInsensitiveVFile file) throws WrongPermissionException {
    return openWrite(file, false);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull CaseInsensitiveVFile file, boolean append)
      throws WrongPermissionException {
    assert !isClosed();

    return file.getEncodedFile().getOutputStream(append);
  }

  //
  // VElement
  //

  @Override
  @Nonnull
  CaseInsensitiveVDir getVDir(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name)
      throws NotDirectoryException, NoSuchFileException {
    CaseInsensitiveVDir vDir = getVDirFromCache(parent, name);
    if (vDir != null) {
      return vDir;
    } else {
      throw new NoSuchFileException(getVDirLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  CaseInsensitiveVFile getVFile(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name)
      throws NotFileException, NoSuchFileException {
    CaseInsensitiveVFile vFile = getVFileFromCache(parent, name);
    if (vFile != null) {
      return vFile;
    } else {
      throw new NoSuchFileException(getVFileLocation(parent, name));
    }
  }

  @CheckForNull
  CaseInsensitiveVFile getVFileFromCache(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name)
      throws NotFileException {
    BaseVElement element = parent.getFromCache(name);
    if (element == null) {
      return null;
    } else if (!element.isVDir()) {
      return (CaseInsensitiveVFile) element;
    } else {
      throw new NotFileException(getVFileLocation(parent, name));
    }
  }

  @CheckForNull
  CaseInsensitiveVDir getVDirFromCache(@Nonnull CaseInsensitiveVDir parent, @Nonnull String name)
      throws NotDirectoryException {
    BaseVElement element = parent.getFromCache(name);
    if (element == null) {
      return null;
    } else if (element.isVDir()) {
      return (CaseInsensitiveVDir) element;
    } else {
      throw new NotDirectoryException(getVDirLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  CaseInsensitiveVDir createVDir(@Nonnull CaseInsensitiveVDir parent,
      @Nonnull String name) throws CannotCreateFileException {
    assert !isClosed();

    try {
      return loadVDir(parent, name);
    } catch (NotDirectoryException e) {
      throw new CannotCreateFileException(getVDirLocation(parent, name));
    }
  }

  @Override
  @Nonnull
  synchronized CaseInsensitiveVFile createVFile(
      @Nonnull CaseInsensitiveVDir parent, @Nonnull String name) throws CannotCreateFileException {
    assert !isClosed();
    try {
      CaseInsensitiveVFile vFile = getVFileFromCache(parent, name);
      if (vFile != null) {
        return vFile;
      } else {
        CaseInsensitiveVFile original = new CaseInsensitiveVFile(this, parent, name);
        BaseVFile encoded = vfs.getRootDir().createVFile(encode(original.getPath()));
        original.setEncodedFile(encoded);
        parent.putInCache(name, original);

        return original;
      }
    } catch (NotFileException e) {
      throw new CannotCreateFileException(getVFileLocation(parent, name));
    }
  }

  @Nonnull
  synchronized CaseInsensitiveVDir loadVDir(
      @Nonnull CaseInsensitiveVDir parent, @Nonnull String name) throws NotDirectoryException {
    assert !isClosed();

    CaseInsensitiveVDir vDir = getVDirFromCache(parent, name);
    if (vDir != null) {
      return vDir;
    } else {
      CaseInsensitiveVDir dir = new CaseInsensitiveVDir(this, parent, name);
      parent.putInCache(name, dir);
      return dir;
    }
  }

  @Nonnull
  synchronized CaseInsensitiveVFile loadVFile(
      @Nonnull CaseInsensitiveVDir parent, @Nonnull String name)
      throws NotFileException, NotDirectoryException, NoSuchFileException {
    assert !isClosed();
    CaseInsensitiveVFile vFile = getVFileFromCache(parent, name);
    if (vFile != null) {
      return vFile;
    } else {
      CaseInsensitiveVFile original = new CaseInsensitiveVFile(this, parent, name);
      BaseVFile encoded = vfs.getRootDir().getVFile(encode(original.getPath()));
      original.setEncodedFile(encoded);
      parent.putInCache(name, original);

      return original;
    }
  }

  @Override
  @Nonnull
  void delete(@Nonnull CaseInsensitiveVFile file) throws CannotDeleteFileException {
    assert !isClosed();

    try {
      BaseVFile encoded = vfs.getRootDir().getVFile(encode(file.getPath()));
      vfs.delete(encoded);
      file.deleteFromCache();
    } catch (NotDirectoryException e) {
      throw new CannotDeleteFileException(file);
    } catch (NotFileException e) {
      throw new CannotDeleteFileException(file);
    } catch (NoSuchFileException e) {
      throw new CannotDeleteFileException(file);
    }
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull CaseInsensitiveVDir dir) {
    return dir.getAllFromCache();
  }

  @Override
  boolean isEmpty(@Nonnull CaseInsensitiveVDir dir) {
    return list(dir).isEmpty();
  }

  @Override
  @Nonnull
  public FileTime getLastModified(@Nonnull CaseInsensitiveVFile file)
      throws CannotGetModificationTimeException {
    return vfs.getLastModified(file.getEncodedFile());
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

    StringBuilder sb = new StringBuilder();
    int idx = 0;
    try {
      for (int groupIdx = 0; groupIdx < numGroups; groupIdx++) {
        for (int letterIdx = 0; letterIdx < groupSize; letterIdx++) {
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

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull CaseInsensitiveVDir parent, @Nonnull CaseInsensitiveVFile file) {
    StringBuilder path = getPathFromDirInternal(parent, (CaseInsensitiveVDir) file.getParent())
        .append(file.getName());
    return new VPath(path.toString(), '/');
  }

  @Nonnull
  private StringBuilder getPathFromDirInternal(@Nonnull CaseInsensitiveVDir baseDir,
      @Nonnull CaseInsensitiveVDir currentDir) {
    if (baseDir == currentDir) {
      return new StringBuilder();
    }
    CaseInsensitiveVDir currentParent = (CaseInsensitiveVDir) currentDir.getParent();
    assert currentParent != null;
    return getPathFromDirInternal(baseDir, currentParent).append(currentDir.getName()).append('/');
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull CaseInsensitiveVFile file) {
    return getPathFromDir(root, file);
  }

  @Override
  @CheckForNull
  public String getInfoString() {
    return vfs.getInfoString();
  }

  @Override
  public String toString() {
    return "ciFS >> " + vfs.toString();
  }
}
