/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.preprocessor;

import com.android.jack.Jack;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.preprocessor.PreProcessorApplier.Entry;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.request.TransformationStep;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * {@link TransformationStep} for adding some {@link JAnnotation} on one {@link Annotable}.
 */
public class AddAnnotationStep {
  @Nonnull
  private final Rule rule;

  @Nonnull
  private final JAnnotationType annotationType;

  @Nonnull
  private final Collection<?> toAnnotate;

  public AddAnnotationStep(@Nonnull Rule rule, @Nonnull JAnnotationType annotation,
      @Nonnull Collection<?> toAnnotate) {
    this.rule = rule;
    this.annotationType = annotation;
    this.toAnnotate = toAnnotate;
  }

  public void apply(@Nonnull Map<Entry, Rule> map) {
    for (Object candidate : toAnnotate) {
      if (candidate instanceof Annotable) {
        Annotable annotable = (Annotable) candidate;
        Entry entry = new Entry(annotable, annotationType);
        Reportable reportable;

        // Do not override existing annotation
        if (annotable.getAnnotations(annotationType).isEmpty()) {
          JRetentionPolicy retention = JRetentionPolicy.SOURCE;
          if (annotationType instanceof JDefinedAnnotationType) {
            retention = ((JDefinedAnnotationType) annotationType).getRetentionPolicy();
          }
          JAnnotation annotation = new JAnnotation(SourceInfo.UNKNOWN, retention, annotationType);
          annotable.addAnnotation(annotation);
          annotation.updateParents((JNode) annotable);
          map.put(entry, rule);

          reportable = new AnnotateReportable(annotable);
        } else {
          Rule  rule = map.get(entry);
          if (rule == null) {
            reportable = new AlreadySourceAnnotateReportable(annotable);
          } else {
            reportable = new AlreadyRuleAnnotateReportable(annotable, rule);
          }
        }

        Jack.getSession().getReporter().report(Severity.NON_FATAL, reportable);
      }
    }
  }

  private final class AnnotateReportable implements Reportable {
    @Nonnull
    private final Annotable annotated;

    public AnnotateReportable(@Nonnull Annotable annotated) {
      this.annotated = annotated;
    }

    @Override
    @Nonnull
    public String getMessage() {
      TypePackageAndMethodFormatter formatter = Jack.getUserFriendlyFormatter();

      return "Preprocessor: rule '" + rule.getName() + "' from "
          + rule.getLocation().getDescription() + ": annotates " + getElement(formatter, annotated)
          + " with @" + formatter.getName(annotationType);
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.INFO;
    }
  }

  private final class AlreadySourceAnnotateReportable implements Reportable {
    @Nonnull
    private final Annotable annotated;

    public AlreadySourceAnnotateReportable(@Nonnull Annotable annotated) {
      this.annotated = annotated;
    }

    @Override
    @Nonnull
    public String getMessage() {
      TypePackageAndMethodFormatter formatter = Jack.getUserFriendlyFormatter();

      return "Preprocessor: rule '" + rule.getName() + "' from "
          + rule.getLocation().getDescription() + ": " + getElement(formatter, annotated)
          + " already annotated with @" + formatter.getName(annotationType);
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.INFO;
    }
  }

  private final class AlreadyRuleAnnotateReportable implements Reportable {
    @Nonnull
    private final Annotable annotated;
    @Nonnull
    private final Rule ruleUsed;

    public AlreadyRuleAnnotateReportable(@Nonnull Annotable annotated, @Nonnull Rule rule) {
      this.annotated = annotated;
      this.ruleUsed = rule;
    }

    @Override
    @Nonnull
    public String getMessage() {
      TypePackageAndMethodFormatter formatter = Jack.getUserFriendlyFormatter();

      return "Preprocessor: rule '" + rule.getName() + "' from "
          + rule.getLocation().getDescription() + ": " + getElement(formatter, annotated)
          + " already annotated with @" + formatter.getName(annotationType) + " by rule '"
          + ruleUsed.getName() + "' from " + ruleUsed.getLocation().getDescription();
    }


    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.INFO;
    }
  }

  @Nonnull
  public static String getElement(@Nonnull TypePackageAndMethodFormatter formatter,
      @Nonnull Annotable annotable) {
    String element = "";

    if (annotable instanceof JType) {
      element = "type '" + formatter.getName((JType) annotable) + "'";
    } else if (annotable instanceof JField) {
      JField field = (JField) annotable;

      element =
          "field '" + formatter.getName(field.getEnclosingType()) + "." + field.getName() + "'";
    } else if (annotable instanceof JMethod) {
      JMethod method = (JMethod) annotable;

      element = "method '" + formatter.getName(method.getEnclosingType()) + "."
          + formatter.getNameWithoutReturnType(method.getMethodId()) + "'";
    } else if (annotable instanceof JPackage) {
      element = "package '" + formatter.getName((JPackage) annotable) + "'";
    } else {
      throw new AssertionError(annotable.getClass().getCanonicalName());
    }

    return element;
  }
}
