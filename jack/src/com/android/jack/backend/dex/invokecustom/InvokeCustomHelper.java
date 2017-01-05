/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.backend.dex.invokecustom;

import com.android.jack.Jack;
import com.android.jack.backend.dex.DexAnnotations;
import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.rop.cst.CstArray;
import com.android.jack.dx.rop.cst.CstCallSiteRef;
import com.android.jack.dx.rop.cst.CstDouble;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstFloat;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.cst.CstLong;
import com.android.jack.dx.rop.cst.CstMethodHandleRef;
import com.android.jack.dx.rop.cst.CstMethodHandleRef.MethodHandleKind;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstPrototypeRef;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.TypedConstant;
import com.android.jack.dx.rop.type.Prototype;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.ir.ast.JAbstractMethodCall;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JStringLiteral;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Helper to generate invoke custom based on annotations.
 */
public class InvokeCustomHelper {

  public static boolean isInvokeCustom(@Nonnull JAbstractMethodCall call) {
    Collection<JMethod> methods = call.getMethodIdWide().getMethods();
    Iterator<JMethod> methodsIt = methods.iterator();
    boolean isInvokeCustom = false;
    while (methodsIt.hasNext()) {
      JMethod method = methodsIt.next();
      if (getInvokeCustomCallsite(method) != null) {
        isInvokeCustom = true;
        break;
      }
    }

    assert !isInvokeCustom || methods.size() == 1;

    return isInvokeCustom;
  }

  @Nonnull
  public static CstCallSiteRef readInvokeCustomCallSite(
      @Nonnull JAnnotation invokeCustomCallSite) {
    CstMethodHandleRef methodHandle = null;
    JStringLiteral callSiteMethodName = null;
    JClassLiteral callSiteReturnType = null;
    JArrayLiteral callSiteArgumentTypes = null;
    CstArray extraArgs = null;
    for (JNameValuePair nameValuePair : invokeCustomCallSite.getNameValuePairs()) {
      switch (nameValuePair.getName()) {
        case "invokeMethodHandle": {
          JArrayLiteral arrayLiteral = (JArrayLiteral) nameValuePair.getValue();
          assert arrayLiteral.getValues().size() == 1;
          methodHandle = readLinkerMethodHandle((JAnnotation) arrayLiteral.getValues().get(0));
          break;
        }
        case "fieldMethodHandle": {
          JArrayLiteral arrayLiteral = (JArrayLiteral) nameValuePair.getValue();
          assert arrayLiteral.getValues().size() == 1;
          methodHandle = readLinkerFieldHandle((JAnnotation) arrayLiteral.getValues().get(0));
          break;
        }
        case "methodHandleExtraArgs": {
          extraArgs = readExtraArgs((JArrayLiteral) nameValuePair.getValue());
          break;
        }
        case "name": {
          callSiteMethodName = (JStringLiteral) (nameValuePair.getValue());
          break;
        }
        case "returnType": {
          callSiteReturnType = (JClassLiteral) (nameValuePair.getValue());
          break;
        }
        case "argumentTypes": {
          callSiteArgumentTypes = (JArrayLiteral) (nameValuePair.getValue());
          break;
        }
      }
    }
    assert methodHandle != null;
    assert callSiteMethodName != null;
    assert callSiteArgumentTypes != null;
    assert callSiteReturnType != null;

    return new CstCallSiteRef(methodHandle, callSiteMethodName.getValue(),
        new CstPrototypeRef(buildPrototype(callSiteArgumentTypes, callSiteReturnType)), extraArgs);
  }

  @CheckForNull
  public static JAnnotation getInvokeCustomCallsite(@Nonnull JMethod method) {
    for (JAnnotation annotation : method.getAnnotations()) {
      if (annotation.getType().getName().equals("CalledByInvokeCustom")) {
        return annotation;
      }
    }
    return null;
  }

