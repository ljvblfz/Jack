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

package com.android.jack.coverage;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ConditionalBasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.PeiBasicBlock;
import com.android.jack.cfg.ReturnBasicBlock;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes.CommonType;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.request.TransformationStep;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.collect.Lists;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Instruments classes for code coverage.
 */
@Description("Instruments classes for code coverage")
@Support(CodeCoverage.class)
@Constraint(need = {CodeCoverageMarker.Analyzed.class, ProbeMarker.class, ControlFlowGraph.class,
    ThreeAddressCodeForm.class, NoImplicitBlock.class})
@Transform(add = {CodeCoverageMarker.Complete.class, JField.class, JFieldRef.class, JMethod.class,
    JMethodCall.class, JMethodBody.class, JBlock.class, JLocal.class, JLocalRef.class,
    JExpressionStatement.class, JAsgOperation.NonReusedAsg.class, JIntLiteral.class,
    JLongLiteral.class, JStringLiteral.class, JIfStatement.class, JEqOperation.class,
    JNullLiteral.class, JReturnStatement.class},
    remove = ProbeMarker.class)
public class CodeCoverageTransformer implements RunnableSchedulable<JDefinedClassOrInterface> {

  /**
   * The name of the field containing the array of coverage probes.
   */
  private static final String COVERAGE_DATA_FIELD_NAME =
      NamingTools.getNonSourceConflictingName("jacocoData");

  /**
   * The modifiers of the added field.
   */
  private static final int COVERAGE_DATA_FIELD_MODIFIERS =
      JModifier.PRIVATE | JModifier.STATIC | JModifier.FINAL | JModifier.TRANSIENT;

  /**
   * The name of the method that registers (once) and returns the array of coverage probes.
   */
  private static final String COVERAGE_DATA_INIT_METHOD_NAME =
      NamingTools.getNonSourceConflictingName("jacocoInit");

  /**
   * The modifiers of the added method.
   */
  private static final int COVERAGE_DATA_INIT_METHOD_MODIFIERS =
      JModifier.PRIVATE | JModifier.STATIC;

  private static final String LOCAL_VAR_NAME_PREFIX = "cov";

  @Nonnull
  private final TypeFormatter binaryTypeFormatter = BinaryQualifiedNameFormatter.getFormatter();

  /**
   * The cached Jacoco class <code>org.jacoco.agent.rt.internal[version].Offline</code>.
   */
  @CheckForNull
  private JDefinedClass jacocoProbesClass = null;

  /**
   * The cached Jacoco method
   * <pre>public static boolean[] getProbes(long, java.lang.String, int)</pre> of
   * the Jacoco class <code>org.jacoco.agent.rt.internal[version].Offline</code>.
   */
  @CheckForNull
  private JMethodId jacocoProbesMethod = null;

  @Nonnull
  private static final String JACOCO_RUNTIME_CLASS_NAME = "Offline";

