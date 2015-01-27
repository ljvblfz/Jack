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

import com.google.common.io.NullOutputStream;

import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.LineLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.MessageDigestFS.MessageDigestVFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link VFS} filter implementation that creates a file containing a message digest for each
 * file.
 */
public class MessageDigestFS extends BaseVFS<BaseVDir, MessageDigestVFile> implements VFS {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private static final String DIGEST_FILE_NAME = "digest";

  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;
  @Nonnull
  private final MessageDigestFactory mdFactory;
  @Nonnull
  private final Map<VPath, String> digests = new HashMap<VPath, String>();
  @CheckForNull
  private String digest = null;
  @Nonnull
  private final Set<Capabilities> capabilities;

  class MessageDigestVFile extends BaseVFile {

    @Nonnull
    private final BaseVFile wrappedFile;

    public MessageDigestVFile(@Nonnull BaseVFS<BaseVDir, MessageDigestVFile> vfs,
        @Nonnull BaseVFile wrappedFile) {
      super(vfs, wrappedFile.getName());
      this.wrappedFile = wrappedFile;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return wrappedFile.getLocation();
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return wrappedFile.getPath();
    }

    @Override
    @CheckForNull
    public String getDigest() {
      synchronized (MessageDigestFS.this) {
        return digests.get(this.getPath());
      }
    }

    @Override
    @Nonnull
    public InputStream openRead() throws WrongPermissionException {
      return wrappedFile.openRead();
    }

    @Override
    @Nonnull
    public OutputStream openWrite() throws WrongPermissionException {
      synchronized (MessageDigestFS.this) {
        digests.remove(getPath());
        digest = null;
      }

      return new DigestOutputStream(wrappedFile.openWrite(), mdFactory.create()) {
        @Override
        public void close() throws IOException {
          super.close();
          // XXX open order instead of close order !
          synchronized (MessageDigestFS.this) {
            digests.put(getPath(), getDigestString(getMessageDigest().digest()));
            digest = null;
          }
        }
      };
    }
  }

  @SuppressWarnings("unchecked")
  public MessageDigestFS(@Nonnull VFS vfs, @Nonnull MessageDigestFactory factory)
      throws WrongVFSFormatException {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;
    this.mdFactory = factory;
    changeVFS(this.vfs.getRootDir());

    Set<Capabilities> capabilities = EnumSet.copyOf(vfs.getCapabilities());
    capabilities.add(Capabilities.DIGEST);
    this.capabilities = Collections.unmodifiableSet(capabilities);

    init();
  }

