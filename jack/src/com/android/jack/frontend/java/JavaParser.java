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
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.PrintWriter;

public class JavaParser extends Compiler {
  public JavaParser(INameEnvironment environment, IErrorHandlingPolicy policy,
      CompilerOptions options, ICompilerRequestor requestor, IProblemFactory problemFactory,
      PrintWriter out, CompilationProgress progress) {
    super(environment, policy, options, requestor, problemFactory, out, progress);
  }

  /** Parses, resolves and analyzes the given {@link CompilationUnitDeclaration}. */
  @Override
  public void process(CompilationUnitDeclaration unit, int i) {
    lookupEnvironment.unitBeingCompleted = unit;

    long start = System.currentTimeMillis();
    parser.getMethodBodies(unit);
    stats.parseTime = System.currentTimeMillis() - start;

    start = System.currentTimeMillis();
    if (unit.scope != null) {
      unit.scope.faultInTypes();
      unit.scope.verifyMethods(lookupEnvironment.methodVerifier());
    }
    unit.resolve();
    stats.resolveTime = System.currentTimeMillis() - start;

    start = System.currentTimeMillis();
    unit.analyseCode();
    stats.analyzeTime = System.currentTimeMillis() - start;
  }
}