  /**
   * An exception thrown when the Jacoco runtime package (containing classes required for
   * instrumentation) cannot be found.
   */
  private static class JacocoPackageNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public JacocoPackageNotFoundException(@Nonnull String msg, @CheckForNull Throwable cause) {
      super(msg, cause);
    }
  }

  /**
   * Looks up the Jacoco package org.jacoco.agent.rt.internal[<suffix>] for which we don't
   * know the exact name due to <suffix> changing for each release.
   *
   * @param lookup the lookup class to find the package
   * @return the Jacoco {@link JPackage}
   * @throws JacocoPackageNotFoundException if the Jacoco package cannot be found
   */
  @Nonnull
  private static JPackage lookupJacocoRuntimePackage(@Nonnull JLookup lookup)
      throws JacocoPackageNotFoundException {
    JacocoPackage jacocoPackage = ThreadConfig.get(CodeCoverage.COVERAGE_JACOCO_PACKAGE_NAME);
    String jacocoPackageName = jacocoPackage.getPackageName();
    if (!jacocoPackageName.isEmpty()) {
      // The package name has been provided through property: lookup that package.
      String packageString = NamingTools.getBinaryName(jacocoPackageName);
      try {
        return lookup.getPackage(packageString);
      } catch (JPackageLookupException e) {
        throw new JacocoPackageNotFoundException(
            "Cannot find Jacoco package " + jacocoPackageName, e);
      }
    } else {
      // No package has been provided: lookup for org.jacoco.agent.rt.internal* package.
      String parentPackageName = NamingTools.getBinaryName("org.jacoco.agent.rt");
      Throwable lookupFailureCause = null;
      try {
        JPackage parentPackage = lookup.getPackage(parentPackageName);
        for (JPackage p : parentPackage.getSubPackages()) {
          if (p.getName().startsWith("internal")) {
            return p;
          }
        }
      } catch (JPackageLookupException e) {
        // We did not find the parent package. We do not want to throw a JPackageLookupException
        // so we catch and save it here so we can throw a JacocoPackageNotFoundException and
        // set this exception as its cause.
        lookupFailureCause = e;
      }
      throw new JacocoPackageNotFoundException(
          "Cannot find any Jacoco package org.jacoco.agent.rt.internal*", lookupFailureCause);
    }
  }

  /**
   * Looks up and caches the <code>org.jacoco.agent.rt.internal*.Offline</code> class required
   * for code coverage instrumentation.
   *
   * @param lookup the {@link JNodeLookup} used to lookup the class
   * @return the Jacoco class required for code coverage
   */
  @Nonnull
  private synchronized JDefinedClass lookupJacocoOfflineClass(@Nonnull JLookup lookup) {
    if (jacocoProbesClass == null) {
      JPackage jacocoRuntimePackage;
      try {
        jacocoRuntimePackage = lookupJacocoRuntimePackage(lookup);
      } catch (JacocoPackageNotFoundException e) {
        CodeCoverageLookupException cle = new CodeCoverageLookupException(e.getMessage(), e);
        Jack.getSession().getReporter().report(Severity.FATAL, cle);
        throw new JackAbortException(cle);
      }

      JDefinedClassOrInterface clOrI;
      try {
        clOrI = jacocoRuntimePackage.getType(JACOCO_RUNTIME_CLASS_NAME);
        if (!(clOrI instanceof JDefinedClass)) {
          throw new MissingJTypeLookupException(jacocoRuntimePackage, JACOCO_RUNTIME_CLASS_NAME);
        }
      } catch (JLookupException e) {
        CodeCoverageLookupException cle = new CodeCoverageLookupException(e.getMessage(), e);
        Jack.getSession().getReporter().report(Severity.FATAL, cle);
        throw new JackAbortException(cle);
      }
      jacocoProbesClass = (JDefinedClass) clOrI;
    }
    return jacocoProbesClass;
  }

  @Nonnull
  private synchronized JMethodId lookupJacocoProbesMethod(
      @Nonnull JLookup lookup,
      @Nonnull JType classIdType,
      @Nonnull JType classNameType,
      @Nonnull JType probeCountType,
      @Nonnull JType probeArrayType) {
    if (jacocoProbesMethod == null) {
      // Look for org.jacoco.agent.rt.internal[version].Offline class.
      JDefinedClass jacocoClass = lookupJacocoOfflineClass(lookup);

      // Look for method 'public static boolean[] getProbes(long, java.lang.String, int)' in the
      // JaCoCo agent lib.
      List<JType> argsTypes = Lists.create(classIdType, classNameType, probeCountType);
      jacocoProbesMethod =
          jacocoClass.getMethodId("getProbes", argsTypes, MethodKind.STATIC, probeArrayType);
    }
    return jacocoProbesMethod;
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    CodeCoverageMarker marker = declaredType.getMarker(CodeCoverageMarker.class);
    if (marker == null) {
      // This class is excluded from code coverage.
      return;
    }

    // First we need to compute the class ID of the class.
    final long classID = CodeCoverageSelector.computeClassID(declaredType);
    marker.setClassId(classID);

    TransformationRequest transformationRequest = new TransformationRequest(declaredType);

    // Add coverage data field.
    JField coverageDataField = createProbesArrayField(declaredType);
    transformationRequest.append(new AppendField(declaredType, coverageDataField));

    // Add coverage init method.
    JMethod coverageInitMethod = createProbesArrayInitMethod(declaredType,
        marker.getNumberOfProbes(), transformationRequest, coverageDataField, classID);
    marker.setInitMethod(coverageInitMethod);
    transformationRequest.append(new AppendMethod(declaredType, coverageInitMethod));

    // Instrument methods
    new TransformerVisitor(coverageInitMethod, transformationRequest)
        .accept(declaredType.getMethods());

    // We visited the whole class: we can commit our transformations now.
    transformationRequest.commit();
  }

  @Nonnull
  private JField createProbesArrayField(@Nonnull JDefinedClassOrInterface declaredType) {
    JType booleanArrayType = getCoverageDataType();
    SourceInfo sourceInfo = declaredType.getSourceInfo();
    return new JField(sourceInfo, COVERAGE_DATA_FIELD_NAME, declaredType, booleanArrayType,
        COVERAGE_DATA_FIELD_MODIFIERS);
  }

  /**
   * Visits methods of the class and instrument them with coverage probes.
   */
  private static class TransformerVisitor extends JVisitor {
    @Nonnull
    private final JMethod coverageMethod;

    @Nonnull
    private final TransformationRequest transformationRequest;

    @CheckForNull
    private LocalVarCreator localVarCreator;

    /**
     * The local holding the coverage array in the method being visited.
     */
    @CheckForNull
    private JLocal coverageProbesArrayLocal;

    public TransformerVisitor(
        @Nonnull JMethod initMethod, @Nonnull TransformationRequest transformationRequest) {
      this.transformationRequest = transformationRequest;
      this.coverageMethod = initMethod;
    }

    private static boolean canInsertProbeBeforeLastStatement(@Nonnull BasicBlock bb) {
      if (bb instanceof PeiBasicBlock) {
        // We may throw an exception so we must insert the probe before the last statement.
        return true;
      }
      if (bb instanceof ReturnBasicBlock) {
        // We return from the method so we must insert the probe before the 'return' statement.
        return true;
      }
      if (bb instanceof ConditionalBasicBlock || bb instanceof SwitchBasicBlock) {
        // We branch to different blocks so we must insert the probe before the branch statement.
        return true;
      }
      assert !bb.getStatements().isEmpty() : "there is no statement in block " + bb.getId();
      JStatement lastStmt = bb.getLastInstruction();
      assert lastStmt != null;
      if (lastStmt instanceof JGoto) {
        // We branch to a different block so we must insert the probe before the 'goto' statement.
        return true;
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMethod m) {
      if (m.isNative() || m.isAbstract()) {
        // This method has no code so cannot be instrumented.
        return false;
      }
      if (m.getBody() == null || !(m.getBody() instanceof JMethodBody)) {
        return false;
      }

      // Create a LocalVarCreator to add synthetic local variables.
      localVarCreator = new LocalVarCreator(m, LOCAL_VAR_NAME_PREFIX);

      // Add a synthetic local variable assigned with the array of probes returned by the
      // initialization method.
      JMethodBody methodBody = (JMethodBody) m.getBody();
      assert methodBody != null;
      coverageProbesArrayLocal =
          insertCoverageLocal(methodBody, coverageMethod, coverageMethod.getSourceInfo());

      // Iterates over every basic block to insert code for probe and removes the ProbeMarker.
      ControlFlowGraph controlFlowGraph = m.getMarker(ControlFlowGraph.class);
      assert controlFlowGraph != null;
      final BasicBlock entryBlock = controlFlowGraph.getEntryNode();
      final BasicBlock exitBlock = controlFlowGraph.getExitNode();
      for (BasicBlock bb : controlFlowGraph.getNodes()) {
        ProbeMarker probeMarker = bb.removeMarker(ProbeMarker.class);
        if (probeMarker == null) {
          // The basic block may not have been marked.
          assert (bb == entryBlock || bb == exitBlock);
          continue;
        }
        if (bb == entryBlock || bb == exitBlock) {
          continue;
        }
        BasicBlock insertionBlock = probeMarker.getInsertionBlock();
        assert insertionBlock != null;
        if (bb == insertionBlock) {
          // We must set the probe at the end of this basic block.
          assert !bb.getStatements().isEmpty() : bb;
          JStatement insertionPoint = bb.getLastInstruction();
          ProbeDescription probe = probeMarker.getProbe();
          TransformationStep transformationStep;
          JStatement probeStatement = createProbeStatement(insertionPoint.getSourceInfo(), probe);
          boolean insertBeforeLastStatement = canInsertProbeBeforeLastStatement(bb);
          if (insertBeforeLastStatement) {
            transformationStep = new AppendBefore(insertionPoint, probeStatement);
          } else {
            transformationStep = new PrependAfter(insertionPoint, probeStatement);
          }
          transformationRequest.append(transformationStep);
        }
      }
      return false;
    }

    private JLocal insertCoverageLocal(
        @Nonnull JMethodBody x, @Nonnull JMethod coverageMethod, @Nonnull SourceInfo sourceInfo) {
      // Create the local and add it to the method body.
      JType booleanArrayType = coverageMethod.getType();
      assert localVarCreator != null;
      JLocal local =
          localVarCreator.createTempLocal(booleanArrayType, sourceInfo, transformationRequest);
      // Insert initialization statement as the first statement.
      JLocalRef localRef = local.makeRef(sourceInfo);
      JExpression expr = new JMethodCall(sourceInfo, null, coverageMethod.getEnclosingType(),
          coverageMethod.getMethodIdWide(), booleanArrayType, false);
      JAsgOperation assign = new JAsgOperation(sourceInfo, localRef, expr);
      transformationRequest.append(
          new PrependStatement(x.getBlock(), new JExpressionStatement(sourceInfo, assign)));
      return local;
    }

    /**
     * Creates statement "<localVar>[<probeId>] = true"
     * @return a statement
     */
    @Nonnull
    private JStatement createProbeStatement(
        @Nonnull SourceInfo sourceInfo, @Nonnull ProbeDescription probe) {
      assert coverageProbesArrayLocal != null;
      assert coverageProbesArrayLocal.getType().equals(
          JPrimitiveTypeEnum.BOOLEAN.getType().getArray());
      JLocalRef localRef = coverageProbesArrayLocal.makeRef(sourceInfo);
      JArrayRef arrayRef = new JArrayRef(localRef.getSourceInfo(), localRef,
          new JIntLiteral(localRef.getSourceInfo(), probe.getProbeId()));
      JAsgOperation assign = new JAsgOperation(
          arrayRef.getSourceInfo(), arrayRef, new JBooleanLiteral(arrayRef.getSourceInfo(), true));
      return assign.makeStatement();
    }
  }

  @Nonnull
  private JMethod createProbesArrayInitMethod(@Nonnull JDefinedClassOrInterface declaredType,
      @Nonnegative int probeCount, @Nonnull TransformationRequest transformationRequest,
      @Nonnull JField coverageDataField, long classId) {
    SourceInfo sourceInfo = declaredType.getSourceInfo();
    JType returnType = getCoverageDataType();
    JMethodId methodId = new JMethodId(
        new JMethodIdWide(COVERAGE_DATA_INIT_METHOD_NAME, MethodKind.STATIC),
        returnType);
    JMethod coverageInitMethod = new JMethod(
        sourceInfo, methodId, declaredType, COVERAGE_DATA_INIT_METHOD_MODIFIERS);
    fillCoverageInitMethodBody(coverageInitMethod, declaredType, probeCount, transformationRequest,
        coverageDataField, classId);
    return coverageInitMethod;
  }

  /** Generates body of JaCoCo initialization method in three-adress-code form.
   *
   * @param coverageInitMethod
   * @param coverageDataField
   * @param transformationRequest
   * @param classId
   */
  private void fillCoverageInitMethodBody(@Nonnull JMethod coverageInitMethod,
      @Nonnull JDefinedClassOrInterface declaredType, @Nonnegative int probeCount,
      @Nonnull TransformationRequest transformationRequest, @Nonnull JField coverageDataField,
      long classId) {
    JType coverageDataType = coverageDataField.getType();
    assert coverageDataType.equals(JPrimitiveTypeEnum.BOOLEAN.getType().getArray());
    assert coverageDataField.getEnclosingType().equals(declaredType);

    // Create empty body and attach to the method. This is required to create a LocalVarCreator.
    JBlock block = new JBlock(coverageInitMethod.getSourceInfo());
    coverageInitMethod.setBody(new JMethodBody(block.getSourceInfo(), block));

    // Look for method public static boolean[] getProbes(long, java.lang.String, int)' in the
    // JaCoCo class org.jacoco.agent.rt.internal[suffix].Offline from the agent lib.
    JLookup lookup = Jack.getSession().getLookup();
    JDefinedClass jacocoClass = lookupJacocoOfflineClass(lookup);
    JType classIdType = JPrimitiveTypeEnum.LONG.getType();
    JType classNameType = lookup.getType(CommonType.STRING);
    JType probeCountType = JPrimitiveTypeEnum.INT.getType();
    JMethodId jacocoMethodId =
        lookupJacocoProbesMethod(
            lookup, classIdType, classNameType, probeCountType, coverageDataType);

    // <local#1> = <field>
    // if (<local#1> == null) {
    //   <local#2> = <classId>
    //   <local#3> = <className>
    //   <local#4> = <probeCount>
    //   <local#1> = org.jacoco...Offline.getProbes(<local#2>, <local#3>, <local#4>);
    //   <field> = <local#1>;
    // }
    // return <local#1>
    LocalVarCreator localVarCreator =
        new LocalVarCreator(coverageInitMethod, LOCAL_VAR_NAME_PREFIX);
    JLocal coverageDataLocal = localVarCreator.createTempLocal(
        coverageDataType, block.getSourceInfo(), transformationRequest);
    JStatement coverageDataLocalInit = createLocalAssignStatement(
        coverageDataLocal,
        new JFieldRef(
            coverageDataLocal.getSourceInfo(), null, coverageDataField.getId(), declaredType));
    transformationRequest.append(new AppendStatement(block, coverageDataLocalInit));

    // Create 'if' block.
    JBlock ifBlock = new JBlock(block.getSourceInfo());
    {
      // Create a local for each call argument.
      JLocal classIdLocal = localVarCreator.createTempLocal(
          classIdType, ifBlock.getSourceInfo(), transformationRequest);
      JLocal classNameLocal = localVarCreator.createTempLocal(
          classNameType, ifBlock.getSourceInfo(), transformationRequest);
      JLocal probeCountLocal = localVarCreator.createTempLocal(
          probeCountType, ifBlock.getSourceInfo(), transformationRequest);

      // Init locals.
      JStatement classIdInit = createLocalAssignStatement(
          classIdLocal, new JLongLiteral(classIdLocal.getSourceInfo(), classId));
      String className = binaryTypeFormatter.getName(declaredType);
      JStatement classNameInit = createLocalAssignStatement(
          classNameLocal, new JStringLiteral(classNameLocal.getSourceInfo(), className));
      JStatement probeCountInit = createLocalAssignStatement(
          probeCountLocal, new JIntLiteral(probeCountLocal.getSourceInfo(), probeCount));
      transformationRequest.append(new AppendStatement(ifBlock, classIdInit));
      transformationRequest.append(new AppendStatement(ifBlock, classNameInit));
      transformationRequest.append(new AppendStatement(ifBlock, probeCountInit));

      // Add '<local> = org.jacoco...Offline.getProbes(<classId>, <className>, <probeCount>)'
      JMethodCall methodCall = new JMethodCall(ifBlock.getSourceInfo(), null, jacocoClass,
          jacocoMethodId.getMethodIdWide(), JPrimitiveTypeEnum.BOOLEAN.getType().getArray(), false);
      methodCall.addArg(classIdLocal.makeRef(ifBlock.getSourceInfo()));
      methodCall.addArg(classNameLocal.makeRef(ifBlock.getSourceInfo()));
      methodCall.addArg(probeCountLocal.makeRef(ifBlock.getSourceInfo()));
      JStatement assignLocal = createLocalAssignStatement(coverageDataLocal, methodCall);
      transformationRequest.append(new AppendStatement(ifBlock, assignLocal));

      // Add '<field> = <local>'
      JFieldRef putFieldRef = new JFieldRef(ifBlock.getSourceInfo(), null/* static access */,
          coverageDataField.getId(), coverageDataField.getEnclosingType());
      JAsgOperation assignField = new JAsgOperation(putFieldRef.getSourceInfo(), putFieldRef,
          coverageDataLocal.makeRef(putFieldRef.getSourceInfo()));
      transformationRequest.append(new AppendStatement(
          ifBlock, new JExpressionStatement(assignField.getSourceInfo(), assignField)));
    }

    // Add 'if (<local> == null) { ... }'
    JEqOperation condition = new JEqOperation(block.getSourceInfo(),
        coverageDataLocal.makeRef(block.getSourceInfo()), new JNullLiteral(block.getSourceInfo()));
    JIfStatement ifStatement =
        new JIfStatement(condition.getSourceInfo(), condition, ifBlock, null);
    transformationRequest.append(new AppendStatement(block, ifStatement));

    // Add 'return <local>'
    JReturnStatement returnStatement = new JReturnStatement(
        block.getSourceInfo(), coverageDataLocal.makeRef(block.getSourceInfo()));
    transformationRequest.append(new AppendStatement(block, returnStatement));
  }

  @Nonnull
  private static JStatement createLocalAssignStatement(
      @Nonnull JLocal local, @Nonnull JExpression expr) {
    JLocalRef localRef = local.makeRef(local.getSourceInfo());
    JExpression assign = new JAsgOperation(local.getSourceInfo(), localRef, expr);
    return assign.makeStatement();
  }

  @Nonnull
  private static JType getCoverageDataType() {
    return JPrimitiveTypeEnum.BOOLEAN.getType().getArray();
  }
}
