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
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.codec.VFSCodec;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link VFS}.
 */
public class VFSPropertyId extends PropertyId<VFS> implements HasDescription {

  @Nonnull
  public static VFSPropertyId create(@Nonnull String name, @Nonnull String description,
      @Nonnull VFSCodec codec) {
    return new VFSPropertyId(name, description, codec);
  }

  protected VFSPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull StringCodec<VFS> codec) {
    super(name, description, codec);
    withAutoCheck();
  }


  @Override
  @Nonnull
  public VFSPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public VFSPropertyId addDefaultValue (@Nonnull VFS defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public VFSPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public VFSPropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public VFSPropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }

  @Nonnull
  public VFSPropertyId withAutoCheck() {
    setShutdownHook(
        new ShutdownRunnable<VFS>() {
          @Override
          public void run(@Nonnull VFS vfs) {
            if (!vfs.isClosed()) {
              throw new AssertionError(
                  "VFS '"
                      + vfs.getDescription()
                      + "' in "
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
  public VFSPropertyId withoutAutoAction() {
    removeShutdownHook();

    return this;
  }

}
