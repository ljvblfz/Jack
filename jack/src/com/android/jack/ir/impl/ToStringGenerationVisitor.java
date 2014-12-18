/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.impl;


import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.util.TextOutput;

import javax.annotation.Nonnull;

/**
 * Implements a reasonable toString() for all JNodes. The goal is to print a
 * recognizable declaration for large constructs (classes, methods) for easy use
 * in a debugger. Expressions and Statements should look like Java code
 * fragments.
 */
public class ToStringGenerationVisitor extends BaseGenerationVisitor {
  public ToStringGenerationVisitor(TextOutput textOutput) {
    super(textOutput);
  }

  @Override
  public boolean visit(@Nonnull JDefinedClass x) {
    printTypeFlags(x);
    print(CHARS_CLASS);
    printTypeName(x);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JConstructor x) {
    // Modifiers
    if (x.isPrivate()) {
      print(CHARS_PRIVATE);
    } else {
      print(CHARS_PUBLIC);
    }
    printName(x);

    // Parameters
    printParameterList(x);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JField x) {
    print(JModifier.getStringFieldModifier(x.getModifier()));
    printType(x);
    space();
    printName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDefinedInterface x) {
    printTypeFlags(x);
    print(CHARS_INTERFACE);
    printTypeName(x);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethod x) {
    printMethodHeader(x);

    if (x instanceof JAnnotationMethod) {
      JLiteral defaultValue = ((JAnnotationMethod) x).getDefaultValue();
      if (defaultValue != null) {
        space();
        print(CHARS_DEFAULT);
        space();
        accept(defaultValue);
      }
    }

    return false;
  }

  @Override
  protected void printMethodHeader(JMethod x) {
    // Modifiers
    print(JModifier.getStringMethodModifier(x.getModifier()));
    printType(x);
    space();
    printName(x);

    // Parameters
    printParameterList(x);
  }
}
