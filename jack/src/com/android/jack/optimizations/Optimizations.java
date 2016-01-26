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
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * {@link Feature} and {@link PropertyId} related to optimizations.
 */
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
}
