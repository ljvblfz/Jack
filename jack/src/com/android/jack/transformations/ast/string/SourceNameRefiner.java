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

package com.android.jack.transformations.ast.string;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.marker.SourceName;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that refines source names of types into more specific
 * charsequences.
 */
@Description("Refines source names of types into more specific charsequences")
@Transform(modify = SourceName.class)
public class SourceNameRefiner implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    SourceName marker = type.getMarker(SourceName.class);
    if (marker != null) {
      TypeName newName = new TypeName(Kind.SIMPLE_NAME, type);
      assert marker.getSourceName().equals(newName.toString());
      marker.setSourceName(newName);
    }
  }

}
