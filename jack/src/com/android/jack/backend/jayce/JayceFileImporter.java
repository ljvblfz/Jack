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
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.JayceVersionException;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VElement;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Imports jayce file content in J-AST.
 */
@HasKeyId
public class JayceFileImporter {

  @Nonnull
  public static final String JAYCE_FILE_EXTENSION = ".jack";

  public static final int JACK_EXTENSION_LENGTH = JAYCE_FILE_EXTENSION.length();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final java.util.logging.Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final List<InputVDir> jayceContainers;

  private enum CollisionPolicy {
    KEEP_FIRST,
    FAIL
  }

  @Nonnull
  public static final PropertyId<CollisionPolicy> COLLISION_POLICY = PropertyId.create(
      "jack.jackimport.policy",
      "Defines the policy to follow concerning type collision in imported jack files",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.values()).ignoreCase())
      .addDefaultValue(CollisionPolicy.FAIL);

  @Nonnull
  private final CollisionPolicy collisionPolicy = ThreadConfig.get(COLLISION_POLICY);

  public JayceFileImporter(@Nonnull List<InputVDir> jayceContainers) {
    this.jayceContainers = jayceContainers;
  }

  public void doImport(@Nonnull JSession session) throws JayceFormatException,
      JayceVersionException, JackFileException {

    ResourceContainerMarker resourceMarker = session.getMarker(ResourceContainerMarker.class);
    if (resourceMarker == null) {
      resourceMarker = new ResourceContainerMarker();
      session.addMarker(resourceMarker);
    }
    for (InputVDir jayceContainer : jayceContainers) {
      try {
        logger.log(Level.FINE, "Importing {0}", jayceContainer.getLocation().getDescription());
        JPackage topLevelPackage = session.getTopLevelPackage();
        for (VElement subFile : jayceContainer.list()) {
          importJayceFile(subFile, session, topLevelPackage, resourceMarker);
        }
      } catch (IOException e) {
        throw new JackFileException(
            "Error importing " + jayceContainer.getLocation().getDescription(), e);
      }
    }
  }

  private void importJayceFile(@Nonnull VElement element, @Nonnull JSession session,
      @Nonnull JPackage pack, @Nonnull ResourceContainerMarker resourceMarker) throws IOException,
      JayceFormatException, JayceVersionException {
    if (element instanceof InputVDir) {
      for (VElement subFile : ((InputVDir) element).list()) {
        importJayceFile(subFile, session, pack.getSubPackage(element.getName()), resourceMarker);
      }
    } else if (element instanceof InputVFile) {
      InputVFile file = (InputVFile) element;
      if (isJackFileName(file.getName())) {
        addImportedTypes(session, file.getName(), pack, file.getLocation());
      } else {
        resourceMarker.addResource(file);
      }
    } else {
      throw new AssertionError();
    }
  }

  private void addImportedTypes(@Nonnull JSession session, @Nonnull String path,
      @Nonnull JPackage pack, @Nonnull Location expectedLoadSource) throws JayceFormatException,
      JayceVersionException {
    Event readEvent = tracer.start(JackEventType.NNODE_READING_FOR_IMPORT);
    try {
      logger.log(Level.FINEST, "Importing jack file ''{0}'' in package ''{1}''",
          new Object[] {path, Jack.getUserFriendlyFormatter().getName(pack)});
      String simpleName = path.substring(0, path.length() - JAYCE_FILE_EXTENSION.length());
      JDefinedClassOrInterface declaredType = pack.getType(simpleName);
      Location existingSource = declaredType.getLocation();
      if (!expectedLoadSource.equals(existingSource)) {
        if (collisionPolicy == CollisionPolicy.FAIL) {
          throw new ImportConflictException(declaredType, expectedLoadSource);
        } else {
          logger.log(Level.INFO,
              "Type ''{0}'' has already been imported from {1}: "
              + "ignoring import", new Object[] {
              Jack.getUserFriendlyFormatter().getName(declaredType),
              existingSource.getDescription()});
        }
      } else {
        session.addTypeToEmit(declaredType);
      }
    } finally {
      readEvent.end();
    }
  }

  public static boolean isJackFileName(@Nonnull String name) {
    return (name.length() > JACK_EXTENSION_LENGTH) && (name.substring(
        name.length() - JACK_EXTENSION_LENGTH).equalsIgnoreCase(JAYCE_FILE_EXTENSION));
  }
}
