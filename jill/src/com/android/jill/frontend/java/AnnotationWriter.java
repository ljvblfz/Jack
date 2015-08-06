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
import com.android.jill.backend.jayce.JayceWriter;
import com.android.jill.backend.jayce.Token;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Annotation transformer to Jayce.
 */
public class AnnotationWriter extends JillWriter {

  private static final String JAVA_LANG_SYNTHETIC = "Ljava/lang/Synthetic;";

  public AnnotationWriter(@Nonnull JayceWriter writer,
      @Nonnull SourceInfoWriter sourceInfoWriter) {
    super(writer, sourceInfoWriter);
  }

  public void writeRetentionPolicy(@Nonnull ClassNode cn) throws IOException {
    assert AsmHelper.isAnnotation(cn);

    boolean retentionAnnotationFound = false;

    if (cn.visibleAnnotations != null) {
      for (AnnotationNode anno : cn.visibleAnnotations) {
        // Into Jayce, retention policy is written as a token
        if (anno.desc.equals(Type.getType(Retention.class).getDescriptor())) {
          assert anno.values.size() == 2;
          assert anno.values.get(0) instanceof String;
          assert ((String) anno.values.get(0)).equals("value");
          assert anno.values.get(1) instanceof String[];

          retentionAnnotationFound = true;

          String[] enumAccess = (String[]) anno.values.get(1);
          assert enumAccess.length == 2;
          assert enumAccess[0].equals(Type.getType(java.lang.annotation.RetentionPolicy.class)
              .getDescriptor());
          if (enumAccess[1].equals(RetentionPolicy.CLASS.toString())) {
            writer.writeRetentionPolicyEnum(RetentionPolicy.CLASS);
          } else if (enumAccess[1].equals(RetentionPolicy.SOURCE.toString())) {
            writer.writeRetentionPolicyEnum(RetentionPolicy.SOURCE);
          } else if (enumAccess[1].equals(RetentionPolicy.RUNTIME.toString())) {
            writer.writeRetentionPolicyEnum(RetentionPolicy.RUNTIME);
          } else {
            throw new JillException("Unknown retention policy.");
          }
          break;
        }
      }
    }

    if (!retentionAnnotationFound) {
      // Default retention policy as specify in java doc of Annotation Type Retention.
      writer.writeRetentionPolicyEnum(RetentionPolicy.CLASS);
    }
  }

  public void writeAnnotations(@Nonnull ClassNode cn) throws IOException {
    writer.writeOpenNodeList();

    writeAnnotations(cn.invisibleAnnotations, RetentionPolicy.CLASS);
    writeAnnotations(cn.visibleAnnotations, RetentionPolicy.RUNTIME);

    writer.writeCloseNodeList();
  }

  public void writeAnnotations(@Nonnull MethodNode mn) throws IOException {
    writer.writeOpenNodeList();

    writeAnnotations(mn.invisibleAnnotations, RetentionPolicy.CLASS);
    writeAnnotations(mn.visibleAnnotations, RetentionPolicy.RUNTIME);

    writer.writeCloseNodeList();
  }

  public void writeAnnotations(@Nonnull FieldNode fn) throws IOException {
    writer.writeOpenNodeList();

    writeAnnotations(fn.invisibleAnnotations, RetentionPolicy.CLASS);
    writeAnnotations(fn.visibleAnnotations, RetentionPolicy.RUNTIME);

    writer.writeCloseNodeList();
  }

  public void writeAnnotations(@Nonnull MethodNode mn, @Nonnegative int parameterAnnotIdx)
      throws IOException {
    writer.writeOpenNodeList();

    if (mn.invisibleParameterAnnotations != null) {
      writeAnnotations(mn.invisibleParameterAnnotations[parameterAnnotIdx], RetentionPolicy.CLASS);
    }

    if (mn.visibleParameterAnnotations != null) {
      writeAnnotations(mn.visibleParameterAnnotations[parameterAnnotIdx], RetentionPolicy.RUNTIME);
    }

    writer.writeCloseNodeList();
  }

  @Override
  public void writeValue(Object value) throws IOException {
    if (value instanceof String) {
      writeValue((String) value);
    } else if (value instanceof Integer) {
      writeValue(((Integer) value).intValue());
    } else if (value instanceof Boolean) {
      writeValue(((Boolean) value).booleanValue());
    } else if (value instanceof Byte) {
      writeValue(((Byte) value).byteValue());
    } else if (value instanceof Character) {
      writeValue(((Character) value).charValue());
    } else if (value instanceof Short) {
      writeValue(((Short) value).shortValue());
    } else if (value instanceof Float) {
      writeValue(((Float) value).floatValue());
    } else if (value instanceof Double) {
      writeValue(((Double) value).doubleValue());
    } else if (value instanceof Long) {
      writeValue(((Long) value).longValue());
    } else if (value instanceof String[]) {
      writeValue((String[]) value);
    } else if (value == null) {
      writeValue();
    } else if (value instanceof Type) {
      writeValue((Type) value);
    } else if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive()) {
      writeValue(convertPrimitiveArrayToObject(value));
    } else if (value instanceof List) {
      writeValue(((List<?>) value).toArray());
    } else if (value instanceof AnnotationNode) {
      AnnotationNode annotationNode = (AnnotationNode) value;
      assert !JAVA_LANG_SYNTHETIC.equals(annotationNode.desc);
      writeAnnotation(annotationNode, RetentionPolicy.UNKNOWN);
    } else {
      throw new JillException("Not yet supported.");
    }
  }

  private void writeAnnotations(@CheckForNull List<AnnotationNode> annotations,
      @Nonnull RetentionPolicy retentionPolicy) throws IOException {
    if (annotations != null) {
      for (AnnotationNode anno : annotations) {
        if (!JAVA_LANG_SYNTHETIC.equals(anno.desc)) {
          writeAnnotation(anno, retentionPolicy);
        }
      }
    }
  }

  private void writeAnnotation(@Nonnull AnnotationNode anno,
      @Nonnull RetentionPolicy retentionPolicy) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.ANNOTATION);
    writer.writeOpen();
    writer.writeRetentionPolicyEnum(retentionPolicy);
    writer.writeId(anno.desc);
    writeNameValuePair(anno.values);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  private void writeNameValuePair(@CheckForNull List<Object> values) throws IOException {
    writer.writeOpenNodeList();

    if (values != null) {
      for (int i = 0; i < values.size(); i += 2) {
        String name = (String) values.get(i);
        Object value = values.get(i + 1);
        sourceInfoWriter.writeUnknwonDebugBegin();
        writer.writeKeyword(Token.NAME_VALUE_PAIR);
        writer.writeOpen();
        writer.writeString(name);
        writeValue(value);
        sourceInfoWriter.writeUnknownDebugEnd();
        writer.writeClose();
      }
    }

    writer.writeCloseNodeList();
  }

  private void writeValue(@Nonnull String[] value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.ENUM_LITERAL);
    writer.writeOpen();
    writer.writeId(value[0]);
    writer.writeString(value[1]);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }
}
