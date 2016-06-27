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

package com.android.jack.frontend;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JEnum;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.transformations.SanityChecks;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;
/**
 * This {@link RunnableSchedulable} checks that external instances of JType
 * have been replaced by their resolved counterparts in IR.
 */
@Description("checks that external instances of JType have been replaced by their resolved " +
    "counterparts in IR.")
@Name("TypeDuplicatesRemoverChecker")
@Support(SanityChecks.class)
public class TypeDuplicateRemoverChecker implements RunnableSchedulable<JSession> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JSession session;

    public Visitor(@Nonnull JSession session) {
      this.session = session;
    }

    @Override
    public void endVisit(@Nonnull JNode x) {
      checkFieldsOf(x.getClass(), x, session);
    }
  }

  @Override
  public void run(@Nonnull JSession session) {
    TypeDuplicateRemoverChecker.checkFieldsOf(Jack.getSession().getPhantomLookup().getClass(),
        Jack.getSession().getPhantomLookup(), session);

    Visitor visitor = new Visitor(session);
    for (JDefinedClassOrInterface declaredType : session.getTypesToEmit()) {
      visitor.accept(declaredType);
    }
  }

  @SuppressWarnings("rawtypes")
  public static void checkFieldsOf(@Nonnull Class<?> type, @Nonnull Object node, JSession session) {
      JPhantomLookup lookup = session.getPhantomLookup();
      for (Field f : type.getDeclaredFields()) {
        boolean fieldAccess = f.isAccessible();
        try {
          f.setAccessible(true);
          Object fieldObject = f.get(node);
          if (fieldObject instanceof JType) {
            JType typeField = (JType) fieldObject;
            if (typeField instanceof JClassOrInterface ||
                typeField instanceof JArrayType) {
              checkType(node, lookup, f, typeField);
              if (typeField instanceof JArrayType) {
                // break the stack overflow (JArrayType.array <=> JArrayType.elementType)
                if (((JArrayType) typeField).getElementType() != node) {
                  checkFieldsOf(typeField.getClass(), typeField, session);
                }
              }
            }
          } else if (fieldObject instanceof Collection){
            Collection collection = (Collection) fieldObject;
            Iterator it = collection.iterator();
            while (it.hasNext()) {
              Object object = it.next();
              if (object instanceof JClassOrInterface ||
                  object instanceof JArrayType) {
                checkType(node, lookup, f, (JType) object);
              }
             }
           } else if (fieldObject instanceof JType[]) {
             JType [] types = (JType[]) fieldObject;
             for (JType t : types) {
               checkType(node, lookup, f, t);
             }
            } else if (fieldObject instanceof JFieldId) {
              checkFieldsOf(fieldObject.getClass(), fieldObject, session);
            }
        } catch (IllegalArgumentException e) {
          throw new AssertionError("Error during duplicate types checking.");
        } catch (SecurityException e) {
          throw new AssertionError("Error during duplicate types checking.");
        } catch (IllegalAccessException e) {
          throw new AssertionError("Error during duplicate types checking.");
        } finally {
          f.setAccessible(fieldAccess);
        }
      }
      if (type.getSuperclass() != null && type.getSuperclass() != JNode.class) {
        checkFieldsOf(type.getSuperclass(), node, session);
      }
      for (Class<?> interf : type.getInterfaces()) {
        checkFieldsOf(interf, node, session);
      }
  }

  private static void checkType(@Nonnull Object node, @Nonnull JPhantomLookup lookup,
      @Nonnull Field f, @Nonnull JType typeToCheck) throws AssertionError {
    JType typeFoundInLookup;
    String signature = Jack.getLookupFormatter().getName(typeToCheck);
    if (typeToCheck instanceof JEnum) {
      typeFoundInLookup = lookup.getEnum(signature);
    } else if (typeToCheck instanceof JAnnotationType) {
      typeFoundInLookup = lookup.getAnnotationType(signature);
    } else if (typeToCheck instanceof JClass) {
      typeFoundInLookup = lookup.getClass(signature);
    } else if (typeToCheck instanceof JInterface) {
      typeFoundInLookup = lookup.getInterface(signature);
    } else {
      typeFoundInLookup = lookup.getType(signature);
    }
    if (typeToCheck != typeFoundInLookup) {
      throw createError(node, f);
    }
  }

  @Nonnull
  private static AssertionError createError(@Nonnull Object checked, @Nonnull Field f) {
    String message = "Duplicate type found in " + checked.toString() + " of class " +
        checked.getClass().getName() + " in field " + f.getName();
    return new AssertionError(message);
  }

}
