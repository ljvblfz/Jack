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

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdRef;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Finishing lambda group classes construction */
@Description("Lambdas optimization, finishing lambda group classes construction")
@Constraint(need = LambdaGroupMarker.class)
@Transform(
    add = {JAsgOperation.class, JBlock.class, JByteLiteral.class, JConstructor.class,
           JDynamicCastOperation.class, JField.class, JFieldRef.class, JIntLiteral.class,
           JMethod.class, JMethodBody.class, JMethodCall.class, JNewInstance.class,
           JParameter.class, JParameterRef.class, JReturnStatement.class, JShortLiteral.class,
           JSwitchStatement.class, JThisRef.class, JThrowStatement.class, LambdaInfoMarker.class},
    remove = {NewInstanceRemoved.class})
@ExclusiveAccess(JSession.class)
@Support(LambdaToAnonymousConverter.class)
@Filter(TypeWithoutValidTypePrebuilt.class)
public final class LambdaGroupClassFinalizer
    implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private static final TypePackageAndMethodFormatter FORMATTER =
      Jack.getLookupFormatter();

  private final boolean simplifyStateless =
      ThreadConfig.get(Options.LAMBDA_SIMPLIFY_STATELESS).booleanValue();
  private final boolean mergeInterfaces =
      ThreadConfig.get(Options.LAMBDA_MERGE_INTERFACES).booleanValue();
  @Nonnull
  private final JPhantomLookup phantomLookup =
      Jack.getSession().getPhantomLookup();
  @Nonnull
  private final JClass javaLangObject =
      phantomLookup.getClass(CommonTypes.JAVA_LANG_OBJECT);
  @Nonnull
  private final JClass javaLangAssertionError =
      phantomLookup.getClass(CommonTypes.JAVA_LANG_ASSERTION_ERROR);

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    LambdaGroupMarker infoMarker = type.getMarker(LambdaGroupMarker.class);
    if (infoMarker == null) {
      return;
    }
    assert type instanceof JDefinedClass;
    new Builder(infoMarker.getGroup()).build();
  }

  private static class MethodGroupData {
    @Nonnull
    final JMethodId id;
    @Nonnull
    final List<JLambda> lambdas = new ArrayList<>();
    @Nonnull
    final List<JMethod> methods = new ArrayList<>();

    MethodGroupData(@Nonnull JMethodId id) {
      this.id = id;
    }

    void addMethod(@Nonnull JLambda lambda, @Nonnull JMethod method) {
      lambdas.add(lambda);
      methods.add(method);
    }
  }

  private class Builder {
    /** Transformation request to use */
    @Nonnull
    final TransformationRequest request;
    /** Lambda class group information */
    @Nonnull
    final LambdaGroup group;

    /** The fields representing the lambda captured values */
    @Nonnull
    final List<JField> captureFields;

    /** The fields representing lambda class instances */
    @Nonnull
    final List<JField> staticFields = new ArrayList<>();

    /** Field representing the lambda class id, optional */
    @CheckForNull
    final JField idField;

    /** Stable-sorted method groups */
    @Nonnull
    final TreeMap<String, MethodGroupData> methodGroups = new TreeMap<>();

    Builder(@Nonnull LambdaGroup group) {
      JDefinedClass groupClass = group.getGroupClass();
      List<JLambda> lambdas = group.getLambdas();
      LambdaCaptureSignature capture = group.getCaptureSignature();

      this.request = new TransformationRequest(groupClass);
      this.group = group;
      this.captureFields = capture.createFields(groupClass, javaLangObject);

      // If this is a non-capturing lambda and 'simplify-stateless' is
      // true, create separate static fields to represent instances of x
      // the lambda group class representing separate lambda instances.
      int size = lambdas.size();
      if (simplifyStateless && this.captureFields.isEmpty()) {
        for (int i = 0; i < size; i++) {
          this.staticFields.add(
              new JField(SourceInfo.UNKNOWN, "$INST$" + i, groupClass, groupClass,
                  JModifier.PUBLIC | JModifier.STATIC | JModifier.FINAL | JModifier.SYNTHETIC));
        }
      }

      // Optionally create a field representing lambda class id,
      // also add to the original lambda classes a marker with
      // information needed to substitute lambda class instantiation
      // with that of group class
      assert size > 0;
      if (size == 1) {
        // Special case of one single lambda class in the group
        JLambda lambda = lambdas.get(0);
        lambda.addMarker(
            new LambdaInfoMarker(groupClass,
                hasStaticFields() ? this.staticFields.get(0) : null,
                LambdaInfoMarker.NO_LAMBDA_ID,
                capture.createMapping(lambda)));
        idField = null;

      } else {
        for (int i = 0; i < size; i++) {
          JLambda lambda = lambdas.get(i);
          lambda.addMarker(
              new LambdaInfoMarker(groupClass,
                  hasStaticFields() ? this.staticFields.get(i) : null,
                  /* lambdaClassId: */ i,
                  capture.createMapping(lambda)));
        }
        idField = createIdField(groupClass, size);
      }
    }

    private boolean hasStaticFields() {
      return !this.staticFields.isEmpty();
    }

    @Nonnull
    private JField createIdField(@Nonnull JDefinedClass groupClass, @Nonnegative int size) {
      JPrimitiveType type;
      if (size <= Byte.MAX_VALUE) {
        type = JPrimitiveType.JPrimitiveTypeEnum.BYTE.getType();
      } else if (size <= Short.MAX_VALUE) {
        type = JPrimitiveType.JPrimitiveTypeEnum.SHORT.getType();
      } else {
        type = JPrimitiveType.JPrimitiveTypeEnum.INT.getType();
      }
      return new JField(SourceInfo.UNKNOWN, "$id", groupClass,
          type, JModifier.PRIVATE | JModifier.FINAL | JModifier.SYNTHETIC);
    }

    /**
     * Creates the following list of members inside previously created
     * class representing lambda group:
     * <pre>
     * public synthetic final class $LambdaGroup$XYZ
     *   // +++ implement clause for all interfaces
     *   // +++ defined in the interface signature
     *        implements Interface0, Interface1, ... {
     *
     *   // +++ optionally add an $id field
     *   private synthetic final (byte|short|int) $id;
     *
     *   // +++ add fields for captured values
     *   private synthetic final type0 $f0;
     *   private synthetic final type1 $f1;
     *   ...
     *
     *   // +++ optionally add fields referencing stateless instances
     *   public static synthetic final $LambdaGroup$XYZ $INST$0;
     *   public static synthetic final $LambdaGroup$XYZ $INST$1;
     *   ...
     *
     *   // +++ optionally add a static initializer for instance fields
     *   public synthetic $LambdaGroup$XYZ(...) { ... }
     *   ...
     *
     *   // +++ add a constructor initializing capture fields
     *   public synthetic $LambdaGroup$XYZ(...) { ... }
     *
     *   // +++ create forwarding methods for all lambda methods
     *   private synthetic final type0 $m$XYZ(...) { ... }
     *
     *   // +++ add method group methods for all groups
     *   public synthetic final type0 methodName0(...) { ... }
     * }
     * </pre>
     */
    void build() {
      JDefinedClass groupClass = this.group.getGroupClass();

      // Add implements clause
      for (JInterface inter : getInterfaces()) {
        groupClass.addImplements(inter);
      }

      // Create $id field if needed
      if (idField != null) {
        request.append(new AppendField(groupClass, idField));
      }

      // Create capture fields
      for (JField field : captureFields) {
        request.append(new AppendField(groupClass, field));
      }

      // Create static fields
      for (JField field : staticFields) {
        request.append(new AppendField(groupClass, field));
      }

      // Create constructor
      JConstructor constructor = createConstructor();

      // Create static initializer if there are any fields to initialize
      if (hasStaticFields()) {
        createStaticConstructor(constructor);
      }

      // Generate all forwarding methods, fill in method groups data
      createAllForwardingMethods();

      // Create method group dispatch methods
      for (MethodGroupData data : this.methodGroups.values()) {
        createMethodGroupDispatchMethod(data);
      }

      request.commit();
    }

    @Nonnull
    private List<JInterface> getInterfaces() {
      List<JLambda> lambdas = this.group.getLambdas();
      if (mergeInterfaces) {
        return LambdaInterfaceSignature.normalizeInterfaces(lambdas);
      }

      // If there is no interface merging, we use non-normalized interface signature
      // which means that all the lambdas in the group should have the same
      // set and order of the interfaces, so we just take them from the first one.
      assert lambdas.size() > 0;
      return LambdaInterfaceSignature.extractOrderedInterfaces(lambdas.get(0));
    }

    /**
     * Create a static constructor of the lambda group class in a form:
     * <pre>
     * static synthetic {
     *   $LambdaGroup$XYZ.$INST$0 = new $LambdaGroup$XYZ(0);
     *   $LambdaGroup$XYZ.$INST$1 = new $LambdaGroup$XYZ(1);
     *   ...
     * }
     * </pre>
     */
    private void createStaticConstructor(@Nonnull JConstructor instanceConstructor) {
      assert this.captureFields.isEmpty(); // No capture fields!
      assert !this.staticFields.isEmpty();

      int modifier = JModifier.STATIC | JModifier.SYNTHETIC | JModifier.STATIC_INIT;
      JMethodIdWide methodIdWide =
          new JMethodIdWide(NamingTools.STATIC_INIT_NAME, MethodKind.STATIC);
      JMethodId methodId =
          new JMethodId(methodIdWide, JPrimitiveType.JPrimitiveTypeEnum.VOID.getType());
      JMethod constructor =
          new JMethod(SourceInfo.UNKNOWN, methodId, this.group.getGroupClass(), modifier);
      request.append(new AppendMethod(this.group.getGroupClass(), constructor));

      // Build a body
      JBlock block = new JBlock(SourceInfo.UNKNOWN);
      JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, block);
      constructor.setBody(body);
      body.updateParents(constructor);

      // Initialize all the static fields
      List<JLambda> lambdas = group.getLambdas();
      assert staticFields.size() == lambdas.size();
      for (int i = 0; i < staticFields.size(); i++) {
        JField field = staticFields.get(i);
        JLambda lambda = lambdas.get(i);

        LambdaInfoMarker marker =
            lambda.getMarker(LambdaInfoMarker.class);
        assert marker != null;

        // Build: $INST$XXX = new $LambdaGroup$XYZ(XXX);
        JNewInstance newInstance =
            marker.createGroupClassInstance(request, instanceConstructor,
                Collections.<JExpression>emptyList(), SourceInfo.UNKNOWN);
        createAssignStatement(block, null, field.getId(), newInstance);
      }

      // implicit return statement
      request.append(new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN, null)));
    }

    /**
     * Create a constructor of the lambda group class in a form:
     * <pre>
     * (public|private) synthetic $LambdaGroup$XYZ([byte|short|int] p$id, type0 p$f0, ...) {
     *   super();
     *   [this.$id = pid]
     *   this.f0 = pf0;
     *   this.f1 = pf1;
     *   ...
     * }
     * </pre>
     */
    @Nonnull
    private JConstructor createConstructor() {
      int modifier = hasStaticFields() ? JModifier.PRIVATE : JModifier.PUBLIC;
      JConstructor constructor = new JConstructor(
          SourceInfo.UNKNOWN, this.group.getGroupClass(), modifier | JModifier.SYNTHETIC);
      request.append(new AppendMethod(this.group.getGroupClass(), constructor));

      // Create a parameter representing lambda class id is needed
      List<JParameter> params = new ArrayList<>();
      if (idField != null) {
        JParameter param = new JParameter(idField.getSourceInfo(),
            NamingTools.getNonSourceConflictingName("p" + idField.getName()),
            idField.getType(), JModifier.SYNTHETIC, constructor);
        params.add(param);
        addParam(constructor, param);
      }

      // Create parameters matching fields
      for (JField field : captureFields) {
        JParameter param = new JParameter(field.getSourceInfo(),
            NamingTools.getNonSourceConflictingName("p" + field.getName()),
            field.getType(), JModifier.SYNTHETIC, constructor);
        params.add(param);
        addParam(constructor, param);
      }

      // Build a body
      JBlock block = new JBlock(SourceInfo.UNKNOWN);
      JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, block);
      constructor.setBody(body);
      body.updateParents(constructor);

      // 'this' reference
      JThis thisLocal = constructor.getThis();
      assert thisLocal != null;

      // super()
      JMethodId superConstructor =
          javaLangObject.getOrCreateMethodId(NamingTools.INIT_NAME, Collections.<JType>emptyList(),
              MethodKind.INSTANCE_NON_VIRTUAL, JPrimitiveType.JPrimitiveTypeEnum.VOID.getType());
      JMethodCall superCall = new JMethodCall(SourceInfo.UNKNOWN,
          thisLocal.makeRef(SourceInfo.UNKNOWN), javaLangObject, superConstructor, false);
      request.append(new AppendStatement(block, superCall.makeStatement()));

      // Add $id assignment if needed
      int paramIdx = 0;
      if (idField != null) {
        createAssignStatement(block, thisLocal, idField.getId(), params.get(paramIdx++));
      }

      // Add field assignments
      for (JField field : captureFields) {
        createAssignStatement(block, thisLocal, field.getId(), params.get(paramIdx++));
      }

      // implicit return statement
      request.append(new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN, null)));
      return constructor;
    }

    private void addParam(@Nonnull JMethod method, @Nonnull JParameter param) {
      method.addParam(param);
      method.getMethodIdWide().addParam(param.getType());
      param.updateParents(method);
    }

    private void createAssignStatement(@Nonnull JBlock block,
        @Nonnull JThis thisLocal, @Nonnull JFieldId fieldId, @Nonnull JParameter param) {
      createAssignStatement(
          block, thisLocal.makeRef(SourceInfo.UNKNOWN),
          fieldId, param.makeRef(SourceInfo.UNKNOWN));
    }

    private void createAssignStatement(@Nonnull JBlock block, @CheckForNull JExpression instance,
        @Nonnull JFieldId fieldId, @Nonnull JExpression rhs) {
      JFieldRef fieldRef = new JFieldRef(
          SourceInfo.UNKNOWN, instance, fieldId, this.group.getGroupClass());
      JAsgOperation assignment = new JAsgOperation(SourceInfo.UNKNOWN, fieldRef, rhs);
      request.append(new AppendStatement(block, assignment.makeStatement()));
    }

    /** Return the signature of the method id */
    @Nonnull
    private String getSignature(@Nonnull JMethodId id) {
      JMethodIdWide idWide = id.getMethodIdWide();
      return FORMATTER.getName(idWide.getName(), idWide.getParamTypes(), id.getType());
    }

    /** Generates all forwarding methods and adds them to method groups */
    private void createAllForwardingMethods() {
      // For each lambda create all required methods and put them
      // into the map mapping their signatures into their symbols.

      // NOTE: methods created with new synthesized names '$m$<id>',
      //       ids are assigned in stable order
      int nextUniqueId = 0;
      TreeMap<String, JMethodId> sortedIds = new TreeMap<>();

      // NOTE: getLambdas() return lambdas in stable order.
      for (JLambda lambda : this.group.getLambdas()) {
        JMethodId methodIdWithErasure = lambda.getMethodIdWithErasure();
        JMethodId methodIdWithoutErasure = lambda.getMethodIdWithoutErasure();

        // Empty main lambda method (created first in the lambda)
        JMethod mainMethod = createEmptyForwardingMethod(
            lambda, methodIdWithErasure, nextUniqueId++, /* isBridge: */ false);

        // Bridge methods (ensure stable order)
        sortedIds.clear();
        for (JMethodId bridgeMethodId : lambda.getBridgeMethodIds()) {
          sortedIds.put(getSignature(bridgeMethodId), bridgeMethodId);
        }
        for (JMethodId bridgeMethodId : sortedIds.values()) {
          JMethod bridge = createEmptyForwardingMethod(
              lambda, bridgeMethodId, nextUniqueId++, /* isBridge= */ true);
          delegateBridgingCall(bridge, mainMethod, methodIdWithoutErasure);
        }

        // Generate a body of the main method
        JMethodIdRef lambdaMethodIdRef = lambda.getMethodIdRef();
        JMethodId methodId = lambdaMethodIdRef.getMethodId();
        JMethod lambdaMethod = lambdaMethodIdRef.getEnclosingType().getMethod(methodId);

        LambdaInfoMarker marker = lambda.getMarker(LambdaInfoMarker.class);
        assert marker != null;

        delegateLambdaCall(mainMethod, lambdaMethod, methodIdWithoutErasure,
            lambda.getCapturedVariables(), marker.getCaptureMapping());
      }
    }

    @Nonnull
    private JExpression getFieldRef(
        @Nonnegative int index, @Nonnull JType type, @Nonnull JThisRef ref) {
      assert index < captureFields.size();
      JExpression fieldRef = new JFieldRef(SourceInfo.UNKNOWN, ref,
          captureFields.get(index).getId(), this.group.getGroupClass());

      if (type instanceof JPrimitiveType) {
        // Value of a primitive type must be captured in a field of
        // the same type, until we support boxing or type promotions
        assert fieldRef.getType() == type;
        return fieldRef;
      }

      return javaLangObject.isSameType(type) ? fieldRef :
          new JDynamicCastOperation(SourceInfo.UNKNOWN, fieldRef, type);
    }

    /**
     * Creates a body of the bridge method by simply delegating the call to
     * another method, the signatures of the both methods must match exactly.
     */
    private void delegateBridgingCall(
        @Nonnull JMethod caller, @Nonnull JMethod callee, @Nonnull JMethodId enforcedId) {
      JThis callerThis = caller.getThis();
      assert callerThis != null;
      delegateCallImpl(caller, callee, enforcedId,
          callerThis.makeRef(SourceInfo.UNKNOWN), null);
    }

    private void delegateLambdaCall(@Nonnull JMethod caller,
        @Nonnull JMethod callee, @Nonnull JMethodId enforcedId,
        @Nonnull List<JExpression> captures, @Nonnull int[] mapping) {

      int capturesCount = captures.size();
      assert mapping.length == capturesCount;

      JThis callerThis = caller.getThis();
      assert callerThis != null;

      // Remap captures
      JExpression[] captureFields = new JExpression[capturesCount];
      for (int idx = 0; idx < capturesCount; idx++) {
        captureFields[idx] = getFieldRef(mapping[idx],
            captures.get(idx).getType(), callerThis.makeRef(SourceInfo.UNKNOWN));
      }

      // For instances methods, the first captured value must be 'this'
      boolean calleeNeedsInstance = !callee.isStatic();
      int captureStart = calleeNeedsInstance ? 1 : 0;

      JExpression calleeThisRef = calleeNeedsInstance ? captureFields[0] : null;

      List<JExpression> extraArgs = null;
      if (capturesCount > captureStart) {
        extraArgs = Arrays.asList(captureFields).subList(captureStart, capturesCount);
      }

      delegateCallImpl(caller, callee, enforcedId, calleeThisRef, extraArgs);
    }

    private void delegateCallImpl(
        @Nonnull JMethod caller, @Nonnull JMethod callee, @Nonnull JMethodId enforcedId,
        @CheckForNull JExpression calleeThisRef, @CheckForNull List<JExpression> extraArgs) {

      JBlock bodyBlock = new JBlock(SourceInfo.UNKNOWN);
      JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, bodyBlock);

      JMethodCall call =
          new JMethodCall(SourceInfo.UNKNOWN, calleeThisRef, callee.getEnclosingType(),
              callee.getMethodId(), !callee.isStatic() && !callee.isPrivate());

      // Build call arguments, reshuffled captures first, then arguments
      List<JParameter> callerParams = caller.getParams();
      List<JParameter> calleeParams = callee.getParams();
      assert calleeParams.size() ==
          callerParams.size() + (extraArgs == null ? 0 : extraArgs.size());

      // Add extra args
      if (extraArgs != null) {
        call.addArgs(extraArgs);
      }

      List<JType> enforcedTypes = enforcedId.getMethodIdWide().getParamTypes();
      int idx = 0;
      for (JParameter param : callerParams) {
        call.addArg(new JDynamicCastOperation(
            SourceInfo.UNKNOWN, param.makeRef(SourceInfo.UNKNOWN), enforcedTypes.get(idx++)));
      }

      if (caller.getType() != JPrimitiveType.JPrimitiveTypeEnum.VOID.getType()) {
        bodyBlock.addStmt(new JReturnStatement(SourceInfo.UNKNOWN, call));
      } else {
        bodyBlock.addStmt(new JExpressionStatement(SourceInfo.UNKNOWN, call));
        bodyBlock.addStmt(new JReturnStatement(SourceInfo.UNKNOWN, null));
      }

      caller.setBody(body);
    }

    /**
     * Creates a forwarding method without a body and adds it to the map
     * of method groups, note that method groups are defined by the
     * signature of the original method id.
     */
    @Nonnull
    private JMethod createEmptyForwardingMethod(
        @Nonnull JLambda lambda, @Nonnull JMethodId origMethodId,
        @Nonnegative int uniqueSuffix, boolean isBridge) {

      JMethodIdWide origMethodIdWide = origMethodId.getMethodIdWide();

      JMethodIdWide newMethodIdWide = new JMethodIdWide(
          "$m$" + uniqueSuffix, origMethodIdWide.getParamTypes(),
          MethodKind.INSTANCE_NON_VIRTUAL /* The forwarding method is private */);

      JMethodId newMethodId = new JMethodId(newMethodIdWide, origMethodId.getType());
      int modifier = JModifier.PRIVATE | JModifier.SYNTHETIC |
          (isBridge ? JModifier.BRIDGE : 0) | JModifier.FINAL;
      JMethod newMethod = new JMethod(SourceInfo.UNKNOWN,
          newMethodId, this.group.getGroupClass(), modifier);

      // Create parameters matching fields
      int idx = 0;
      for (JType type : origMethodIdWide.getParamTypes()) {
        JParameter param = new JParameter(SourceInfo.UNKNOWN,
            "arg" + idx++, type, JModifier.DEFAULT, newMethod);
        newMethod.addParam(param);
        param.updateParents(newMethod);
      }

      // Record method in method group
      String signature = getSignature(origMethodId);
      MethodGroupData data = methodGroups.get(signature);
      if (data == null) {
        data = new MethodGroupData(origMethodId);
        methodGroups.put(signature, data);
      }
      data.addMethod(lambda, newMethod);

      request.append(new AppendMethod(this.group.getGroupClass(), newMethod));
      return newMethod;
    }

    /**
     * Creates the main method representing the method group and dispatching calls
     * to appropriate $m$XYZ methods based on lambda class id. The new method is
     * created in the following form:
     * <pre>
     *   public synthetic final type0 name0(...) {
     *     // dispatch to a proper $m$XYZ method
     *   }
     * </pre>
     *
     * NOTE: we always generate a switch in cases there are more than one lambda
     * class in *lambda group* and put "throw new AssertionError();" in default
     * case to ensure the correct usage.
     *
     * This may not be exactly required if the *method group* has only one single method
     * and thus switch will have only one non-default case, but it will ensure the fact
     * that the method is not called on wrong lambda class id when classes with different
     * interface signatures are merged.
     *
     * Switch statement will not be generated only in case there is only one lambda
     * class in the whole group, in which case we don't even generate '$id' field
     * to switch on.
     */
    private void createMethodGroupDispatchMethod(@Nonnull MethodGroupData data) {
      JMethodId origMethodId = data.id;
      JMethodIdWide origMethodIdWide = origMethodId.getMethodIdWide();

      // Create a method
      JMethodIdWide newMethodIdWide = new JMethodIdWide(origMethodIdWide.getName(),
          origMethodIdWide.getParamTypes(), origMethodIdWide.getKind());
      JMethodId newMethodId = new JMethodId(newMethodIdWide, origMethodId.getType());
      JMethod newMethod = new JMethod(SourceInfo.UNKNOWN, newMethodId,
          this.group.getGroupClass(), JModifier.PUBLIC | JModifier.FINAL);

      // Create parameters matching method signature
      List<JParameter> params = new ArrayList<>();
      for (JType type : origMethodIdWide.getParamTypes()) {
        JParameter newParam = new JParameter(SourceInfo.UNKNOWN,
            NamingTools.getNonSourceConflictingName("p" + params.size()),
            type, JModifier.SYNTHETIC, newMethod);
        newMethod.addParam(newParam);
        newParam.updateParents(newMethod);
        params.add(newParam);
      }

      // Create a new body with empty block
      JBlock block = new JBlock(SourceInfo.UNKNOWN);
      JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, block);
      newMethod.setBody(body);
      body.updateParents(newMethod);

      // Generate forwarding call(s)
      JThis self = newMethod.getThis();
      if (idField == null) {
        // There is no $id field to switch on
        assert data.methods.size() == 1;
        generateForwardCall(block, self, params, data.methods.get(0));
      } else {
        // Generate switch-based dispatch
        createSwitchBasedDispatch(block, self, params, data);
      }

      request.append(new AppendMethod(this.group.getGroupClass(), newMethod));
    }

    /**
     * Generates a switch statement representing the dispatch inside method group
     * main method to forward a call to appropriate $m$XYZ method.
     * <pre>
     *   switch ($id) {
     *     case X:
     *       return $m$AAA(...);
     *     case Y:
     *       return $m$BBB(...);
     *     ...
     *     ...
     *     default:
     *       throw new AssertionError();
     *   }
     * </pre>
     */
    private void createSwitchBasedDispatch(@Nonnull JBlock block, @Nonnull JThis self,
        @Nonnull List<JParameter> params, @Nonnull MethodGroupData data) {
      assert idField != null;
      JPrimitiveType idType = (JPrimitiveType) idField.getType();

      JBlock mainSwitchBlock = new JBlock(SourceInfo.UNKNOWN);
      List<JCaseStatement> cases = new ArrayList<>();

      // Process all methods: create "case ID: return $m$XYZ(...); "
      int size = data.methods.size();
      for (int idx = 0; idx < size; idx++) {
        JLambda lambda = data.lambdas.get(idx);
        LambdaInfoMarker marker = lambda.getMarker(LambdaInfoMarker.class);
        assert marker != null;
        assert marker.hasId();

        JCaseStatement caseStmt = new JCaseStatement(SourceInfo.UNKNOWN,
            ((JIntegralType32) idType).createLiteral(SourceInfo.UNKNOWN, marker.getId()));
        cases.add(caseStmt);
        request.append(new AppendStatement(mainSwitchBlock, caseStmt));

        generateForwardCall(mainSwitchBlock, self, params, data.methods.get(idx));
      }

      // Add default case "default: throw new AssertionError(); "
      JCaseStatement defaultCase = new JCaseStatement(SourceInfo.UNKNOWN, null);
      request.append(new AppendStatement(mainSwitchBlock, defaultCase));
      JBlock next = new JBlock(SourceInfo.UNKNOWN);
      request.append(new AppendStatement(mainSwitchBlock, next));
      JNewInstance newAssertionError = new JNewInstance(SourceInfo.UNKNOWN, javaLangAssertionError,
          javaLangAssertionError.getOrCreateMethodId(NamingTools.INIT_NAME,
              Collections.<JType>emptyList(), MethodKind.INSTANCE_NON_VIRTUAL,
              JPrimitiveTypeEnum.VOID.getType()));
      JThrowStatement throwStmt =
          new JThrowStatement(SourceInfo.UNKNOWN, newAssertionError);
      request.append(new AppendStatement(next, throwStmt));

      // Finally create switch statement
      JFieldRef fieldRef = new JFieldRef(SourceInfo.UNKNOWN,
          self.makeRef(SourceInfo.UNKNOWN), idField.getId(), this.group.getGroupClass());
      JSwitchStatement switchStmt = new JSwitchStatement(
          SourceInfo.UNKNOWN, fieldRef, mainSwitchBlock, cases, defaultCase);
      request.append(new AppendStatement(block, switchStmt));
    }

    /**
     * Generates a forward call to an appropriate method in a form of:
     * <pre>
     *   1) value returning function:
     *        return $m$XYZ(...);
     *
     *   2) void returning function:
     *        $m$XYZ(...);
     *        return;
     * </pre>
     */
    private void generateForwardCall(@Nonnull JBlock block,
        @Nonnull JThis self, @Nonnull List<JParameter> params, @Nonnull JMethod method) {
      JType returnType = method.getType();
      JMethodCall call = new JMethodCall(SourceInfo.UNKNOWN,
          self.makeRef(SourceInfo.UNKNOWN), this.group.getGroupClass(),
          method.getMethodId(), false /* method is private */);

      for (JParameter param : params) {
        call.addArg(param.makeRef(SourceInfo.UNKNOWN));
      }

      if (JPrimitiveType.JPrimitiveTypeEnum.VOID.getType().isSameType(returnType)) {
        request.append(new AppendStatement(block, call.makeStatement()));
        request.append(new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN, null)));
      } else {
        request.append(new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN, call)));
      }
    }
  }
}
