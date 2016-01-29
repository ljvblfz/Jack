/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.ir.ast;

/**
 * Modifiers.
 */
public class JModifier {

  public static final int DEFAULT = 0;

  public static final int PUBLIC       = 0x0001;
  public static final int PRIVATE      = 0x0002;
  public static final int PROTECTED    = 0x0004;
  public static final int STATIC       = 0x0008;
  public static final int FINAL        = 0x0010;
  public static final int SUPER        = 0x0020;
  public static final int SYNCHRONIZED = 0x0020;
  public static final int VOLATILE     = 0x0040;
  public static final int BRIDGE       = 0x0040;
  public static final int TRANSIENT    = 0x0080;
  public static final int VARARGS      = 0x0080;
  public static final int NATIVE       = 0x0100;
  public static final int INTERFACE    = 0x0200;
  public static final int ABSTRACT     = 0x0400;
  public static final int STRICTFP     = 0x0800;
  public static final int SYNTHETIC    = 0x1000;
  public static final int ANNOTATION   = 0x2000;
  public static final int ENUM         = 0x4000;
  public static final int STATIC_INIT  = 0x10000;
  public static final int DEPRECATED   = 0x100000;

  // Extra Jack modifiers
  public static final int COMPILE_TIME_CONSTANT = 0x20000;
  //Jack does not longer support ANONYMOUS_TYPE modifier, nevertheless old libraries can have it
  @Deprecated
  public static final int ANONYMOUS_TYPE        = 0x40000;

  private static final int TYPE_MODIFIER_MASK = PUBLIC | PROTECTED | PRIVATE | STATIC | FINAL
      | ENUM | SYNTHETIC | ABSTRACT | INTERFACE | ANNOTATION | SUPER | STRICTFP | DEPRECATED;

  private static final int FIELD_MODIFIER_MASK = PUBLIC | PROTECTED | PRIVATE | STATIC
      | FINAL | TRANSIENT | VOLATILE | ENUM | SYNTHETIC | COMPILE_TIME_CONSTANT | DEPRECATED;

  private static final int METHOD_MODIFIER_MASK = PUBLIC | PROTECTED | PRIVATE | STATIC | NATIVE
      | ABSTRACT | FINAL | SYNCHRONIZED | BRIDGE | SYNTHETIC | STRICTFP | VARARGS | STATIC_INIT
      | DEPRECATED;

  private static final int LOCAL_MODIFIER_MASK = FINAL | SYNTHETIC;

  public static boolean isPublic(int modifier) {
    return ((modifier & PUBLIC) == PUBLIC);
  }

  public static boolean isPrivate(int modifier) {
    return ((modifier & PRIVATE) == PRIVATE);
  }

  public static boolean isProtected(int modifier) {
    return ((modifier & PROTECTED) == PROTECTED);
  }

  public static boolean isStatic(int modifier) {
    return ((modifier & STATIC) == STATIC);
  }

  public static boolean isFinal(int modifier) {
    return ((modifier & FINAL) == FINAL);
  }

  public static boolean isSynchronized(int modifier) {
    return ((modifier & SYNCHRONIZED) == SYNCHRONIZED);
  }

  public static boolean isVolatile(int modifier) {
    return ((modifier & VOLATILE) == VOLATILE);
  }

  public static boolean isBridge(int modifier) {
    return ((modifier & BRIDGE) == BRIDGE);
  }

  public static boolean isTransient(int modifier) {
    return ((modifier & TRANSIENT) == TRANSIENT);
  }

  public static boolean isVarargs(int modifier) {
    return ((modifier & VARARGS) == VARARGS);
  }

  public static boolean isNative(int modifier) {
    return ((modifier & NATIVE) == NATIVE);
  }

  public static boolean isInterface(int modifier) {
    return ((modifier & INTERFACE) == INTERFACE);
  }

  public static boolean isAbstract(int modifier) {
    return ((modifier & ABSTRACT) == ABSTRACT);
  }

  public static boolean isStrictfp(int modifier) {
    return ((modifier & STRICTFP) == STRICTFP);
  }

  public static boolean isSynthetic(int modifier) {
    return ((modifier & SYNTHETIC) == SYNTHETIC);
  }

  public static boolean isAnnotation(int modifier) {
    return ((modifier & ANNOTATION) == ANNOTATION);
  }

  public static boolean isEnum(int modifier) {
    return ((modifier & ENUM) == ENUM);
  }

  public static boolean isCompileTimeConstant(int modifier) {
    return ((modifier & COMPILE_TIME_CONSTANT) == COMPILE_TIME_CONSTANT);
  }

  public static boolean isStaticInitializer(int modifier) {
    return ((modifier & STATIC_INIT) == STATIC_INIT);
  }

  public static boolean isDeprecated(int modifier) {
    return ((modifier & DEPRECATED) == DEPRECATED);
  }

  /**
   * Check that this modifier only has flags meant for types
   * @param modifier the modifier to test
   * @return true if the modifier is conform, false otherwise
   */
  public static boolean isTypeModifier(int modifier) {
    return ((modifier & TYPE_MODIFIER_MASK) == modifier);
  }

  /**
   * Check if the type modifier is valid (all flags are compatibles)
   * @param modifier to test
   * @return true if all flags are compatibles, false otherwise
   */
  public static boolean isValidTypeModifier(int modifier) {
    if (!checkAccessibilityFlags(modifier)) {
      return false;
    }

    // Check annotation
    if (isAnnotation(modifier) && isInterface(modifier) && isAbstract(modifier)
        && !isFinal(modifier) && !isEnum(modifier)) {
      return true;
    }

    // Check interface
    if (isInterface(modifier) && isAbstract(modifier)
        && !isFinal(modifier) && !isEnum(modifier)) {
      return true;
    }

    // Check enum
    if (isEnum(modifier) && !isAbstract(modifier)) {
      return true;
    }

    // Check class
    if (isAbstract(modifier) && isFinal(modifier)) {
      return false;
    }

    return true;
  }

