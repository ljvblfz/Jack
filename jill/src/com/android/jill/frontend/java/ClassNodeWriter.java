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

import com.android.jill.Options;
import com.android.jill.backend.jayce.JayceWriter;
import com.android.jill.backend.jayce.Token;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Write class node of Asm into Jayce.
 */
public class ClassNodeWriter extends JillWriter {

  // Extra Jack modifiers
  public static final int COMPILE_TIME_CONSTANT = 0x20000;
  public static final int ANONYMOUS_TYPE = 0x40000;

  @Nonnull
  private final AnnotationWriter annotWriter;

  private static final int ORDINAL_UNKNOWN = -1;

  @Nonnull
  private final Options options;

  public ClassNodeWriter(@Nonnull JayceWriter writer,
      @Nonnull SourceInfoWriter sourceInfoWriter,
      @Nonnull Options options) {
    super(writer, sourceInfoWriter);
    annotWriter = new AnnotationWriter(writer, sourceInfoWriter);
    this.options = options;
  }

  public void write(@Nonnull ClassNode cn) throws IOException {
    if (AsmHelper.isAnnotation(cn)) {
      writeAnnotation(cn);
    } else if (AsmHelper.isInterface(cn)) {
      writeInterface(cn);
    } else if (AsmHelper.isEnum(cn)){
      writeEnum(cn);
    } else {
      writeClass(cn);
    }
  }

  private void writeEnum(@Nonnull ClassNode cn) throws IOException {
    sourceInfoWriter.writeDebugBegin(cn);
    writer.writeKeyword(Token.ENUM);
    writer.writeOpen();
    writer.writeInt(AsmHelper.getModifiers(cn));
    writer.writeId(AsmHelper.getDescriptor(cn));
    writer.writeId(cn.superName != null ? Type.getObjectType(cn.superName).getDescriptor() : null);
    writer.writeIds(AsmHelper.getDescriptorsFromInternalNames(cn.interfaces));
    writeEnclosingInformation(cn);
    writingInners(cn);
    writeEnumFields(cn);
    writeMethods(cn);
    annotWriter.writeAnnotations(cn);
    writer.writeOpenNodeList(); // Markers
    writeGenericSignatureMarker(cn);
    writeSourceNameMarker(cn);
    writeThisRefTypeInfoMarker(cn);
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(cn);
    writer.writeClose();
  }

  private void writeClass(@Nonnull ClassNode cn) throws IOException {
    sourceInfoWriter.writeDebugBegin(cn);
    writer.writeKeyword(Token.CLASS);
    writer.writeOpen();
    writer.writeInt(
        AsmHelper.getModifiers(cn) | (AsmHelper.getSourceName(cn).equals("") ? ANONYMOUS_TYPE : 0));
    writer.writeId(AsmHelper.getDescriptor(cn));
    writer.writeId(cn.superName != null ? Type.getObjectType(cn.superName).getDescriptor() : null);
    writer.writeIds(AsmHelper.getDescriptorsFromInternalNames(cn.interfaces));
    writeEnclosingInformation(cn);
    writingInners(cn);
    writeFields(cn);
    writeMethods(cn);
    annotWriter.writeAnnotations(cn);
    writer.writeOpenNodeList(); // Markers
    writeGenericSignatureMarker(cn);
    writeSourceNameMarker(cn);
    writeThisRefTypeInfoMarker(cn);
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(cn);
    writer.writeClose();
  }

  private void writeInterface(@Nonnull ClassNode cn) throws IOException {
    assert isPackageInfoIfNotAbstract(cn);
    sourceInfoWriter.writeDebugBegin(cn);
    writer.writeKeyword(Token.INTERFACE);
    writer.writeOpen();
    writer.writeInt(AsmHelper.getModifiers(cn) | Opcodes.ACC_ABSTRACT);
    writer.writeId(AsmHelper.getDescriptor(cn));
    writer.writeIds(AsmHelper.getDescriptorsFromInternalNames(cn.interfaces));
    writeEnclosingInformation(cn);
    writingInners(cn);
    writeFields(cn);
    writeMethods(cn);
    annotWriter.writeAnnotations(cn);
    writer.writeOpenNodeList(); // Markers
    writeGenericSignatureMarker(cn);
    writeSourceNameMarker(cn);
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(cn);
    writer.writeClose();
  }

  private boolean isPackageInfoIfNotAbstract(@Nonnull ClassNode cn) {
    return !(((AsmHelper.getModifiers(cn) & Opcodes.ACC_ABSTRACT) == 0)
        && !cn.name.endsWith("package-info"));
  }

