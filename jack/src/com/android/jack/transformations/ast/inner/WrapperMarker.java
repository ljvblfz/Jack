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

package com.android.jack.transformations.ast.inner;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.formatter.IdentifierFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.util.NamingTools;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This marker indicates that a method has an associated wrapper.
 */
@ValidOn(JDefinedClass.class)
@Description("This marker indicates that a method has an associated wrapper.")
public class WrapperMarker implements Marker {

  @Nonnull
  private static final String WRAPPER_PREFIX = NamingTools.getNonSourceConflictingName("wrap");

  @Nonnull
  private final HashMap<MethodCallDescriptor, JMethod> wrappers =
    new HashMap<MethodCallDescriptor, JMethod>();

   private static class MethodCallDescriptor {

    @Nonnull
    private final JMethod method;

    private final boolean isSuper;

    @Nonnull
    private final JClassOrInterface mthCallReceiverType;

    private MethodCallDescriptor(@Nonnull JMethod method, boolean isSuper,
        @Nonnull JClassOrInterface mthCallReceiverType) {
      this.method = method;
      this.isSuper = isSuper;
      this.mthCallReceiverType = mthCallReceiverType;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
      if (obj instanceof MethodCallDescriptor) {
        MethodCallDescriptor toCompare = (MethodCallDescriptor) obj;
        return method.equals(toCompare.method)
            && isSuper == toCompare.isSuper
            && mthCallReceiverType.isSameType(toCompare.mthCallReceiverType);
      }
      return false;
    }

    @Override
    public final int hashCode() {
      int hashCode = method.hashCode() ^ mthCallReceiverType.hashCode();
      return 31 * hashCode + Boolean.valueOf(isSuper).hashCode();
    }
  }

  @CheckForNull
  private JMethod getWrapper(@Nonnull JMethod method, boolean isStaticDispatchOnly,
      @Nonnull JClassOrInterface mthCallReceiverType) {
    MethodCallDescriptor descriptor =
        new MethodCallDescriptor(method, isStaticDispatchOnly, mthCallReceiverType);
    return wrappers.get(descriptor);
  }

  private boolean hasSameKeyWithoutReceiverType(@Nonnull MethodCallDescriptor newKey) {
    for (MethodCallDescriptor mcd : wrappers.keySet()) {
      if (mcd.method.equals(newKey.method)
            && mcd.isSuper == newKey.isSuper) {
        return true;
      }
    }

    return false;
  }

  private void addWrapper(@Nonnull JMethod method, @Nonnull JMethod wrapper,
      boolean isStaticDispatchOnly, @Nonnull JClassOrInterface mthCallReceiverType) {
    MethodCallDescriptor descriptor =
        new MethodCallDescriptor(method, isStaticDispatchOnly, mthCallReceiverType);
    // Check if Jack generates an additional accessor due to receiver type.
    if (hasSameKeyWithoutReceiverType(descriptor)) {
      throw new AssertionError();
    }
    assert !wrappers.containsKey(descriptor);
    wrappers.put(descriptor, wrapper);
  }

  @Nonnull
  Collection<JMethod> getAllWrappers() {
    return wrappers.values();
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError("Not yet supported");
  }

  @Name("InnerAccessorWrapper")
  @Description("All JNodes created for a wrapper allowing to access an inner method.")
  @ComposedOf({JMethod.class,
      JConstructor.class,
      JThisRef.class,
      JParameter.class,
      JParameterRef.class,
      JMethodCall.class,
      JMethodBody.class,
      JReturnStatement.class,
      JBlock.class})
  static class InnerAccessorWrapper implements AbstractComponent {
  }

