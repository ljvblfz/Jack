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

package com.android.jack.shrob.obfuscation.remover;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.shrob.obfuscation.KeepNameMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@code Schedulable} that removes {@code KeepNameMarker} from types.
 */
@Description("Removes KeepNameMarker from types.")
@Transform(remove = KeepNameMarker.class)
public class TypeKeepNameMarkerRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    type.removeMarker(KeepNameMarker.class);
  }

}
