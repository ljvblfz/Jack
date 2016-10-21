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

package com.android.jack.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

class AclFileAccess extends FileAccess {

  @Nonnull
  private final AclFileAttributeView view;

  public AclFileAccess(@Nonnull Path path, @Nonnull AclFileAttributeView aclView) {
    super(path);
    this.view = aclView;
  }

  @Override
  public void removeAccessRightButOwner() throws IOException {
    UserPrincipal owner = view.getOwner();
    List<AclEntry> keptAcl = new ArrayList<>(1);
    // Tests showed that getAcl is returning all ACEs: inherited ACEs and explicit ACEs.
    for (AclEntry acl : view.getAcl()) {
      if (acl.principal().equals(owner)) {
        keptAcl.add(acl);
      }
    }
    // Even if not explicitly stated in the doc, tests showed that setAcl is removing ACE
    // inheritance from parent.
    view.setAcl(keptAcl);
  }

  @Override
  public void checkAccessibleOnlyByOwner() throws IOException {
    UserPrincipal owner = view.getOwner();
    // Tests showed that getAcl is returning all ACEs: inherited ACEs and explicit ACEs.
    for (AclEntry acl : view.getAcl()) {
      if (acl.type() == AclEntryType.ALLOW && !acl.principal().equals(owner)) {
        throw new IOException("'" + getPath().toString() + "' is allowed to be accessed by '"
            + acl.principal().getName() + "' while it should only be accessible by owner '"
            + owner.getName() + "'");
      }
    }
  }

  @Override
  @Nonnull
  public UserPrincipal getOwner() throws IOException {
    return view.getOwner();
  }

}
