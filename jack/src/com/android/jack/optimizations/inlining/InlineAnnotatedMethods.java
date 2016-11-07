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
import com.android.jack.optimizations.Optimizations;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.location.Location;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Insert a {@link InlineMarker} to {@link JMethodCall} so that the call site will be inlined by
 * {@link JMethodInliner} later on.
 *
 * <p>
 * As this class is made for testing purpose only, it makes no attempt to validate the correctness
 * of the corresponding inlining.
 *
 * <p>
 * Use with cautions.
 */
@Description("Inline methods annotated with @ForceInline")
@Transform(add = {InlineMarker.class})
@Constraint(no = {DefinitionMarker.class, UseDefsMarker.class})
@Support(Optimizations.InlineAnnotatedMethods.class)
@Filter(SourceTypeFilter.class)
public class InlineAnnotatedMethods implements RunnableSchedulable<JMethod> {
  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @CheckForNull
  private final JDefinedAnnotationType forceInlineAnnotationType =
      getJAnnotationType("com.android.jack.annotations.ForceInline");

  @Nonnull
  private final InlineVisitor inlineVisitor = new InlineVisitor();

  private class InlineVisitor extends JVisitor {
    @Override
    public void endVisit(@Nonnull JMethodCall call) {
      Collection<JMethod> targets = call.getMethodId().getMethods();
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
        call.addMarker(new InlineMarker(target));
      }
    }
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
    inlineVisitor.accept(method);
  }
}
