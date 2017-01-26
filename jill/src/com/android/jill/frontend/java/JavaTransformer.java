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

package com.android.jill.frontend.java;

import com.android.jill.JillException;
import com.android.jill.Options;
import com.android.jill.backend.jayce.JayceWriter;
import com.android.jill.utils.FileUtils;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.MessageDigestCodec;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.OutputZipFile.Compression;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.BadVFSFormatException;
import com.android.sched.vfs.DeflateFS;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.GenericOutputVFS;
import com.android.sched.vfs.MessageDigestFS;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.PrefixedFS;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;
import com.android.sched.vfs.WriteZipFS;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider.Service;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.Nonnull;

/**
 * Transforms java binary files into jayce.
 */
public class JavaTransformer {

  @Nonnull
  private static final String LIB_MAJOR_VERSION = "3";

  @Nonnull
  private static final String LIB_MINOR_VERSION = "5";

  @Nonnull
  private static final String JAYCE_MAJOR_VERSION = "4";

  @Nonnull
  private static final String JAYCE_MINOR_VERSION = "4";

  @Nonnull
  private static final String KEY_LIB_MAJOR_VERSION = "lib.version.major";

  @Nonnull
  private static final String KEY_LIB_MINOR_VERSION = "lib.version.minor";

  @Nonnull
  private static final String KEY_LIB_EMITTER = "lib.emitter";

  @Nonnull
  private static final String KEY_LIB_EMITTER_VERSION = "lib.emitter.version";

  @Nonnull
  private static final String KEY_JAYCE = "jayce";

  @Nonnull
  private static final String KEY_JAYCE_MAJOR_VERSION = "jayce.version.major";

  @Nonnull
  private static final String KEY_JAYCE_MINOR_VERSION = "jayce.version.minor";

  @Nonnull
  private static final String KEY_LIB_JAYCE_DIGEST = "lib.jayce.digest";

  @Nonnull
  private static final String JACK_LIBRARY_PROPERTIES = "jack.properties";

  @Nonnull
  private final String version;

  private final Options options;

  @Nonnull
  private static final String JAYCE_FILE_EXTENSION = ".jayce";

  @Nonnull
  private static final char TYPE_NAME_SEPARATOR = '/';

  @Nonnull
  private final Properties jackLibraryProperties;

  public JavaTransformer(@Nonnull String version, @Nonnull Options options) {
    this.version = version;
    this.options = options;
    jackLibraryProperties = new Properties();
    jackLibraryProperties.put(KEY_LIB_EMITTER, "jill");
    jackLibraryProperties.put(KEY_LIB_EMITTER_VERSION, version);
    jackLibraryProperties.put(KEY_LIB_MAJOR_VERSION, LIB_MAJOR_VERSION);
    jackLibraryProperties.put(KEY_LIB_MINOR_VERSION, LIB_MINOR_VERSION);
    jackLibraryProperties.put(KEY_LIB_JAYCE_DIGEST, "true");
  }

  public void transform(@Nonnull List<File> javaBinaryFiles) {

    try (VFS baseVFS = getBaseOutputVFS()) {
      try (OutputVFS outputVFS = wrapOutputVFS(baseVFS)) {

        for (File fileToTransform : javaBinaryFiles) {
          try (FileInputStream fis = new FileInputStream(fileToTransform)) {
            transformToVFS(fis, outputVFS);
          }
        }
        dumpJackLibraryProperties(baseVFS);
      }
    } catch (IOException | CannotCloseException e) {
      throw new JillException(e);
    }
  }

  public void transform(@Nonnull JarFile jarFile) {

    try (VFS baseVFS = getBaseOutputVFS()) {
      try (OutputVFS outputVFS = wrapOutputVFS(baseVFS)) {
        transformJavaFiles(jarFile, outputVFS);
      }
      dumpJackLibraryProperties(baseVFS);
    } catch (IOException | CannotCloseException e) {
      throw new JillException(e);
    }
  }

