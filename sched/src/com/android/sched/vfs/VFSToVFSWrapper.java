/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.ByteStreamSucker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A double {@link VFS} wrapper that uses a work {@link VFS} during its life, and dumps it contents
 * into a final {@link VFS} when it is closed.
 */
public class VFSToVFSWrapper implements VFS {

  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> workVFS;
  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> finalVFS;
  @Nonnull
  private final Set<Capabilities> capabilities;

  private boolean closed = false;

  @SuppressWarnings("unchecked")
  public VFSToVFSWrapper(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> workVFS,
      @Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> finalVFS) {
    this.workVFS = (BaseVFS<BaseVDir, BaseVFile>) workVFS;
    this.finalVFS = (BaseVFS<BaseVDir, BaseVFile>) finalVFS;

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    for (Capabilities capability : workVFS.getCapabilities()) {
      switch (capability) {
        case CASE_SENSITIVE:
          if (finalVFS.getCapabilities().contains(capability)) {
            // both VFSes need to support case sensitivity for this wrapper to support it
            capabilities.add(capability);
          }
          break;
        case DIGEST:
          capabilities.add(capability);
          break;
        case PARALLEL_READ:
          capabilities.add(capability);
          break;
        case PARALLEL_WRITE:
          capabilities.add(capability);
          break;
        case READ:
          capabilities.add(capability);
          break;
        case UNIQUE_ELEMENT:
          capabilities.add(capability);
          break;
        case WRITE:
          capabilities.add(capability);
          break;
        default:
          throw new AssertionError();
      }
    }
    this.capabilities = Collections.unmodifiableSet(capabilities);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return finalVFS.getLocation();
  }

  @Override
  public void close() throws CannotCreateFileException, WrongPermissionException, IOException {
    if (!closed) {
      dumpToDir(getRootDir(), finalVFS.getRootDir());
      finalVFS.close();
      workVFS.close();
      closed = true;
    }
  }

  private void dumpToDir(VDir srcRootDir, VDir destRootDir) throws CannotCreateFileException,
      WrongPermissionException, IOException {
    for (VElement element : srcRootDir.list()) {
      String elementName = element.getName();
      if (element.isVDir()) {
        VDir dir = destRootDir.createVDir(elementName);
        dumpToDir((VDir) element, dir);
      } else {
        VFile file = destRootDir.createVFile(elementName);
        InputStream is = null;
        OutputStream os = null;
        try {
          is = ((VFile) element).openRead();
          os = file.openWrite();
          ByteStreamSucker sucker = new ByteStreamSucker(is, os);
          sucker.suck();
        } finally {
          if (is != null) {
            is.close();
          }
          if (os != null) {
            os.close();
          }
        }
      }
    }

  }

  @Override
  @Nonnull
  public String getDescription() {
    return "a VFS-to-VFS wrapper";
  }

  @Override
  @Nonnull
  public String getPath() {
    return finalVFS.getPath();
  }

  @Override
  @Nonnull
  public VDir getRootDir() {
    return workVFS.getRootDir();
  }

  @Override
  public boolean needsSequentialWriting() {
    return workVFS.needsSequentialWriting();
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return this.capabilities;
  }

}
