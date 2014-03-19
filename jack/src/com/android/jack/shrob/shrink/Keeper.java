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

package com.android.jack.shrob.shrink;

import com.android.jack.Options;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JType;
import com.android.jack.shrob.spec.ClassSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.shrob.spec.KeepModifier;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.log.Event;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that marks all classes and members that will be kept when
 * shrinking.
 */
@Description("Marks all classes and members that will be kept when shrinking.")
@Transform(add = KeepMarker.class)
@Constraint(need = ExtendingOrImplementingClassMarker.class)
@HasKeyId
public class Keeper implements RunnableSchedulable<JDefinedClassOrInterface> {

  private class Visitor extends Tracer {

    private Visitor() {
      super(ThreadConfig.get(KEEP_ENCLOSING_METHOD).booleanValue());
    }

    private void findSeeds(@Nonnull JDefinedClassOrInterface declaredType,
        @Nonnull List<ClassSpecification> specs, boolean allSpecsMustMatch) {
      for (ClassSpecification classSpec : specs) {
        boolean classMatches;

        Event findingClassSeedsEvent = tracer.start(ShrinkEventType.FINDING_SEEDS);
        try {
          classMatches = classSpec.getKeepModifier() != KeepModifier.ALLOW_SHRINKING
              && classSpec.matches(declaredType);
        } finally {
          findingClassSeedsEvent.end();
        }

        if (classMatches) {
          NodeFinder<JField> fieldFinder;
          List<JField> fieldsFound;
          NodeFinder<JMethod> methodFinder;
          List<JMethod> methodsFound;

          Event findingMemberSeedsEvent = tracer.start(ShrinkEventType.FINDING_SEEDS);
          try {
            fieldFinder = new NodeFinder<JField>(declaredType.getFields());
            fieldsFound = fieldFinder.find(classSpec.getFieldSpecs());

            methodFinder = new NodeFinder<JMethod>(declaredType.getMethods());
            methodsFound = methodFinder.find(classSpec.getMethodSpecs());
          } finally {
            findingMemberSeedsEvent.end();
          }

          // If all member specifications must be matched, we have to check that the field and
          // the method finder have matched all their respective specifications before keeping the
          // fields and methods found and their enclosing type.
          if (!allSpecsMustMatch || (fieldFinder.allSpecificationsMatched()
              && methodFinder.allSpecificationsMatched())) {
            trace(declaredType);

            for (JField node : fieldsFound) {
              trace(node);
            }

            for (JMethod node : methodsFound) {
              trace(node);
              for (JType paramType : node.getMethodId().getParamTypes()) {
                trace(paramType);
              }
            }
          }
        }
      }
    }

    @Override
    public void trace(@Nonnull JDefinedClassOrInterface t) {
      if (!isMarked(t)) {
        super.trace(t);
        // Handle "keepclassmember" rules
        findSeeds(t, flags.getKeepClassMembersSpecs(), false /* allSpecsMustMatch */);
      }

    }

    @Override
    public boolean markIfNecessary(@Nonnull JNode node) {
      synchronized (node) {
        if (!isMarked(node)) {
          node.addMarker(new KeepMarker());
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean isMarked(@Nonnull JNode node) {
      if (node instanceof JDefinedClassOrInterface
          && ((JDefinedClassOrInterface) node).isExternal()) {
        return true;
      } else if (node instanceof JMethod
          && ((JMethod) node).getEnclosingType().isExternal()) {
        return true;
      } else {
        synchronized (node) {
          return node.containsMarker(KeepMarker.class);
        }
      }
    }
  }

  public static final BooleanPropertyId KEEP_ENCLOSING_METHOD = BooleanPropertyId.create(
      "jack.shrink.keep.enclosing.method",
      "Keep the enclosing method of annonymous classes").addDefaultValue("false");

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    Visitor visitor = new Visitor();
    // Handle "keep" rules
    visitor.findSeeds(type, flags.getKeepClassSpecs(), false /* allSpecsMustMatch */);

    // Handle "keep classes with members" rules
    visitor.findSeeds(type, flags.getKeepClassesWithMembersSpecs(),
        true /* allSpecsMustMatch */);
  }
}
