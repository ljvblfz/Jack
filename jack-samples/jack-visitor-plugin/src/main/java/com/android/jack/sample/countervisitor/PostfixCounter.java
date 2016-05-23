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

package com.android.jack.sample.countervisitor;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A schedulable that counts the number of postfix operations ({@code <expr>++} or
 * {@code <expr>--}).
 * <p>
 * This schedulable works on the {@link JMethod} component because we are interested in visiting
 * every method's code. Like any schedulable, it is annotated with the mandatory
 * {@link Description} annotation.
 * <p>
 * Postfix operations are represented by {@link JPostfixOperation}. Since we need to visit these
 * nodes, we explicitly declare that we need this node using the {@link Constraint} annotation.
 * This schedulable also adds the {@link CountingMarker} that holds the count of postfix
 * operations that were visited. Therefore we also need to declare that we add this marker using
 * the {@link Transform} annotation.
 * <p>
 * Finally, since Jack will execute this schedulable on every method using multiple threads in
 * parallel, we must ensure thread safety when we increment the counter of the
 * {@link CountingMarker}.
 */
@Description("Counts postfix nodes")
@Constraint(need = JPostfixOperation.class)
@Transform(add = CountingMarker.class)
public class PostfixCounter implements RunnableSchedulable<JMethod> {

  // Cache the session to avoid fetching it multiple times.
  @Nonnull
  private final JSession session = Jack.getSession();

  /**
   * This method is called by the scheduler (from multiple threads) for each method in the IR.
   */
  @Override
  public void run(@Nonnull JMethod method) {
    // We only process method with code.
    if (method.isNative() || method.isAbstract()) {
      return;
    }

    // We create a visitor to visit the postfix operations in the IR of the method.
    PostfixVisitor visitor = new PostfixVisitor();
    visitor.accept(method);

    // We retrieve (or create the first time) the CountingMarker that holds the number of postfix
    // operations visited in all methods that have been processed so far.
    CountingMarker sessionMarker = getOrCreateSessionMarker();

    // Increment the counter with the number of postfix operations we visited in this method.
    sessionMarker.incrementCounter(visitor.getCounter());
  }

  /**
   * This methods retrieves the CountingMarker of the Jack session. If the marker
   * does not exist yet, this method creates a new one and adds it onto the session.
   * <p>
   * This method deals with concurrency issues between threads where the current thread could
   * attempt to add a new marker on the session at the same time than another thread.
   *
   * @return the {@link CountingMarker} of the Jack session
  */
  @Nonnull
  private CountingMarker getOrCreateSessionMarker() {
    CountingMarker sessionMarker = session.getMarker(CountingMarker.class);
    if (sessionMarker == null) {
      sessionMarker = new CountingMarker();
      CountingMarker existingMarker = session.addMarkerIfAbsent(sessionMarker);
      if (existingMarker != null) {
        // Another thread beat us: use that marker.
        sessionMarker = existingMarker;
      }
    }
    return sessionMarker;
  }

}
