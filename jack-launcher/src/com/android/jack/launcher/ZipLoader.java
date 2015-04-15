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

package com.android.jack.launcher;

import com.android.jack.launcher.util.BytesStreamSucker;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class ZipLoader extends ClassLoader {

  private static final char BANG = '!';
  static {
    // ensure class is loaded early, don't wait for first usage
    ZipURLConnection.class.getName();
  }

  private static class ZipURLConnection extends URLConnection {
    @Nonnull
    private final ZipFile zip;
    @Nonnull
    private final ZipEntry entry;

    ZipURLConnection(@Nonnull URL url, @Nonnull ZipFile zip, @Nonnull ZipEntry entry) {
      super(url);
      this.zip = zip;
      this.entry = entry;
    }

    @Override
    public void connect() {
      // nothing to do
    }

    @Override
    @Nonnull
    public InputStream getInputStream() throws IOException {
      return zip.getInputStream(entry);
    }

  }

  private static class ZipURLStreamHandler extends URLStreamHandler {
    @Nonnull
    private final ZipFile zip;

    ZipURLStreamHandler(@Nonnull ZipFile zip) {
      this.zip = zip;
    }

    @Override
    protected URLConnection openConnection(@Nonnull URL url) throws IOException {
      ZipEntry entry = zip.getEntry(getZipEntry(url));
      if (entry == null) {
        throw new FileNotFoundException(url.toString());
      }
      return new ZipURLConnection(url, zip, entry);
    }
  }

  @Nonnull
  private static URL makeURL(@Nonnull ZipEntry entry, @Nonnull ZipURLStreamHandler handler) {
    try {
      assert entry.getName().indexOf(BANG) == -1;
      return new URL("launcherzip", "", -1, handler.zip.getName() + BANG + entry.getName(),
          handler);
    } catch (MalformedURLException e) {
      throw new AssertionError();
    }
  }

  @Nonnull
  private static String getZipEntry(@Nonnull URL url) {
    String file = url.getFile();
    int dashIndex = file.lastIndexOf(BANG);
    return file.substring(dashIndex + 1);
  }

  @Nonnull
  private final ZipURLStreamHandler[] handlers;


  public ZipLoader(@Nonnull ZipFile[] entries) {
    handlers = new ZipURLStreamHandler[entries.length];
    for (int i = 0; i < entries.length; i++) {
      handlers[i] = new ZipURLStreamHandler(entries[i]);
    }
  }

  @Nonnull
  @Override
  protected Class<?> findClass(@Nonnull String name) throws ClassNotFoundException {

    ZipEntry foundEntry = null;
    ZipFile foundZip = null;
    for (ZipURLStreamHandler handler : handlers) {
      ZipFile zip = handler.zip;
      foundEntry = zip.getEntry(name.replace('.', '/') + ".class");
      if (foundEntry != null) {
        foundZip = zip;
        break;
      }
    }
    if (foundEntry == null) {
      throw new ClassNotFoundException(name);
    }

    InputStream in = null;
    try {
      // FINDBUGS
      assert foundZip != null;
      in = foundZip.getInputStream(foundEntry);
      long size = foundEntry.getSize();
      assert size >= -1 && size <= Integer.MAX_VALUE;
      byte[] classData;
      if (size == -1) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new BytesStreamSucker(in, out, /* toBeClose = */ true).suck();
        classData = out.toByteArray();
      } else {
        classData = new byte[(int) size];
        // FINDBUGS
        in = new DataInputStream(in);
        ((DataInputStream) in).readFully(classData);
      }
      return defineClass(name, classData, 0, classData.length);
    } catch (IOException e) {
      throw new ClassNotFoundException(name);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  @CheckForNull
  @Override
  public InputStream getResourceAsStream(@Nonnull String name) {
    InputStream found = getParent().getResourceAsStream(name);
    if (found != null) {
      return found;
    } else {
      for (ZipURLStreamHandler handler : handlers) {
        ZipFile zip = handler.zip;
        ZipEntry foundEntry = zip.getEntry(name);
        if (foundEntry != null) {
          try {
            return zip.getInputStream(foundEntry);
          } catch (IOException e) {
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
          }
        }
      }
      return null;
    }
  }

  @CheckForNull
  @Override
  protected URL findResource(@Nonnull String name) {
    for (ZipURLStreamHandler handler : handlers) {
      ZipFile zip = handler.zip;
      ZipEntry foundEntry = zip.getEntry(name);
      if (foundEntry != null) {
        return makeURL(foundEntry, handler);
      }
    }
    return null;
  }

  @Nonnull
  @Override
  protected Enumeration<URL> findResources(@Nonnull String name) {
    Vector<URL> vector = new Vector<URL>();
    for (ZipURLStreamHandler handler : handlers) {
      ZipFile zip = handler.zip;
      ZipEntry foundEntry = zip.getEntry(name);
      if (foundEntry != null) {
        vector.add(makeURL(foundEntry, handler));
      }
    }
    return vector.elements();
  }
}