  @Nonnull
  // TODO(delphinemartin): Warning: this is not thread-safe
  JMethod getOrCreateWrapper(@Nonnull JMethod method,
      @Nonnull JDefinedClass accessorClass,
      boolean isSuper,
      @Nonnull JClassOrInterface mthCallReceiverType) {
    // $wrap<id>($param) {
    //   return method($param);
    // }

    JMethod wrapper = getWrapper(method, isSuper, mthCallReceiverType);
    if (wrapper == null) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;

      boolean isConstructor = method instanceof JConstructor;
      if (isConstructor) {
        wrapper = new JConstructor(sourceInfo, accessorClass, JModifier.SYNTHETIC);
      } else {
        String wrapperName = WRAPPER_PREFIX;
        // It is a temporary deterministic name that will be replace by an index into
        // InnerAccessorAdder
        wrapperName += IdentifierFormatter.getFormatter().getName(mthCallReceiverType)
            + IdentifierFormatter.getFormatter().getName(method) + isSuper;
        wrapper =
            new JMethod(sourceInfo,
                new JMethodId(new JMethodIdWide(wrapperName, MethodKind.STATIC),
                    method.getType()),
                accessorClass,
                JModifier.SYNTHETIC | JModifier.STATIC);
      }

      JExpression instance = null;
      JMethodIdWide id = wrapper.getMethodIdWide();
      if (isConstructor) {
        JThis jThis = wrapper.getThis();
        assert jThis != null;
        instance = jThis.makeRef(sourceInfo);
      } else if (!method.isStatic()){
        JParameter thisParam =
            new JParameter(sourceInfo, InnerAccessorGenerator.THIS_PARAM_NAME, accessorClass,
                JModifier.FINAL | JModifier.SYNTHETIC, wrapper);
        wrapper.addParam(thisParam);
        id.addParam(accessorClass);
        instance = thisParam.makeRef(sourceInfo);
      }

      JMethodIdWide calledMethodId = method.getMethodIdWide();
      JMethodCall methodCall = new JMethodCall(sourceInfo, instance, mthCallReceiverType,
          calledMethodId,
          method.getType(), calledMethodId.canBeVirtual() && !isSuper /* isVirtualDispatch */);
      for (JParameter param : method.getParams()) {
        JType paramType = param.getType();
        JParameter newParam = new JParameter(sourceInfo, param.getName(), paramType,
            param.getModifier(), wrapper);
        wrapper.addParam(newParam);
        id.addParam(paramType);
        methodCall.addArg(newParam.makeRef(sourceInfo));
      }

      if (isConstructor) {
        while (constructorExists((JConstructor) wrapper, accessorClass)) {
          JParameter newParam = new JParameter(
              sourceInfo, InnerAccessorGenerator.THIS_PARAM_NAME + wrapper.getParams().size(),
              accessorClass, JModifier.SYNTHETIC, wrapper);
          wrapper.addParam(newParam);
          id.addParam(accessorClass);
        }
      }

      JBlock bodyBlock = new JBlock(sourceInfo);
      JMethodBody body = new JMethodBody(sourceInfo, bodyBlock);

      assert methodCall.getArgs().size() == methodCall.getMethodId().getParamTypes().size();

      if (method.getType() == JPrimitiveTypeEnum.VOID.getType()) {
        bodyBlock.addStmt(methodCall.makeStatement());
        bodyBlock.addStmt(new JReturnStatement(sourceInfo, null));
      } else {
        bodyBlock.addStmt(new JReturnStatement(sourceInfo, methodCall));
      }
      wrapper.setBody(body);
      addWrapper(method, wrapper, isSuper, mthCallReceiverType);
    }

    return wrapper;
  }

  /**
   * Searches in the tree and in the wrapper that will be added
   * @param wrapper
   * @param accessorClass
   * @return true if the constructor already exists
   */
  private boolean constructorExists(
      @Nonnull JConstructor wrapper, @Nonnull JDefinedClass accessorClass) {
    for (JMethod method : accessorClass.getMethods()) {
      if (method instanceof JConstructor && hasSameArgumentType(wrapper, (JConstructor) method)) {
        return true;
      }
    }

    for (JMethod method : getAllWrappers()) {
      if (method instanceof JConstructor && hasSameArgumentType(wrapper, (JConstructor) method)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasSameArgumentType(@Nonnull JMethod wrapper, @Nonnull JConstructor method) {
    List<JParameter> wrapperParams = wrapper.getParams();
    List<JParameter> methodParams = method.getParams();
    int size = wrapperParams.size();
    if (size != methodParams.size()) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!wrapperParams.get(i).getType().isSameType(methodParams.get(i).getType())) {
        return false;
      }
    }
    return true;
  }
}
