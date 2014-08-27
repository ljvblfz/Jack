/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.preprocessor;

import com.android.jack.ir.ast.HasModifier;

import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

/**
 * Modifier expression.
 */
public class ModifierPrimaryExpression implements Expression<Boolean, HasModifier> {

  @Nonnull
  public static final Expression<Boolean, HasModifier> PUBLIC =
      new ModifierPrimaryExpression(Modifier.PUBLIC, "public");
  @Nonnull
  public static final Expression<Boolean, HasModifier> PROTECTED =
      new ModifierPrimaryExpression(Modifier.PROTECTED, "protected");

  @Nonnull
  public static final Expression<Boolean, HasModifier> PACKAGE =
      new Expression<Boolean, HasModifier>() {
    @Override
    public Boolean eval(HasModifier hasModfier, Context context) {
      return Boolean.valueOf((hasModfier.getModifier()
          & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == 0);
    }

    @Override
    @Nonnull
    public String toString() {
      return "package";
    }
  };

  @Nonnull
  public static final ModifierPrimaryExpression PRIVATE =
      new ModifierPrimaryExpression(Modifier.PRIVATE, "private");
  @Nonnull
  public static final ModifierPrimaryExpression ABSTRACT =
      new ModifierPrimaryExpression(Modifier.ABSTRACT, "abstract");
  @Nonnull
  public static final ModifierPrimaryExpression FINAL =
      new ModifierPrimaryExpression(Modifier.FINAL, "final");
  @Nonnull
  public static final ModifierPrimaryExpression NATIVE =
      new ModifierPrimaryExpression(Modifier.NATIVE, "native");
  @Nonnull
  public static final ModifierPrimaryExpression STATIC =
      new ModifierPrimaryExpression(Modifier.STATIC, "static");
  @Nonnull
  public static final ModifierPrimaryExpression STRICT =
      new ModifierPrimaryExpression(Modifier.STRICT, "strictfp");
  @Nonnull
  public static final ModifierPrimaryExpression SYNCHRONIZED =
      new ModifierPrimaryExpression(Modifier.SYNCHRONIZED, "synchronized");
  @Nonnull
  public static final ModifierPrimaryExpression TRANSIENT =
      new ModifierPrimaryExpression(Modifier.TRANSIENT, "transient");
  @Nonnull
  public static final ModifierPrimaryExpression VOLATILE =
      new ModifierPrimaryExpression(Modifier.VOLATILE, "volatile");


    private final int mask;
    @Nonnull
    private final String name;

    private ModifierPrimaryExpression(int mask, @Nonnull String name) {
      this.mask = mask;
      this.name = name;
    }

    @Override
    public Boolean eval(@Nonnull HasModifier hasModfier, @Nonnull Context context) {
      return Boolean.valueOf((hasModfier.getModifier() & mask) == mask);
    }

    @Override
    @Nonnull
    public String toString() {
      return name;
    }

}
