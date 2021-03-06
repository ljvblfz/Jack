/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.annotation;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * {@link RunnableSchedulable} that removes annotations from methods.
 */
@Description("RunnableSchedulable that removes annotations from methods.")
@Constraint(need = JAnnotation.class)
@Transform(modify = JAnnotation.class)
@Optional(@ToSupport(feature = SourceVersion8.class,
    add = @Constraint(need = JAnnotation.RepeatedAnnotation.class)))
public class MethodAnnotationRemover extends AnnotationRemover implements
    RunnableSchedulable<JMethod> {

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  public MethodAnnotationRemover() {
    super(
        ThreadConfig.get(EMIT_SOURCE_RETENTION_ANNOTATION).booleanValue(),
        ThreadConfig.get(EMIT_CLASS_RETENTION_ANNOTATION).booleanValue(),
        ThreadConfig.get(EMIT_RUNTIME_RETENTION_ANNOTATION).booleanValue(),
        true /* keepSystemAnnotations */);
  }

  @Override
  public void run(@Nonnull JMethod method) {
    TransformationRequest request = new TransformationRequest(method);

    for (JAnnotation annotation : method.getAnnotations()) {
      if (!mustBeKept(annotation)) {
        request.append(new Remove(annotation));
        logger.log(Level.INFO, "Removed annotation {0} from method {1}.{2}", new Object[] {
            Jack.getUserFriendlyFormatter().getName(annotation.getType()),
            Jack.getUserFriendlyFormatter().getName(method.getEnclosingType()),
            Jack.getUserFriendlyFormatter().getName(method)});
      }
    }
    request.commit();
  }
}
