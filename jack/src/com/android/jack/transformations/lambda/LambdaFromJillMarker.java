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

import com.android.jack.ir.ast.JLambda;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * This {@link Marker} indicates that a lambda was generated by Jill.
 */
@ValidOn(JLambda.class)
@Description("This marker indicates that a lambda was generated by Jill.")
public class LambdaFromJillMarker implements Marker {

  @Nonnull
  public static final LambdaFromJillMarker INSTANCE = new LambdaFromJillMarker();

  private LambdaFromJillMarker() {
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
