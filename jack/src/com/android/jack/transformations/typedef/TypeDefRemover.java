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

package com.android.jack.transformations.typedef;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.TypeRemover;
import com.android.jack.transformations.typedef.TypeDefRemover.RemoveTypeDef;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} removing @StringDef, @IntDef and all annotations
 * annotated with them.
 */
@Description("Removes @StringDef, @IntDef and all annotations annotated with them")
@Synchronized
@Support(RemoveTypeDef.class)
@HasKeyId
public class TypeDefRemover extends TypeRemover {

  @Nonnull
  public static final BooleanPropertyId REMOVE_TYPEDEF = BooleanPropertyId.create(
      "jack.android.remove-typedef",
      "Removes @StringDef, @IntDef and all annotations annotated with them")
      .addDefaultValue(false);

  /**
   * A {@link Feature} allowing to remove @StringDef, @IntDef and all annotations annotated with
   * them.
   */
  @Description("Removes @StringDef, @IntDef and all annotations annotated with them")
  public static class RemoveTypeDef implements Feature {
  }

  private static class InvalidRetentionForTypeDef implements Reportable {

    @Nonnull
    private final JDefinedAnnotationType typeDef;

    public InvalidRetentionForTypeDef(@Nonnull JDefinedAnnotationType typeDef) {
      this.typeDef = typeDef;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Annotation @" + Jack.getUserFriendlyFormatter().getName(typeDef)
          + "should be annotated with @Retention(RetentionPolicy.SOURCE)";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }
  }

  private static class InvalidTypeDefTarget implements Reportable {

    @Nonnull
    private final JDefinedClassOrInterface annotated;

    public InvalidTypeDefTarget(@Nonnull JDefinedClassOrInterface annotated) {
      this.annotated = annotated;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Type " + Jack.getUserFriendlyFormatter().getName(annotated)
          + " should not be annotated with @IntDef or @StringDef,"
          + " only annotations are valid targets";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }
  }

  @Nonnull
  private final JDefinedAnnotationType stringDef =
    Jack.getSession().getLookup().getAnnotationType("Landroid/annotation/StringDef;");

  @Nonnull
  private final JDefinedAnnotationType intDef =
    Jack.getSession().getLookup().getAnnotationType("Landroid/annotation/IntDef;");

  @Override
  protected boolean mustBeRemoved(@Nonnull JDefinedClassOrInterface type) {
    if (type instanceof JDefinedAnnotationType) {
      if ((!type.getAnnotations(stringDef).isEmpty()) || !type.getAnnotations(intDef).isEmpty()) {
        JDefinedAnnotationType typeDef = (JDefinedAnnotationType) type;
        if (typeDef.getRetentionPolicy() != JRetentionPolicy.SOURCE) {
          type.getSession().getReporter().report(Severity.NON_FATAL,
              new InvalidRetentionForTypeDef(typeDef));
        }
        return true;
      }
    } else if ((!type.getAnnotations(stringDef).isEmpty())
        || !type.getAnnotations(intDef).isEmpty()) {
      Jack.getSession().getReporter().report(Severity.NON_FATAL,
          new InvalidTypeDefTarget(type));
    }
    return false;
  }

  @Override
  protected boolean isPlannedForRemoval(@Nonnull JMethod method) {
    return mustBeRemoved(method.getEnclosingType());
  }
}
