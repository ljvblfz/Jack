/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.google.common.collect.Ordering;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodCall.DispatchKind;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JMethodWithReturnLookupException;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.Comparator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Generate accessors for outer fields and methods in an inner class
 */
@Description("Generate accessors for outer fields and methods in an inner class")
@Synchronized
@Transform(
    add = {GetterMarker.class, SetterMarker.class, WrapperMarker.class, JMethodCall.class,
        JNewInstance.class, JNullLiteral.class, JExpressionStatement.class,
        InnerAccessorSchedulingSeparator.SeparatorTag.class},
    remove = {ThreeAddressCodeForm.class, NewInstanceRemoved.class})
@Constraint(no = {SideEffectOperation.class, JAlloc.class,
    InnerAccessorGeneratorSchedulingSeparator.SeparatorConcatRemoverTag.class})
@Filter(SourceTypeFilter.class)
// This schedulable bypasses the classic visiting order by visiting outer classes first
// and their inner classes afterwards.
@ExclusiveAccess(JSession.class)
public class InnerAccessorGenerator implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  static final String THIS_PARAM_NAME = NamingTools.getNonSourceConflictingName("this");

  class Visitor extends JVisitor {

    @Nonnull
    protected final com.android.jack.util.filter.Filter<JMethod> filter =
        ThreadConfig.get(Options.METHOD_FILTER);

    @CheckForNull
    protected TransformationRequest tr;

    @CheckForNull
    private JDefinedClassOrInterface currentType = null;

    /**
     * Determines where the accessor must be located in case of super invocation
     * @param declaringType where the member is declared
     * @return the class where the accessor will be located
     */
    @Nonnull
    private JDefinedClassOrInterface getAccessorClassForSuperCall(
        @Nonnull JDefinedClassOrInterface declaringType) {
      JDefinedClassOrInterface enclosing = currentType;
      assert enclosing != null;

      // If declaringType is an interface, the accessor class is the first enclosing
      // type implementing this interface.
      // If declaringType is a class, the accessor is the first enclosing type
      // extending this class.
      while (!enclosing.canBeSafelyUpcast(declaringType)) {
        enclosing = (JDefinedClass) enclosing.getEnclosingType();
      }

      return enclosing;
    }

    /**
     * Determines where the accessor must be located
     * @param modifier the modifier of the member we try to access to
     * @param declaringType where the member is declared
     * @return the class where the accessor will be located
     */
    @Nonnull
    private JDefinedClassOrInterface getAccessorClass(int modifier,
        @Nonnull JDefinedClassOrInterface declaringType) {
      // Search the first class in which the member is accessible
      // from the referencing type to its enclosing types
      JDefinedClassOrInterface refType = currentType;
      while (refType != null) {
        if (isDirectlyVisibleFrom(modifier, declaringType, refType)) {
          return refType;
        }
        refType = (JDefinedClassOrInterface) refType.getEnclosingType();
      }

      // If not found, the accessor must be in the declaring class
      assert JModifier.isPrivate(modifier);
      return declaringType;
    }

    /**
     * Indicates that a field or method can be accessed without accessor
     * @param modifier the modifier of the field or method
     * @param declaringType the class where the field or method is declared
     * @param type the type from where we want to know if the field or method is accessible
     * @return true if the field or method is visible without accessor
     */
    private boolean isDirectlyVisibleFrom(int modifier,
        @Nonnull JDefinedClassOrInterface declaringType, @Nonnull JDefinedClassOrInterface type) {
      if (JModifier.isPublic(modifier) || declaringType.isSameType(type)) {
        return true;
      }

      if (JModifier.isPrivate(modifier)) {
        // The case when type is the declaring type has already been treated
        return false;
      }

      if (JModifier.isProtected(modifier) && type.canBeSafelyUpcast(declaringType)) {
        return true;
      }

      // The field is protected (but not from a super class) or package
      // We test if both classes are in same package
      return declaringType.getEnclosingPackage() == type.getEnclosingPackage();
    }

    @Override
    public boolean visit(@Nonnull JFieldRef x) {
      JNode parent = x.getParent();
      JField field = x.getFieldId().getField();

      if (field != null) {
        JDefinedClassOrInterface accessorClass =
            getAccessorClass(field.getModifier(), field.getEnclosingType());
        assert currentType != null;
        assert tr != null;
        if (!accessorClass.isSameType(currentType)) {
          assert accessorClass.getSourceInfo().getFileSourceInfo()
              .equals(currentType.getSourceInfo().getFileSourceInfo());
          if (parent instanceof JAsgOperation && ((JAsgOperation) parent).getLhs() == x) {
            // writing access
            //
            handleOuterFieldWrite(tr, x, accessorClass);
          } else {
            // reading access
            //
            handleOuterFieldRead(tr, x, accessorClass);
          }
        }
      }
      return super.visit(x);
    }

    @Override
    public boolean visit(@Nonnull JMethodCall x) {
      JClassOrInterface receiverType = x.getReceiverType();

      // No need to generate an accessor if receiver type is an interface since method will be
      // visible
      if (receiverType instanceof JDefinedClass) {
        JType returnType =
            x instanceof JNewInstance ? JPrimitiveTypeEnum.VOID.getType() : x.getType();
        JMethod method = getMethod((JDefinedClassOrInterface) receiverType,
            (JDefinedClassOrInterface) receiverType, returnType, x.getMethodId());
        // Method can be null when an interface method is implemented by a sub type of the receiver
        // type, but in this case accessors are not needed
        if (method != null) {
          JDefinedClassOrInterface accessorClass;
          boolean isSuper = x.getDispatchKind() == DispatchKind.DIRECT
              && method.getMethodIdWide().getKind() == MethodKind.INSTANCE_VIRTUAL;
          if (isSuper) {
            accessorClass = getAccessorClassForSuperCall(method.getEnclosingType());
          } else {
            accessorClass = getAccessorClass(method.getModifier(), method.getEnclosingType());
          }

          assert accessorClass != null;
          assert currentType != null;
          if (!accessorClass.isSameType(currentType)) {
            assert accessorClass.getSourceInfo().getFileSourceInfo()
                .equals(currentType.getSourceInfo().getFileSourceInfo());
            assert tr != null;
            handleOuterMethodCall(tr, x, method, accessorClass, isSuper);
          }
        }
      }
      return super.visit(x);
    }

    @CheckForNull
    private JMethod getMethod(@Nonnull JDefinedClassOrInterface receiverType,
        @Nonnull JDefinedClassOrInterface typeToSearchMth,
        @Nonnull JType returnType, @Nonnull JMethodIdWide mthId) {
      try {
        JMethod methodFound =
            typeToSearchMth.getMethod(mthId.getName(), returnType, mthId.getParamTypes());
        if (isDirectlyVisibleFrom(methodFound.getModifier(), methodFound.getEnclosingType(),
            receiverType)) {
          return methodFound;
        }
      } catch (JMethodWithReturnLookupException e) {
        // Continue to search into super class
      }

      JClass superClass = typeToSearchMth.getSuperClass();
      JMethod methodFound;
      if (superClass instanceof JDefinedClass) {
        methodFound = getMethod(receiverType, (JDefinedClass) superClass, returnType, mthId);
        if (methodFound != null) {
          return methodFound;
        }
      }

      return null;
    }

    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface type) {
      currentType = type;
      tr = new TransformationRequest(type);
      // Sort types and methods to make this visitor deterministic
      for (JMethod method : methodOrdering.sortedCopy(type.getMethods())) {
        if (!method.isNative() && !method.isAbstract()
            && filter.accept(InnerAccessorGenerator.class, method)) {
          this.accept(method);
        }
      }
      assert tr != null;
      tr.commit();

      for (JClassOrInterface innerType : typeOrdering.sortedCopy(type.getMemberTypes())) {
        if (innerType instanceof JDefinedClassOrInterface) {
          visit((JDefinedClassOrInterface) innerType);
        }
      }
      return false;
    }
  }

  protected void handleOuterFieldWrite(@Nonnull TransformationRequest tr,
      @Nonnull JFieldRef fieldRef, @Nonnull JDefinedClassOrInterface accessorClass) {
    JField field = fieldRef.getFieldId().getField();
    assert(field != null);
    SetterMarker marker = accessorClass.getMarker(SetterMarker.class);
    if (marker == null) {
      marker = new SetterMarker();
      accessorClass.addMarker(marker);
    }
    JMethod setter = marker.getOrCreateSetter(field, (JDefinedClass) accessorClass);

    // this.this$0.field = $value => $set<id>(this.this$0, $value)
    JBinaryOperation binOp = (JBinaryOperation) fieldRef.getParent();

    JMethodIdWide setterId = setter.getMethodIdWide();
    JMethodCall setterCall =
        new JMethodCall(binOp.getSourceInfo(), null, accessorClass, setterId,
            setter.getType(), setterId.canBeVirtual());

    if (!field.isStatic()) {
      JExpression instance = fieldRef.getInstance();
      assert instance != null;
      setterCall.addArg(instance);
    }
    setterCall.addArg(binOp.getRhs());
    assert setterCall.getArgs().size() == setter.getParams().size();

    tr.append(new Replace(binOp, setterCall));
  }

  protected void handleOuterFieldRead(@Nonnull TransformationRequest tr,
      @Nonnull JFieldRef fieldRef, @Nonnull JDefinedClassOrInterface accessorClass) {
    JField field = fieldRef.getFieldId().getField();
    assert(field != null);
    GetterMarker marker = accessorClass.getMarker(GetterMarker.class);
    if (marker == null) {
      marker = new GetterMarker();
      accessorClass.addMarker(marker);
    }
    JMethod getter = marker.getOrCreateGetter(field, (JDefinedClass) accessorClass);

    // this.this$0.field => $get<id>(this.this$0)
    JMethodIdWide getterId = getter.getMethodIdWide();
    JMethodCall getterCall = new JMethodCall(fieldRef.getSourceInfo(), null, accessorClass,
        getterId, getter.getType(), getterId.canBeVirtual());

    if (!field.isStatic()) {
      JExpression instance = fieldRef.getInstance();
      assert instance != null;
      getterCall.addArg(instance);
    }

    assert getterCall.getArgs().size() == getter.getParams().size();

    tr.append(new Replace(fieldRef, getterCall));
  }

  protected void handleOuterMethodCall(@Nonnull TransformationRequest tr,
      @Nonnull JMethodCall methodCall, @Nonnull JMethod method,
      @Nonnull JDefinedClassOrInterface accessorClass, boolean isSuper) {
    WrapperMarker marker = accessorClass.getMarker(WrapperMarker.class);
    if (marker == null) {
      marker = new WrapperMarker();
      accessorClass.addMarker(marker);
    }

    JMethod wrapper = marker.getOrCreateWrapper(method, (JDefinedClass) accessorClass,
        isSuper, methodCall.getReceiverType());

    JMethodCall wrapperCall = null;
    SourceInfo sourceInfo = methodCall.getSourceInfo();
    if (methodCall instanceof JNewInstance) {
      assert wrapper instanceof JConstructor;
      wrapperCall =
          new JNewInstance(sourceInfo, wrapper.getEnclosingType(), wrapper.getMethodIdWide());
    } else {

      JMethodIdWide wrapperId = wrapper.getMethodIdWide();

      // this.this$0.method(param) => $wrap<id>(this.this$0, param)
      if (!method.isStatic() && !(wrapper instanceof JConstructor)) {
        wrapperCall = new JMethodCall(sourceInfo, null, accessorClass, wrapperId,
            wrapper.getType(), wrapperId.canBeVirtual());
        JExpression instance = methodCall.getInstance();
        assert instance != null;
        wrapperCall.addArg(instance);
      } else {
        wrapperCall = new JMethodCall(sourceInfo, methodCall.getInstance(), accessorClass,
            wrapperId, wrapper.getType(), wrapperId.canBeVirtual());
      }
    }

    for (JExpression arg : methodCall.getArgs()) {
      wrapperCall.addArg(arg);
    }

    if (wrapper instanceof JConstructor) {
      int numberOfParamToAdd = wrapper.getParams().size() - method.getParams().size();
      for (int i = 0; i < numberOfParamToAdd; i++) {
        wrapperCall.addArg(new JNullLiteral(sourceInfo));
      }
    }

    assert wrapperCall.getArgs().size() == wrapper.getParams().size();
    tr.append(new Replace(methodCall, wrapperCall));
  }

  @Nonnull
  TypePackageAndMethodFormatter formatter = Jack.getLookupFormatter();

  @Nonnull
  private final Ordering<JMethod> methodOrdering = Ordering.from(new Comparator<JMethod>() {
    @Override
    public int compare(@Nonnull JMethod m1, @Nonnull JMethod m2) {
      return formatter.getName(m1).compareTo(formatter.getName(m2));
    }
  });

  @Nonnull
  private final Ordering<JClassOrInterface> typeOrdering =
      Ordering.from(new Comparator<JClassOrInterface>() {
        @Override
        public int compare(@Nonnull JClassOrInterface t1, @Nonnull JClassOrInterface t2) {
          return formatter.getName(t1).compareTo(formatter.getName(t2));
        }
      });

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    // Start visit on outer types for a deterministic visit order.
    if (type.getEnclosingType() != null) {
      return;
    }

    // No need to visit types without inner classes.
    if (type.getMemberTypes().isEmpty()) {
      return;
    }

    new Visitor().accept(type);
  }
}
