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

package com.android.jack.transformations.lambda;

import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.AppendArgument;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.schedulable.Transform;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Represents information about mapping of a lambda into lambda group class */
@Description("Represents information about mapping of a lambda into lambda group class")
@ValidOn(JLambda.class)
@Transform(add = { JByteLiteral.class, JIntLiteral.class,
                   JNewInstance.class, JShortLiteral.class },
    remove = NewInstanceRemoved.class)
final class LambdaInfoMarker implements Marker {
  /** Using -1 to indicate it is the only lambda in the group */
  static final int NO_LAMBDA_ID = -1;

  @Nonnull
  private final JDefinedClass groupClass;
  @CheckForNull
  private final JField instanceField;
  /** Lambda class id or NO_LAMBDA_ID */
  private final int lambdaId;
  @Nonnull
  private final int[] captureMapping;

  LambdaInfoMarker(@Nonnull JDefinedClass groupClass,
      @CheckForNull JField instanceField, int lambdaId, @Nonnull int[] captureMapping) {
    this.groupClass = groupClass;
    this.instanceField = instanceField;
    this.lambdaId = lambdaId;
    this.captureMapping = captureMapping;
  }

  boolean hasId() {
    return lambdaId != NO_LAMBDA_ID;
  }

  @Nonnegative
  public int getId() {
    assert hasId();
    return lambdaId;
  }

  boolean hasInstanceField() {
    return instanceField != null;
  }

  @Nonnull
  JField getInstanceField() {
    assert instanceField != null;
    return instanceField;
  }

  @Nonnull
  int[] getCaptureMapping() {
    return captureMapping;
  }

  /** Create an instantiation expression for the lambda group class */
  @Nonnull
  JNewInstance createGroupClassInstance(@Nonnull TransformationRequest request,
      @Nonnull List<JExpression> origArgs, @Nonnull SourceInfo origSourceInfo) {
    return createGroupClassInstance(request, getGroupClassConstructor(), origArgs, origSourceInfo);
  }

  @Nonnull
  private JConstructor getGroupClassConstructor() {
    // There must be one and only one constructor
    for (JMethod method : groupClass.getMethods()) {
      if (method instanceof JConstructor) {
        return (JConstructor) method;
      }
    }
    throw new AssertionError();
  }

  /** Create an instantiation expression for the lambda group class */
  @Nonnull
  JNewInstance createGroupClassInstance(
      @Nonnull TransformationRequest request, @Nonnull JConstructor constructor,
      @Nonnull List<JExpression> args, @Nonnull SourceInfo sourceInfo) {

    JMethodId constructorId = constructor.getMethodId();
    List<JType> paramTypes = constructorId.getMethodIdWide().getParamTypes();
    JExpression[] newArgs = new JExpression[paramTypes.size()];
    int offset = 0;

    // The first argument may be a lambda class id
    if (this.hasId()) {
      // If the lambda has an id the constructor of the group
      // class must have the first parameter taking it
      assert paramTypes.size() > 0;
      JIntegralType32 idType = (JIntegralType32) paramTypes.get(0);
      newArgs[0] = idType.createLiteral(SourceInfo.UNKNOWN, this.getId());
      offset = 1;
    }

    // Remap the rest of the arguments
    int size = args.size();
    assert newArgs.length == (size + offset);
    int[] mapping = this.getCaptureMapping();
    assert size == mapping.length;
    for (int i = 0; i < size; i++) {
      int newIdx = mapping[i] + offset;
      assert newIdx < newArgs.length && newArgs[newIdx] == null;
      newArgs[newIdx] = args.get(i);
    }

    // Create instance creation
    JNewInstance newNode = new JNewInstance(
        sourceInfo, this.groupClass, constructorId);
    for (JExpression newArg : newArgs) {
      assert newArg != null;
      request.append(new AppendArgument(newNode, newArg));
    }
    return newNode;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError();
  }
}
