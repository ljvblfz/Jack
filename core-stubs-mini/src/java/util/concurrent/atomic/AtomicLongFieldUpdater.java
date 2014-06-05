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

public abstract class AtomicLongFieldUpdater<T> {
  protected AtomicLongFieldUpdater() {
    throw new RuntimeException("Stub!");
  }

  public static <U> java.util.concurrent.atomic.AtomicLongFieldUpdater<U> newUpdater(
      java.lang.Class<U> tclass, java.lang.String fieldName) {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean compareAndSet(T obj, long expect, long update);

  public abstract boolean weakCompareAndSet(T obj, long expect, long update);

  public abstract void set(T obj, long newValue);

  public abstract void lazySet(T obj, long newValue);

  public abstract long get(T obj);

  public long getAndSet(T obj, long newValue) {
    throw new RuntimeException("Stub!");
  }

  public long getAndIncrement(T obj) {
    throw new RuntimeException("Stub!");
  }

  public long getAndDecrement(T obj) {
    throw new RuntimeException("Stub!");
  }

  public long getAndAdd(T obj, long delta) {
    throw new RuntimeException("Stub!");
  }

  public long incrementAndGet(T obj) {
    throw new RuntimeException("Stub!");
  }

  public long decrementAndGet(T obj) {
    throw new RuntimeException("Stub!");
  }

  public long addAndGet(T obj, long delta) {
    throw new RuntimeException("Stub!");
  }
}
