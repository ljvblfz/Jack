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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Asm helpers.
 */
public class AsmHelper {

  @Nonnull
  private static final String JAVA_LANG_ENUM = "java/lang/Enum";

  private static final int JAVA_ACCESS_FLAGS_MASK = 0xFFFF;

  @Nonnull
  private static final String GENERIC_SIGNATURE_START = "<";
  @Nonnull
  private static final String GENERIC_SIGNATURE_END = ">";

  public static boolean isGenericSignature(@Nonnull ClassNode cn) {
    if (cn.signature != null && cn.signature.contains(GENERIC_SIGNATURE_START)) {
      assert cn.signature.contains(GENERIC_SIGNATURE_END);
      return true;
    }
    return false;
  }

  public static boolean isInterface(@Nonnull ClassNode cn) {
    return ((cn.access & Opcodes.ACC_INTERFACE) != 0);
  }

  public static boolean isAnnotation(@Nonnull ClassNode cn) {
    return ((cn.access & Opcodes.ACC_ANNOTATION) != 0);
  }

  public static boolean isEnum(@Nonnull ClassNode cn) {
    if ((cn.access & Opcodes.ACC_ENUM) != 0) {
      assert cn.superName != null;
      return cn.superName.equals(JAVA_LANG_ENUM);
    }
    return false;
  }

  public static boolean isStatic(@Nonnull FieldNode fn) {
    return ((fn.access & Opcodes.ACC_STATIC) != 0);
  }

  public static boolean isEnumField(@Nonnull FieldNode fn) {
    return ((fn.access & Opcodes.ACC_ENUM) != 0);
  }

  public static boolean isNative(@Nonnull MethodNode mn) {
    return ((mn.access & Opcodes.ACC_NATIVE) != 0);
  }

  public static boolean isAbstract(@Nonnull MethodNode mn) {
    return ((mn.access & Opcodes.ACC_ABSTRACT) != 0);
  }

  public static boolean isStatic(@Nonnull MethodNode mn) {
    return ((mn.access & Opcodes.ACC_STATIC) != 0);
  }

  public static boolean isPrivate(MethodNode mn) {
    return ((mn.access & Opcodes.ACC_PRIVATE) != 0);
  }

  public static boolean isConstructor(@Nonnull MethodNode mn) {
    return mn.name.equals("<init>");
  }

  public static boolean isStaticInit(@Nonnull MethodNode mn) {
    return mn.name.equals("<clinit>");
  }

  public static boolean isLocalStoreOf(@Nonnull AbstractInsnNode insn, @Nonnegative int localIdx) {
    if (!(insn instanceof VarInsnNode) && !(insn instanceof IincInsnNode)) {
      return false;
    }

    switch (insn.getOpcode()) {
      case Opcodes.IINC: {
        return ((IincInsnNode) insn).var == localIdx;
      }
      case Opcodes.ASTORE:
      case Opcodes.LSTORE:
      case Opcodes.DSTORE:
      case Opcodes.FSTORE:
      case Opcodes.ISTORE: {
        VarInsnNode varInsn = (VarInsnNode) insn;
        return varInsn.var == localIdx;
      }
      default : {
        return false;
      }
    }
  }

  public static boolean isLocalLoadOf(@Nonnull AbstractInsnNode insn, @Nonnegative int localIdx) {
    if (!(insn instanceof VarInsnNode) && !(insn instanceof IincInsnNode)) {
      return false;
    }

    switch (insn.getOpcode()) {
      case Opcodes.IINC: {
        return ((IincInsnNode) insn).var == localIdx;
      }
      case Opcodes.ALOAD:
      case Opcodes.LLOAD:
      case Opcodes.DLOAD:
      case Opcodes.FLOAD:
      case Opcodes.ILOAD: {
        VarInsnNode varInsn = (VarInsnNode) insn;
        return varInsn.var == localIdx;
      }
      default : {
        return false;
      }
    }
  }

  @Nonnull
  public static String getDescriptor(@Nonnull ClassNode cn) {
    return Type.getObjectType(cn.name).getDescriptor();
  }

  @Nonnull
  public static List<String> getDescriptorsFromInternalNames(
      @Nonnull List<String> internalInterfaceNames) {
    List<String> interfaceDescs = new ArrayList<String>(internalInterfaceNames.size());

    for (String internalName : internalInterfaceNames) {
      interfaceDescs.add(Type.getObjectType(internalName).getDescriptor());
    }

    return interfaceDescs;
  }

  @Nonnull
  public static String getSourceName(@Nonnull ClassNode cn) {
    if (cn.innerClasses != null) {
      // Class is either an inner/local/anonymous or has inner classes
      InnerClassNode matchingInnerClassNode = null;
      for (InnerClassNode innerClassNode : cn.innerClasses) {
        if (innerClassNode.name.equals(cn.name)) {
          matchingInnerClassNode = innerClassNode;
          break;
        }
      }
      if (matchingInnerClassNode != null) {
        return (matchingInnerClassNode.innerName != null) ? matchingInnerClassNode.innerName : "";
      }
    }
    int lastPathSeparatorIndex = cn.name.lastIndexOf('/');
    int startIndex = lastPathSeparatorIndex >= 0 ? lastPathSeparatorIndex + 1 : 0;
    return cn.name.substring(startIndex);
  }

  public static int getModifiers(@Nonnull ClassNode cn) {
    int modifier = cn.access;

    if (cn.innerClasses != null) {
      // Class is either an inner/local/anonymous or has inner classes
      InnerClassNode matchingInnerClassNode = null;
      for (InnerClassNode innerClassNode : cn.innerClasses) {
        if (innerClassNode.name.equals(cn.name)) {
          matchingInnerClassNode = innerClassNode;
          break;
        }
      }
      if (matchingInnerClassNode != null) {
        modifier = matchingInnerClassNode.access;
      }
    }
    // "super" is not relevant in dex format
    modifier &= ~Opcodes.ACC_SUPER;
    return (modifier & JAVA_ACCESS_FLAGS_MASK);
  }

  public static int getModifiers(@Nonnull MethodNode mn) {
    return (mn.access & JAVA_ACCESS_FLAGS_MASK);
  }

  public static int getModifiers(@Nonnull FieldNode fn) {
    return (fn.access & JAVA_ACCESS_FLAGS_MASK);
  }

}
