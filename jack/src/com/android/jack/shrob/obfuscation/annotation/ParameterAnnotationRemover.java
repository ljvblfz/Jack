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
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * {@link RunnableSchedulable} that removes annotations from parameters.
 */
@HasKeyId
@Description("RunnableSchedulable that removes annotations from parameters.")
@Constraint(need = JAnnotationLiteral.class)
@Transform(modify = JAnnotationLiteral.class)
public class ParameterAnnotationRemover extends AnnotationRemover implements
    RunnableSchedulable<JMethod> {

  @Nonnull
  public static final
      BooleanPropertyId EMIT_RUNTIME_VISIBLE_PARAMETER_ANNOTATION = BooleanPropertyId.create(
          "jack.annotation.runtimevisible.parameter",
          "Emit parameters annotations that are runtime visible").addDefaultValue("true");

  @Nonnull
  public static final
      BooleanPropertyId EMIT_RUNTIME_INVISIBLE_PARAMETER_ANNOTATION = BooleanPropertyId.create(
          "jack.annotation.runtimeinvisible.parameter",
          "Emit parameters annotations that are runtime invisible").addDefaultValue("true");

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public ParameterAnnotationRemover() {
    super(ThreadConfig.get(EMIT_RUNTIME_VISIBLE_PARAMETER_ANNOTATION).booleanValue(), ThreadConfig
        .get(EMIT_RUNTIME_INVISIBLE_PARAMETER_ANNOTATION).booleanValue(),
        true /* addSystemAnnotations */);
  }

  private class Visitor extends JVisitor {
    @Nonnull
    private final TransformationRequest request;

    private Visitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public boolean visit(@Nonnull JParameter param) {
      for (JAnnotationLiteral annotation : param.getAnnotations()) {
        if (!mustBeKept(annotation)) {
          request.append(new Remove(annotation));
          JMethod currentMethod = param.getEnclosingMethod();
          assert currentMethod != null;
          logger.log(Level.INFO,
              "Removed parameter annotation {0} from method {1}.{2}", new Object[] {
                  Jack.getUserFriendlyFormatter().getName(annotation.getType()),
                  Jack.getUserFriendlyFormatter().getName(currentMethod.getEnclosingType()),
                  Jack.getUserFriendlyFormatter().getName(currentMethod)});
        }
      }
      return super.visit(param);
    }

    @Override
    public boolean visit(@Nonnull JMethodBody x) {
      return false;
    }
  }

  @Override
  public void run(@Nonnull JMethod t) throws Exception {
    TransformationRequest request = new TransformationRequest(t);
    Visitor visitor = new Visitor(request);
    visitor.accept(t);
    request.commit();
  }
}
