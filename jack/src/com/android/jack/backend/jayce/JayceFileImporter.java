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
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.Resource;
import com.android.jack.lookup.JLookup;
import com.android.sched.util.HasDescription;
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
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

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

  private static final char VPATH_SEPARATOR = JLookup.PACKAGE_SEPARATOR;

  private enum CollisionPolicy implements HasDescription {
    KEEP_FIRST("keep the first element encountered"),
    FAIL("fail when a collision occured");

    @Nonnull
    private String description;

    private CollisionPolicy(@Nonnull String description) {
      this.description = description;
    }

    @Override
    @Nonnull
    public String getDescription() {
      return description;
    }
  }

  @Nonnull
  public static final PropertyId<CollisionPolicy> COLLISION_POLICY = PropertyId.create(
      "jack.import.jackfile.policy",
      "Defines the policy to follow concerning type collision from imported jack files",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.values()).ignoreCase())
      .addDefaultValue(CollisionPolicy.FAIL);

  @Nonnull
  private final CollisionPolicy collisionPolicy = ThreadConfig.get(COLLISION_POLICY);

  @Nonnull
  public static final PropertyId<CollisionPolicy> RESOURCE_COLLISION_POLICY = PropertyId.create(
      "jack.import.resource.policy",
      "Defines the policy to follow concerning resource collision in imported jack containers",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.values()).ignoreCase())
      .addDefaultValue(CollisionPolicy.FAIL);

  @Nonnull
  private final CollisionPolicy resourceCollisionPolicy =
      ThreadConfig.get(RESOURCE_COLLISION_POLICY);

  public JayceFileImporter(@Nonnull List<InputVDir> jayceContainers) {
    this.jayceContainers = jayceContainers;
  }

  public void doImport(@Nonnull JSession session) throws JPackageLookupException,
      ImportConflictException, JTypeLookupException {

    for (InputVDir jayceContainer : jayceContainers) {
      logger.log(Level.FINE, "Importing {0}", jayceContainer.getLocation().getDescription());
      for (InputVElement subFile : jayceContainer.list()) {
        importJayceFile(subFile, session, "");
      }
    }
  }

  private void importJayceFile(@Nonnull InputVElement element, @Nonnull JSession session,
      @Nonnull String currentPath) throws JPackageLookupException,
      TypeImportConflictException, ResourceImportConflictException, JTypeLookupException {
    String path = currentPath + element.getName();
    if (element.isVDir()) {
      for (InputVElement subFile : ((InputVDir) element).list()) {
        importJayceFile(subFile, session, path + VPATH_SEPARATOR);
      }
    } else {
      InputVFile file = (InputVFile) element;
      if (isJackFileName(file.getName())) {
        addImportedTypes(session, path, file.getLocation());
      } else {
        addImportedResource(file, session, path);
      }
    }
  }

  private void addImportedTypes(@Nonnull JSession session, @Nonnull String path,
      @Nonnull Location expectedLoadSource) throws TypeImportConflictException,
      JTypeLookupException {
    Event readEvent = tracer.start(JackEventType.NNODE_READING_FOR_IMPORT);
    try {
      logger.log(Level.FINEST, "Importing jack file ''{0}''", expectedLoadSource.getDescription());
      String signature = convertJackFilePathToSignature(path);
      JDefinedClassOrInterface declaredType =
          (JDefinedClassOrInterface) session.getLookup().getType(signature);
      Location existingSource = declaredType.getLocation();
      if (!expectedLoadSource.equals(existingSource)) {
        if (collisionPolicy == CollisionPolicy.FAIL) {
          throw new TypeImportConflictException(declaredType, expectedLoadSource);
        } else {
          logger.log(Level.INFO,
              "Type ''{0}'' from {1} has already been imported from {2}: "
              + "ignoring import", new Object[] {
              Jack.getUserFriendlyFormatter().getName(declaredType),
              expectedLoadSource.getDescription(),
              existingSource.getDescription()});
        }
      } else {
        session.addTypeToEmit(declaredType);
      }
    } finally {
      readEvent.end();
    }
  }

  @Nonnull
  private String convertJackFilePathToSignature(@Nonnull String path) {
    String pathWithoutExt = path.substring(0, path.length() - JAYCE_FILE_EXTENSION.length());
    return "L" + pathWithoutExt.replace(VPATH_SEPARATOR, JLookup.PACKAGE_SEPARATOR) + ";";
  }

  private void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath) throws ResourceImportConflictException {
    VPath path = new VPath(currentPath, VPATH_SEPARATOR);
    Resource newResource = new Resource(path, file);
    for (Resource existingResource : session.getResources()) {
      if (existingResource.getPath().equals(path)) {
        if (resourceCollisionPolicy == CollisionPolicy.FAIL) {
          throw new ResourceImportConflictException(newResource.getLocation(),
              existingResource.getLocation());
        } else {
          logger.log(Level.INFO,
              "Resource in {0} has already been imported from {1}: ignoring import", new Object[] {
                  newResource.getLocation().getDescription(),
                  existingResource.getLocation().getDescription()});
        }
        return;
      }
    }
    session.addResource(newResource);
  }

  public static boolean isJackFileName(@Nonnull String name) {
    return (name.length() > JACK_EXTENSION_LENGTH) && (name.substring(
        name.length() - JACK_EXTENSION_LENGTH).equalsIgnoreCase(JAYCE_FILE_EXTENSION));
  }
}
