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

package com.android.jack.frontend.java;


import com.android.jack.JackEventType;
import com.android.jack.JackUserException;
import com.android.jack.frontend.java.JackBatchCompiler.TransportJUEAroundEcjError;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.impl.EcjSourceTypeLoader;
import com.android.jack.ir.impl.JackIrBuilder;
import com.android.jack.ir.impl.ReferenceMapper;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The jack {@code JAstBuilder} is inserted in ecj build chain to build a J-AST.
 */
class JAstBuilder extends JavaParser {

  @Nonnull
  private static final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final JSession session;

  @Nonnull
  private final JackIrBuilder astBuilder;

  private boolean hasErrors = false;

  /**
   * Creates ecj {@code Compiler} for jack.
   * Forwards all arguments to the constructor of the super class
   * {@link org.eclipse.jdt.internal.compiler.Compiler#Compiler(
   * INameEnvironment, IErrorHandlingPolicy, CompilerOptions,
   * ICompilerRequestor, IProblemFactory, PrintWriter, CompilationProgress)}.
   */
  public JAstBuilder(@Nonnull INameEnvironment environment,
      @Nonnull IErrorHandlingPolicy policy,
      @Nonnull CompilerOptions options,
      @Nonnull ICompilerRequestor requestor,
      @Nonnull IProblemFactory problemFactory,
      @CheckForNull PrintWriter out,
      @CheckForNull CompilationProgress progress,
      @Nonnull JSession session) {
    super(environment,
        policy,
        options,
        requestor,
        problemFactory,
        out,
        progress);
    this.session = session;
    astBuilder = new JackIrBuilder(lookupEnvironment, session);
  }

  @Nonnull
  private JPackage getOrCreatePackage(@Nonnull char[][] compoundName, int compoundNameLength) {
    assert compoundNameLength <= compoundName.length && compoundNameLength >= 0;
    JPackage currentPackage = session.getTopLevelPackage();
    for (int i = 0; i < compoundNameLength; i++) {
      String name = String.valueOf(compoundName[i]);
      currentPackage = currentPackage.getOrCreateSubPackage(name);
      currentPackage.setOnPath();
    }

    return currentPackage;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(CompilationUnitDeclaration unit, int i) {
    try {
      Event jastEvent = tracer.start(JackEventType.J_AST_BUILDER);
      try {
        super.process(unit, i);

        if (hasErrors || unit.hasErrors() || unit.compilationResult().hasErrors()) {
          // An error has already been detected, don't even try to handle the unit.
          hasErrors = true;
          return;
        }

        loadLocalClasses(unit);

        // Generate Jack IR after each compilation of CompilationUnitDeclaration.
        // It could not be done at the end of compile(ICompilationUnit[] sourceUnits) method since
        // reset method of ecj was called by super.compile(sourceUnits) and after the lookup
        // environment is no longer usable.
        Event jackIrBuilderEvent = tracer.start(JackEventType.JACK_IR_BUILDER);
        List<JDefinedClassOrInterface> types;
        try {
          types = astBuilder.process(unit);
        } finally {
          jackIrBuilderEvent.end();
        }

        for (JDefinedClassOrInterface type : types) {
          session.addTypeToEmit(type);
        }
      } finally {
        jastEvent.end();
      }
    } catch (IllegalArgumentException e) {
      // This is a workaround to reduce bad handling of IllegalArgumentException in
      // JackBatchCompiler.
      AssertionError error = new AssertionError();
      error.initCause(e);
      throw error;
    }
  }

  @Override
  protected synchronized void addCompilationUnit(
      @CheckForNull ICompilationUnit sourceUnit,
      @CheckForNull CompilationUnitDeclaration parsedUnit) {
    super.addCompilationUnit(sourceUnit, parsedUnit);

    assert parsedUnit != null;

    if (parsedUnit.types != null) {
      JPackage enclosingPackage;
      if (parsedUnit.currentPackage != null) {
        char[][] packageNames = parsedUnit.currentPackage.tokens;
        enclosingPackage = getOrCreatePackage(packageNames, packageNames.length);
      } else {
        enclosingPackage = session.getTopLevelPackage();
      }
      ReferenceMapper refMap = astBuilder.getTypeMap();

      for (TypeDeclaration typeDeclaration : parsedUnit.types) {
        createTypes(enclosingPackage, refMap, typeDeclaration);
      }
    }
  }

  private void loadLocalClasses(@Nonnull CompilationUnitDeclaration unit) {
    if (unit.localTypes != null) {
      JPackage enclosingPackage;
      if (unit.currentPackage != null) {
        char[][] packageNames = unit.currentPackage.tokens;
        enclosingPackage = getOrCreatePackage(packageNames, packageNames.length);
      } else {
        enclosingPackage = session.getTopLevelPackage();
      }
      ReferenceMapper refMap = astBuilder.getTypeMap();
      for (LocalTypeBinding binding : unit.localTypes) {
        /* binding.constantPoolName() == null means that ecj detected the local type to be dead
         * code and didn't completed processing */
        if (binding != null && binding.constantPoolName() != null) {
          EcjSourceTypeLoader.createType(refMap, enclosingPackage, binding, null,
              new FileLocation(new File(new String(unit.getFileName()))));
        }
      }
    }
  }

  private void createTypes(@Nonnull JPackage enclosingPackage, @Nonnull ReferenceMapper refMap,
      @Nonnull TypeDeclaration typeDeclaration) {
    EcjSourceTypeLoader.createType(refMap, enclosingPackage, typeDeclaration.binding,
        typeDeclaration,
        new FileLocation(new File(new String(typeDeclaration.compilationResult.fileName))));
    if (typeDeclaration.memberTypes != null) {
      for (TypeDeclaration memberType : typeDeclaration.memberTypes) {
        createTypes(enclosingPackage, refMap, memberType);
      }
    }
  }

  @Override
  protected void handleInternalException(@Nonnull Throwable internalException,
      @CheckForNull CompilationUnitDeclaration unit, @CheckForNull CompilationResult result) {
    if (internalException instanceof IllegalArgumentException) {
      throw new TransportJUEAroundEcjError(new JackUserException(internalException));
    } else if (internalException instanceof JackUserException) {
      throw new TransportJUEAroundEcjError((JackUserException) internalException);
    } else if (internalException instanceof RuntimeException) {
      throw (RuntimeException) internalException;
    } else if (internalException instanceof Error) {
      throw (Error) internalException;
    } else {
      throw new JNodeInternalError(internalException);
    }
  }
}