  private void writeAnnotation(@Nonnull ClassNode cn) throws IOException {
    sourceInfoWriter.writeDebugBegin(cn);
    writer.writeKeyword(Token.ANNOTATION_TYPE);
    writer.writeOpen();
    annotWriter.writeRetentionPolicy(cn);
    writer.writeInt(AsmHelper.getModifiers(cn));
    writer.writeId(AsmHelper.getDescriptor(cn));
    writer.writeIds(AsmHelper.getDescriptorsFromInternalNames(cn.interfaces));
    writeEnclosingInformation(cn);
    writingInners(cn);
    writeFields(cn);
    writeAnnotationMethods(cn);
    annotWriter.writeAnnotations(cn);
    writer.writeOpenNodeList(); // Markers
    writeGenericSignatureMarker(cn);
    writeSourceNameMarker(cn);
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(cn);
    writer.writeClose();
  }

  private void writeThisRefTypeInfoMarker(@Nonnull ClassNode cn) throws IOException {
    String thisRefSignature = null;
    for (MethodNode mn : cn.methods) {
      if (!AsmHelper.isStatic(mn) && mn.localVariables != null) {
        for (LocalVariableNode lvn : mn.localVariables) {
          if (lvn.name.equals("this")) {
            if (thisRefSignature == null) {
              thisRefSignature = lvn.signature;
            } else {
              assert thisRefSignature.equals(lvn.signature);
            }
          }
        }
      }
    }
    if (thisRefSignature != null) {
      writer.writeKeyword(Token.THIS_REF_TYPE_INFO);
      writer.writeOpen();
      writer.writeString(thisRefSignature);
      writer.writeClose();
    }
  }

  private void writeGenericSignatureMarker(@Nonnull ClassNode cn) throws IOException {
    if (AsmHelper.isGenericSignature(cn)) {
      writer.writeKeyword(Token.GENERIC_SIGNATURE);
      writer.writeOpen();
      writer.writeString(cn.signature);
      writer.writeClose();
    }
  }

  private void writeSourceNameMarker(@Nonnull ClassNode cn) throws IOException {
    writer.writeKeyword(Token.SIMPLE_NAME);
    writer.writeOpen();
    writer.writeString(AsmHelper.getSourceName(cn));
    writer.writeClose();
  }

  private void writeGenericSignatureMarker(@Nonnull FieldNode fn) throws IOException {
    if (fn.signature != null) {
      writer.writeKeyword(Token.GENERIC_SIGNATURE);
      writer.writeOpen();
      writer.writeString(fn.signature);
      writer.writeClose();
    }
  }

  private void writeFields(@Nonnull ClassNode cn) throws IOException {
    writer.writeOpenNodeList();
    for (FieldNode fn : cn.fields) {
      writeField(cn, fn, Token.FIELD);
    }
    writer.writeCloseNodeList();
  }

  private void writeEnumFields(@Nonnull ClassNode cn) throws IOException {
    writer.writeOpenNodeList();
    for (FieldNode fn : cn.fields) {
      if (!AsmHelper.isEnumField(fn)) {
        writeField(cn, fn, Token.FIELD);
      } else {
        writeField(cn, fn, Token.ENUM_FIELD);
      }
    }
    writer.writeCloseNodeList();
  }

  private void writeField(@Nonnull ClassNode cn, @Nonnull FieldNode fn, @Nonnull Token kind)
      throws IOException {
    assert kind == Token.FIELD || kind == Token.ENUM_FIELD;
    sourceInfoWriter.writeDebugBegin(cn, fn);
    writer.writeKeyword(kind);
    writer.writeOpen();
    writer.writeInt(fn.value != null ? AsmHelper.getModifiers(fn) | COMPILE_TIME_CONSTANT
        : AsmHelper.getModifiers(fn));
    writer.writeString(fn.desc);
    writer.writeString(fn.name);
    writeFieldValue(cn, fn);
    if (kind == Token.ENUM_FIELD) {
      writer.writeInt(ORDINAL_UNKNOWN);
    }
    annotWriter.writeAnnotations(fn);
    writer.writeOpenNodeList(); // Markers
    writeGenericSignatureMarker(fn);
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(cn, fn);
    writer.writeClose();
  }

