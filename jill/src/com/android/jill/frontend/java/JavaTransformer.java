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

import com.android.jill.ContainerType;
import com.android.jill.JillException;
import com.android.jill.Options;
import com.android.jill.backend.jayce.JayceWriter;
import com.android.jill.utils.FileUtils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Transforms java binary files into jayce.
 */
public class JavaTransformer {

  @Nonnull
  private static final String LIB_MAJOR_VERSION = "1";

  @Nonnull
  private static final String LIB_MINOR_VERSION = "1";

  @Nonnull
  private static final String JAYCE_MAJOR_VERSION = "2";

  @Nonnull
  private static final String JAYCE_MINOR_VERSION = "15";

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
  private static final String JACK_LIBRARY_PROPERTIES = "jack.properties";

  @Nonnull
  private final String version;

  private final Options options;

  @Nonnull
  private static final String JAYCE_FILE_EXTENSION = ".jayce";

  @Nonnull
  private static final String JAYCE_PREFIX_INTO_LIB = "jayce";

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
  }

  public void transform(@Nonnull List<File> javaBinaryFiles) {
    ZipOutputStream zos = null;
    try {
      if (options.getOutputContainer() == ContainerType.ZIP) {
        zos = new ZipOutputStream(new FileOutputStream(options.getOutput()));
        for (File fileToTransform : javaBinaryFiles) {
          FileInputStream fis = new FileInputStream(fileToTransform);
          try {
            transformToZip(fis, zos, null);
          } catch (DuplicateJackFileException e) {
            System.err.println(e.getMessage());
          } finally {
            fis.close();
          }
        }
      } else {
        for (File fileToTransform : javaBinaryFiles) {
          FileInputStream fis = new FileInputStream(fileToTransform);
          try {
            transformToDir(fis, options.getOutput());
          } catch (DuplicateJackFileException e) {
            System.err.println(e.getMessage());
          } finally {
            fis.close();
          }
        }
      }
      dumpJackLibraryProperties(zos);
    } catch (IOException e) {
      throw new JillException("Transformation failure.", e);
    } finally {
      if (zos != null) {
        try {
          zos.close();
        } catch (IOException e) {
          throw new JillException("Error closing zip.", e);
        }
      }
    }
  }

  public void transform(@Nonnull JarFile jarFile) {
    ZipOutputStream zos = null;
    try {
      if (options.getOutputContainer() == ContainerType.ZIP) {
        zos = new ZipOutputStream(new FileOutputStream(options.getOutput()));
      }
      transformJavaFiles(jarFile, zos);
      dumpJackLibraryProperties(zos);
    } catch (Exception e) {
      throw new JillException("Failed to transform " + jarFile.getName(), e);
    } finally {
      if (zos != null) {
        try {
          zos.close();
        } catch (IOException e) {
          throw new JillException("Error closing zip.", e);
        }
      }
    }
  }

  private void dumpJackLibraryProperties(@CheckForNull ZipOutputStream zos) {
    if (zos != null) {
      dumpPropertiesToZip(zos, jackLibraryProperties);
    } else {
      dumpPropertiesToFile(new File(options.getOutput(), JACK_LIBRARY_PROPERTIES),
          jackLibraryProperties);
    }
  }

  private void dumpPropertiesToZip(@Nonnull ZipOutputStream zos,
      @Nonnull Properties libraryProperties) {
    try {
      ZipEntry entry = new ZipEntry(JACK_LIBRARY_PROPERTIES);
      zos.putNextEntry(entry);
      libraryProperties.store(zos, "Library Properties");
    } catch (IOException e) {
      throw new JillException("Error writing '" + JACK_LIBRARY_PROPERTIES + "' to output zip", e);
    }
  }

  private void dumpPropertiesToFile(@Nonnull File outputFile,
      @Nonnull Properties libraryProperties) {
    File outputDir = options.getOutput();
    File libraryPropertiesFile = new File(outputDir, JACK_LIBRARY_PROPERTIES);
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(libraryPropertiesFile);
      libraryProperties.store(fos, "Library Properties");
    } catch (IOException e) {
      throw new JillException(
          "Error writing '" + JACK_LIBRARY_PROPERTIES + "' to " + outputFile.getAbsolutePath(), e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          throw new JillException("Error closing output " + outputFile.getAbsolutePath(), e);
        }
      }
    }
  }

  private void transformJavaFiles(@Nonnull JarFile jarFile, @CheckForNull ZipOutputStream zos)
      throws IOException {
    final Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();
      String name = entry.getName();
      if (FileUtils.isJavaBinaryFile(name)) {
        JarEntry fileEntry = jarFile.getJarEntry(name);
        if (!fileEntry.isDirectory()) {
          InputStream is = jarFile.getInputStream(fileEntry);
          try {
            if (zos != null) {
              assert options.getOutputContainer() == ContainerType.ZIP;
              transformToZip(is, zos, jarFile);
            } else {
              assert options.getOutputContainer() == ContainerType.DIR;
              transformToDir(is, options.getOutput());
            }
          } catch (DuplicateJackFileException e) {
            System.err.println(e.getMessage());
          }
        }
      }
    }
  }

  private void transformToZip(@Nonnull InputStream is, @Nonnull ZipOutputStream zipOutputStream,
      @CheckForNull JarFile jarFile) throws IOException, DuplicateJackFileException {
    ClassNode cn = getClassNode(is);
    String filePath = getFilePath(cn.name);
    if (jarFile != null && jarFile.getEntry(filePath) != null) {
      throw new DuplicateJackFileException("Jack file '" + filePath
          + "' was already copied as a resource to archive '" + options.getOutput()
          + "' and thus won't be retransformed from class file.");
    }
    try {
      ZipEntry entry = new ZipEntry(filePath);
      zipOutputStream.putNextEntry(entry);
      transform(cn, zipOutputStream);
    } catch (IOException e) {
      throw new JillException("Error writing to output zip", e);
    }
  }

  private void transformToDir(@Nonnull InputStream is, @Nonnull File outputDir)
      throws IOException, DuplicateJackFileException {
    ClassNode cn = getClassNode(is);
    String filePath = getFilePath(cn.name);

    File outputFile = new File(outputDir, filePath);
    if (outputFile.exists()) {
      throw new DuplicateJackFileException("Jack file '" + outputFile.getAbsolutePath()
          + "' was already copied as a resource and thus won't be retransformed from class file.");
    }
    FileOutputStream fos = null;
    try {
      createParentDirectories(outputFile);
      fos = new FileOutputStream(outputFile);
      transform(cn, fos);
    } catch (IOException e) {
      throw new JillException("Unable to create output file " + outputFile.getName(), e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          throw new JillException("Error closing output " + outputFile.getAbsolutePath(), e);
        }
      }
    }
  }

  private void transform(@Nonnull ClassNode cn, @Nonnull OutputStream os) throws IOException {

    JayceWriter writer = createWriter(os);

    ClassNodeWriter asm2jayce =
        new ClassNodeWriter(writer, new SourceInfoWriter(writer), options);

    asm2jayce.write(cn);

    writer.flush();
  }

  private void createParentDirectories(File outputFile) throws IOException {
    File parentFile = outputFile.getParentFile();
    if (!parentFile.exists() && !parentFile.mkdirs()) {
      throw new IOException("Could not create directory \"" + parentFile.getName() + "\"");
    }
  }

  private JayceWriter createWriter(@Nonnull OutputStream os) {
    JayceWriter writer = new JayceWriter(os);
    setJayceProperties();
    return writer;
  }

  @Nonnull
  private static String getFilePath(@Nonnull String typeBinaryName) {
    return JAYCE_PREFIX_INTO_LIB + File.separatorChar
        + typeBinaryName.replace(TYPE_NAME_SEPARATOR, File.separatorChar) + JAYCE_FILE_EXTENSION;
  }

  @Nonnull
  private ClassNode getClassNode(@Nonnull InputStream is) throws IOException {
    ClassReader cr = new ClassReader(is);
    ClassNode cn = new ClassNode();
    cr.accept(cn, ClassReader.SKIP_FRAMES
        | (options.isEmitDebugInfo() ? 0 : ClassReader.SKIP_DEBUG));
    return cn;
  }

  private void setJayceProperties() {
    jackLibraryProperties.put(KEY_JAYCE, String.valueOf(true));
    jackLibraryProperties.put(KEY_JAYCE_MAJOR_VERSION, JAYCE_MAJOR_VERSION);
    jackLibraryProperties.put(KEY_JAYCE_MINOR_VERSION, JAYCE_MINOR_VERSION);
  }
}