  /**
   * Check that this modifier only has flags meant for fields
   * @param modifier the modifier to test
   * @return true if the modifier is conform, false otherwise
   */
  public static boolean isFieldModifier(int modifier) {
    return ((modifier & FIELD_MODIFIER_MASK) == modifier);
  }

  /**
   * Check if the field modifier is valid (all flags are compatibles)
   * @param modifier to test
   * @return true if all flags are compatibles, false otherwise
   */
  public static boolean isValidFieldModifier(int modifier) {
    if (!checkAccessibilityFlags(modifier)) {
      return false;
    }
    return !(isFinal(modifier) && isVolatile(modifier));
  }
  /**
   * Check that the accessibility flags (public, private, protected) are valid
   * @param modifier the modifier to test
   * @return false if two accessibility flags or more are set
   */
  public static boolean checkAccessibilityFlags(int modifier) {
    if (isPublic(modifier)) {
      if (isProtected(modifier) || isPrivate(modifier)) {
        return false;
      }
    }
    if (isProtected(modifier)) {
      if (isPublic(modifier) || isPrivate(modifier)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check that this modifier only has flags meant for methods
   * @param modifier the modifier to test
   * @return true if the modifier is conform, false otherwise
   */
  public static boolean isMethodModifier(int modifier) {
    return ((modifier & METHOD_MODIFIER_MASK) == modifier);
  }

  /**
   * Check if the method modifier is valid (all flags are compatibles)
   * @param modifier to test
   * @return true if all flags are compatibles, false otherwise
   */
  public static boolean isValidMethodModifier(int modifier) {
    if (!checkAccessibilityFlags(modifier)) {
      return false;
    }
    if (isAbstract(modifier)) {
      if (isFinal(modifier)
          || isNative(modifier)
          || isPrivate(modifier)
          || isStatic(modifier)
          || isStrictfp(modifier)
          || isSynchronized(modifier)) {
        return false;
      }
    }

    if (isBridge(modifier) && isSynchronized(modifier)) {
      return false;
    }

    if (isStaticInitializer(modifier) && !isStatic(modifier)) {
      return false;
    }
    return true;
  }

  /**
   * Check that this modifier only has flags meant for locals
   * @param modifier the modifier to test
   * @return true if the modifier is conform, false otherwise
   */
  public static boolean isLocalModifier(int modifier) {
    return ((modifier & LOCAL_MODIFIER_MASK) == modifier);
  }

  /**
   * Check that this modifier only has flags meant for parameters
   * @param modifier the modifier to test
   * @return true if the modifier is conform, false otherwise
   */
  public static boolean isParameterModifier(int modifier) {
    return ((modifier & LOCAL_MODIFIER_MASK) == modifier);
  }

  private static void getStringModifierCommon(int modifier, StringBuilder modifierStrBuilder) {
    if (isPublic(modifier)) {
      modifierStrBuilder.append("public ");
    }
    if (isPrivate(modifier)) {
      modifierStrBuilder.append("private ");
    }
    if (isProtected(modifier)) {
      modifierStrBuilder.append("protected ");
    }
    if (isStatic(modifier)) {
      modifierStrBuilder.append("static ");
    }
    if (isFinal(modifier)) {
      modifierStrBuilder.append("final ");
    }
    if (isSynthetic(modifier)) {
      modifierStrBuilder.append("synthetic ");
    }
  }

  public static String getStringTypeModifier(int modifier) {
    assert isTypeModifier(modifier);
    assert isValidTypeModifier(modifier);

    StringBuilder modifierStrBuilder = new StringBuilder();
    getStringModifierCommon(modifier, modifierStrBuilder);

    if (isAbstract(modifier)) {
      modifierStrBuilder.append("abstract ");
    }
    if (isEnum(modifier)) {
      modifierStrBuilder.append("enum ");
    }
    if (isStrictfp(modifier)) {
      modifierStrBuilder.append("strictfp ");
    }

    return modifierStrBuilder.toString();
  }

  public static String getStringFieldModifier(int modifier) {
    assert isFieldModifier(modifier);
    assert isValidFieldModifier(modifier);

    StringBuilder modifierStrBuilder = new StringBuilder();
    getStringModifierCommon(modifier, modifierStrBuilder);

    if (isVolatile(modifier)) {
      modifierStrBuilder.append("volatile ");
    }
    if (isTransient(modifier)) {
      modifierStrBuilder.append("transient ");
    }
    if (isEnum(modifier)) {
      modifierStrBuilder.append("enum ");
    }

    return modifierStrBuilder.toString();
  }

  public static String getStringMethodModifier(int modifier) {
    assert isMethodModifier(modifier);
    assert isValidMethodModifier(modifier);

    StringBuilder modifierStrBuilder = new StringBuilder();
    getStringModifierCommon(modifier, modifierStrBuilder);

    if (isAbstract(modifier)) {
      modifierStrBuilder.append("abstract ");
    }
    if (isNative(modifier)) {
      modifierStrBuilder.append("native ");
    }
    if (isSynchronized(modifier)) {
      modifierStrBuilder.append("synchronized ");
    }

    return modifierStrBuilder.toString();
  }
}
