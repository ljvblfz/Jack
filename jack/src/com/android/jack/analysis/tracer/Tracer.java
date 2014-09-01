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
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JAnnotationMethod;
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
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldNameLiteral;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodNameLiteral;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeStringLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.sched.item.Description;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.util.findbugs.SuppressFBWarnings;
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
public class Tracer extends JVisitor {

  @Nonnull
  protected static final com.android.sched.util.log.Tracer tracer = TracerFactory.getTracer();

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
    for (JAnnotationLiteral a : annotable.getAnnotations()) {
      accept(a);
    }
  }

  private boolean isNullaryConstructor(@Nonnull JMethod m) {
    return m instanceof JConstructor && m.getParams().isEmpty();
  }

  protected void traceImplementation(
      @Nonnull JDefinedClass extendingOrImplementingClass, @Nonnull JClassOrInterface superClOrI) {
    if (superClOrI instanceof JDefinedClassOrInterface) {
      JDefinedClassOrInterface definedSuperClOrI = (JDefinedClassOrInterface) superClOrI;
      for (JMethod method : definedSuperClOrI.getMethods()) {
        if (brush.startTraceOverridingMethod(method)) {
          JMethodId methodId = method.getMethodId();
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

  protected void trace(@Nonnull JDefinedClassOrInterface t) {
    if (brush.startTrace(t)) {
      traceAnnotations(t);
      for (JMethod m : t.getMethods()) {
        if ((JMethod.isClinit(m) || isNullaryConstructor(m))) {
          trace(m);
        }
      }

      if (t instanceof JDefinedClass) {
        JDefinedClass definedClass = (JDefinedClass) t;
        JClass superClass = definedClass.getSuperClass();

        if (superClass != null) {
          traceImplementation(definedClass, superClass);
        }

        for (JInterface i : definedClass.getImplements()) {
          traceImplementation(definedClass, i);
        }


        if (JModifier.isAnonymousType(t.getModifier())) {
          trace(t.getEnclosingType());
          if (brush.startTraceEnclosingMethod()) {
            JMethod enclosingMethod = ((JDefinedClass) t).getEnclosingMethod();
            if (enclosingMethod != null) {
              trace(enclosingMethod);
            }
            brush.endTraceEnclosingMethod();
          }
        }

        if (t instanceof JDefinedEnum) {
          // The values() method is needed for the switches on enum support
          JMethod values = definedClass.getMethod("values", definedClass.getArray());
          trace(values);
        }
      }

      for (JField field : t.getFields()) {
        if (brush.startTraceSeed(field)) {
          trace(field);
          brush.endTraceSeed(field);
        }
      }

      for (JMethod method : t.getMethods()) {
        if (brush.startTraceSeed(method)) {
          trace(method);
          brush.endTraceSeed(method);
        }
      }

      brush.endTrace(t);
    }
  }

  protected void trace(@Nonnull JField f) {
    if (brush.startTrace(f)) {
      trace(f.getEnclosingType());
      trace(f.getType());
      traceAnnotations(f);
      brush.endTrace(f);
    }
  }

  protected void trace(@Nonnull JFieldId fid, @Nonnull JClassOrInterface receiverType) {
    trace(receiverType);
    JField field = fid.getField();
    if (field != null) {
      trace(field);
    }
  }

  @CheckForNull
  protected JMethod findMethod(@Nonnull JMethodId methodId,
      @Nonnull JClassOrInterface enclosingType, @Nonnull JType returnType) {
    for (JMethod m : methodId.getMethods()) {
      if (m.getEnclosingType() == enclosingType && m.getType() == returnType) {
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
  protected void trace(@Nonnull JMethodId mid, @Nonnull JClassOrInterface receiverType,
      @Nonnull JType returnType, boolean mustTraceOverridingMethods) {
    JMethod foundMethod = findMethod(mid, receiverType, returnType);
    if (foundMethod != null) {
      trace(foundMethod);
      if (mustTraceOverridingMethods) {
        brush.setMustTraceOverridingMethods(foundMethod);
      }
    }

    if (receiverType instanceof JDefinedClassOrInterface && mustTraceOverridingMethods) {
      ExtendingOrImplementingClassMarker marker =
          ((LocalMarkerManager) receiverType).getMarker(ExtendingOrImplementingClassMarker.class);
      if (marker != null) {
        for (JDefinedClass subClass : marker.getExtendingOrImplementingClasses()) {
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

  protected void trace(@Nonnull JMethod m) {
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

  /* Ignore: "Using pointer equality to compare a com.android.jack.ir.ast.JAnnotation with a
   * com.android.jack.ir.ast.JDefinedClassOrInterface"
   */
  @SuppressFBWarnings("EC")
  protected void trace(@Nonnull JAnnotationLiteral al) {
    JAnnotation type = al.getType();
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
    JMethodId methodId = mc.getMethodId();
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
    JMethodId methodId = newInstance.getMethodId();
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
  private JMethod findDefinition(@Nonnull JMethodId methodId,
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
      @Nonnull JMethodId methodId, @Nonnull JType returnType, @Nonnull JDefinedClass receiverType) {
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
      @Nonnull JMethodId methodId, @Nonnull JType returnType, @Nonnull JDefinedClass receiverType) {
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
  public void endVisit(@Nonnull JAnnotationLiteral annotationLiteral) {
    trace(annotationLiteral);
  }

  @Override
  public void endVisit(@Nonnull JBinaryOperation x) {
    trace(x.getType());
  }

  @Override
  public void endVisit(@Nonnull JDynamicCastOperation x) {
    trace(x.getType());
  }

  @Override
  public void endVisit(@Nonnull JClassLiteral x) {
    trace(x.getRefType());
  }

  @Override
  public void endVisit(@Nonnull JEnumLiteral enumLit) {
    JField field = enumLit.getFieldId().getField();
    if (field != null) {
      this.accept(field);
    }
    super.endVisit(enumLit);
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
  public void endVisit(@Nonnull JVariable x) {
    trace(x.getType());
  }
}
