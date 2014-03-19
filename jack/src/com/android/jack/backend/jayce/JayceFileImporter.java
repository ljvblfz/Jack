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

package com.android.jack.backend.jayce;

import com.android.jack.Jack;
import com.android.jack.JackEventType;
import com.android.jack.JackFileException;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.JayceVersionException;
import com.android.jack.lookup.JLookup;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.config.FileLocation;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.Location;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.ZipLocation;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Imports jayce file content in J-AST.
 */
@HasKeyId
public class JayceFileImporter {

  @Nonnull
  public static final String JAYCE_FILE_EXTENSION = ".jack";

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final java.util.logging.Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final List<File> jayceContainers;

  private enum CollisionPolicy {
    KEEP_FIRST,
    FAIL
  }

  @Nonnull
  public static final PropertyId<CollisionPolicy> COLLISION_POLICY = PropertyId.create(
      "jack.jackimport.policy",
      "Defines the policy to follow concerning type collision in imported jack files",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.values()).ignoreCase())
      .addDefaultValue("fail");

  @Nonnull
  private final CollisionPolicy collisionPolicy = ThreadConfig.get(COLLISION_POLICY);

  public JayceFileImporter(@Nonnull List<File> jayceContainers) {
    this.jayceContainers = jayceContainers;
  }

  public void doImport(@Nonnull JProgram program)
      throws JayceFormatException, JayceVersionException, JackFileException {

    JLookup lookup = program.getPhantomLookup();
    for (File jayceContainer : jayceContainers) {
      String rootDirPath = jayceContainer.getAbsolutePath();
      try {
        if (jayceContainer.isDirectory()) {
          logger.log(Level.FINE, "Importing jack directory ''{0}''",
              jayceContainer.getAbsolutePath());
          for (File subFile : jayceContainer.listFiles()) {
            importJayceFile(subFile, program, lookup, rootDirPath);
          }
        } else {
          // try zip
          ZipFile zipFile = new ZipFile(jayceContainer);
          logger.log(Level.FINE, "Importing jack archive ''{0}''",
              jayceContainer.getAbsolutePath());
          List<ZipEntry> resources = new ArrayList<ZipEntry>();
          Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
          while (zipFileEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipFileEntries.nextElement();
            if (zipEntry.getName().endsWith(JAYCE_FILE_EXTENSION)) {
              addImportedTypesToProgram(
                  program,
                  lookup,
                  zipEntry.getName(),
                  rootDirPath,
                  new ZipLocation(new FileLocation(jayceContainer), zipEntry));
            } else {
              resources.add(zipEntry);
            }
          }
          program.addMarker(new ResourceContainerMarker(zipFile, resources));
        }
      } catch (IOException e) {
        throw new JackFileException("Error reading jack archive " + rootDirPath, e);
      }
    }
  }

  private void importJayceFile(@Nonnull File file, @Nonnull JProgram program,
      @Nonnull JLookup lookup,
      @Nonnull String rootDirPath) throws IOException, JayceFormatException, JayceVersionException {
    if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        importJayceFile(subFile, program, lookup, rootDirPath);
      }
    } else {
      if (file.getName().endsWith(JAYCE_FILE_EXTENSION)) {
        String fullName = file.getAbsolutePath().substring(rootDirPath.length() + 1);
        addImportedTypesToProgram(program, lookup, fullName, rootDirPath, new FileLocation(file));
      }
    }
  }

  private void addImportedTypesToProgram(
      @Nonnull JProgram program,
      @Nonnull JLookup lookup,
      @Nonnull String path,
      @Nonnull String rootDirPath,
      @Nonnull Location expectedLoadSource) throws JayceFormatException, JayceVersionException {

    Event readEvent = tracer.start(JackEventType.NNODE_READING_FOR_IMPORT);
    try {
      logger.log(Level.FINEST, "Importing jack file ''{0}'' - from ''{1}''",
          new Object[] {path, rootDirPath});
      String typeBinaryName = path.substring(0, path.length() - JAYCE_FILE_EXTENSION.length());
      JDefinedClassOrInterface declaredType =
          (JDefinedClassOrInterface) lookup.getType('L' + typeBinaryName + ';');
      Location existingSource = declaredType.getLocation();
      if (!expectedLoadSource.equals(existingSource)) {
        if (collisionPolicy == CollisionPolicy.FAIL) {
          throw new ImportConflictException(declaredType, expectedLoadSource);
        } else {
          logger.log(Level.INFO, "Type '{0}' from '{1}' has already been imported from {2}: "
              + "ignoring import", new Object[] {
              Jack.getUserFriendlyFormatter().getName(declaredType), rootDirPath,
              "'" + existingSource.getDescription() + "'"});
        }
      } else {
        program.addTypeToEmit(declaredType);
      }
    } finally {
      readEvent.end();
    }
  }
}
