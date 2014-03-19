/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations;

import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JStatement;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A marker which contains the initialization expression of a field.
 */
@Description("A marker which contains the initialization expression of a field.")
@ValidOn(JField.class)
public class InitializationExpression implements Marker {

  @Nonnull
  private final JStatement statement;

  public InitializationExpression(@Nonnull JStatement statement) {
    this.statement = statement;
  }

  @Nonnull
  public JStatement getStatement() {
    return statement;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError("Not yet supported");
  }

}
