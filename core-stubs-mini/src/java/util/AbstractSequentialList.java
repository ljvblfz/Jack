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

package java.util;

public abstract class AbstractSequentialList<E> extends java.util.AbstractList<E> {
  protected AbstractSequentialList() {
    throw new RuntimeException("Stub!");
  }

  public void add(int location, E object) {
    throw new RuntimeException("Stub!");
  }

  public boolean addAll(int location, java.util.Collection<? extends E> collection) {
    throw new RuntimeException("Stub!");
  }

  public E get(int location) {
    throw new RuntimeException("Stub!");
  }

  public java.util.Iterator<E> iterator() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.util.ListIterator<E> listIterator(int location);

  public E remove(int location) {
    throw new RuntimeException("Stub!");
  }

  public E set(int location, E object) {
    throw new RuntimeException("Stub!");
  }
}
