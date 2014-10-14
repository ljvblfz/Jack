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

package com.android.jack.library;

import com.android.jack.Jack;
import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Jack library used as input.
 */
public class InputJackLibrary implements InputLibrary {

  @Nonnull
  private final InputRootVDir libraryVDir;

  @Nonnull
  private final InputLibraryLocation location;

  private final Set<BinaryKind> binaryKinds = new HashSet<BinaryKind>(1);

  public InputJackLibrary(@Nonnull InputRootVDir libraryVDir) {
    this.libraryVDir = libraryVDir;
    location = new InputLibraryLocation(this);
    fillBinaryKinds(libraryVDir);
  }

  @Override
  @Nonnull
  public InputRootVDir getInputVDir() {
    return libraryVDir;
  }

  @Override
  @Nonnull
  public InputLibraryLocation getLocation() {
    return location;
  }

  @Override
  @Nonnull
  public Collection<BinaryKind> getBinaryKinds() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(binaryKinds);
  }

  @Override
  public boolean hasBinary(@Nonnull BinaryKind binaryKind) {
    return binaryKinds.contains(binaryKind);
  }

  private void fillBinaryKinds(@Nonnull InputVDir vDir) {
    for (InputVElement subFile : vDir.list()) {
      if (subFile.isVDir()) {
        fillBinaryKinds((InputVDir) subFile);
      } else {
        try {
          binaryKinds.add(BinaryKind.getBinaryKind((InputVFile) subFile));
        } catch (NotBinaryException e) {
          // Ok, nothing to do
        }
      }
    }
  }
}
