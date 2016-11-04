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

package com.android.jack.optimizations;

import com.android.jack.library.DumpInLibrary;
import com.android.jack.library.PrebuiltCompatibility;
import com.android.jack.optimizations.cfg.VariablesScope;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.category.Private;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.EnumPropertyId;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * {@link Feature} and {@link PropertyId} related to optimizations.
 */
@HasKeyId
public class Optimizations {

  /**
   * A {@link Feature} that represents optimization of def/use chain.
   */
  @HasKeyId
  @Description("Optimize def/use chain")
  public static class DefUseSimplifier implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.def-use-simplifier", "Optimize def/use chain")
        .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);
  }

  /**
   * A {@link Feature} that represents optimization of use/def chain.
   */
  @HasKeyId
  @Description("Optimize use/def chain")
  public static class UseDefSimplifier implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.use-def-simplifier", "Optimize use/def chain")
        .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);
  }

  /**
   * A {@link Feature} that represents optimization of expressions using constants.
   */
  @HasKeyId
  @Description("Optimize expressions using constants")
  public static class ExpressionSimplifier implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.expression-simplifier",
            "Optimize expressions using constants")
        .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);
  }

  /**
   * A {@link Feature} that represents optimization of 'if' expressions using a boolean constant.
   */
  @HasKeyId
  @Description("Optimize 'if' expressions using a boolean constant")
  public static class IfSimplifier implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.if-simplifier",
            "Optimize 'if' expressions using a boolean constant")
        .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);
  }

  /**
   * A {@link Feature} that represents optimization of '!' operator.
   */
  @HasKeyId
  @Description("Optimize '!' operator")
  public static class NotSimplifier implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE =
        BooleanPropertyId.create("jack.optimization.not-simplifier", "Optimize '!' operator")
            .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);
  }

  /**
   * A {@link Feature} that represents optimization of class final modifiers.
   */
  @HasKeyId
  @Description("Detect effectively final classes, add modifiers when possible")
  public static class ClassFinalizer implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.class-finalizer",
            "Detect effectively final classes, add modifiers when possible")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId ADD_FINAL_MODIFIER = BooleanPropertyId
        .create("jack.optimization.class-finalizer.add-final-modifier",
            "Set final modifier to all effectively final classes")
        .addDefaultValue(Boolean.TRUE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents optimization of method final modifiers.
   */
  @HasKeyId
  @Description("Detect effectively final methods, add modifiers when possible")
  public static class MethodFinalizer implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.method-finalizer",
            "Detect effectively final methods, add modifiers when possible")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId ADD_FINAL_MODIFIER = BooleanPropertyId
        .create("jack.optimization.method-finalizer.add-final-modifier",
            "Set final modifier to all effectively final methods")
        .addDefaultValue(Boolean.TRUE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents optimization of field final modifiers.
   */
  @HasKeyId
  @Description("Detect effectively final fields, add modifiers when possible")
  public static class FieldFinalizer implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.field-finalizer",
            "Detect effectively final fields, add modifiers when possible")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId ENFORCE_INIT_SEMANTIC = BooleanPropertyId
        .create("jack.optimization.field-finalizer.enforce-initialization-semantic",
            "Ensure initialization semantic for final field")
        .addDefaultValue(Boolean.TRUE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId ADD_FINAL_MODIFIER = BooleanPropertyId
        .create("jack.optimization.field-finalizer.add-final-modifier",
            "Set final modifier to all effectively final fields")
        .addDefaultValue(Boolean.TRUE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents field value propagation optimization.
   */
  @HasKeyId
  @Description("Apply field value propagation optimization")
  public static class FieldValuePropagation implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.field-value-propagation",
            "Apply field value propagation optimization")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId PRESERVE_NULL_CHECKS = BooleanPropertyId
        .create("jack.optimization.field-value-propagation.preserve-null-checks",
            "Preserves null checks when a value of the instance field is propagated")
        .addDefaultValue(Boolean.FALSE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId ENSURE_TYPE_INITIALIZERS = BooleanPropertyId
        .create("jack.optimization.field-value-propagation.ensure-type-initializers",
            "Ensures type initializers are called if caused by field access")
        .addDefaultValue(Boolean.FALSE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents argument value propagation optimization.
   */
  @HasKeyId
  @Description("Apply method argument value propagation optimization")
  public static class ArgumentValuePropagation implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.argument-value-propagation",
            "Apply method argument value propagation optimization")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents write-only field removal optimization.
   */
  @HasKeyId
  @Description("Apply write-only field removal optimization")
  public static class WriteOnlyFieldRemoval implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.write-only-field-removal",
            "Apply write-only field removal optimization")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId PRESERVE_NULL_CHECKS = BooleanPropertyId
        .create("jack.optimization.write-only-field-removal.preserve-null-checks",
            "Preserves null checks when a field assignment is removed")
        .addDefaultValue(Boolean.FALSE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId PRESERVE_OBJECT_LIFETIME = BooleanPropertyId
        .create("jack.optimization.write-only-field-removal.preserve-object-lifetime",
            "Prevents field writes removal of object values that can affect object lifetime")
        .addDefaultValue(Boolean.FALSE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId ENSURE_TYPE_INITIALIZERS = BooleanPropertyId
        .create("jack.optimization.write-only-field-removal.ensure-type-initializers",
            "Ensures type initializers are called if caused by field access")
        .addDefaultValue(Boolean.FALSE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId REMOVE_UNUSED_FIELDS = BooleanPropertyId
        .create("jack.optimization.write-only-field-removal.remove-unused-fields",
            "Remove fields without reads or writes")
        .addDefaultValue(Boolean.TRUE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents simple block merging optimization.
   */
  @HasKeyId
  @Description("Apply simple block merging optimization")
  public static class SimpleBasicBlockMerging implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.simple-block-merging",
            "Apply simple block merging optimization")
        .addDefaultValue(Boolean.FALSE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final BooleanPropertyId PRESERVE_SOURCE_INFO = BooleanPropertyId
        .create("jack.optimization.simple-block-merging.preserve-source-info",
            "Preserves source info while merging blocks")
        .addDefaultValue(Boolean.TRUE)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final EnumPropertyId<VariablesScope> MERGE_VARIABLES = EnumPropertyId
        .create("jack.optimization.simple-block-merging.merge-vars",
            "Merge variables before merging blocks",
            VariablesScope.class, VariablesScope.values())
        .ignoreCase()
        .addDefaultValue(VariablesScope.SYNTHETIC)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  /**
   * A {@link Feature} that represents unused variables removal optimization.
   */
  @HasKeyId
  @Description("Apply unused variables removal optimization")
  public static class UnusedVariableRemoval implements Feature {
    @Nonnull
    public static final BooleanPropertyId ENABLE = BooleanPropertyId
        .create("jack.optimization.unused-variables-removal",
            "Apply unused variables removal optimization")
        .addDefaultValue(Boolean.TRUE)
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);

    @Nonnull
    public static final EnumPropertyId<VariablesScope> MERGE_VARIABLES = EnumPropertyId
        .create("jack.optimization.unused- variables-removal.merge-vars",
            "Merge variables before unused variables removal",
            VariablesScope.class, VariablesScope.values())
        .ignoreCase()
        .addDefaultValue(VariablesScope.ALL)
        .requiredIf(ENABLE.getValue().isTrue())
        .addCategory(DumpInLibrary.class)
        .addCategory(PrebuiltCompatibility.class)
        .addCategory(Private.class);
  }

  @Nonnull
  public static final BooleanPropertyId ENABLE_NULL_INSTANCEOF =
      BooleanPropertyId.create(
          "jack.optimization.null-instanceof-simplifier", "Optimize null instanceof")
          .addDefaultValue(Boolean.FALSE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId REMOVE_UNUSED_NON_SYNTHETIC_DEFINITION = BooleanPropertyId
      .create("jack.optimization.remove-unused-non-synthetic-definition",
          "Allow to remove unused non synthetic definitions")
      .addDefaultValue(Boolean.FALSE)
      .addCategory(DumpInLibrary.class)
      .addCategory(PrebuiltCompatibility.class);
}
