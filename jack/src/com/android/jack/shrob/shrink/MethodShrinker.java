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

package com.android.jack.shrob.shrink;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.TracerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} removing all methods not marked with the {@link KeepMarker}.
 */
@Description("Removes all methods not marked with the KeepMarker")
@Synchronized
@Constraint(need = {KeepMarker.class, PartialTypeHierarchy.class})
public class MethodShrinker implements RunnableSchedulable<JMethod> {

  private static final class UnknownReferencedTypeException extends Exception {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final JPhantomClassOrInterface unknownType;

    @Nonnull
    private final JMethod shrinkedMethod;

    private UnknownReferencedTypeException(@Nonnull JPhantomClassOrInterface unknownType,
        @Nonnull JMethod shrinkedMethod) {
      this.unknownType = unknownType;
      this.shrinkedMethod = shrinkedMethod;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Unknown referenced type '"
          + Jack.getUserFriendlyFormatter().getName(unknownType)
          + "' while trying to remove method '"
          + Jack.getUserFriendlyFormatter().getName(shrinkedMethod)
          + "' from '"
          + Jack.getUserFriendlyFormatter().getName(shrinkedMethod.getEnclosingType()) + "'";
    }
  }

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final com.android.sched.util.log.Tracer tracer = TracerFactory.getTracer();

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    boolean toRemove = !method.containsMarker(KeepMarker.class);
    if (toRemove) {
      TransformationRequest request = new TransformationRequest(method);
      request.append(new Remove(method));
      request.commit();
      logger.log(Level.INFO, "Removed method {0} from {1}", new Object[] {
          Jack.getUserFriendlyFormatter().getName(method),
          Jack.getUserFriendlyFormatter().getName(method.getEnclosingType())});
      PartialTypeHierarchy pth = method.getEnclosingType().getMarker(PartialTypeHierarchy.class);
      if (pth != null)  {
        ShrinkingException reportable = new ShrinkingException(
            new UnknownReferencedTypeException(pth.getUnknownType(), method));
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      }
    }
    tracer.getStatistic(ShrinkStatistic.METHODS_REMOVED).add(toRemove);
  }

}
