/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.annotation;

import com.android.jack.backend.dex.annotations.tag.ReflectAnnotations;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.marker.OriginalTypeInfo;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes signatures from types.
 */
@Description("Removes signatures from types")
@Constraint(no = ReflectAnnotations.class)
@Transform(modify = OriginalTypeInfo.class)
@Support(RemoveGenericSignature.class)
public class TypeGenericSignatureRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OriginalTypeInfo info = type.getMarker(OriginalTypeInfo.class);
    if (info != null) {
      info.setGenericSignature(null);
    }
  }

}
