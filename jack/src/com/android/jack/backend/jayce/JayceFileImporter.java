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

import com.android.jack.JackEventType;
import com.android.jack.config.id.Brest;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.Resource;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.IgnoringImportMessage;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.ResourceInInputLibraryLocation;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.reporting.Reporter;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.resource.ResourceImportConflictException;
import com.android.jack.resource.ResourceImporter;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.codec.EnumName;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Imports jayce files from jack libraries in J-AST.
 */
@HasKeyId
public class JayceFileImporter {

  @Nonnull
  public static final String JAYCE_FILE_EXTENSION = ".jayce";

  public static final int JACK_EXTENSION_LENGTH = JAYCE_FILE_EXTENSION.length();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final List<InputJackLibrary> jackLibraries;

  private static final char VPATH_SEPARATOR = JLookup.PACKAGE_SEPARATOR;

  /**
   * How to handle file collisions.
   */
  @VariableName("policy")
  public enum CollisionPolicy {
    @EnumName(name = "keep-first", description = "keep the first element encountered")
    KEEP_FIRST,
    @EnumName(name = "fail", description = "fail when a collision occurs")
    FAIL;
  }

  @Nonnull
  public static final PropertyId<CollisionPolicy> COLLISION_POLICY = PropertyId.create(
      "jack.import.type.policy",
      "Defines the policy to follow concerning type collision",
      new EnumCodec<CollisionPolicy>(CollisionPolicy.class).ignoreCase())
      .addDefaultValue(CollisionPolicy.FAIL).addCategory(Brest.class);

  @Nonnull
  private final CollisionPolicy collisionPolicy = ThreadConfig.get(COLLISION_POLICY);

  @Nonnull
  private final CollisionPolicy resourceCollisionPolicy =
      ThreadConfig.get(ResourceImporter.RESOURCE_COLLISION_POLICY);

  public JayceFileImporter(@Nonnull List<InputJackLibrary> jackLibraries) {
    this.jackLibraries = jackLibraries;
  }

  public void doJayceImport(@Nonnull JSession session) throws LibraryReadingException {
    for (InputJackLibrary jackLibrary : jackLibraries) {
      Reporter reporter = session.getReporter();
      logger.log(Level.FINE, "Importing jayces from {0}",
          jackLibrary.getLocation().getDescription());
      Iterator<InputVFile> jayceFileIt = jackLibrary.iterator(FileType.JAYCE);
      while (jayceFileIt.hasNext()) {
        InputVFile jayceFile = jayceFileIt.next();

        try {
          addImportedTypes(session, jayceFile, jackLibrary);
        } catch (JLookupException e) {
          throw new LibraryReadingException(e);
        } catch (TypeImportConflictException e) {
          if (collisionPolicy == CollisionPolicy.FAIL) {
            throw new LibraryReadingException(e);
          } else {
            reporter.report(Severity.NON_FATAL, new IgnoringImportMessage(e));
          }
        }
      }
    }
  }

  public void doResourceImport(@Nonnull JSession session) throws LibraryReadingException {
    for (InputJackLibrary jackLibrary : jackLibraries) {
      Reporter reporter = session.getReporter();
      logger.log(Level.FINE, "Importing resources from {0}",
          jackLibrary.getLocation().getDescription());
      Iterator<InputVFile> rscFileIt = jackLibrary.iterator(FileType.RSC);
      while (rscFileIt.hasNext()) {
        InputVFile rscFile = rscFileIt.next();
        String name = rscFile.getPathFromRoot().getPathAsString('/');
        try {
          addImportedResource(rscFile, session, name, jackLibrary);
        } catch (ResourceImportConflictException e) {
          if (resourceCollisionPolicy == CollisionPolicy.FAIL) {
            throw new LibraryReadingException(e);
          } else {
            reporter.report(Severity.NON_FATAL, new IgnoringImportMessage(e));
          }
        }
      }
    }
  }

  private void addImportedTypes(@Nonnull JSession session, @Nonnull InputVFile jayceFile,
      @Nonnull InputLibrary intendedInputLibrary) throws TypeImportConflictException,
      JTypeLookupException {
    try (Event readEvent = tracer.open(JackEventType.NNODE_READING_FOR_IMPORT)) {
      String path = jayceFile.getPathFromRoot().getPathAsString('/');
      logger.log(Level.FINEST, "Importing jayce file ''{0}'' from {1}", new Object[] {path,
          intendedInputLibrary.getLocation().getDescription()});
      String signature = convertJackFilePathToSignature(path);
      JDefinedClassOrInterface declaredType =
          (JDefinedClassOrInterface) session.getLookup().getType(signature);
      if (!isTypeFromLibrary(declaredType, intendedInputLibrary)) {
        Location previousLocation = declaredType.getLocation();
        if (previousLocation instanceof TypeInInputLibraryLocation) {
          InputLibrary previousInputLibrary =
              ((TypeInInputLibraryLocation) previousLocation).getInputLibrary();
          String pathWithoutExt =
              path.substring(0, path.lastIndexOf(JAYCE_FILE_EXTENSION));
          try {
            String previousDigest =
                previousInputLibrary
                    .getFile(FileType.PREBUILT, new VPath(pathWithoutExt, '/'))
                    .getDigest();
            if (previousDigest != null
                && previousDigest.equals(jayceFile.getDigest())) {
              return; // both types are identical, ignore
            }
          } catch (FileTypeDoesNotExistException e) {
            // best effort, throw the conflict exception
          }
        }
        throw new TypeImportConflictException(declaredType, intendedInputLibrary.getLocation());
      } else {
        session.addTypeToEmit(declaredType);
      }
    }
  }

  private static boolean isTypeFromLibrary(@Nonnull JDefinedClassOrInterface declaredType,
      @Nonnull InputLibrary intendedInputLibrary) {
      Location existingSource = declaredType.getLocation();
      if (!(existingSource instanceof TypeInInputLibraryLocation)) {
          return false;
      }
      TypeInInputLibraryLocation existingLocation = (TypeInInputLibraryLocation) existingSource;
    return intendedInputLibrary.equals(existingLocation.getInputLibrary());
  }

  @Nonnull
  private String convertJackFilePathToSignature(@Nonnull String path) {
    String pathWithoutExt = path.substring(0, path.length() - JAYCE_FILE_EXTENSION.length());
    return "L" + pathWithoutExt.replace(VPATH_SEPARATOR, JLookup.PACKAGE_SEPARATOR) + ";";
  }

  private void addImportedResource(@Nonnull InputVFile file, @Nonnull JSession session,
      @Nonnull String currentPath, @Nonnull InputLibrary inputLibrary)
          throws ResourceImportConflictException {
    VPath path = new VPath(currentPath, VPATH_SEPARATOR);
    ResourceInInputLibraryLocation resourceLocation =
        new ResourceInInputLibraryLocation(inputLibrary, path);
    Resource newResource = new Resource(path, file, resourceLocation);
    for (Resource existingResource : session.getResources()) {
      if (existingResource.getPath().equals(path)) {
        throw new ResourceImportConflictException(existingResource, newResource.getLocation());
      }
    }
    session.addResource(newResource);
  }

  public static boolean isJackFileName(@Nonnull String name) {
    return (name.length() > JACK_EXTENSION_LENGTH) && (name.substring(
        name.length() - JACK_EXTENSION_LENGTH).equalsIgnoreCase(JAYCE_FILE_EXTENSION));
  }
}
