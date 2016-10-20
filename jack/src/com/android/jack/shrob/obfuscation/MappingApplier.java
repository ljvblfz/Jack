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

package com.android.jack.shrob.obfuscation;

import com.google.common.primitives.Chars;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.JackIOException;
import com.android.jack.frontend.MethodIdDuplicateRemover.UniqMethodIds;
import com.android.jack.ir.ast.CanBeRenamed;
import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.lookup.JLookupException;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.shrob.obfuscation.key.FieldKey;
import com.android.jack.shrob.obfuscation.key.MethodKey;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.shrink.MappingCollisionException;
import com.android.jack.shrob.shrink.MappingCollisionPolicy;
import com.android.jack.shrob.shrink.MappingContextException;
import com.android.jack.shrob.shrink.MappingContextInfo;
import com.android.jack.transformations.request.ChangeEnclosingPackage;
import com.android.jack.transformations.request.Rename;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.marker.MarkerManager;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.ColumnAndLineLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A class that parses a mapping file and rename the remapped nodes.
 */
@HasKeyId
@Transform(add = {OriginalNameMarker.class, OriginalPackageMarker.class, KeepNameMarker.class})
@Constraint(need = UniqMethodIds.class)
public class MappingApplier {

  @Nonnull
  private static final char[] EMPTY_STOP_CHARS = new char[] {};

  @Nonnull
  private static final char[] CLASSINFO_STOP_CHARS = new char[] {':'};

  @Nonnull
  private static final char[] BEGIN_PARAMETER_STOP_CHARS = new char[] {'('};

  @Nonnull
  private static final char[] END_PARAMETER_STOP_CHARS = new char[] {',', ')'};

  @Nonnull
  protected static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final TransformationRequest request;

  @Nonnull
  public static final PropertyId<MappingCollisionPolicy> COLLISION_POLICY =
      BooleanPropertyId
          .create(
              "jack.obfuscation.mapping.collision-policy",
              "Abort obfuscation when a mapping collision is detected",
              new EnumCodec<MappingCollisionPolicy>(
                  MappingCollisionPolicy.class, MappingCollisionPolicy.values()).ignoreCase())
          .addDefaultValue(MappingCollisionPolicy.FAIL);

  @Nonnull
  private final MappingCollisionPolicy collisionPolicy =
      ThreadConfig.get(COLLISION_POLICY);

  public MappingApplier(@Nonnull TransformationRequest request) {
    this.request = request;
  }

  private static boolean isClassInfo(@Nonnull String line) {
    // A class info line ends with ':'.
    return line.charAt(line.length() - 1) == ':';
  }

  private static boolean isMethodInfo(@Nonnull String line) {
    // A method info line contains '('.
    return line.indexOf('(') != -1;
  }

  private void throwException(@Nonnull File mappingFile, int lineNumber, @Nonnull String message)
      throws JackIOException {
    throw new JackIOException(mappingFile.getPath() + ":" + lineNumber + ":" + message);
  }

