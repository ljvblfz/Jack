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

package com.android.jack.analysis.tracer;

import com.android.jack.Jack;
import com.android.jack.debug.DebugVariableInfoMarker;
import com.android.jack.frontend.MethodIdDuplicateRemover.UniqMethodIds;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldNameLiteral;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdRef;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JMethodNameLiteral;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeStringLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.shrob.obfuscation.SubClassOrInterfaceMarker;
import com.android.jack.shrob.shrink.PartialTypeHierarchy;
import com.android.sched.item.Description;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.TracerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A visitor that traces dependencies
 */
@Description("traces dependencies")
@Constraint(need = UniqMethodIds.class)
// Visit type hierarchy, access referenced types and depends on isAnonymous
@Access(JSession.class)
public class Tracer extends JVisitor {

  @Nonnull
  protected final com.android.sched.util.log.Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  public Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final TracerBrush brush;

  public Tracer(@Nonnull TracerBrush brush) {
    this.brush = brush;
  }

  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    if (brush.startTraceSeed(type)) {
      trace(type);
      brush.endTraceSeed(type);
    }
  }

  private void trace(@Nonnull JType t) {
    if (t instanceof JDefinedClassOrInterface) {
      trace((JDefinedClassOrInterface) t);
    } else if (t instanceof JArrayType) {
      trace(((JArrayType) t).getLeafType());
    }
  }

  private void traceAnnotations(@Nonnull Annotable annotable) {
    for (JAnnotation annotation : annotable.getAnnotations()) {
      accept(annotation);
    }
  }

  private boolean isNullaryConstructor(@Nonnull JMethod m) {
    return m instanceof JConstructor && m.getParams().isEmpty();
  }

  private void traceImplementation(
      @Nonnull JDefinedClass extendingOrImplementingClass, @Nonnull JClassOrInterface superClOrI) {
    if (superClOrI instanceof JDefinedClassOrInterface) {
      JDefinedClassOrInterface definedSuperClOrI = (JDefinedClassOrInterface) superClOrI;
      for (JMethod method : definedSuperClOrI.getMethods()) {
        if (brush.startTraceOverridingMethod(method)) {
          JMethodIdWide methodId = method.getMethodIdWide();
          JType returnType = method.getType();
          JMethod implementation =
              findImplementation(methodId, returnType, extendingOrImplementingClass);
          // method was already marked, if implementation is the same, no need to re-trace it, and
          // no need to mark implementation in subtypes. It was already done when the method
          // was marked the first time, and for further subtypes that will be marked later,
          // the case will be managed by this method.
          if (implementation != null && implementation != method) {
            trace(methodId, implementation.getEnclosingType(), returnType,
                true /* mustTraceOverridingMethods */);
          }
          brush.endTraceOverridingMethod(method);
        }
      }

      JClass superClass = definedSuperClOrI.getSuperClass();
      if (superClass != null) {
        traceImplementation(extendingOrImplementingClass, superClass);
      }
      for (JInterface i : definedSuperClOrI.getImplements()) {
        traceImplementation(extendingOrImplementingClass, i);
      }
    }
  }

  private void trace(@Nonnull JDefinedClassOrInterface t) {
    if (brush.startTrace(t)) {
      traceAnnotations(t);

      if (t instanceof JDefinedClass) {
        JDefinedClass definedClass = (JDefinedClass) t;
        JClass superClass = definedClass.getSuperClass();

        if (superClass != null) {
          traceImplementation(definedClass, superClass);
        }

        for (JInterface i : definedClass.getImplements()) {
          traceImplementation(definedClass, i);
        }


        if (t.isAnonymous()) {
          if (brush.startTraceEnclosingMethod()) {
            JMethod enclosingMethod = ((JDefinedClass) t).getEnclosingMethod();
            if (enclosingMethod != null) {
              trace(t.getEnclosingType());
              trace(enclosingMethod);
            }
            brush.endTraceEnclosingMethod();
          }
        }

        if (t instanceof JDefinedEnum) {
          // The values() method is needed for the switches on enum support
          try {
            JMethod values = definedClass.getMethod("values", definedClass.getArray());
            trace(values);
          } catch (JMethodLookupException e) {
            // A valid enum must have a values() method
            throw new AssertionError(e);
          }
        }
      }

      for (JField field : t.getFields()) {
        if (brush.startTraceSeed(field)) {
          trace(field);
          brush.endTraceSeed(field);
        }
      }

      PartialTypeHierarchy pth = t.getMarker(PartialTypeHierarchy.class);
      if (pth != null) {
        Jack.getSession().getReporter().report(Severity.NON_FATAL, pth);
      }
      for (JMethod method : t.getMethods()) {
        // Clinit and constructor without parameters must always be trace without taking into
        // account seed.
        if ((JMethod.isClinit(method) || isNullaryConstructor(method))) {
          trace(method);
        } else {
          // To be safe, Jack is conservative when there is partial type hierarchy.
          // It considers all methods of the type as seed.
          if (brush.startTraceSeed(method) || pth != null) {
            trace(method.getMethodIdWide(), method.getEnclosingType(), method.getType(),
                true /* mustTraceOverridingMethods */);
            brush.endTraceSeed(method);
          }
        }
      }

      brush.endTrace(t);
    }
  }

  private void trace(@Nonnull JField f) {
    if (brush.startTrace(f)) {
      trace(f.getEnclosingType());
      trace(f.getType());
      traceAnnotations(f);
      brush.endTrace(f);
    }
  }

  private void trace(@Nonnull JFieldId fid, @Nonnull JClassOrInterface receiverType) {
    trace(receiverType);
    JField field = fid.getField();
    if (field != null) {
      trace(field);
    }
  }

  @CheckForNull
  private JMethod findMethod(@Nonnull JMethodIdWide methodIdWide,
      @Nonnull JClassOrInterface enclosingType,
      @Nonnull JType returnType) {
    JMethodId id = methodIdWide.getMethodId(returnType);
    if (id == null) {
      return null;
    }
    for (JMethod m : id.getMethods()) {
      if (m.getEnclosingType().isSameType(enclosingType)) {
        return m;
      }
    }
    return null;
  }

  /**
   * Traces the methods corresponding to a method id whose enclosing type is a subclass of
   * receiverType
   * @param mid the methodId of the searched method
   * @param receiverType the type with which the methodId was used
   * @param returnType the return type of the searched method
   * @param mustTraceOverridingMethods indicates if the overriding methods of the traced method
   * should be traced as well
   */
  private void trace(@Nonnull JMethodIdWide mid, @Nonnull JClassOrInterface receiverType,
      @Nonnull JType returnType, boolean mustTraceOverridingMethods) {
    JMethod foundMethod = findMethod(mid, receiverType, returnType);
    if (foundMethod != null) {
      trace(foundMethod);
      if (mustTraceOverridingMethods) {
        brush.setMustTraceOverridingMethods(foundMethod);
      }
    }

    if (receiverType instanceof JDefinedClassOrInterface && mustTraceOverridingMethods) {
      SubClassOrInterfaceMarker marker =
          ((LocalMarkerManager) receiverType).getMarker(SubClassOrInterfaceMarker.class);
      if (marker != null) {
        for (JDefinedClass subClass : marker.getSubClasses()) {
          if (brush.traceMarked(subClass)) {
            JMethod implementation = findImplementation(mid, returnType, subClass);
            if (implementation != null) {
              trace(implementation);
              brush.setMustTraceOverridingMethods(implementation);
            }
            brush.endTraceMarked(subClass);
          }
        }
      }
    }
  }

  private void trace(@Nonnull JMethod m) {
    if (brush.startTrace(m)) {
      trace(m.getEnclosingType());
      traceAnnotations(m);
      for (JParameter arg : m.getParams()) {
        trace(arg.getType());
      }
      trace(m.getType());
      ThrownExceptionMarker marker = m.getMarker(ThrownExceptionMarker.class);
      if (marker != null) {
        for (JClass throwException : marker.getThrownExceptions()) {
          trace(throwException);
        }
      }
      if (!m.isExternal()) {
        JAbstractMethodBody body = m.getBody();
        if (body != null) {
          accept(body);
        }
      }
      brush.endTrace(m);
    }
  }

  private void trace(@Nonnull JAnnotation al) {
    JAnnotationType type = al.getType();
    trace(type);
    for (JNameValuePair pair : al.getNameValuePairs()) {
      for (JMethod method : pair.getMethodId().getMethods()) {
        if (method.getEnclosingType().isSameType(type)) {
          trace(method);
          JLiteral defaultValue = ((JAnnotationMethod) method).getDefaultValue();
          if (defaultValue != null) {
            this.accept(defaultValue);
          }
        }
      }
    }
  }

  @Override
  public void endVisit(@Nonnull JFieldRef fr) {
    trace(fr.getFieldId(), fr.getReceiverType());
  }

  @Override
  public void endVisit(@Nonnull JMethodCall mc) {
    JType returnType = mc.getType();
    trace(returnType);
    JMethodIdWide methodId = mc.getMethodId();
    JClassOrInterface receiverType = mc.getReceiverType();
    trace(receiverType);
    JMethod implementationOrDefinition = null;
    if (receiverType instanceof JDefinedClass) {
      implementationOrDefinition = findImplementationOrDefinition(
          methodId, returnType, (JDefinedClass) receiverType);
      if (implementationOrDefinition == null && !receiverType.isExternal()) {
        logger.log(Level.WARNING,
            "No implementation or definition found for method {0} in {1} or its super types",
            new Object[] {Jack.getUserFriendlyFormatter().getName(methodId.getName(),
                methodId.getParamTypes(), returnType),
                Jack.getUserFriendlyFormatter().getName(receiverType)});
      }
    } else if (receiverType instanceof JDefinedInterface) {
      implementationOrDefinition =
          findDefinition(methodId, returnType, (JDefinedClassOrInterface) receiverType);
      if (implementationOrDefinition == null && !receiverType.isExternal()) {
        logger.log(Level.WARNING,
            "No implementation or definition found for method {0} in {1} or its super types",
            new Object[] {Jack.getUserFriendlyFormatter().getName(methodId.getName(),
                methodId.getParamTypes(), returnType),
                Jack.getUserFriendlyFormatter().getName(receiverType)});
      }
    }
    JClassOrInterface tracingStartingPoint = null;
    if (implementationOrDefinition != null) {
      tracingStartingPoint = implementationOrDefinition.getEnclosingType();
      trace(tracingStartingPoint);
    } else {
      tracingStartingPoint = receiverType;
    }
    trace(methodId, tracingStartingPoint, returnType, true /* mustTraceOverridingMethods */);
  }

  @Override
  public void endVisit(@Nonnull JNewInstance newInstance) {
    JClass returnType = newInstance.getType();
    trace(returnType);
    JMethodIdWide methodId = newInstance.getMethodId();
    trace(methodId, returnType, JPrimitiveTypeEnum.VOID.getType(),
        false /* mustTraceOverridingMethods */);
  }

  /**
   * Look up the super interfaces of a type to find a method with a matching methodId and
   * return type.
   *
   * @param methodId
   * @param returnType
   * @param receiverType
   * @return the method was found
   */
  @CheckForNull
  private JMethod findDefinition(@Nonnull JMethodIdWide methodId,
      @Nonnull JType returnType, @Nonnull JDefinedClassOrInterface receiverType) {
    JMethod foundMethod = findMethod(methodId, receiverType, returnType);
    if (foundMethod != null) {
      return foundMethod;
    }

    for (JInterface i : receiverType.getImplements()) {
      if (i instanceof JDefinedInterface) {
        JMethod foundDefinition =
            findDefinition(methodId, returnType, (JDefinedInterface) i);
        if (foundDefinition != null) {
          return foundDefinition;
        }
      }
    }
    return null;
  }

  @CheckForNull
  private JMethod findImplementation(
      @Nonnull JMethodIdWide methodId, @Nonnull JType returnType,
      @Nonnull JDefinedClass receiverType) {
    JClass currentType = receiverType;
    while (currentType instanceof JDefinedClass) {
      JMethod foundMethod = findMethod(methodId, currentType, returnType);
      if (foundMethod != null) {
        return foundMethod;
      }
      currentType = ((JDefinedClass) currentType).getSuperClass();
    }

    return null;
  }


  /**
   * Look up the super classes of a type to find a method with a matching methodId.
   *
   * @param methodId
   * @param receiverType
   * @return the type where the method was found
   */
  @CheckForNull
  private JMethod findImplementationOrDefinition(
      @Nonnull JMethodIdWide methodId, @Nonnull JType returnType,
      @Nonnull JDefinedClass receiverType) {
    JMethod implementation =
        findImplementation(methodId, returnType, receiverType);
    if (implementation != null) {
      return implementation;
    }

    JClass currentType = receiverType;
    while (currentType instanceof JDefinedClass) {
      JMethod definition =
          findDefinition(methodId, returnType, (JDefinedClassOrInterface) currentType);
      if (definition != null) {
        return definition;
      }
      currentType = ((JDefinedClass) currentType).getSuperClass();
    }

    return null;
  }

  @Override
  public void endVisit(@Nonnull JMethodNameLiteral mnl) {
    trace(mnl.getMethod());
  }

  @Override
  public void endVisit(@Nonnull JFieldNameLiteral fnl) {
    trace(fnl.getField());
  }

  @Override
  public void endVisit(@Nonnull JTypeStringLiteral tsl) {
    trace(tsl.getReferencedType());
  }

  @Override
  public void endVisit(@Nonnull JAlloc alloc) {
    trace(alloc.getInstanceType());
  }

  @Override
  public void endVisit(@Nonnull JAnnotation annotation) {
    trace(annotation);
  }

  @Override
  public void endVisit(@Nonnull JBinaryOperation x) {
    trace(x.getType());
  }

  @Override
  public void endVisit(@Nonnull JReinterpretCastOperation x) {
    trace(x.getType());
  }

  @Override
  public void endVisit(@Nonnull JDynamicCastOperation x) {
    for (JType type : x.getTypes()) {
      trace(type);
    }
  }

  @Override
  public void endVisit(@Nonnull JClassLiteral x) {
    trace(x.getRefType());
  }

  @Override
  public void endVisit(@Nonnull JEnumLiteral enumLit) {
    // No need to trace field since JEnumLiteral will be replace by constant
    JField field = enumLit.getFieldId().getField();
    if (field != null) {
      traceAnnotations(field);
    }
  }

  @Override
  public void endVisit(@Nonnull JInstanceOf x) {
    trace(x.getTestType());
  }

  @Override
  public void endVisit(@Nonnull JNewArray x) {
    trace(x.getArrayType());
  }

  @Override
  public void endVisit(@Nonnull JAbstractStringLiteral x) {
    trace(x.getType());
  }

  @Override
  public void endVisit(@Nonnull JVariableRef x) {
    DebugVariableInfoMarker debugInfo = x.getMarker(DebugVariableInfoMarker.class);
    if (debugInfo != null) {
      trace(debugInfo.getType());
    }
  }

  @Override
  public void endVisit(@Nonnull JVariable x) {
    trace(x.getType());
  }

  @Override
  public void endVisit(@Nonnull JLambda lambdaExpr) {
    trace(lambdaExpr.getType());

    for (JInterface interfaze : lambdaExpr.getInterfaceBounds()) {
      trace(interfaze);
    }

    for (JExpression capturedVar : lambdaExpr.getCapturedVariables()) {
      accept(capturedVar);
    }

    JMethodIdRef methodIdRef = lambdaExpr.getMethodIdRef();
    JMethodId methodId = methodIdRef.getMethodId();
    JMethodIdWide methodIdWide = methodId.getMethodIdWide();
    JClassOrInterface receiverType = methodIdRef.getEnclosingType();

    trace(receiverType);

    trace(methodIdWide, methodIdRef.getEnclosingType(), methodId.getType(),
        /*mustTraceOverridingMethods=*/ false);
  }
}
