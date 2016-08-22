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

package com.android.jack.shrob.spec;

import com.android.jack.ir.ast.HasModifier;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;

import java.util.Collections;
import java.util.EnumSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a modifier in a {@code field}, {@code method} or {@code class specification}.
 */
public class ModifierSpecification implements Specification<HasModifier> {

  public enum AccessFlags {
    PUBLIC(         "public",       JModifier.PUBLIC),
    PRIVATE (       "private",      JModifier.PRIVATE),
    PROTECTED (     "protected",    JModifier.PROTECTED);

    private final int value;

    private final String name;

    private AccessFlags(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }

  public enum Modifier {
    STATIC(         "static",       JModifier.STATIC),
    FINAL(          "final",        JModifier.FINAL),
    SUPER(          "super",        JModifier.SUPER),
    SYNCHRONIZED(   "synchronized", JModifier.SYNCHRONIZED),
    VOLATILE (      "volatile",     JModifier.VOLATILE),
    BRIDGE(         "bridge",       JModifier.BRIDGE),
    TRANSIENT(      "transient",    JModifier.TRANSIENT),
    VARARGS(        "varargs",      JModifier.VARARGS),
    NATIVE(         "native",       JModifier.NATIVE),
    INTERFACE(      "interface",    JModifier.INTERFACE),
    ABSTRACT(       "abstract",     JModifier.ABSTRACT),
    STRICTFP(       "strictfp",     JModifier.STRICTFP),
    SYNTHETIC(      "synthetic",    JModifier.SYNTHETIC),
    ANNOTATION(     "annotation",   JModifier.ANNOTATION),
    ENUM(           "enum",         JModifier.ENUM);

    private final int value;

    private final String name;

    private Modifier(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }

  @Nonnull
  private static final EnumSet<Modifier> TYPE_MODIFIERS = EnumSet.of(Modifier.STATIC,
      Modifier.FINAL, Modifier.ENUM, Modifier.SYNTHETIC, Modifier.ABSTRACT, Modifier.INTERFACE,
      Modifier.ANNOTATION, Modifier.SUPER, Modifier.STRICTFP);

  @Nonnull
  private static final EnumSet<Modifier> FIELD_MODIFIERS = EnumSet.of(Modifier.STATIC,
      Modifier.FINAL, Modifier.TRANSIENT, Modifier.VOLATILE, Modifier.ENUM, Modifier.SYNTHETIC);

  @Nonnull
  private static final EnumSet<Modifier> METHOD_MODIFIERS = EnumSet.of(Modifier.STATIC,
      Modifier.NATIVE, Modifier.ABSTRACT, Modifier.FINAL, Modifier.SYNCHRONIZED, Modifier.BRIDGE,
      Modifier.SYNTHETIC, Modifier.STRICTFP, Modifier.VARARGS);

  @Nonnull
  private final EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

  @Nonnull
  private final EnumSet<Modifier> modifiersWithNegator = EnumSet.noneOf(Modifier.class);

  @Nonnull
  private final EnumSet<AccessFlags> accessFlags = EnumSet.noneOf(AccessFlags.class);

  @Nonnull
  private final EnumSet<AccessFlags> accessFlagsWithNegator = EnumSet.noneOf(AccessFlags.class);

  public void addModifier(Modifier modifier, boolean hasNegator) {
    if (hasNegator) {
      this.modifiersWithNegator.add(modifier);
    } else {
      this.modifiers.add(modifier);
    }
  }

  public void addAccessFlag(AccessFlags accessFlag, boolean hasNegator) {
    if (hasNegator) {
      this.accessFlagsWithNegator.add(accessFlag);
    } else {
      this.accessFlags.add(accessFlag);
    }
  }

  @Nonnull
  private static EnumSet<Modifier> convertJModifier(HasModifier hasModifier) {
    EnumSet<Modifier> listOfModifiers;
    if (hasModifier instanceof JClassOrInterface) {
        listOfModifiers = TYPE_MODIFIERS;
    } else if (hasModifier instanceof JField) {
      listOfModifiers = FIELD_MODIFIERS;
    } else if (hasModifier instanceof JMethod) {
        listOfModifiers = METHOD_MODIFIERS;
    } else {
        throw new AssertionError();
    }

    int toConvert = hasModifier.getModifier();
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    for (Modifier currentModifier : listOfModifiers) {
      if ((currentModifier.value & toConvert) != 0) {
        modifiers.add(currentModifier);
      }
    }
    return modifiers;
  }

  @CheckForNull
  private static AccessFlags getAccessFlags(HasModifier hasModifier) {
    int toConvert = hasModifier.getModifier();
    for (AccessFlags accFlags : AccessFlags.values()) {
      if ((accFlags.value & toConvert) != 0) {
        return accFlags;
      }
    }
    return null;
  }

  @Override
  public boolean matches(@Nonnull HasModifier candidate) {
    // Combining multiple flags is allowed (e.g. public static).
    // It means that both access flags have to be set (e.g. public and static),
    // except when they are conflicting, in which case at least one of them has
    // to be set (e.g. at least public or protected).

    AccessFlags candidateAccFlags = getAccessFlags(candidate);

    // If the visibility is "package" but the specification isn't,
    // the modifier doesn't match
    if (!accessFlags.isEmpty()) {
      if (!accessFlags.contains(candidateAccFlags)) {
        return false;
      }
    }

    if (accessFlagsWithNegator.contains(candidateAccFlags)) {
      return false;
    }

    EnumSet<Modifier> candidateModifiers = convertJModifier(candidate);
    if (!candidateModifiers.containsAll(modifiers)) {
      return false;
    }

    if (!Collections.disjoint(candidateModifiers, modifiersWithNegator)) {
      return false;
    }
    return true;
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (AccessFlags accessFlag : accessFlags) {
      sb.append(accessFlag.name);
      sb.append(' ');
    }

    for (AccessFlags accessFlag : accessFlagsWithNegator) {
      sb.append('!');
      sb.append(accessFlag.name);
      sb.append(' ');
    }

    for (Modifier modifier : modifiers) {
      sb.append(modifier.name);
      sb.append(' ');
    }

    for (Modifier modifier : modifiersWithNegator) {
      sb.append('!');
      sb.append(modifier.name);
      sb.append(' ');
    }

    return sb.toString();
  }
}