  @Nonnull
  private VFS getBaseOutputVFS() {
    VFS baseVFS;
    try {
      switch (options.getOutputContainer()) {
        case DIR:
          baseVFS =
              new DirectFS(
                  new Directory(options.getOutput().getPath(), /* hooks = */ null,
                      Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
                  Permission.WRITE);
          break;
        case ZIP:
          baseVFS =
              new WriteZipFS(new OutputZipFile(options.getOutput().getPath(), /* hooks = */ null,
                  Existence.MAY_EXIST, ChangePermission.NOCHANGE, Compression.UNCOMPRESSED));
          break;
        default:
          throw new AssertionError();
      }
    } catch (NotFileException | FileAlreadyExistsException | CannotCreateFileException
        | CannotChangePermissionException | WrongPermissionException | NoSuchFileException
        | NotDirectoryException e) {
      throw new JillException(e);
    }
    return baseVFS;
  }

  @Nonnull
  private OutputVFS wrapOutputVFS(@Nonnull VFS baseVFS) {
    MessageDigestCodec mdCodec = new MessageDigestCodec();
    Service service = mdCodec.parseString(new CodecContext(), "SHA");

    try {
      return new GenericOutputVFS(new DeflateFS(new MessageDigestFS(
          new PrefixedFS(baseVFS, new VPath("jayce", '/')), new MessageDigestFactory(service))));
    } catch (NotDirectoryException | CannotCreateFileException | BadVFSFormatException e) {
      throw new JillException(e);
    }
  }

  private void dumpJackLibraryProperties(@Nonnull VFS baseVFS) {
    try {
      @SuppressWarnings("resource")
      OutputVFS goVFS = new GenericOutputVFS(baseVFS);
      OutputVFile libraryPropertiesOut =
          goVFS.getRootDir().createOutputVFile(new VPath(JACK_LIBRARY_PROPERTIES, '/'));
      try (OutputStream os = libraryPropertiesOut.getOutputStream()) {
        jackLibraryProperties.store(os, "Library Properties");
      } catch (IOException e) {
        throw new CannotCloseException(libraryPropertiesOut, e);
      }
    } catch (CannotCreateFileException | WrongPermissionException | CannotCloseException e) {
      throw new JillException(e);
    }
  }

  private void transformJavaFiles(@Nonnull JarFile jarFile, @Nonnull OutputVFS outputVFS)
      throws IOException {
    final Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();
      String name = entry.getName();
      if (FileUtils.isJavaBinaryFile(name)) {
        JarEntry fileEntry = jarFile.getJarEntry(name);
        if (!fileEntry.isDirectory()) {
          InputStream is = jarFile.getInputStream(fileEntry);
          transformToVFS(is, outputVFS);
        }
      }
    }
  }

  private void transformToVFS(@Nonnull InputStream is, @Nonnull OutputVFS outputVFS)
      throws IOException {
    ClassNode cn = getClassNode(is);
    VPath outputPath = getVPath(cn.name);
    try {
      OutputVFile vFile = outputVFS.getRootDir().createOutputVFile(outputPath);
      try (OutputStream os = vFile.getOutputStream()) {
        transform(cn, os, vFile.getLocation());
      } catch (IOException e) {
        throw new CannotCloseException(vFile.getLocation(), e);
      }
    } catch (CannotCreateFileException | WrongPermissionException | CannotCloseException
        | CannotWriteException e) {
      throw new JillException(e);
    }
  }

  private void transform(@Nonnull ClassNode cn, @Nonnull OutputStream os,
      @Nonnull Location location) throws CannotWriteException {

      JayceWriter writer = createWriter(os);

      ClassNodeWriter asm2jayce =
          new ClassNodeWriter(writer, new SourceInfoWriter(writer), options);

      try {
        asm2jayce.write(cn);

        writer.flush();
      } catch (IOException e) {
        throw new CannotWriteException(location, e);
      }
  }

  private JayceWriter createWriter(@Nonnull OutputStream os) {
    JayceWriter writer = new JayceWriter(os);
    setJayceProperties();
    return writer;
  }

  @Nonnull
  private static VPath getVPath(@Nonnull String typeBinaryName) {
    return new VPath(typeBinaryName + JAYCE_FILE_EXTENSION, TYPE_NAME_SEPARATOR);
  }

  @Nonnull
  private ClassNode getClassNode(@Nonnull InputStream is) throws IOException {
    try {
      ClassReader cr = new ClassReader(is);
      ClassNode cn = new ClassNode();
      cr.accept(cn,
          ClassReader.SKIP_FRAMES | (options.isEmitDebugInfo() ? 0 : ClassReader.SKIP_DEBUG));
      return cn;
    } catch (IllegalArgumentException e) {
      // It means that class files come from an unsupported Java version
      throw new JillException("class files coming from an unsupported Java version", e);
    }
  }

  private void setJayceProperties() {
    jackLibraryProperties.put(KEY_JAYCE, String.valueOf(true));
    jackLibraryProperties.put(KEY_JAYCE_MAJOR_VERSION, JAYCE_MAJOR_VERSION);
    jackLibraryProperties.put(KEY_JAYCE_MINOR_VERSION, JAYCE_MINOR_VERSION);
  }
}
