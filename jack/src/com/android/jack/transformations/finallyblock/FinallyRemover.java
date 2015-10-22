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

package com.android.jack.transformations.finallyblock;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.CloneStatementVisitor;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.marker.Marker;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes all finally blocks and adds their content where
 * needed.
 * <p>
 * Two visits are needed. First visit finds the most nested finally blocks and treats them first.
 * Second visit is partial, it starts from {@link JTryStatement}s and inlines the finally block.
 */
@Description("Removes finally blocks. Their contents are copied where they should.")
@Name("FinallyRemover")
@Constraint(need = {NoImplicitBlock.class, JTryStatement.class},
    no = JFieldInitializer.class)
@Transform(add = {JTryStatement.class,
    JLocalRef.class,
    JThrowStatement.class,
    JBlock.class,
    InlinedFinallyMarker.class,
    JExpressionStatement.class}, remove = JTryStatement.FinallyBlock.class)
@Use({LocalVarCreator.class, CloneStatementVisitor.class})
public class FinallyRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final JClass throwableType =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

  /**
   * Finds the most nested {@link JTryStatement}s to inline its finally block. Then all of them are
   * handled going back up and the transformations are applied each time.
   */
  private class JTryStatementVisitor extends JVisitor {

    /**
     * A stack of {@link TransformationRequest} that contains the original
     * {@code TransformationRequest} passed as parameter. A new {@code TransformationRequest} is
     * added to the stack each time a {@code JTryStatement} is visited. This allows to treat the
     * {@code JTryStatement}s starting with the most nested. Each time we go back up, the
     * {@code TransformationRequest} at the top of the stack is popped and committed.
     */
    @Nonnull
    private final Stack<TransformationRequest> requestStack = new Stack<TransformationRequest>();

    @Nonnull
    private final JMethod currentMethod;
    @Nonnull
    private final List<InlinedFinallyMarker> inlinedFinallyMarkers =
        new ArrayList<InlinedFinallyMarker>();

    private JTryStatementVisitor(@Nonnull TransformationRequest trRequest,
        @Nonnull JMethod currentMethod) {
      this.requestStack.add(trRequest);
      this.currentMethod = currentMethod;
    }

    @Override
    public boolean visit(@Nonnull JTryStatement tryStmt) {
      requestStack.add(new TransformationRequest(tryStmt));

      return super.visit(tryStmt);
    }

    @Override
    public void endVisit(@Nonnull JTryStatement tryStmt) {

      TransformationRequest request = requestStack.pop();
      request.commit();

      JBlock finallyBlock = tryStmt.getFinallyBlock();

      if (finallyBlock != null) {
        request = requestStack.peek();

        // visit again to inline the finally block
        FinallyInliner finallyInliner =
            new FinallyInliner(tryStmt, throwableType, currentMethod,
                finallyBlock, request, inlinedFinallyMarkers);
        finallyInliner.inlineFinally();

        request.append(new Remove(finallyBlock));
      }
    }
  }

  /**
   * Inline the finally block inside a {@link JTryStatement}. It is inlined before a
   * {@link JReturnStatement}, a {@link JGoto}, at the end of the try and catch blocks and in a
   * universal catch block of an inserted surrounding try.
   */
  private static class FinallyInliner extends JVisitor {

    @Nonnull
    private final JBlock finallyBlockToInsert;
    @Nonnull
    private final CloneStatementVisitor cloner;
    @Nonnull
    private final TransformationRequest currentRequest;
    @Nonnull
    private final JType throwableType;
    @Nonnull
    private final JMethod currentMethod;
    @Nonnull
    private final LocalVarCreator localForReturnCreator;
    @Nonnegative
    private int nameIndex = 0;
    @Nonnull
    private final List<InlinedFinallyMarker> inlinedFinallyMarkers;
    @Nonnull
    private final JTryStatement tryStmt;

    public FinallyInliner(@Nonnull JTryStatement tryStmt,
        @Nonnull JType throwableType,
        @Nonnull JMethod currentMethod,
        @Nonnull JBlock finallyBlockToInsert,
        @Nonnull TransformationRequest request,
        @Nonnull List<InlinedFinallyMarker> inlinedMarker) {
      this.tryStmt = tryStmt;
      this.throwableType = throwableType;
      this.currentMethod = currentMethod;
      this.finallyBlockToInsert = finallyBlockToInsert;
      currentRequest = request;
      cloner = new CloneStatementVisitor(request, currentMethod);
      localForReturnCreator = new LocalVarCreator(currentMethod, "ret");
      this.inlinedFinallyMarkers = inlinedMarker;
    }

    public void inlineFinally() {
      accept(tryStmt);

      JBlock tryBlock = tryStmt.getTryBlock();
      List<JCatchBlock> catchBlocks = tryStmt.getCatchBlocks();

      // add statements at the end of the try block
      addFinallyAtEndOfBlock(tryBlock);

      // add statements at the end of the catch blocks
      for (JCatchBlock catchBlock : catchBlocks) {
        addFinallyAtEndOfBlock(catchBlock);
      }

      // add statements in a universal catch block of a new surrounding try
      addCatchThrowableBlockWithFinallyStatements(tryStmt);
    }

    @Override
    public boolean visit(@Nonnull JReturnStatement returnStmt) {
      JExpression expr = returnStmt.getExpr();

      if (expr != null) {
        // When a local is returned in a try or catch block but modified in the matching finally
        // block, the value returned must be the value before it was modified in the finally block.
        // That's why we need to store the expression of the return in a new local that will be
        // returned after the content of the finally block.

        JLocal local =
            localForReturnCreator.createTempLocal(expr.getType(), expr.getSourceInfo(),
                currentRequest);

        JLocalRef returnedLocalRef = new JLocalRef(expr.getSourceInfo(), local);
        currentRequest.append(new Replace(expr, returnedLocalRef));

        JLocalRef assignedLocalRef = new JLocalRef(expr.getSourceInfo(), local);
        JAsgOperation assign =
            new JAsgOperation(expr.getSourceInfo(), assignedLocalRef, expr);
        currentRequest.append(new AppendBefore(returnStmt, assign.makeStatement()));
      }

      addFinallyBeforeBranching(returnStmt);

      return super.visit(returnStmt);
    }

    @Override
    public boolean visit(@Nonnull JLambda x) {
      // Body of lambda expression must not be visited into the context of the method that contains
      // the lambda expression.
      return false;
    }

    @Override
    public boolean visit(@Nonnull JGoto gotoStmt) {
      if (isBranchingOutsideOfTryStatement(gotoStmt)) {
        addFinallyBeforeBranching(gotoStmt);
      }
      return super.visit(gotoStmt);
    }

    @Nonnull
    private InlinedFinallyMarker getMarkerOfTryCatchingExceptions(@Nonnull JNode stmt) {
      JNode previous = stmt;

      do {
        stmt = stmt.getParent();

        // Find the previous try statement which will catch exceptions.
        while (!(stmt instanceof JTryStatement) && !(stmt instanceof JMethodBody)) {
          previous = stmt;
          stmt = stmt.getParent();
        }

        if (stmt instanceof JTryStatement) {
          if (((JTryStatement) stmt).getTryBlock() == previous) {
            // We are into a try block, thus exception catch here
            return new InlinedFinallyMarker((JTryStatement) stmt, false);
          } else if (((JTryStatement) stmt).getCatchBlocks().contains(previous) &&
              ((JTryStatement) stmt).getFinallyBlock() != null) {
            // We are into a catch block and there is a finally, thus exception is catched here
            return new InlinedFinallyMarker((JTryStatement) stmt, true);
          }
        }
      } while (!(stmt instanceof JMethodBody));

      return new InlinedFinallyMarker((JTryStatement) null, false);
    }

    @Nonnull
    private JBlock getClonedBlock(@Nonnull JBlock finallyBlock) {
      JBlock clonedFinallyBlock = cloner.cloneStatement(finallyBlockToInsert);

      JNode parent = finallyBlock.getParent();
      assert parent instanceof JTryStatement;

      InlinedFinallyMarker marker = getMarkerOfTryCatchingExceptions(parent);
      clonedFinallyBlock.addMarker(marker);

      //TODO(mikaelpeltier) Think a solution to have marker reflecting transformation.
      for (Marker m : cloner.getClonedMarkers()) {
        if (m instanceof InlinedFinallyMarker) {
          inlinedFinallyMarkers.add((InlinedFinallyMarker) m);
        }
      }
      inlinedFinallyMarkers.add(marker);

      return clonedFinallyBlock;
    }

    private void addFinallyBeforeBranching(@Nonnull JStatement branchingStmt) {
      JBlock clonedFinallyBlock = getClonedBlock(finallyBlockToInsert);
      currentRequest.append(new AppendBefore(branchingStmt, clonedFinallyBlock));
    }

    private void addFinallyAtEndOfBlock(@Nonnull JStatementList block) {
      List<JStatement> blockStatements = block.getStatements();

      if (!blockStatements.isEmpty()) {
        JStatement lastStmt = blockStatements.get(blockStatements.size() - 1);
        boolean isLastStmtBranching = (lastStmt instanceof JReturnStatement)
            || (lastStmt instanceof JGoto && isBranchingOutsideOfTryStatement((JGoto) lastStmt));

        if (!isLastStmtBranching) {
          JBlock clonedFinallyBlock = getClonedBlock(finallyBlockToInsert);
          currentRequest.append(new PrependAfter(lastStmt, clonedFinallyBlock));
        }
      } else {
        JBlock clonedFinallyBlock = getClonedBlock(finallyBlockToInsert);
        currentRequest.append(new AppendStatement(block, clonedFinallyBlock));
      }
    }

    /**
     * Inserts the given {@code JTryStatement} into the try block of a new {@code JTryStatement}
     * that only has a {@code catch(Throwable)} block which contains the statements of the finally
     * block and rethrows the catched {@code Throwable}.
     * <p>
     * This aims to handle the case where an exception is thrown inside the original try or catch
     * blocks.
     */
    private void addCatchThrowableBlockWithFinallyStatements(@Nonnull JTryStatement tryStmt) {

      boolean hasCatchBlock = !tryStmt.getCatchBlocks().isEmpty();

      JBlock finallyBlock = tryStmt.getFinallyBlock();
      assert finallyBlock != null;
      SourceInfo finallySourceInfo = finallyBlock.getSourceInfo();

      // create the new JTryStatement
      JBlock tryBlock = null;
      if (hasCatchBlock) {
        tryBlock = new JBlock(tryStmt.getTryBlock().getSourceInfo());
      } else { // if there are no catch blocks, we can reuse the try block directly
        tryBlock = tryStmt.getTryBlock();
      }

      JMethodBody methodBody = (JMethodBody) currentMethod.getBody();
      assert methodBody != null;
      JLocal local =
          new JLocal(finallySourceInfo, "t" + nameIndex++, throwableType, JModifier.SYNTHETIC,
              methodBody);

      JCatchBlock catchBlock =
          new JCatchBlock(finallySourceInfo, Collections.singletonList((JClass) throwableType),
              local);
      List<JCatchBlock> catchBlockList = new ArrayList<JCatchBlock>();
      catchBlockList.add(catchBlock);
      JTryStatement newTryStmt = new JTryStatement(tryStmt.getSourceInfo(),
          Collections.<JStatement>emptyList(),
          tryBlock,
          catchBlockList,
          null);

      // clone the finally block and add it in the catch(Throwable) block of the new JTryStatement
      JBlock clonedFinallyBlock = getClonedBlock(finallyBlockToInsert);

      currentRequest.append(new AppendStatement(catchBlock, clonedFinallyBlock));

      // add a Statement that rethrows the catched Throwable
      JLocalRef throwLocalRef = new JLocalRef(finallySourceInfo, local);
      JThrowStatement throwStmt = new JThrowStatement(finallySourceInfo, throwLocalRef);
      currentRequest.append(new AppendStatement(catchBlock, throwStmt));

      // TODO(mikaelpeltier) Think a better solution to take into account transformation not already
      // apply.
      for (InlinedFinallyMarker m : inlinedFinallyMarkers) {
        if (tryStmt == m.getTryStmt() && (m.isCatchIntoFinally()  || !hasCatchBlock)) {
          m.setTryStmt(newTryStmt);
        }
      }
      // add the new JTryStatement where the old one was
      currentRequest.append(new Replace(tryStmt, newTryStmt));

      // add the original JTryStatement inside the try block of the new one
      if (hasCatchBlock) {
        currentRequest.append(new AppendStatement(tryBlock, tryStmt));
      }
    }

    private boolean isBranchingOutsideOfTryStatement(@Nonnull JGoto gotoStatement) {
      JNode parent = gotoStatement.getTargetBlock().getParent();
      while (parent != tryStmt && !(parent instanceof JMethodBody)) {
        parent = parent.getParent();
      }
      if (parent == tryStmt) {
        return false;
      }
      return true;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest trRequest = new TransformationRequest(method);

    JTryStatementVisitor visitor = new JTryStatementVisitor(trRequest, method);

    visitor.accept(method);

    trRequest.commit();
  }

}