  private void writeFieldValue(@Nonnull ClassNode cn, @Nonnull FieldNode fn) throws IOException {
    if (AsmHelper.isStatic(fn)) {
      Object value = fn.value;
      if (value instanceof Integer) {
        int intValue = ((Integer) value).intValue();
        if (fn.desc.equals("Z")) {
          writeValue(intValue != 0);
        } else if (fn.desc.equals("B")) {
          writeValue((byte) intValue);
        } else if (fn.desc.equals("C")) {
          writeValue((char) intValue);
        } else if (fn.desc.equals("S")) {
          writeValue((short) intValue);
        } else {
          writeValue(intValue);
        }
      } else if (value instanceof Long) {
        writeValue(((Long) value).longValue());
      } else if (value instanceof Float) {
        writeValue(((Float) value).floatValue());
      } else if (value instanceof Double) {
        writeValue(((Double) value).doubleValue());
      } else if (value instanceof String) {
        writeValue((String) value);
      } else {
        writer.writeNull(); // No initial value
      }
    } else {
      writer.writeNull(); // No initial value
    }
  }

  private void writeAnnotationMethods(@Nonnull ClassNode cn) throws IOException {
    assert AsmHelper.isAnnotation(cn);

    writer.writeOpenNodeList();

    for (MethodNode mn : cn.methods) {
      new MethodBodyWriter(writer, annotWriter, cn, mn, sourceInfoWriter, options).write();
    }
    writer.writeCloseNodeList();
  }

  private void writeMethods(@Nonnull ClassNode cn) throws IOException {
    writer.writeOpenNodeList();

    for (MethodNode mn : cn.methods) {
      new MethodBodyWriter(writer, annotWriter, cn, mn, sourceInfoWriter, options).write();
    }
    writer.writeCloseNodeList();
  }

  private void writingInners(@Nonnull ClassNode cn) throws IOException {
    List<InnerClassNode> innerClasses = cn.innerClasses;
    List<String> innerIds = new ArrayList<String>();

    if (innerClasses != null) {
      // Class is either an inner/local/anonymous or has inner classes
      InnerClassNode matchingInnerClassNode = null;
      for (InnerClassNode innerClassNode : innerClasses) {
        if (innerClassNode.outerName != null && innerClassNode.outerName.equals(cn.name)
            && innerClassNode.name != null) {
          innerIds.add(Type.getObjectType(innerClassNode.name).getDescriptor());
        }
      }
    }
    writer.writeIds(innerIds);
  }

  private void writeEnclosingInformation(@Nonnull ClassNode cn) throws IOException {
    List<InnerClassNode> innerClasses = cn.innerClasses;
    if (innerClasses != null) {
      // Class is either an inner/local/anonymous or has inner classes
      InnerClassNode matchingInnerClassNode = null;
      for (InnerClassNode innerClassNode : innerClasses) {
        if (innerClassNode.name.equals(cn.name)) {
          matchingInnerClassNode = innerClassNode;
          break;
        }
      }
      if (matchingInnerClassNode != null) {
        // Inner, anonymous or local)
        if (cn.outerMethod != null) {
          // Local or anonymous in method
          assert cn.outerMethodDesc != null;
          if (!(AsmHelper.isInterface(cn) || AsmHelper.isAnnotation(cn))) {
            // EnclosingClass
            writer.writeId(Type.getObjectType(cn.outerClass).getDescriptor()); // EnclosingType
            writer.writeId(Type.getObjectType(cn.outerClass).getDescriptor());
            writer.writeId(cn.outerMethod + cn.outerMethodDesc); // EnclosingMethod
          } else {
            writer.writeId(null); // EnclosingType
          }
        } else {
          // Inner or anonymous as init of field
          String outerClassName =
              (cn.outerClass != null) ? (cn.outerClass) : (matchingInnerClassNode.outerName);
          if (outerClassName != null) {
            writer.writeId(Type.getObjectType(outerClassName).getDescriptor()); // EnclosingType
          } else {
            writer.writeId(null); // EnclosingType unknown
          }
          if (!(AsmHelper.isInterface(cn) || AsmHelper.isAnnotation(cn))) {
            writer.writeId(null); // EnclosingMethodClass
            writer.writeId(null); // EnclosingMethod
          }
        }
      } else {
        writer.writeId(null); // EnclosingType
        if (!(AsmHelper.isInterface(cn) || AsmHelper.isAnnotation(cn))) {
          writer.writeId(null); // EnclosingMethodClass
          writer.writeId(null); // EnclosingMethod
        }
      }
    } else {
      writer.writeId(null); // EnclosingType
      if (!(AsmHelper.isInterface(cn) || AsmHelper.isAnnotation(cn))) {
        writer.writeId(null); // EnclosingMethodClass
        writer.writeId(null); // EnclosingMethod
      }
    }
  }

}
