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

package java.lang;

public abstract class Enum<E extends java.lang.Enum<E>> implements java.io.Serializable,
    java.lang.Comparable<E> {
  protected Enum(java.lang.String name, int ordinal) {
    throw new RuntimeException("Stub!");
  }

  public final java.lang.String name() {
    throw new RuntimeException("Stub!");
  }

  public final int ordinal() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public final boolean equals(java.lang.Object other) {
    throw new RuntimeException("Stub!");
  }

  public final int hashCode() {
    throw new RuntimeException("Stub!");
  }

  protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException {
    throw new RuntimeException("Stub!");
  }

  public final int compareTo(E o) {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public final java.lang.Class<E> getDeclaringClass() {
    throw new RuntimeException("Stub!");
  }

  public static <T extends java.lang.Enum<T>> T valueOf(java.lang.Class<T> enumType,
      java.lang.String name) {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"FinalizeDoesntCallSuperFinalize"})
  protected final void finalize() {
    throw new RuntimeException("Stub!");
  }
}
