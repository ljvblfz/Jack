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

package java.util.concurrent.atomic;

public abstract class AtomicIntegerFieldUpdater<T> {
  protected AtomicIntegerFieldUpdater() {
    throw new RuntimeException("Stub!");
  }

  public static <U> java.util.concurrent.atomic.AtomicIntegerFieldUpdater<U> newUpdater(
      java.lang.Class<U> tclass, java.lang.String fieldName) {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean compareAndSet(T obj, int expect, int update);

  public abstract boolean weakCompareAndSet(T obj, int expect, int update);

  public abstract void set(T obj, int newValue);

  public abstract void lazySet(T obj, int newValue);

  public abstract int get(T obj);

  public int getAndSet(T obj, int newValue) {
    throw new RuntimeException("Stub!");
  }

  public int getAndIncrement(T obj) {
    throw new RuntimeException("Stub!");
  }

  public int getAndDecrement(T obj) {
    throw new RuntimeException("Stub!");
  }

  public int getAndAdd(T obj, int delta) {
    throw new RuntimeException("Stub!");
  }

  public int incrementAndGet(T obj) {
    throw new RuntimeException("Stub!");
  }

  public int decrementAndGet(T obj) {
    throw new RuntimeException("Stub!");
  }

  public int addAndGet(T obj, int delta) {
    throw new RuntimeException("Stub!");
  }
}
