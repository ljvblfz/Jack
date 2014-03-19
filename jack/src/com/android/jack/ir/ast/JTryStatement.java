/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java try statement.
 */
@Description("Java try statement")
public class JTryStatement extends JStatement {

  /**
   * A {@link Tag} meaning that a {@code JTryStatement} may contain a finally block.
   */
  @Description("A JTryStatement may contain a finally block.")
  public static final class FinallyBlock implements Tag {
  }

  /**
   * A {@link Tag} meaning that a {@code JTryStatement} is in try-with-resources form.
   */
  @Description("A JTryStatement is in try-with-resources form.")
  public static final class TryWithResourcesForm implements Tag {
  }

  private static final long serialVersionUID = 1L;
  @Nonnull
  private List<JStatement> resourcesDeclaration;
  @Nonnull
  private final List<JCatchBlock> catchBlocks;
  @CheckForNull
  private JBlock finallyBlock;
  @Nonnull
  private JBlock tryBlock;

  public JTryStatement(SourceInfo info,
      @Nonnull List<JStatement> resourcesDeclaration,
      @Nonnull JBlock tryBlock,
      @Nonnull List<JCatchBlock> catchBlocks,
      @CheckForNull JBlock finallyBlock) {
    super(info);
    this.resourcesDeclaration = resourcesDeclaration;
    this.tryBlock = tryBlock;
    this.catchBlocks = catchBlocks;
    this.finallyBlock = finallyBlock;
  }

  @Nonnull
  public List<JCatchBlock> getCatchBlocks() {
    return catchBlocks;
  }

  @CheckForNull
  public JBlock getFinallyBlock() {
    return finallyBlock;
  }

  @Nonnull
  public JBlock getTryBlock() {
    return tryBlock;
  }

  @Nonnull
  public List<JStatement> getResourcesDeclarations() {
    return resourcesDeclaration;
  }

  public void setResourcesDeclarations(@Nonnull List<JStatement> resourcesDeclaration) {
    this.resourcesDeclaration = resourcesDeclaration;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(resourcesDeclaration);
      visitor.accept(tryBlock);
      visitor.accept(catchBlocks);
      if (finallyBlock != null) {
        visitor.accept(finallyBlock);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JStatement stmt : resourcesDeclaration) {
      stmt.traverse(schedule);
    }
    tryBlock.traverse(schedule);
    for (JCatchBlock block : catchBlocks) {
      block.traverse(schedule);
    }
    if (finallyBlock != null) {
      finallyBlock.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(resourcesDeclaration, existingNode, (JStatement) newNode, transformation)) {
        if (!transform(catchBlocks, existingNode, (JCatchBlock) newNode, transformation)) {
          super.transform(existingNode, newNode, transformation);
        }
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (tryBlock == existingNode) {
      tryBlock = (JBlock) newNode;
    } else if (finallyBlock == existingNode) {
      finallyBlock = (JBlock) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (finallyBlock == existingNode) {
      finallyBlock = null;
    } else {
      super.removeImpl(existingNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
