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
import com.android.jack.ir.CompoundAssignment;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAddOperation;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBitAndOperation;
import com.android.jack.ir.ast.JBitOrOperation;
import com.android.jack.ir.ast.JBitXorOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JDivOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JModOperation;
import com.android.jack.ir.ast.JMulOperation;
import com.android.jack.ir.ast.JShlOperation;
import com.android.jack.ir.ast.JShrOperation;
import com.android.jack.ir.ast.JShruOperation;
import com.android.jack.ir.ast.JSubOperation;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Remove compound assignment operator.
 */
@Description("Remove compound assignment operator.")
@Name("CompoundAssignmentRemover")
@Transform(add = {JAddOperation.class, JBitAndOperation.class, JBitOrOperation.class,
    JBitXorOperation.class, JDivOperation.class, JMulOperation.class, JModOperation.class,
    JShlOperation.class, JShrOperation.class, JShruOperation.class, JSubOperation.class,
    JConcatOperation.class, JAsgOperation.class},
    remove = {CompoundAssignment.class, ThreeAddressCodeForm.class})
@Constraint(need = OriginalNames.class)
@Use(SideEffectExtractor.class)
public class CompoundAssignmentRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class RemoveComplexAssignVisitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @CheckForNull
    private SideEffectExtractor extractor;

    @Nonnull
    private final JClass javaLangString;

    private RemoveComplexAssignVisitor(
        @Nonnull TransformationRequest tr, @Nonnull JClass javaLangString) {
      this.tr = tr;
      this.javaLangString = javaLangString;
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      JAbstractMethodBody body = method.getBody();
      if (body != null && body instanceof JMethodBody) {
        extractor = new SideEffectExtractor(new LocalVarCreator(method, "car"));
      }
      return super.visit(method);
    }

    @Override
    public void endVisit(@Nonnull JMethod x) {
      extractor = null;
      super.endVisit(x);
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binary) {
      // a += b => a = a + b
      if (binary.isCompoundAssignment()) {
        SourceInfo binarySourceInfo = binary.getSourceInfo();
        JExpression binaryLhs = binary.getLhs();
        assert extractor != null;
        JExpression binaryLhsCopy = extractor.copyWithoutSideEffects(binaryLhs, tr);
        JBinaryOperation newBinary = null;

        switch (binary.getOp()) {
          case ASG_ADD:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.ADD, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_BIT_AND:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.BIT_AND, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_BIT_OR:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.BIT_OR, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_BIT_XOR:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.BIT_XOR, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_DIV:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.DIV, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_MOD:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.MOD, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_MUL:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.MUL, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_SHL:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.SHL, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_SHR:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.SHR, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_SHRU:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.SHRU, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_SUB:
            newBinary = JBinaryOperation.create(
                binarySourceInfo, JBinaryOperator.SUB, binaryLhsCopy, binary.getRhs());
            break;
          case ASG_CONCAT:
            newBinary = new JConcatOperation(
                binarySourceInfo, javaLangString, binaryLhsCopy, binary.getRhs());
            break;
          default:
            throw new AssertionError();
        }
        tr.append(new Replace(binary, new JAsgOperation(binarySourceInfo, binaryLhs, newBinary)));
      }

      return super.visit(binary);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    JClass javaLangString = method.getEnclosingType()
        .getJProgram().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING);
    TransformationRequest tr = new TransformationRequest(method);
    RemoveComplexAssignVisitor rca = new RemoveComplexAssignVisitor(tr, javaLangString);
    rca.accept(method);
    tr.commit();
  }
}
