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

package com.android.jack.jayce;

import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

@ImplementationName(iface = JaycePackageLoader.class, name = "full")
class FullPackageLoader extends JaycePackageLoader {

  public FullPackageLoader(@Nonnull InputJackLibrary inputJackLibrary,
      @Nonnull JPhantomLookup lookup) throws FileTypeDoesNotExistException {
    super(inputJackLibrary, inputJackLibrary.getDir(FileType.JAYCE, VPath.ROOT), lookup,
        NodeLevel.FULL);
  }
}
