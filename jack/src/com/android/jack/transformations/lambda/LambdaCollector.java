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
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/** Collects information regarding the lambdas used in the code. */
@Description("Collects information regarding the lambdas used in the code.")
@Constraint(need = { JLambda.class, JLambda.DefaultBridgeAddedInLambda.class })
@Transform(add = LambdaCollectionMarker.class)
@Support(LambdaToAnonymousConverter.class)
@Access(JSession.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class LambdaCollector implements RunnableSchedulable<JMethod> {
  @Nonnull
  private static final TypePackageAndMethodFormatter FORMATTER = Jack.getLookupFormatter();

  /** References the current lambda class collector */
  @Nonnull
  private final LambdaCollection collection;

  /** Initializes the marker on the current session and collector */
  public LambdaCollector() {
    JSession session = Jack.getSession();
    LambdaCollectionMarker marker =
        session.getMarker(LambdaCollectionMarker.class);
    if (marker == null) {
      marker = new LambdaCollectionMarker(new LambdaCollection());
      LambdaCollectionMarker existing = session.addMarkerIfAbsent(marker);
      if (existing != null) {
        marker = existing;
      }
    }
    collection = marker.getCollection();
  }

  @Override
  public void run(@Nonnull final JMethod method) {
    JVisitor processor = new JVisitor() {
      private int nextId = 0;

      @Override
      public boolean visit(@Nonnull JLambda lambda) {
        // Generate a stable-ordered lambda id by combining class name,
        // method name and the index of the lambda in visiting order
        String lambdaId = FORMATTER.getName(method.getEnclosingType()) + ";;" +
            FORMATTER.getName(method) + ";;" + nextId++;
        collection.addLambda(method.getEnclosingType(), lambdaId, lambda);
        return true;
      }
    };
    processor.accept(method);
  }
}