  @Nonnull
  private static CstArray readExtraArgs(@Nonnull JArrayLiteral extraArgs) {
    CstArray.List list = new CstArray.List(extraArgs.getValues().size());
    int idx = 0;
    for (JLiteral extraArg : extraArgs.getValues()) {
      assert extraArg instanceof JAnnotation;
      JAnnotation extrArgAnnot = (JAnnotation) extraArg;
      for (JNameValuePair nameValuePair : extrArgAnnot.getNameValuePairs()) {
        JArrayLiteral value = (JArrayLiteral) nameValuePair.getValue();
        assert value.getValues().size() == 1;
        TypedConstant cst = null;
        JLiteral jLiteral = value.getValues().get(0);
        switch (nameValuePair.getName()) {
          case "booleanValue" : {
            cst = CstInteger.make(((JBooleanLiteral) jLiteral).getValue() ? 1 : 0);
            break;
          }
          case "byteValue" : {
            cst = CstInteger.make(((JByteLiteral) jLiteral).getValue());
            break;
          }
          case "charValue" : {
            cst = CstInteger.make(((JCharLiteral) jLiteral).getValue());
            break;
          }
          case "shortValue" : {
            cst = CstInteger.make(((JShortLiteral) jLiteral).getValue());
            break;
          }
          case "intValue" : {
            cst = CstInteger.make(((JIntLiteral) jLiteral).getValue());
            break;
          }
          case "floatValue" : {
            cst = CstFloat
                .make(Float.floatToIntBits(((JFloatLiteral) jLiteral).getValue()));
            break;
          }
          case "doubleValue" : {
            cst = CstDouble.make(
                Double.doubleToLongBits(((JDoubleLiteral) jLiteral).getValue()));
            break;
          }
          case "longValue" : {
            cst = CstLong.make(((JLongLiteral) jLiteral).getValue());
            break;
          }
          case "stringValue" : {
            cst = new CstString(((JStringLiteral) jLiteral).getValue());
            break;
          }
          case "classValue" : {
            cst = RopHelper.getCstType(((JClassLiteral) jLiteral).getRefType());
            break;
          }
          default: {
            throw new AssertionError();
          }
        }
        list.set(idx++, cst);
      }
    }

    list.setImmutable();
    return new CstArray(list);
  }

  @Nonnull
  private static CstMethodHandleRef readLinkerFieldHandle(@Nonnull JAnnotation linkerFieldHandle) {
    JNameValuePair kindValuePair = linkerFieldHandle.getNameValuePair("kind");
    assert kindValuePair != null;
    JEnumLiteral enumKind = (JEnumLiteral) kindValuePair.getValue();
    MethodHandleKind kind = (MethodHandleKind.valueOf(enumKind.getFieldId().getName()));
    assert kind == MethodHandleKind.GET_INSTANCE || kind == MethodHandleKind.GET_STATIC
        || kind == MethodHandleKind.PUT_INSTANCE || kind == MethodHandleKind.PUT_STATIC;
    JNameValuePair ownerValuePair = linkerFieldHandle.getNameValuePair("enclosingType");
    assert ownerValuePair != null;
    JClassLiteral owner = (JClassLiteral) ownerValuePair.getValue();
    JNameValuePair nameValuePair = linkerFieldHandle.getNameValuePair("name");
    assert nameValuePair != null;
    JStringLiteral name = (JStringLiteral) nameValuePair.getValue();
    JNameValuePair typeValuePair = linkerFieldHandle.getNameValuePair("type");
    JClassLiteral type;
    if (typeValuePair == null) {
      type = (JClassLiteral) getDefaultValue(linkerFieldHandle, "type");
    } else {
      type = (JClassLiteral) typeValuePair.getValue();
    }
    assert type != null;

    CstFieldRef fieldRef = new CstFieldRef(RopHelper.getCstType(owner.getRefType()),
        new CstString(name.getValue()), RopHelper.getCstType(type.getRefType()));

    return new CstMethodHandleRef(kind, fieldRef);
  }

