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

package com.android.jack.scheduling.filter;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.sched.item.Description;
import com.android.sched.schedulable.ComponentFilter;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * A filter that only accepts types without no corresponding prebuilt code. These are either types
 * from the source code or types from libraries without prebuilt for this type.
 */
@Description("Filter accepting types without prebuilt")
public class TypeWithoutPrebuiltFilter implements ComponentFilter<JDefinedClassOrInterface> {

  @Override
  public boolean accept(@Nonnull JDefinedClassOrInterface clOrI) {
    Location location = clOrI.getLocation();
    if (location instanceof TypeInInputLibraryLocation) {
      InputLibrary inputLibrary = ((TypeInInputLibraryLocation) location).getInputLibrary();
      if (inputLibrary.containsFileType(FileType.PREBUILT)) {
        return false;
      }
    }
    return true;
  }

}

