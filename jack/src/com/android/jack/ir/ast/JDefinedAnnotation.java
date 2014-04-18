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

package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.sched.item.Description;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java annotation type definition
 */
@Description("Java annotation type definition")
public class JDefinedAnnotation extends JDefinedInterface implements JAnnotation {

  @CheckForNull
  private JRetentionPolicy retentionPolicy;

  public JDefinedAnnotation(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
      @Nonnull JPackage enclosingPackage,
      @Nonnull ClassOrInterfaceLoader loader) {
    super(info, name, modifier, enclosingPackage, loader);
  }

  public void setRetentionPolicy(@Nonnull JRetentionPolicy retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

  @Nonnull
  public JRetentionPolicy getRetentionPolicy() {
    loader.ensureRetentionPolicy(this);
    assert retentionPolicy != null;
    return retentionPolicy;
  }
}