  @Nonnull
  private static CstMethodHandleRef readLinkerMethodHandle(
      @Nonnull JAnnotation linkerMethodHandle) {
    JNameValuePair kindValuePair = linkerMethodHandle.getNameValuePair("kind");
    assert kindValuePair != null;
    JEnumLiteral enumKind = (JEnumLiteral) kindValuePair.getValue();
    MethodHandleKind kind = (MethodHandleKind.valueOf(enumKind.getFieldId().getName()));
    assert kind == MethodHandleKind.INVOKE_CONSTRUCTOR || kind == MethodHandleKind.INVOKE_INSTANCE
        || kind == MethodHandleKind.INVOKE_STATIC;
    JNameValuePair ownerValuePair = linkerMethodHandle.getNameValuePair("enclosingType");
    assert ownerValuePair != null;
    JClassLiteral owner = (JClassLiteral) ownerValuePair.getValue();
    JNameValuePair nameValuePair = linkerMethodHandle.getNameValuePair("name");
    assert nameValuePair != null;
    JStringLiteral name = (JStringLiteral) nameValuePair.getValue();
    JNameValuePair returnTypeValuePair = linkerMethodHandle.getNameValuePair("returnType");
    JClassLiteral returnType;
    if (returnTypeValuePair == null) {
      returnType = (JClassLiteral) getDefaultValue(linkerMethodHandle, "returnType");
    } else {
      returnType = (JClassLiteral) returnTypeValuePair.getValue();
    }
    assert returnType != null;
    JNameValuePair argumentTypesValuePair = linkerMethodHandle.getNameValuePair("argumentTypes");
    JArrayLiteral argumentsTypes;
    if (argumentTypesValuePair == null) {
      argumentsTypes = (JArrayLiteral) getDefaultValue(linkerMethodHandle, "argumentTypes");
    } else {
      argumentsTypes = (JArrayLiteral) argumentTypesValuePair.getValue();
    }
    assert argumentsTypes != null;

    CstMethodRef methodRef = new CstMethodRef(RopHelper.getCstType(owner.getRefType()),
        new CstString(name.getValue()), buildPrototype(argumentsTypes, returnType));

    return new CstMethodHandleRef(kind, methodRef);
  }

  @Nonnull
  private static JLiteral getDefaultValue(@Nonnull JAnnotation annotation, @Nonnull String name) {
    JLiteral returnValue = null;
    JDefinedAnnotationType annotationType = (JDefinedAnnotationType) annotation.getType();
    if (annotationType.isToEmit()) {
      List<JAnnotation> annotations = annotationType.getAnnotations(Jack.getSession()
          .getPhantomLookup().getAnnotationType(DexAnnotations.ANNOTATION_ANNOTATION_DEFAULT));
      assert annotations.size() == 1;
      JNameValuePair defaultAnnotationPair = annotations.get(0).getNameValuePair("value");
      assert defaultAnnotationPair != null;
      JNameValuePair nameValuePair =
          ((JAnnotation) defaultAnnotationPair.getValue()).getNameValuePair(name);
      assert nameValuePair != null;
      returnValue = nameValuePair.getValue();
    } else {
      for (JMethod annotationMethod : annotationType.getMethods()) {
        if (annotationMethod.getName().equals(name)) {
          returnValue = ((JAnnotationMethod) annotationMethod).getDefaultValue();
          break;
        }
      }
    }

    assert returnValue != null;
    return returnValue;
  }

  @Nonnull
  private static Prototype buildPrototype(@Nonnull JArrayLiteral signature,
      @Nonnull JClassLiteral returnType) {
    List<JLiteral> values = signature.getValues();
    StdTypeList stdTypeList = new StdTypeList(values.size());

    int idx = 0;
    for (JLiteral lit : values) {
      assert lit instanceof JClassLiteral;
      JClassLiteral classLit = (JClassLiteral) lit;
      stdTypeList.set(idx++, RopHelper.convertTypeToDx(classLit.getRefType()));
    }

    return Prototype.intern(stdTypeList, RopHelper.convertTypeToDx(returnType.getRefType()));
  }
}