  @CheckForNull
  private JDefinedClassOrInterface createMappingForType(@Nonnull String oldName,
      @Nonnull String newName, @Nonnull JSession session, @Nonnull File mappingFile,
      int lineNumber) {
    JClassOrInterface type = null;
    JNodeLookup lookup = session.getLookup();
    try {
      String typeSignature = NamingTools.getTypeSignatureName(oldName);
      type = (JClassOrInterface) lookup.getType(typeSignature);
    } catch (JLookupException e) {
      logger.log(Level.WARNING, "{0}:{1}: Type {2} not found",
          new Object[] {mappingFile.getPath(), Integer.valueOf(lineNumber), oldName});
    }
    if (type instanceof JDefinedClassOrInterface) {
      JDefinedClassOrInterface clOrI = (JDefinedClassOrInterface) type;
      int indexOfNewSimpleName = newName.lastIndexOf('.');
      String newSimpleName, newPackageName;
      if (indexOfNewSimpleName == -1) {
        newPackageName = "";
        newSimpleName = newName;
      } else {
        newPackageName = newName.substring(0, indexOfNewSimpleName).replace('.', '/');
        newSimpleName = newName.substring(indexOfNewSimpleName + 1, newName.length());
      }
      clOrI.addMarker(new OriginalPackageMarker(clOrI.getEnclosingPackage()));
      JPackage newEnclosingPackage = lookup.getOrCreatePackage(newPackageName);
      request.append(new ChangeEnclosingPackage(clOrI, newEnclosingPackage));
      while (newEnclosingPackage != null) {
        if (!newEnclosingPackage.containsMarker(KeepNameMarker.class)) {
          newEnclosingPackage.addMarker(KeepNameMarker.INSTANCE);
        }
        newEnclosingPackage = newEnclosingPackage.getEnclosingPackage();
      }

      rename(clOrI, newSimpleName);
      return clOrI;
    }
    return null;
  }

  private int readLineInfo(@Nonnull String line, int index) {
    char c = line.charAt(index);
    while (Character.isDigit(c) || c == ':') {
      index++;
      c = line.charAt(index);
    }
    return index;
  }

  private int readName(@Nonnull String line, int index, @Nonnull char[] stopChars) {
    int length = line.length();
    char c = line.charAt(index);
    while (!Character.isWhitespace(c) && Chars.indexOf(stopChars, c) == -1) {
      if (++index < length) {
        c = line.charAt(index);
      } else {
        break;
      }
    }
    return index;
  }

  /**
   * Reads this string until a whitespace or the '->' separator is read
   * or until the end of the string, starting the search at the specified index.
   * @param line the line to read from.
   * @param index the index to start the reading from.
   * @return the index of the first occurrence of a whitespace, the '->' separator
   * or the end of the string.
   */
  private int readNameUntilSeparatorOrWhitespace(@Nonnull String line, int index) {
    int length = line.length();
    char c = line.charAt(index);
    while (!Character.isWhitespace(c)) {
      if (c == '-' && line.charAt(index + 1) == '>') {
        // We check the second char to avoid bad parsing of names containing '-'
        break;
      }
      if (++index < length) {
        c = line.charAt(index);
      } else {
        break;
      }
    }
    return index;
  }

  private int readWhiteSpaces(@Nonnull String line, int index) {
    char c = line.charAt(index);
    while (Character.isWhitespace(c)) {
      index++;
      c = line.charAt(index);
    }
    return index;
  }

  private int readSeparator(
      @Nonnull String line, int index, @Nonnull File mappingFile, int lineNumber) {
    if (line.charAt(index) != '-' || line.charAt(index + 1) != '>') {
      throwException(mappingFile, lineNumber,
          "The mapping file is badly formatted (separator \"->\" expected)");
    }
    return index + 2;
  }

  @CheckForNull
  private JDefinedClassOrInterface readClassInfo(
      @Nonnull String line, @Nonnull JSession session, @Nonnull File mappingFile, int lineNumber) {
    // qualifiedOldClassName -> newClassName:
    try {
      int startIndex = readWhiteSpaces(line, 0);
      int endIndex = readNameUntilSeparatorOrWhitespace(line, startIndex);
      String qualifiedOldClassName = line.substring(startIndex, endIndex);
      startIndex = readWhiteSpaces(line, endIndex);
      startIndex = readSeparator(line, startIndex, mappingFile, lineNumber);
      startIndex = readWhiteSpaces(line, startIndex);
      endIndex = readName(line, startIndex, CLASSINFO_STOP_CHARS);
      String newClassName = line.substring(startIndex, endIndex);
      return createMappingForType(
          qualifiedOldClassName, newClassName, session, mappingFile, lineNumber);
    } catch (ArrayIndexOutOfBoundsException e) {
      throwException(
          mappingFile, lineNumber, "The mapping file is badly formatted (class mapping expected)");
      return null;
    }
  }

