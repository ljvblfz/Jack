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

package com.android.jack.backend.dex.annotations;

import com.google.common.collect.Ordering;

import com.android.jack.Jack;
import com.android.jack.backend.dex.DexAnnotations;
import com.android.jack.backend.dex.annotations.tag.ReflectAnnotations;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.ir.formatter.InternalFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.shrob.obfuscation.FinalNames;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.PutNameValuePair;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Add annotations used by reflection.
 */
@Description("Add annotations used by reflection")
@Synchronized
@Transform(add = {ReflectAnnotations.class, JAnnotation.class, JNameValuePair.class,
    JClassLiteral.class, JStringLiteral.class, JMethodLiteral.class, JArrayLiteral.class,
    JNullLiteral.class, JIntLiteral.class})
@Constraint(need = {GenericSignature.class, SimpleName.class, FinalNames.class})
@Protect(add = {GenericSignature.class, SimpleName.class},
    unprotect = @With(remove = ReflectAnnotations.class))
@Optional(@ToSupport(feature = SourceVersion8.class,
    add = @Constraint(no = JAnnotation.RepeatedAnnotation.class)))
@Filter(TypeWithoutPrebuiltFilter.class)
// Access getSimpleName which may depend on TypeName that is accessing enclosing type name.
@Access(JSession.class)
public class ReflectAnnotationsAdder implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;
    @Nonnull
    private final JClass javaLangClass;

    @Nonnull
    private static final String ELT_VALUE = "value";
    @Nonnull
    private static final String ELT_NAME = "name";
    @Nonnull
    private static final String ELT_ACCESS_FLAGS = "accessFlags";
    @Nonnull
    private final JAnnotationType defaultAnnotationType;
    @Nonnull
    private final JAnnotationType signatureAnnotationType;
    @Nonnull
    private final JAnnotationType enclosingMethodAnnotationType;
    @Nonnull
    private final JAnnotationType enclosingClassAnnotationType;
    @Nonnull
    private final JAnnotationType throwsAnnotationType;
    @Nonnull
    private final JAnnotationType innerAnnotationType;
    @Nonnull
    private final JAnnotationType memberClassAnnotationType;

    @Nonnull
    TypeFormatter orderingFormatter = InternalFormatter.getFormatter();

    @Nonnull
    private final Ordering<JClassOrInterface> typeOrdering =
        Ordering.from(new Comparator<JClassOrInterface>() {
          @Override
          public int compare(@Nonnull JClassOrInterface t1, @Nonnull JClassOrInterface t2) {
            return orderingFormatter.getName(t1).compareTo(orderingFormatter.getName(t2));
          }
        });

    public Visitor(@Nonnull TransformationRequest request, @Nonnull JPhantomLookup lookup) {
      this.request = request;
      javaLangClass = lookup.getClass(CommonTypes.JAVA_LANG_CLASS);
      defaultAnnotationType =
          lookup.getAnnotationType(DexAnnotations.ANNOTATION_ANNOTATION_DEFAULT);
      signatureAnnotationType = lookup.getAnnotationType(DexAnnotations.ANNOTATION_SIGNATURE);
      enclosingMethodAnnotationType =
          lookup.getAnnotationType(DexAnnotations.ANNOTATION_ENCLOSING_METHOD);
      enclosingClassAnnotationType =
          lookup.getAnnotationType(DexAnnotations.ANNOTATION_ENCLOSING_CLASS);
      throwsAnnotationType = lookup.getAnnotationType(DexAnnotations.ANNOTATION_THROWS);
      innerAnnotationType = lookup.getAnnotationType(DexAnnotations.ANNOTATION_INNER);
      memberClassAnnotationType =
          lookup.getAnnotationType(DexAnnotations.ANNOTATION_MEMBER_CLASSES);
    }

    @Nonnull
    private JMethodIdWide getOrCreateMethodId(@Nonnull JAnnotationType type, @Nonnull String name) {
      return type.getOrCreateMethodIdWide(
          name, Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
    }

    @Override
    public boolean visit(@Nonnull JMethod x) {
      return false;
    }

    @Override
    public void endVisit(@Nonnull JDefinedClassOrInterface x) {
      addMemberClasses(x);

      JClassOrInterface enclosingType = x.getEnclosingType();

      if (enclosingType != null) {
        addInnerClass(x);

        if ((x instanceof JDefinedClass) && ((JDefinedClass) x).getEnclosingMethod() != null) {
          addEnclosingMethod(x);
        } else {
          addEnclosingClass(x);
        }
      }
      GenericSignature marker = x.getMarker(GenericSignature.class);
      if (marker != null) {
        addSignature(x, marker.getGenericSignature(), x.getSourceInfo());
      }
    }

    @Override
    public void endVisit(@Nonnull JField x) {
      GenericSignature marker = x.getMarker(GenericSignature.class);
      if (marker != null) {
        addSignature(x, marker.getGenericSignature(), x.getSourceInfo());
      }
    }

    @Override
    public void endVisit(@Nonnull JMethod x) {
      addThrows(x);
      GenericSignature marker = x.getMarker(GenericSignature.class);
      if (marker != null) {
        String genericSignature = marker.getGenericSignature();
        if (genericSignature != null) {
          addSignature(x, genericSignature, x.getSourceInfo());
        }
      }
    }

    private void addSignature(@Nonnull Annotable annotable, @Nonnull String signature,
        @Nonnull SourceInfo info) {
      JAnnotation annotation =
          createAnnotation(annotable, signatureAnnotationType, info);
      JArrayLiteral literal = buildSignatureAnnotationValue(signature, info);
      JMethodIdWide methodId = getOrCreateMethodId(signatureAnnotationType, ELT_VALUE);
      JNameValuePair valuePair = new JNameValuePair(info, methodId, literal);
      assert annotation.getNameValuePair(methodId) == null
          : "Type can not have more than one generic signature";
      request.append(new PutNameValuePair(annotation, valuePair));
    }

    private void addEnclosingMethod(@Nonnull JDefinedClassOrInterface type) {
      if (type instanceof JDefinedClass) {
        JDefinedClass classType = (JDefinedClass) type;
        JMethod method = classType.getEnclosingMethod();
        if (method != null) {
          SourceInfo info = type.getSourceInfo();
          JAnnotation annotation =
              createAnnotation(type, enclosingMethodAnnotationType, info);
          JMethodLiteral newLiteral = new JMethodLiteral(method, info);
          JMethodIdWide methodId = getOrCreateMethodId(enclosingMethodAnnotationType, ELT_VALUE);
          JNameValuePair valuePair = new JNameValuePair(info, methodId, newLiteral);
          assert annotation.getNameValuePair(methodId) == null
              : "Type can not have more than one enclosing method";
          request.append(new PutNameValuePair(annotation, valuePair));
        }
      }
    }

    private void addThrows(@Nonnull JMethod method) {
      ThrownExceptionMarker marker = method.getMarker(ThrownExceptionMarker.class);
      if (marker != null) {
        List<JClass> throwns = marker.getThrownExceptions();
        SourceInfo info = method.getSourceInfo();
        JAnnotation annotation = createAnnotation(method, throwsAnnotationType, info);
        List<JLiteral> literals = new ArrayList<JLiteral>();
        for (JClass thrown : throwns) {
          literals.add(new JClassLiteral(info, thrown, javaLangClass));
        }
        JMethodIdWide methodId = getOrCreateMethodId(throwsAnnotationType, ELT_VALUE);
        JArrayLiteral array = new JArrayLiteral(info, literals);
        JNameValuePair valuePair = new JNameValuePair(info, methodId, array);
        request.append(new PutNameValuePair(annotation, valuePair));
      }
   }

    private void addMemberClasses(@Nonnull JDefinedClassOrInterface type) {
      List<JLiteral> literals = new ArrayList<JLiteral>();
      SourceInfo info = type.getSourceInfo();

      // sort member classes to be deterministic even with synthetic member classes
      List<JClassOrInterface> sortedMemberTypes =
          typeOrdering.immutableSortedCopy(type.getMemberTypes());

      for (JClassOrInterface member : sortedMemberTypes) {
        // The method getMemberTypes returns all classes contained directly by another class
        // without taking into account if the class is declared locally to a method or not.
        // The built annotation requires to contain only classes declared outside a method.
        if (member instanceof JDefinedClass
            && ((JDefinedClass) member).getEnclosingMethod() != null) {
          continue;
        }
        literals.add(new JClassLiteral(info, member, javaLangClass));
      }
      if (!literals.isEmpty()) {
        JMethodIdWide methodId = getOrCreateMethodId(memberClassAnnotationType, ELT_VALUE);
        JAnnotation annotation = getAnnotation(type, memberClassAnnotationType, info);
        JArrayLiteral array = new JArrayLiteral(info, literals);
        JNameValuePair valuePair = new JNameValuePair(info, methodId, array);
        request.append(new PutNameValuePair(annotation, valuePair));
      }
    }

    private void addEnclosingClass(@Nonnull JDefinedClassOrInterface innerType) {
      SourceInfo info = innerType.getSourceInfo();
      JAnnotation annotation =
          createAnnotation(innerType, enclosingClassAnnotationType, info);
      JLiteral newValue = new JClassLiteral(info, innerType.getEnclosingType(), javaLangClass);
      JMethodIdWide methodId = getOrCreateMethodId(enclosingClassAnnotationType, ELT_VALUE);
      JNameValuePair valuePair = new JNameValuePair(info, methodId, newValue);
      request.append(new PutNameValuePair(annotation, valuePair));
    }

    private void addInnerClass(@Nonnull JDefinedClassOrInterface innerType) {
      SourceInfo info = innerType.getSourceInfo();
      JAnnotation annotation = createAnnotation(innerType, innerAnnotationType, info);
      SimpleName marker = innerType.getMarker(SimpleName.class);
      assert marker != null;
      String innerShortName = marker.getSimpleName();
      JLiteral newValue;
      if (!innerShortName.isEmpty()) {
        newValue = new JStringLiteral(info, innerShortName);
      } else {
        newValue = new JNullLiteral(info);
      }
      JMethodIdWide nameMethodId = getOrCreateMethodId(innerAnnotationType, ELT_NAME);
      JNameValuePair nameValuePair = new JNameValuePair(info, nameMethodId, newValue);
      request.append(new PutNameValuePair(annotation, nameValuePair));
      int accessFlags = innerType.getModifier();

      // An anonymous class should not be flagged as final
      if (innerType.isAnonymous()) {
        accessFlags &= ~JModifier.FINAL;
      }

      // Add static flag on inner interfaces
      if (JModifier.isInterface(accessFlags)) {
        accessFlags |= JModifier.STATIC;
      }

      accessFlags &= AccessFlags.INNER_CLASS_FLAGS;

      JMethodIdWide flagsMethodId = getOrCreateMethodId(innerAnnotationType, ELT_ACCESS_FLAGS);
      JNameValuePair flagsValuePair =
          new JNameValuePair(info, flagsMethodId, new JIntLiteral(info, accessFlags));
      request.append(new PutNameValuePair(annotation, flagsValuePair));
    }

    @Nonnull
    private JAnnotation createAnnotation(@Nonnull Annotable annotable,
        @Nonnull JAnnotationType annotationType,  @Nonnull SourceInfo info) {
      JAnnotation annotation =
          new JAnnotation(info, JRetentionPolicy.SYSTEM, annotationType);
      request.append(new AddAnnotation(annotation, annotable));
      return annotation;
    }

    private boolean isSystemAnnotation(@Nonnull JAnnotationType annotationType) {
      if (annotationType.isSameType(defaultAnnotationType)
          || annotationType.isSameType(enclosingClassAnnotationType)
          || annotationType.isSameType(enclosingMethodAnnotationType)
          || annotationType.isSameType(innerAnnotationType)
          || annotationType.isSameType(memberClassAnnotationType)
          || annotationType.isSameType(signatureAnnotationType)
          || annotationType.isSameType(throwsAnnotationType)) {
        return true;
      }
      return false;
    }

    @Nonnull
    private JAnnotation getAnnotation(@Nonnull Annotable annotable,
        @Nonnull JAnnotationType annotationType,  @Nonnull SourceInfo info) {
      assert isSystemAnnotation(annotationType);
      JAnnotation annotation = null;
      Collection<JAnnotation> annotations = annotable.getAnnotations(annotationType);
      if (annotations.isEmpty()) {
        annotation = createAnnotation(annotable, annotationType, info);
      } else {
        assert annotations.size() == 1;
        annotation = annotations.iterator().next();
      }
      return annotation;
    }

    @Nonnull
    private JArrayLiteral buildSignatureAnnotationValue (@Nonnull String signature,
        @Nonnull SourceInfo info) {
      int sigLength = signature.length();
      List<JLiteral> pieces = new ArrayList<JLiteral>();
      for (int at = 0; at < sigLength; /*at*/) {
        char c = signature.charAt(at);
        int endAt = at + 1;
        if (c == 'L') {
            // Scan to ';' or '<'. Consume ';' but not '<'.
            while (endAt < sigLength) {
                c = signature.charAt(endAt);
                if (c == ';') {
                    endAt++;
                    break;
                } else if (c == '<') {
                    break;
                }
                endAt++;
            }
        } else {
            // Scan to 'L' without consuming it.
            while (endAt < sigLength) {
                c = signature.charAt(endAt);
                if (c == 'L') {
                    break;
                }
                endAt++;
            }
        }
        pieces.add(new JStringLiteral(info, signature.substring(at, endAt)));
        at = endAt;
      }
      return new JArrayLiteral(info, pieces);
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    TransformationRequest tr = new TransformationRequest(declaredType);
    Visitor visitor = new Visitor(tr, Jack.getSession().getPhantomLookup());
    visitor.accept(declaredType);
    tr.commit();
  }
}
