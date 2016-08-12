/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.backend.dex;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.scheduling.filter.TypeWithValidMethodPrebuilt;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.scheduling.marker.ImportedDexClassMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A schedulable that removes {@link ImportedDexClassMarker} from a {@code JDefinedClassOrInterface}
 */
@Description("Removes useless ImportedDexMethodMarker")
@Constraint(need = ImportedDexClassMarker.class)
@Transform(remove = ImportedDexClassMarker.class)
@Filter({TypeWithValidMethodPrebuilt.class, TypeWithoutValidTypePrebuilt.class})
public class ImportedDexClassMarkerRemover
  implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface jdcoi) {
    jdcoi.removeMarker(ImportedDexClassMarker.class);
  }
}