  @CheckForNull
  private JField findField(@Nonnull JDefinedClassOrInterface currentType,
      @Nonnull String oldName, @Nonnull String typeSignature) {
    List<JField> fields = currentType.getFields(oldName);
    for (JField field : fields) {
      if (GrammarActions.getSignatureFormatter().getName(field.getType()).equals(typeSignature)) {
        return field;
      }
    }
    return null;
  }

  private void readFieldInfo(@Nonnull String line, @Nonnull JDefinedClassOrInterface currentType,
      @Nonnull File mappingFile, int lineNumber) {
    // type oldFieldName -> newFieldName
    try {
      int startIndex = readWhiteSpaces(line, 0);
      int endIndex = readName(line, startIndex, EMPTY_STOP_CHARS);
      String typeSignature = GrammarActions.getSignature(line.substring(startIndex, endIndex));
      startIndex = readWhiteSpaces(line, endIndex);
      endIndex = readNameUntilSeparatorOrWhitespace(line, startIndex);
      String oldName = line.substring(startIndex, endIndex);
      int index = readWhiteSpaces(line, endIndex);
      index = readSeparator(line, index, mappingFile, lineNumber);
      startIndex = readWhiteSpaces(line, index);
      endIndex = readName(line, startIndex, EMPTY_STOP_CHARS);
      String newName = line.substring(startIndex, endIndex);
      JField field = findField(currentType, oldName, typeSignature);
      if (field != null) {
        if (newName.equals(oldName) || !FieldInHierarchyFinderVisitor
            .containsFieldKey(new FieldKey(newName, field.getType()), field)) {
          // No collision was found
          // (the name was not used or the field is renamed with its own name)
          renameField(field, mappingFile, lineNumber, newName);
          NewFieldKeyMarker marker = currentType.getMarker(NewFieldKeyMarker.class);
          if (marker == null) {
            marker = new NewFieldKeyMarker();
          }
          assert marker != null;
          marker.add(new FieldKey(newName, field.getType()));
        } else {
          throw new MappingCollisionException(
              new ColumnAndLineLocation(new FileLocation(mappingFile), lineNumber), field, newName);
        }
      } else {
        logger.log(Level.WARNING, "{0}:{1}: Field {2} not found in {3}", new Object[] {
            mappingFile.getPath(), Integer.valueOf(lineNumber), oldName,
            Jack.getUserFriendlyFormatter().getName(currentType)});
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throwException(
          mappingFile, lineNumber, "The mapping file is badly formatted (field mapping expected)");
    } catch (MappingCollisionException e) {
      if (collisionPolicy.equals(MappingCollisionPolicy.FAIL)) {
        MappingContextException mappingReportableExn = new MappingContextException(e);
        Jack.getSession().getReporter().report(Severity.FATAL, mappingReportableExn);
        throw new JackAbortException(mappingReportableExn);
      } else {
        Jack.getSession().getReporter().report(Severity.NON_FATAL, new MappingContextInfo(e));
      }
    }
  }

  private void rename(@Nonnull CanBeRenamed renamable, @Nonnull String newName) {
    MarkerManager markerManager = (MarkerManager) renamable;
    if (!markerManager.containsMarker(OriginalNameMarker.class)) {
      markerManager.addMarker(new OriginalNameMarker(((HasName) renamable).getName()));
      request.append(new Rename(renamable, newName));
    }
  }

/**
 * Rename a field using the provided name.
 * @param field The field that will be renamed
 * @param mappingFile The file where the mapping is stored
 * @param lineNumber The line number in the mapping file where the new name was defined
 * @param newName The new name of the field
 */
  protected void renameField(@Nonnull JField field, @Nonnull File mappingFile, int lineNumber,
      @Nonnull String newName) {
    rename(field.getId(), newName);
  }

  private int readChar(@Nonnull String line, int index, char expectedChar,
      @Nonnull File mappingFile, int lineNumber) {
    if (line.charAt(index) != expectedChar) {
      throwException(mappingFile, lineNumber,
          "The mapping file is badly formatted (\'" + expectedChar + "\' expected)");
    }
    return index + 1;
  }

  @CheckForNull
  private MethodKey getPreviousNewKey(@Nonnull JMethod method) {
    NewMethodKeyMarker marker =
        method.getEnclosingType().getMarker(NewMethodKeyMarker.class);
    if (marker != null) {
      return marker.getNewKey(method.getMethodId().getMethodIdWide());
    }
    return null;
  }

  private void readMethodInfo(@Nonnull String line, @Nonnull JDefinedClassOrInterface currentType,
      @Nonnull File mappingFile, int lineNumber, @Nonnull JNodeLookup lookup) {
    //  (startLineInfo:endLineInfo:)? type oldMethodName\((type(, type)*)?\) -> newMethodName
    try {
      int startIndex = readWhiteSpaces(line, 0);
      startIndex = readLineInfo(line, startIndex);
      startIndex = readWhiteSpaces(line, startIndex);
      int endIndex = readName(line, startIndex, EMPTY_STOP_CHARS);
      String typeSignature = GrammarActions.getSignature(line.substring(startIndex, endIndex));
      JType returnType = lookup.getType(typeSignature);
      startIndex = readWhiteSpaces(line, endIndex);
      endIndex = readName(line, startIndex, BEGIN_PARAMETER_STOP_CHARS);
      String oldName = line.substring(startIndex, endIndex);
      startIndex = readChar(line, endIndex, '(', mappingFile, lineNumber);
      List<JType> args = new ArrayList<JType>();
      startIndex = readWhiteSpaces(line, startIndex);
      while (line.charAt(startIndex) != ')') {
        endIndex = readName(line, startIndex, END_PARAMETER_STOP_CHARS);
        String parameterType = GrammarActions.getSignature(line.substring(startIndex, endIndex));
        args.add(lookup.getType(parameterType));
        startIndex = readWhiteSpaces(line, endIndex);
        if (line.charAt(startIndex) == ')') {
          break;
        }
        startIndex = readChar(line, startIndex, ',', mappingFile, lineNumber);
        startIndex = readWhiteSpaces(line, startIndex);
      }
      startIndex++;
      startIndex = readWhiteSpaces(line, startIndex);
      startIndex = readSeparator(line, startIndex, mappingFile, lineNumber);
      startIndex = readWhiteSpaces(line, startIndex);
      endIndex = readName(line, startIndex, EMPTY_STOP_CHARS);
      String newName = line.substring(startIndex, endIndex);
      try {
        JMethod method = currentType.getMethod(oldName, returnType, args);
        MethodKey newKey = new MethodKey(newName, args);
        JMethodIdWide methodId = method.getMethodId().getMethodIdWide();
        MethodKey previousNewKey = getPreviousNewKey(method);
        if (previousNewKey != null) {
          // The methodId was already renamed
          if (!previousNewKey.equals(newKey)) {
            throw new MappingCollisionException(
                new ColumnAndLineLocation(new FileLocation(mappingFile), lineNumber), method,
                newName);
          }
        } else if (newKey.getName().equals(oldName)
            || !MethodInHierarchyFinder.containsMethodKey(newKey, methodId)) {
          // No collision was found
          // (the name was not used or the method is renamed with its own name)
          renameMethod(method, mappingFile, lineNumber, newName);
          for (JMethod methodWithSameId : methodId.getMethods()) {
            JDefinedClassOrInterface methodWithSameIdEnclosingType =
                methodWithSameId.getEnclosingType();
            NewMethodKeyMarker marker =
                methodWithSameIdEnclosingType.getMarker(NewMethodKeyMarker.class);
            if (marker == null) {
              NewMethodKeyMarker newMarker = new NewMethodKeyMarker();
              marker = methodWithSameIdEnclosingType.addMarker(newMarker);
              if (marker == null) {
                marker = newMarker;
              }
            }
            assert marker != null;
            marker.add(methodId, newKey);
          }
        } else {
          throw new MappingCollisionException(
              new ColumnAndLineLocation(new FileLocation(mappingFile), lineNumber), method,
              newName);
        }
      } catch (JMethodLookupException e) {
        logger.log(Level.WARNING, "{0}:{1}: Method {2} not found in {3}", new Object[] {
            mappingFile.getPath(), Integer.valueOf(lineNumber), oldName,
            Jack.getUserFriendlyFormatter().getName(currentType)});
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throwException(
          mappingFile, lineNumber, "The mapping file is badly formatted (method mapping expected)");
    } catch (JTypeLookupException e) {
      logger.log(Level.WARNING, "{0}:{1}: {2}", new Object[]{mappingFile.getPath(),
          Integer.valueOf(lineNumber), e.getMessage()});
    } catch (MappingCollisionException e) {
      if (collisionPolicy.equals(MappingCollisionPolicy.FAIL)) {
        MappingContextException mappingReportableExn = new MappingContextException(e);
        Jack.getSession().getReporter().report(Severity.FATAL, mappingReportableExn);
        throw new JackAbortException(mappingReportableExn);
      } else {
        Jack.getSession().getReporter().report(
            Severity.NON_FATAL,
            new MappingContextInfo(e));
      }
    }
  }

  /**
   * Rename a method using the provided name.
   * @param method The method that will be renamed
   * @param mappingFile The file where the mapping is stored
   * @param lineNumber The line number in the mapping file where the new name was defined
   * @param newName The new name of the method
   */
  protected void renameMethod(@Nonnull JMethod method, @Nonnull File mappingFile, int lineNumber,
      @Nonnull String newName) {
    String oldName = method.getName();
    if (oldName.equals(NamingTools.INIT_NAME)) {
      logger.log(Level.WARNING, "{0}:{1}: Constructors cannot be renamed",
          new Object[] {mappingFile.getPath(), Integer.valueOf(lineNumber)});
    } else if (oldName.equals(NamingTools.STATIC_INIT_NAME)) {
      logger.log(Level.WARNING, "{0}:{1}: Static initializers cannot be renamed",
          new Object[] {mappingFile.getPath(), Integer.valueOf(lineNumber)});
    } else {
      rename(method.getMethodIdWide(), newName);
    }
  }

  /**
   * The mapping format must be: qualifiedOldClassName -> newClassName: type oldFieldName ->
   * newFieldName (startLineInfo:endLineInfo:)? type oldMethodName\((type(, type)*)?\) ->
   * newMethodName
   *
   * type must be in the java form (e.g. java.lang.String, boolean).
   *
   * @param mappingFile
   * @param session
   * @throws JackIOException
   */
  public void applyMapping(@Nonnull File mappingFile, @Nonnull JSession session)
      throws JackIOException {
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new FileReader(mappingFile));
      String line = reader.readLine();
      JDefinedClassOrInterface currentType = null;

      while (line != null) {
        if (isClassInfo(line)) {
          currentType = readClassInfo(line, session, mappingFile, reader.getLineNumber());
        } else {
          if (currentType != null) {
            if (isMethodInfo(line)) {
              readMethodInfo(
                  line, currentType, mappingFile, reader.getLineNumber(), session.getLookup());
            } else {
              readFieldInfo(line, currentType, mappingFile, reader.getLineNumber());
            }
          }
        }
        line = reader.readLine();
      }
    } catch (IOException e) {
      throw new JackIOException("Error while reading mapping " + mappingFile.getPath(), e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          logger.log(Level.WARNING,
              "Failed to close reader while reading mapping {0}", mappingFile.getPath());
        }
      }
    }
  }

}
