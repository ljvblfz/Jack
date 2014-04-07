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

package com.android.jack.transformations.ast;

import com.android.jack.Options;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Remove compound assignment operator.
 */
@Description("Remove new array with multiple dimension.")
@Name("MultiDimensionNewArrayRemover")
@Constraint(need = OriginalNames.class)
@Transform(remove = {MultiDimensionNewArray.class, ThreeAddressCodeForm.class}, add = {
    JNewArray.class, JMethodCall.class, JClassLiteral.class, JDynamicCastOperation.class,
    InitInNewArray.class})
public class MultiDimensionNewArrayRemover implements RunnableSchedulable<JMethod>
{

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;
    @CheckForNull
    private JArrayType intArrayType;
    @CheckForNull
    private JClassOrInterface reflectArray;
    @Nonnull
    private final JSession session;
    @CheckForNull
    private JMethodId newInstance;

    public Visitor(@Nonnull TransformationRequest tr, @Nonnull JSession session) {
      this.tr = tr;
      this.session = session;
    }

    @Override
    public boolean visit(@Nonnull JNewArray newArray) {

      List<JExpression> allDims = newArray.getDims();
      if (!allDims.isEmpty()) {
        List<JExpression> presentDimensions = getPresentDimensions(allDims);
        int nbPresentDimensions = presentDimensions.size();
        if (nbPresentDimensions > 1) {
          SourceInfo sourceInfo = newArray.getSourceInfo();
          JClassOrInterface reflectArrayType = getReflectArrayType();
          JMethodId newInstanceId = getNewInstanceId(reflectArrayType);
          JMethodCall call = new JMethodCall(sourceInfo, null, reflectArrayType, newInstanceId,
              session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT),
              newInstanceId.canBeVirtual());
          call.addArg(new JClassLiteral(
              sourceInfo, getComponentTypeForNewInstance(newArray, nbPresentDimensions),
              session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CLASS)));
         call.addArg(JNewArray.createWithInits(sourceInfo, getIntArrayType(), presentDimensions));
          tr.append(new Replace(newArray, new JDynamicCastOperation(sourceInfo, newArray
              .getArrayType(), call)));
        }
      }
      return super.visit(newArray);
    }

    @Nonnull
    private JMethodId getNewInstanceId(JClassOrInterface reflectArrayType) {
      if (newInstance == null) {
        List<JType> argsType = new ArrayList<JType>(2);
        argsType.add(session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CLASS));
        argsType.add(getIntArrayType());
        newInstance = reflectArrayType.getOrCreateMethodId("newInstance", argsType,
            MethodKind.STATIC);
      }

      assert newInstance != null;
      return newInstance;
    }

    /**
     * Return JArrayType of {@code newArray} less one dimension per declared dimension of the
     * {@link JNewArray}. For example : getComponentTypeForNewInstance(<new int[1][2][]>) returns
     * JArrayType(<int[]>).
     */
    @Nonnull
    private JType getComponentTypeForNewInstance(@Nonnull JNewArray newArray,
        @Nonnegative int nbPresentDim) {
      JType componentType = newArray.getArrayType();
      for (int i = 0; i < nbPresentDim; i++) {
        componentType = ((JArrayType) componentType).getElementType();
      }
      return componentType;
    }

    /**
     * Filter out JAbsentArrayDimension.
     */
    @Nonnull
    private List<JExpression> getPresentDimensions(@Nonnull List<JExpression> newArrayDims) {
      List<JExpression> presentDims = new ArrayList<JExpression>(newArrayDims.size());
      boolean inPresentDims = true;
      for (JExpression expression : newArrayDims) {
        if (expression instanceof JAbsentArrayDimension) {
          inPresentDims = false;
        } else {
          assert inPresentDims;
          presentDims.add(expression);
        }
      }
      return presentDims;
    }

    @Nonnull
    private JClassOrInterface getReflectArrayType() {
      if (reflectArray == null) {
        reflectArray =
            (JClassOrInterface) session.getPhantomLookup().getType("Ljava/lang/reflect/Array;");
      }

      assert reflectArray != null;
      return reflectArray;
    }

    /**
     * @return the intArrayType
     */
    @Nonnull
    private JArrayType getIntArrayType() {
      if (intArrayType == null) {
        intArrayType = (JArrayType) session.getLookup().getType("[I");
      }

      assert intArrayType != null;
      return intArrayType;
    }

  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface enclosingType = method.getEnclosingType();
    if (enclosingType.isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr, enclosingType.getSession());
    visitor.accept(method);
    tr.commit();
  }

}
