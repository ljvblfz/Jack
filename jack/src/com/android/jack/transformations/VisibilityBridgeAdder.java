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

package com.android.jack.transformations;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.scheduling.feature.VisibilityBridge;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * Build bridge on public method declared in non public super class.
 */
@Description("Build bridge on public method declared in non public super class.")
@Synchronized
@Transform(add = {JMethod.class, JParameter.class, JMethodBody.class,
    JMethodCall.class, JThisRef.class, JReturnStatement.class, JExpressionStatement.class,
    JParameterRef.class, JBlock.class}, remove = ThreeAddressCodeForm.class)
@Support(VisibilityBridge.class)
@HasKeyId
public class VisibilityBridgeAdder implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  public static final BooleanPropertyId VISIBILITY_BRIDGE = BooleanPropertyId.create(
      "jack.legacy.runtime.visibilitybridges", "Generate visibility bridges").addDefaultValue(
      Boolean.TRUE);

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    if (declaredType.isExternal()
        || !declaredType.isPublic()
        || !(declaredType instanceof JDefinedClass)) {
      return;
    }
    JDefinedClass superClass = (JDefinedClass) declaredType.getSuperClass();
    while (superClass != null && !superClass.isPublic()) {
      for (JMethod method : superClass.getMethods()) {
        if (method.isPublic() && !(method instanceof JConstructor) && !method.isStatic()
            && !method.isFinal()) {
          try {
            declaredType.getMethod(method.getName(), method.getType(),
                method.getMethodId().getParamTypes());
          } catch (JMethodLookupException e) {
            // the method is not declared in class, a bridge is required
            synthesizeBridge((JDefinedClass) declaredType, method);
          }
        }
      }
      superClass = (JDefinedClass) superClass.getSuperClass();
    }
  }

  private void synthesizeBridge(@Nonnull JDefinedClass jClass, @Nonnull JMethod method) {
    SourceInfo sourceInfo = SourceInfo.UNKNOWN;
    JMethodId methodId = method.getMethodId();

    int bridgeModifier = method.getModifier();
    // Remove non inherited flags
    bridgeModifier &= ~(JModifier.SYNCHRONIZED | JModifier.ABSTRACT | JModifier.STRICTFP
        | JModifier.NATIVE);
    // Add bridge specific flags
    bridgeModifier |= JModifier.SYNTHETIC | JModifier.BRIDGE;
    JMethod bridge = new JMethod(sourceInfo, methodId, jClass, method.getType(), bridgeModifier);
    for (JParameter param : method.getParams()) {
      bridge.addParam(new JParameter(sourceInfo, param.getName(), param.getType(),
          param.getModifier(), bridge));
    }

    JBlock bodyBlock = new JBlock(sourceInfo);
    JMethodBody body = new JMethodBody(sourceInfo, bodyBlock);
    JClass superClass = jClass.getSuperClass();
    assert superClass != null;
    JThis jThis = bridge.getThis();
    assert jThis != null;
    JMethodCall callToSuper = new JMethodCall(sourceInfo,
        new JThisRef(sourceInfo, jThis), superClass, methodId, method.getType(),
        false /* isVirtualDispatch */);
    for (JParameter param : bridge.getParams()) {
      callToSuper.addArg(new JParameterRef(sourceInfo, param));
    }

    if (method.getType() != JPrimitiveTypeEnum.VOID.getType()) {
      bodyBlock.addStmt(new JReturnStatement(sourceInfo, callToSuper));
    } else {
      bodyBlock.addStmt(new JExpressionStatement(sourceInfo, callToSuper));
      bodyBlock.addStmt(new JReturnStatement(sourceInfo, null));
    }

    bridge.setBody(body);

    TransformationRequest tr = new TransformationRequest(jClass);
    tr.append(new AppendMethod(jClass, bridge));
    tr.commit();

  }

}
