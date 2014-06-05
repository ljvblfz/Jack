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

package com.android.jack.reflection;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class used to find methods and fields using their names and their declaring type
 */
public class MemberFinder {

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @CheckForNull
  public static JField getDirectField(
      @Nonnull JDefinedClassOrInterface declaringType, @Nonnull String name) {
    JField fieldFound = null;
    for (JField field : declaringType.getFields(name)) {
      if (field.isPublic()) {
        if (fieldFound != null) {
          throwMultipleFieldError(declaringType, name);
        }
        fieldFound = field;
      }
    }
    return fieldFound;
  }

  @CheckForNull
  public static JField getField(
      @Nonnull JDefinedClassOrInterface declaringType, @Nonnull String name) {
    JField fieldFound = getDirectField(declaringType, name);
    if (fieldFound != null) {
      return fieldFound;
    }
    for (JInterface interfaceType : declaringType.getImplements()) {
      if (interfaceType instanceof JDefinedInterface) {
        fieldFound = getField((JDefinedInterface) interfaceType, name);
        if (fieldFound != null) {
          return fieldFound;
        }
      }
    }
    JClass superClass = declaringType.getSuperClass();
    if (superClass instanceof JDefinedClass) {
      fieldFound = getField((JDefinedClass) superClass, name);
    }
    return fieldFound;
  }

  @CheckForNull
  public static JMethod getDirectMethod(@Nonnull JDefinedClassOrInterface declaringType,
      @Nonnull String methodNameWithParam) {
    JMethod methodFound = null;
    TypeAndMethodFormatter formatter = Jack.getLookupFormatter();
    for (JMethod m : declaringType.getMethods()) {
      if (formatter.getName(m).startsWith(methodNameWithParam)) {
        if (m.isPublic()) {
          if (methodFound != null) {
              throwMultipleMethodError(declaringType, methodNameWithParam);
          } else {
            methodFound = m;
          }
        }
      }
    }
    return methodFound;
  }

  @CheckForNull
  public static JMethod getMethod(@Nonnull JDefinedClassOrInterface declaringType,
      @Nonnull String methodNameWithParam) {
    JMethod methodFound = getDirectMethod(declaringType, methodNameWithParam);
    if (methodFound != null) {
      return methodFound;
    }
    JClass superClass = declaringType.getSuperClass();
    if (superClass instanceof JDefinedClass) {
      methodFound = getMethod((JDefinedClass) superClass, methodNameWithParam);
      if (methodFound != null) {
        return methodFound;
      }
    }
    for (JInterface interfaceType : declaringType.getImplements()) {
      if (interfaceType instanceof JDefinedInterface) {
        methodFound = getMethod((JDefinedInterface) interfaceType, methodNameWithParam);
        if (methodFound != null) {
          return methodFound;
        }
      }
    }
    return null;
  }

  private static void throwMultipleFieldError(@Nonnull JDefinedClassOrInterface declaringType,
      @Nonnull String name) {
    String typeString = Jack.getUserFriendlyFormatter().getName(declaringType);
    logger.log(Level.SEVERE,
        "Multiple fields found for dynamically referenced field {0} in type {1}",
        new Object[] {name, typeString});
    throw new MultipleFieldsFoundException(
        "Multiple fields found for dynamically referenced field " + name + " in type "
            + typeString);
  }

  private static void throwMultipleMethodError(@Nonnull JDefinedClassOrInterface declaringType,
      @Nonnull String name) {
    String typeString = Jack.getUserFriendlyFormatter().getName(declaringType);
    logger.log(Level.SEVERE,
        "Multiple methods found for dynamically referenced method {0} in type {1}",
        new Object[] {name, typeString});
    throw new MultipleMethodsFoundException(
        "Multiple methods found for dynamically referenced method " + name + " in type "
            + typeString);
  }
}
