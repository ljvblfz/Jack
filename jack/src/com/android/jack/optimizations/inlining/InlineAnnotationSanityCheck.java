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

package com.android.jack.optimizations.inlining;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.location.Location;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The same overall structure of {@link InlineAnnotatedMethods} but performs verifications on the
 * user specified @ForceInline annotations.
 */
@Description("Performs check @ForceInline annotations")
@Constraint(no = {DefinitionMarker.class, UseDefsMarker.class})
public class InlineAnnotationSanityCheck implements RunnableSchedulable<JMethod> {
  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @CheckForNull
  private final JDefinedAnnotationType forceInlineAnnotationType =
      getJAnnotationType("com.android.jack.annotations.ForceInline");

  private class Checker extends JVisitor {
    private final Set<JMethod> inlineChain;

    private Checker(Set<JMethod> inlineChain) {
      this.inlineChain = inlineChain;
    }

    @Override
    public void endVisit(@Nonnull JMethodCall call) {
      Collection<JMethod> targets = call.getMethodIdWide().getMethods();
      if (targets.size() != 1) {
        return;
      }
      JMethod target = targets.iterator().next();

      if (target.isAbstract() || target.isNative()) {
        return;
      }

      // Avoid inlining targets that are in prebuilts since they might not be in proper
      // three address code.
      Location location = target.getEnclosingType().getLocation();
      if (location instanceof TypeInInputLibraryLocation) {
        InputLibrary inputLibrary = ((TypeInInputLibraryLocation) location).getInputLibrary();
        if (inputLibrary.containsFileType(FileType.PREBUILT)) {
          return;
        }
      }

      assert forceInlineAnnotationType != null;
      List<JAnnotation> annotationList = target.getAnnotations(forceInlineAnnotationType);
      if (!annotationList.isEmpty()) {
        // Inline cycle detected
        if (inlineChain.contains(target)) {
          // We shouldn't recurse if there is a cycle since we are following down an infinite loop.
          reportCycle();
        } else {
          // Recurse, with the target to see if we run into a cycle if we did inline this code.
          inlineChain.add(target);
          new Checker(inlineChain).accept(target);

          // If we don't have any assertions, we can at least replace this method call with
          // completely
          // cloned body.
          inlineChain.remove(target);
        }
      }
    }
  }

  private static void reportCycle() {
    Jack.getSession().getReporter().report(Severity.FATAL, new ForceInlineCycle());
    Jack.getSession().abortEventually();
  }

  @CheckForNull
  private static JDefinedAnnotationType getJAnnotationType(@Nonnull String className) {
    String signature = NamingTools.getTypeSignatureName(className);
    JAnnotationType type = Jack.getSession().getPhantomLookup().getAnnotationType(signature);
    if (type instanceof JDefinedAnnotationType) {
      return (JDefinedAnnotationType) type;
    } else {
      return null;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (forceInlineAnnotationType == null) {
      return;
    }
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }
    Set<JMethod> inlineChain = new HashSet<>();
    inlineChain.add(method);
    new Checker(inlineChain).accept(method);
  }

  /**
   * Report cycle with @ForceInline methods.
   */
  private static class ForceInlineCycle implements Reportable {
    @Override
    public String getMessage() {
      return "Cannot perform annotated method inlining"
          + " due to cycle detected in @ForceInline methods";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }
}
