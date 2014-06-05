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

public abstract class AbstractMap<K, V> implements java.util.Map<K, V> {
  public static class SimpleImmutableEntry<K, V> implements java.util.Map.Entry<K, V>,
      java.io.Serializable {
    public SimpleImmutableEntry(K theKey, V theValue) {
      throw new RuntimeException("Stub!");
    }

    public SimpleImmutableEntry(java.util.Map.Entry<? extends K, ? extends V> copyFrom) {
      throw new RuntimeException("Stub!");
    }

    public K getKey() {
      throw new RuntimeException("Stub!");
    }

    public V getValue() {
      throw new RuntimeException("Stub!");
    }

    public V setValue(V object) {
      throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object object) {
      throw new RuntimeException("Stub!");
    }

    public int hashCode() {
      throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
      throw new RuntimeException("Stub!");
    }
  }
  public static class SimpleEntry<K, V> implements java.util.Map.Entry<K, V>, java.io.Serializable {
    public SimpleEntry(K theKey, V theValue) {
      throw new RuntimeException("Stub!");
    }

    public SimpleEntry(java.util.Map.Entry<? extends K, ? extends V> copyFrom) {
      throw new RuntimeException("Stub!");
    }

    public K getKey() {
      throw new RuntimeException("Stub!");
    }

    public V getValue() {
      throw new RuntimeException("Stub!");
    }

    public V setValue(V object) {
      throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object object) {
      throw new RuntimeException("Stub!");
    }

    public int hashCode() {
      throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
      throw new RuntimeException("Stub!");
    }
  }

  protected AbstractMap() {
    throw new RuntimeException("Stub!");
  }

  public void clear() {
    throw new RuntimeException("Stub!");
  }

  public boolean containsKey(java.lang.Object key) {
    throw new RuntimeException("Stub!");
  }

  public boolean containsValue(java.lang.Object value) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.util.Set<java.util.Map.Entry<K, V>> entrySet();

  public boolean equals(java.lang.Object object) {
    throw new RuntimeException("Stub!");
  }

  public V get(java.lang.Object key) {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public boolean isEmpty() {
    throw new RuntimeException("Stub!");
  }

  public java.util.Set<K> keySet() {
    throw new RuntimeException("Stub!");
  }

  public V put(K key, V value) {
    throw new RuntimeException("Stub!");
  }

  public void putAll(java.util.Map<? extends K, ? extends V> map) {
    throw new RuntimeException("Stub!");
  }

  public V remove(java.lang.Object key) {
    throw new RuntimeException("Stub!");
  }

  public int size() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public java.util.Collection<V> values() {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"unchecked"})
  protected java.lang.Object clone() throws java.lang.CloneNotSupportedException {
    throw new RuntimeException("Stub!");
  }
}
