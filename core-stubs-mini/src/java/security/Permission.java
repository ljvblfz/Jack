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

package java.security;

public abstract class Permission implements java.security.Guard, java.io.Serializable {
  public Permission(java.lang.String name) {
    throw new RuntimeException("Stub!");
  }

  public final java.lang.String getName() {
    throw new RuntimeException("Stub!");
  }

  public void checkGuard(java.lang.Object obj) throws java.lang.SecurityException {
    throw new RuntimeException("Stub!");
  }

  public java.security.PermissionCollection newPermissionCollection() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.lang.String getActions();

  public abstract boolean implies(java.security.Permission permission);
}
