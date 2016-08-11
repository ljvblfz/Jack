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

import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/** Creates empty classes to represent collected lambda groups */
@Description("Creates empty classes to represent collected lambda groups")
@Constraint(need = LambdaCollectionMarker.class)
@Transform(remove = LambdaCollectionMarker.class)
@Support(LambdaToAnonymousConverter.class)
@ExclusiveAccess(JSession.class)
@Use(LambdaCollection.class)
public final class LambdaGroupClassCreator implements RunnableSchedulable<JSession> {
  /** For each lambda group creates an empty lambda group class. */
  @Override
  public void run(@Nonnull JSession session) {
    LambdaCollectionMarker marker =
        session.removeMarker(LambdaCollectionMarker.class);
    assert marker != null;
    // Create a set of classes representing lambda class groups
    marker.getCollection().createLambdaClassGroups(session);
  }
}
