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

public abstract class AbstractCollection<E> implements java.util.Collection<E> {
  protected AbstractCollection() {
    throw new RuntimeException("Stub!");
  }

  public boolean add(E object) {
    throw new RuntimeException("Stub!");
  }

  public boolean addAll(java.util.Collection<? extends E> collection) {
    throw new RuntimeException("Stub!");
  }

  public void clear() {
    throw new RuntimeException("Stub!");
  }

  public boolean contains(java.lang.Object object) {
    throw new RuntimeException("Stub!");
  }

  public boolean containsAll(java.util.Collection<?> collection) {
    throw new RuntimeException("Stub!");
  }

  public boolean isEmpty() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.util.Iterator<E> iterator();

  public boolean remove(java.lang.Object object) {
    throw new RuntimeException("Stub!");
  }

  public boolean removeAll(java.util.Collection<?> collection) {
    throw new RuntimeException("Stub!");
  }

  public boolean retainAll(java.util.Collection<?> collection) {
    throw new RuntimeException("Stub!");
  }

  public abstract int size();

  public java.lang.Object[] toArray() {
    throw new RuntimeException("Stub!");
  }

  public <T> T[] toArray(T[] contents) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }
}