  private void init() throws WrongVFSFormatException {
    BaseVFile digestFile;

    try {
      digestFile = vfs.getRootDir().getVFile(DIGEST_FILE_NAME);
    } catch (NotFileException e) {
      throw new WrongVFSFormatException(this, vfs.getLocation(), e);
    } catch (NoSuchFileException e) {
      if (!vfs.getRootDir().isEmpty()) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), e);
      }

      return;
    }

    LineNumberReader in = null;
    try {
      try {
        in = new LineNumberReader(new InputStreamReader(digestFile.openRead()));
      } catch (WrongPermissionException e) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), e);
      }

      try {
        String line;
        while ((line = in.readLine()) != null) {
          int index = line.indexOf(':');
          if (index < 1) {
            throw new WrongVFSFormatException(this, vfs.getLocation(),
                new WrongFileFormatException(new LineLocation(digestFile.getLocation(),
                    in.getLineNumber())));
          }

          String path = line.substring(index + 1);
          String digest = line.substring(0, index);
          if (!path.equals(DIGEST_FILE_NAME)) { // do not put digest file in the map
            digests.put(new VPath(path, '/'), digest);
          } else {
            this.digest = digest;
          }
        }
      } catch (IOException e) {
        throw new WrongVFSFormatException(this, vfs.getLocation(), new CannotReadException(
            digestFile.getLocation()));
      }
    } finally {
      if (in != null) {
        assert digestFile != null;

        try {
          in.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Cannot close {0}", digestFile.getLocation().getDescription());
        }
      }
    }
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return capabilities;
  }

  @Nonnull
  private String getDigestString(@Nonnull byte[] digestBytes) {
    return mdFactory.getService().getAlgorithm() + '-' + String.valueOf(encode(digestBytes));
  }

  @Nonnull
  private static final byte[] code = "0123456789ABCDEF".getBytes();

  @Nonnull
  private static char[] encode(@Nonnull byte[] bytes) {
    char[] array = new char[bytes.length * 2];

    for (int idx = 0; idx < bytes.length; idx++) {
      array[(idx << 1)] = (char) code[(bytes[idx] & 0xF0) >> 4];
      array[(idx << 1) + 1] = (char) code[(bytes[idx] & 0x0F)];
    }

    return array;
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

  @Nonnull
  public synchronized String getDigest() {
    if (digest == null) {
      printDigest(new NullOutputStream());
      assert digest != null;
    }

    return digest;
  }

  @Override
  public synchronized void close() throws CannotCreateFileException, WrongPermissionException,
      IOException {
    if (!closed) {
      printDigest(vfs.getRootDir().createVFile(DIGEST_FILE_NAME).openWrite());
      vfs.close();
      closed = true;
    }
  }

  @SuppressFBWarnings("DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS")
  private void printDigest(@Nonnull OutputStream out) {
    DigestOutputStream os = new DigestOutputStream(out, mdFactory.create());
    PrintStream printer = new PrintStream(os);

    Set<Entry<VPath, String>> entrySet = digests.entrySet();
    List<Entry<VPath, String>> entryList = new ArrayList<Entry<VPath, String>>(entrySet.size());
    entryList.addAll(entrySet);

    Collections.sort(entryList, new Comparator<Entry<VPath, String>>() {
      @Override
      public int compare(Entry<VPath, String> o1, Entry<VPath, String> o2) {
        return o1.getKey().getPathAsString('/').compareTo(o2.getKey().getPathAsString('/'));
      }
    });

    for (Map.Entry<VPath, String> entry : entryList) {
      String digest = entry.getValue();
      if (digest != null) {
        printer.print(digest);
        printer.print(':');
        printer.print(entry.getKey().getPathAsString('/'));
        printer.println();
      }
    }

    printer.flush();

    digest = getDigestString(os.getMessageDigest().digest());
    printer.print(digest);
    printer.print(":" + DIGEST_FILE_NAME);
    printer.println();

    printer.close();
  }

  @Override
  @Nonnull
  public BaseVDir getRootDir() {
    return vfs.getRootDir();
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull MessageDigestVFile file) {
    // should be implemented in MessageDigestVFile
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull final MessageDigestVFile file) {
    // should be implemented in MessageDigestVFile
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  synchronized void delete(@Nonnull MessageDigestVFile file) throws CannotDeleteFileException {
    vfs.delete(file);
    digests.remove(file.getPath());
    digest = null;
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull BaseVDir dir) {
    Collection<? extends BaseVElement> elements = vfs.list(dir);
    List<BaseVElement> newElements = new ArrayList<BaseVElement>(elements.size());
    for (BaseVElement element : elements) {
      BaseVElement newElement;
      if (element.isVDir()) {
        element.changeVFS(this);
        newElement = element;
      } else {
        newElement = new MessageDigestVFile(this, (BaseVFile) element);
      }
      newElements.add(newElement);
    }

    return newElements;
  }


  @Override
  boolean isEmpty(@Nonnull BaseVDir dir) {
    return vfs.isEmpty(dir);
  }

  @Override
  @Nonnull
  MessageDigestVFile createVFile(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return new MessageDigestVFile(this, vfs.createVFile(parent, name));
  }

  @Override
  @Nonnull
  BaseVDir createVDir(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return changeVFS(vfs.createVDir(parent, name));
  }

  @Override
  @Nonnull
  BaseVDir getVDir(@Nonnull BaseVDir parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    return changeVFS(vfs.getVDir(parent, name));
  }

  @Override
  @Nonnull
  MessageDigestVFile getVFile(@Nonnull BaseVDir parent, @Nonnull String name)
      throws NotFileException, NoSuchFileException {
    return new MessageDigestVFile(this, vfs.getVFile(parent, name));
  }

  @Nonnull
  private BaseVDir changeVFS(@Nonnull BaseVDir dir) {
    dir.changeVFS(this);
    return dir;
  }

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "message digest wrapper";
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull MessageDigestVFile file) {
    // should be implemented in MessageDigestVFile
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVDir parent, @Nonnull String name) {
    return vfs.getVFileLocation(parent, name);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVDir parent, @Nonnull VPath path) {
    return vfs.getVFileLocation(parent, path);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir dir) {
    return vfs.getVDirLocation(dir);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir parent, @Nonnull String name) {
    return vfs.getVDirLocation(parent, name);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir parent, @Nonnull VPath path) {
    return vfs.getVDirLocation(parent, path);
  }
}
