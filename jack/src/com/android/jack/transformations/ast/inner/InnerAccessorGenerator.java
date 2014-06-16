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

import com.android.jack.Options;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
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
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.impl.ResolutionTargetMarker;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Generate accessors for outer fields and methods in an inner class
 */
@Description("Generate accessors for outer fields and methods in an inner class")
@Synchronized
@Transform(add = {GetterMarker.class,
    SetterMarker.class,
    WrapperMarker.class,
    JMethodCall.class,
    JNewInstance.class,
    JNullLiteral.class,
    JExpressionStatement.class,
    InnerAccessorSchedulingSeparator.SeparatorTag.class},
    remove = {ThreeAddressCodeForm.class, NewInstanceRemoved.class})
@Constraint(no = {SideEffectOperation.class, JAlloc.class})
public class InnerAccessorGenerator implements RunnableSchedulable<JMethod> {

  @Nonnull
  static final String THIS_PARAM_NAME = NamingTools.getNonSourceConflictingName("this");

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final JDefinedClassOrInterface currentType;

    public Visitor(@Nonnull TransformationRequest tr,
        @Nonnull JDefinedClassOrInterface currentType) {
      this.tr = tr;
      this.currentType = currentType;
    }

    /**
     * Determines where the accessor must be located in case of super invocation
     * @param declaringType where the member is declared
     * @return the class where the accessor will be located
     */
    @Nonnull
    private JDefinedClassOrInterface getAccessorClassForSuperCall(
        @Nonnull JDefinedClassOrInterface declaringType) {

      // if the instance is the super of an enclosing class, we have to retrieve it
      JDefinedClass enclosing = (JDefinedClass) currentType;
      while (!isSuperClassOf((JDefinedClass) declaringType, enclosing)) {
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
      if (JModifier.isPublic(modifier) || declaringType == type) {
        return true;
      }

      if (JModifier.isPrivate(modifier)) {
        // The case when type is the declaring type has already been treated
        return false;
      }

      if (JModifier.isProtected(modifier) && type instanceof JDefinedClass
          && isSuperClassOf((JDefinedClass) declaringType, (JDefinedClass) type)) {
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
      assert field != null;
      JDefinedClassOrInterface accessorClass = getAccessorClass(field.getModifier(),
          field.getEnclosingType());
      if (accessorClass != currentType) {
        assert accessorClass.getSourceInfo().getFileSourceInfo()
            .equals(currentType.getSourceInfo().getFileSourceInfo());
        if (parent instanceof JAsgOperation
            && ((JAsgOperation) parent).getLhs() == x) {
          // writing access
          //
          SetterMarker marker = accessorClass.getMarker(SetterMarker.class);
          if (marker == null) {
            marker = new SetterMarker();
            accessorClass.addMarker(marker);
          }
          JMethod setter = marker.getOrCreateSetter(field, (JDefinedClass) accessorClass);

          // this.this$0.field = $value => $set<id>(this.this$0, $value)
          JBinaryOperation binOp = (JBinaryOperation) x.getParent();

          JMethodId setterId = setter.getMethodId();
          JMethodCall setterCall =
              new JMethodCall(binOp.getSourceInfo(), null, accessorClass, setterId,
                  setter.getType(), setterId.canBeVirtual());

          if (!field.isStatic()) {
            JExpression instance = x.getInstance();
            assert instance != null;
            setterCall.addArg(instance);
          }
          setterCall.addArg(binOp.getRhs());
          assert setterCall.getArgs().size() == setter.getParams().size();

          tr.append(new Replace(binOp, setterCall));
        } else {
          // reading access
          //
          GetterMarker marker = accessorClass.getMarker(GetterMarker.class);
          if (marker == null) {
            marker = new GetterMarker();
            accessorClass.addMarker(marker);
          }
          JMethod getter = marker.getOrCreateGetter(field, (JDefinedClass) accessorClass);

          // this.this$0.field => $get<id>(this.this$0)
          JMethodId getterId = getter.getMethodId();
          JMethodCall getterCall = new JMethodCall(x.getSourceInfo(), null, accessorClass,
              getterId, getter.getType(), getterId.canBeVirtual());

          if (!field.isStatic()) {
            JExpression instance = x.getInstance();
            assert instance != null;
            getterCall.addArg(instance);
          }

          assert getterCall.getArgs().size() == getter.getParams().size();

          tr.append(new Replace(x, getterCall));
        }
      }
      return super.visit(x);
    }

    @Override
    public boolean visit(@Nonnull JMethodCall x) {
      ResolutionTargetMarker resolutionTargetMarker = x.getMarker(ResolutionTargetMarker.class);
      if (resolutionTargetMarker != null) {
        JMethod method = resolutionTargetMarker.getTarget();
        JDefinedClassOrInterface accessorClass;
        boolean isSuper = x.getDispatchKind() == DispatchKind.DIRECT
            && method.getMethodId().getKind() == MethodKind.INSTANCE_VIRTUAL;
        if (isSuper) {
          accessorClass = getAccessorClassForSuperCall(method.getEnclosingType());
        } else {
          accessorClass = getAccessorClass(method.getModifier(),
              method.getEnclosingType());
        }

        if (accessorClass != currentType) {
          assert accessorClass.getSourceInfo().getFileSourceInfo()
            .equals(currentType.getSourceInfo().getFileSourceInfo());

          WrapperMarker marker = accessorClass.getMarker(WrapperMarker.class);
          if (marker == null) {
            marker = new WrapperMarker();
            accessorClass.addMarker(marker);
          }

          JMethod wrapper = marker.getOrCreateWrapper(method, (JDefinedClass) accessorClass,
              isSuper);

          JMethodCall wrapperCall = null;
          SourceInfo sourceInfo = x.getSourceInfo();
          if (x instanceof JNewInstance) {
            assert wrapper instanceof JConstructor;
            wrapperCall =
                new JNewInstance(sourceInfo, wrapper.getEnclosingType(), wrapper.getMethodId());
          } else {

            JMethodId wrapperId = wrapper.getMethodId();

            // this.this$0.method(param) => $wrap<id>(this.this$0, param)
            if (!method.isStatic() && !(wrapper instanceof JConstructor)) {
              wrapperCall = new JMethodCall(sourceInfo, null, accessorClass, wrapperId,
                  wrapper.getType(), wrapperId.canBeVirtual());
              JExpression instance = x.getInstance();
              assert instance != null;
              wrapperCall.addArg(instance);
            } else {
              wrapperCall = new JMethodCall(sourceInfo, x.getInstance(), accessorClass,
                  wrapperId, wrapper.getType(), wrapperId.canBeVirtual());
            }
          }

          for (JExpression arg : x.getArgs()) {
            wrapperCall.addArg(arg);
          }

          if (wrapper instanceof JConstructor) {
            int numberOfParamToAdd = wrapper.getParams().size() - method.getParams().size();
            for (int i = 0; i < numberOfParamToAdd; i++) {
              wrapperCall.addArg(new JNullLiteral(sourceInfo));
            }
          }

          assert wrapperCall.getArgs().size() == wrapper.getParams().size();
          tr.append(new Replace(x, wrapperCall));
        }
      }
      return super.visit(x);
    }
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr, method.getEnclosingType());
    visitor.accept(method);
    tr.commit();
  }


  /**
   * @param type
   * @return true if the instance is a superclass of type
   */
  static boolean isSuperClassOf(JDefinedClass possibleSuper, JDefinedClass type) {
    JDefinedClassOrInterface superClass = (JDefinedClassOrInterface) type.getSuperClass();
    while (superClass != null) {
      if (possibleSuper == superClass) {
        return true;
      }
      superClass = (JDefinedClassOrInterface) superClass.getSuperClass();
    }
    return false;
  }


}
