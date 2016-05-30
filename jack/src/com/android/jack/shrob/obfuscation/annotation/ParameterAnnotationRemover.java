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
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.library.DumpInLibrary;
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
@Constraint(need = JAnnotation.class)
@Transform(modify = JAnnotation.class)
public class ParameterAnnotationRemover extends AnnotationRemover implements
    RunnableSchedulable<JMethod> {

  @Nonnull
  public static final BooleanPropertyId EMIT_SOURCE_RETENTION_PARAMETER_ANNOTATION =
      BooleanPropertyId
          .create("jack.annotation.parameter.source-retention",
              "Emit parameters annotations that have a source retention")
          .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_CLASS_RETENTION_PARAMETER_ANNOTATION =
      BooleanPropertyId
          .create("jack.annotation.parameter.class-retention",
              "Emit parameters annotations that have a class retention")
          .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_RUNTIME_RETENTION_PARAMETER_ANNOTATION =
      BooleanPropertyId
          .create("jack.annotation.parameter.runtime-retention",
              "Emit parameters annotations that have a runtime retention")
          .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  public ParameterAnnotationRemover() {
    super(
        ThreadConfig.get(EMIT_SOURCE_RETENTION_ANNOTATION).booleanValue(),
        ThreadConfig.get(EMIT_CLASS_RETENTION_ANNOTATION).booleanValue(),
        ThreadConfig.get(EMIT_RUNTIME_RETENTION_ANNOTATION).booleanValue(),
        true /* keepSystemAnnotations */);
  }

  private class Visitor extends JVisitor {
    @Nonnull
    private final TransformationRequest request;

    private Visitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public boolean visit(@Nonnull JParameter param) {
      for (JAnnotation annotation : param.getAnnotations()) {
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
