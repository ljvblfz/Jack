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

package com.android.jack.shrob.obfuscation;

import com.android.jack.Options;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.shrink.NodeFinder;
import com.android.jack.shrob.spec.ClassSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.shrob.spec.KeepModifier;
import com.android.sched.item.Description;
import com.android.sched.marker.MarkerManager;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Marks all classes and members that will not be renamed when obfuscating.
 */
@Description("Marks all classes and members that will not be renamed when obfuscating.")
@Transform(add = KeepNameMarker.class)
public class NameKeeper implements RunnableSchedulable<JPackage> {

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JPackage pack) {
      if (flags.getKeepPackageNames() != null) {
        if (!isMarked(pack)) {
          if (flags.getKeepPackageNames()
              .matches(GrammarActions.getBinaryNameFormatter().getName(pack))) {
            keepName(pack);
          }
        }
      }

      // Overload visit of package to force loading of sub-classes.
      for (JDefinedClassOrInterface subType : pack.getTypes()) {
        subType.traverse(this);
      }
      return false;
   }

    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface clOrI) {
      if (clOrI.isExternal()) {
        keepName(clOrI);
        for (JMethod m : clOrI.getMethods()) {
          keepName(m);
        }
        return false;
      } else {
        // Handle "keep names" rules
        findSeeds(clOrI, flags.getKeepClassSpecs(), false /* allSpecsMustMatch */,
            true /* markType */);

        // Handle "keep classes with member names" rules
        findSeeds(clOrI, flags.getKeepClassesWithMembersSpecs(), true /* allSpecsMustMatch */,
            true /* markType */);

        // Handle "keep class member names" rules
        findSeeds(clOrI, flags.getKeepClassMembersSpecs(), false /* allSpecsMustMatch */,
            false /* markType */);

        return super.visit(clOrI);
      }
    }

    @Override
    public boolean visit(@Nonnull JMethod m) {
      if (JMethod.isClinit(m) || m instanceof JConstructor) {
        keepName(m);
      }
      return false;
    }
  }

  private void findSeeds(@Nonnull JDefinedClassOrInterface declaredType,
      @Nonnull List<ClassSpecification> specs, boolean allSpecsMustMatch,
      boolean markType) {
    for (ClassSpecification classSpec : specs) {
      boolean classMatches;

      Event findingClassSeedsEvent = tracer.start(ObfuscationEventType.FINDING_OBFUSCATION_SEEDS);
      try {
        classMatches = classSpec.getKeepModifier() != KeepModifier.ALLOW_OBFUSCATION
            && classSpec.matches(declaredType);
      } finally {
        findingClassSeedsEvent.end();
      }

      if (classMatches) {
        NodeFinder<JField> fieldFinder;
        List<JField> fieldsFound;
        NodeFinder<JMethod> methodFinder;
        List<JMethod> methodsFound;

        Event findingMemberSeedsEvent =
            tracer.start(ObfuscationEventType.FINDING_OBFUSCATION_SEEDS);
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
        // name of the fields and methods found and their enclosing type.
        if (!allSpecsMustMatch || (fieldFinder.allSpecificationsMatched()
            && methodFinder.allSpecificationsMatched())) {
          if (markType) {
            keepName(declaredType);
          }

          for (JField node : fieldsFound) {
            keepName(node);
          }

          for (JMethod node : methodsFound) {
            keepName(node);
          }
        }
      }
    }
  }

  private boolean markIfNecessary(@Nonnull MarkerManager node) {
    synchronized (node) {
      if (!isMarked(node)) {
        node.addMarker(new KeepNameMarker());
        return true;
      }
      return false;
    }
  }

  private boolean isMarked(@Nonnull MarkerManager node) {
    synchronized (node) {
      return node.containsMarker(KeepNameMarker.class);
    }
  }

  private void keepName(@Nonnull JPackage pack) {
    if (markIfNecessary(pack)) {
      JPackage enclosingPackage = pack.getEnclosingPackage();
      if (enclosingPackage != null) {
        keepName(enclosingPackage);
      }
    }
  }

  private void keepName(@Nonnull JDefinedClassOrInterface type) {
    if (markIfNecessary(type)) {
      keepName(type.getEnclosingPackage());
    }
  }

  private void keepName(JMethod method) {
    JMethodId methodId = method.getMethodId();
    markIfNecessary(methodId);
  }

  private void keepName(JField field) {
    JFieldId fieldId = field.getId();
    markIfNecessary(fieldId);
  }

  @Override
  public void run(@Nonnull JPackage pack) throws Exception {
    new Visitor().accept(pack);
  }

  @Nonnull
  protected static final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);
}
