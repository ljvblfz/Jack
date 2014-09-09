/*
 * Copyright 2007 Google Inc.
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


import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JSession;
import com.android.jack.util.TextOutput;

import javax.annotation.Nonnull;

/**
 * Generates Java source from our AST. ToStringGenerationVisitor is for
 * relatively short toString() results, for easy viewing in a debugger. This
 * subclass delves into the bodies of classes, interfaces, and methods to
 * produce the whole source tree.
 *
 * The goal is not to generate the input source tree. Rather, the goal is to
 * produce a set of classes that can be pasted into an enclosing class and
 * compiled with a standard Java compiler. In practice, there are cases that
 * require hand-editting to actually get a full compilation, due to Java's
 * built-in reliance on particular built-in types.
 *
 * Known to be broken: Our generated String, Class, and Throwable are not
 * compatable with the real ones, which breaks string literals, class literals,
 * try/catch/throw, and overrides of Object methods.
 */
public class SourceGenerationVisitor extends ToStringGenerationVisitor {

  public SourceGenerationVisitor(TextOutput textOutput) {
    super(textOutput);
  }

  @Override
  public boolean visit(@Nonnull JDefinedClass x) {
    super.visit(x);

    openBlock();

    for (JField it : x.getFields()) {
      accept(it);
      newline();
      newline();
    }
    for (JMethod it : x.getMethods()) {
      if (JMethod.isClinit(it)) {
        // Suppress empty clinit.
        JMethodBody body = (JMethodBody) it.getBody();
        assert body != null;
        if (body.getBlock().getStatements().isEmpty()) {
          continue;
        }
      }
      accept(it);
      newline();
      newline();
    }

    closeBlock();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDefinedInterface x) {
    super.visit(x);

    openBlock();

    for (JField field : x.getFields()) {
      accept(field);
      newline();
      newline();
    }
    for (JMethod method : x.getMethods()) {
      accept(method);
      newline();
      newline();
    }

    closeBlock();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JSession x) {
    for (JDefinedClassOrInterface type : x.getTypesToEmit()) {
      if (!type.isExternal()) {
        accept(type);
        newline();
        newline();
      }
    }
    return false;
  }

  @Override
  protected boolean shouldPrintMethodBody() {
    return true;
  }

}
