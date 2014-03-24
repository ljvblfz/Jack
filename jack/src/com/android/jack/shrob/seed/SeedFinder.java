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

package com.android.jack.shrob.seed;

import com.android.jack.Options;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.shrink.NodeFinder;
import com.android.jack.shrob.spec.ClassSpecification;
import com.android.jack.shrob.spec.FieldSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.shrob.spec.MethodSpecification;
import com.android.jack.shrob.spec.Specification;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A visitor that find seeds.
 */
@HasKeyId
@Description("Visitor that find seeds")
@Constraint(need = OriginalNames.class)
@Transform(add = SeedMarker.class)
public class SeedFinder implements RunnableSchedulable<JDefinedClassOrInterface> {

  public static final BooleanPropertyId SEARCH_SEEDS_IN_HIERARCHY = BooleanPropertyId.create(
      "jack.shrob.seed.searchinhierarchy",
      "Search for shrob seeds in hierarchy").addDefaultValue(Boolean.TRUE);

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);

  @Nonnull
  private final boolean searchInHierarchy =
      ThreadConfig.get(SEARCH_SEEDS_IN_HIERARCHY).booleanValue();

  private synchronized void markIfNecessary(@Nonnull JNode node) {
    if (!node.containsMarker(SeedMarker.class)) {
      node.addMarker(SeedMarker.getInstance());
    }
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    boolean matched = false;
    List<FieldSpecification> matchedFieldSpecs = new ArrayList<FieldSpecification>();
    List<MethodSpecification> matchedMethodSpecs = new ArrayList<MethodSpecification>();
    for (ClassSpecification classSpec : flags.getKeepClassSpecs()) {
      if (classSpec.matches(type)) {
        markIfNecessary(type);
        matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
        matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
      }
    }
    for (ClassSpecification classSpec : flags.getKeepClassMembersSpecs()) {
      if (classSpec.matches(type)) {
        matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
        matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
      }
    }
    for (ClassSpecification classSpec : flags.getKeepClassesWithMembersSpecs()) {
      if (classSpec.matches(type)) {
        NodeFinder<JField> fieldFinder = new NodeFinder<JField>(type.getFields());
        fieldFinder.find(classSpec.getFieldSpecs());

        NodeFinder<JMethod> methodFinder = new NodeFinder<JMethod>(type.getMethods());
        methodFinder.find(classSpec.getMethodSpecs());

        if (fieldFinder.allSpecificationsMatched() && methodFinder.allSpecificationsMatched()) {
          markIfNecessary(type);
          matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
          matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
        }
      }
    }

    matchSpecifications(type.getFields(), matchedFieldSpecs);
    matchSpecifications(type.getMethods(), matchedMethodSpecs);

    if (searchInHierarchy) {
      JClass superclass = type.getSuperClass();
      while (superclass instanceof JDefinedClass) {
        JDefinedClass definedSuperclass = (JDefinedClass) superclass;
        matchSpecifications(definedSuperclass.getFields(), matchedFieldSpecs);
        matchSpecifications(definedSuperclass.getMethods(), matchedMethodSpecs);
        superclass = definedSuperclass.getSuperClass();
      }
    }
  }

  private <T extends JNode> void matchSpecifications(@Nonnull List<T> nodes,
      @Nonnull List<? extends Specification<T>> specs) {
    for (T node : nodes) {
      for (Specification<T> spec : specs) {
        if (spec.matches(node)) {
          markIfNecessary(node);
        }
      }
    }
  }

}
