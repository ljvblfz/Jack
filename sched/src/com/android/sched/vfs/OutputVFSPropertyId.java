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

package com.android.sched.vfs;


import com.android.sched.util.HasDescription;
import com.android.sched.util.codec.OutputVFSCodec;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link OutputVFS}.
 */
public class OutputVFSPropertyId extends PropertyId<OutputVFS> implements HasDescription {

  @Nonnull
  public static OutputVFSPropertyId create(
      @Nonnull String name, @Nonnull String description, @Nonnull OutputVFSCodec codec) {
    return new OutputVFSPropertyId(name, description, codec);
  }

  protected OutputVFSPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull StringCodec<OutputVFS> codec) {
    super(name, description, codec);
    withAutoCheck();
  }


  @Override
  @Nonnull
  public OutputVFSPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public OutputVFSPropertyId addDefaultValue (@Nonnull OutputVFS defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public OutputVFSPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public OutputVFSPropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public OutputVFSPropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }

  @Nonnull
  public OutputVFSPropertyId withAutoCheck() {
    setShutdownHook(
        new ShutdownRunnable<OutputVFS>() {
          @Override
          public void run(@Nonnull OutputVFS vfs) {
            if (!vfs.isClosed()) {
              throw new AssertionError(
                  "OutputVFS in "
                      + vfs.getLocation().getDescription()
                      + " from property '"
                      + getName()
                      + "' is not closed");
            }
          }
        });

    return this;
  }

  @Nonnull
  public OutputVFSPropertyId withoutAutoAction() {
    removeShutdownHook();

    return this;
  }

}
