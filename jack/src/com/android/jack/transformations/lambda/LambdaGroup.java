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

package com.android.jack.transformations.lambda;

import com.google.common.collect.Lists;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JLambda;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/**
 * Represents lambda group with information about lambdas represented
 * by this group, capture signature, and implementing group class.
 */
final class LambdaGroup {
  @Nonnull
  private final List<JLambda> lambdas;
  @Nonnull
  private final JDefinedClass groupClass;
  @Nonnull
  private final LambdaCaptureSignature captureSignature;

  LambdaGroup(@Nonnull Map<String, JLambda> lambdas,
      @Nonnull JDefinedClass groupClass, @Nonnull LambdaCaptureSignature captureSignature) {
    this.lambdas = inStableOrder(lambdas);
    this.groupClass = groupClass;
    this.captureSignature = captureSignature;
  }

  @Nonnull
  private static List<JLambda> inStableOrder(@Nonnull Map<String, JLambda> lambdas) {
    return Lists.newArrayList(new TreeMap<>(lambdas).values());
  }

  /** Returns lambda classes in stable order */
  @Nonnull
  List<JLambda> getLambdas() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(lambdas);
  }

  @Nonnull
  JDefinedClass getGroupClass() {
    return groupClass;
  }

  @Nonnull
  LambdaCaptureSignature getCaptureSignature() {
    return captureSignature;
  }
}
