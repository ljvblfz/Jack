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

package com.android.jack.frontend.java;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import java.io.PrintWriter;

/**
 * An ECJ {@link Compiler} for Jack.
 */
public class JavaParser extends Compiler {

  private static class JackJavaParser extends Parser {
    public JackJavaParser(ProblemReporter problemReporter, boolean optimizeStringLiterals) {
      super(problemReporter, optimizeStringLiterals);
    }

    @Override
    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit,
        CompilationResult compilationResult) {
      // The same parser is reused to parse several compilation units. Diet mode is used to jump
      // over some parts of the code/expressions like method bodies. In case of failures in a
      // compilation unit, it can happens that dietInt is no reset to 0 for the following
      // compilation unit that will be parsed. If this field is not reset, method bodies can be
      // parsed but normally it must not in diet mode.
      dietInt = 0;
      return super.dietParse(sourceUnit, compilationResult);
    }
  }

  public JavaParser(INameEnvironment environment, IErrorHandlingPolicy policy,
      CompilerOptions options, ICompilerRequestor requestor, IProblemFactory problemFactory,
      PrintWriter out, CompilationProgress progress) {
    super(environment, policy, options, requestor, problemFactory, out, progress);
  }

  @Override
  public void initializeParser() {
    parser =
        new JackJavaParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
  }

  /** Parses, resolves and analyzes the given {@link CompilationUnitDeclaration}. */
  @Override
  public void process(CompilationUnitDeclaration unit, int i) {
    lookupEnvironment.unitBeingCompleted = unit;

    parser.getMethodBodies(unit);

    if (unit.scope != null) {
      unit.scope.faultInTypes();
      unit.scope.verifyMethods(lookupEnvironment.methodVerifier());
    }
    unit.resolve();

    unit.analyseCode();
  }
}
