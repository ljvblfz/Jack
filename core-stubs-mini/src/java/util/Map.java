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

public interface Map<K, V> {
  public static interface Entry<K, V> {
    public abstract boolean equals(java.lang.Object object);

    public abstract K getKey();

    public abstract V getValue();

    public abstract int hashCode();

    public abstract V setValue(V object);
  }

  public abstract void clear();

  public abstract boolean containsKey(java.lang.Object key);

  public abstract boolean containsValue(java.lang.Object value);

  public abstract java.util.Set<java.util.Map.Entry<K, V>> entrySet();

  public abstract boolean equals(java.lang.Object object);

  public abstract V get(java.lang.Object key);

  public abstract int hashCode();

  public abstract boolean isEmpty();

  public abstract java.util.Set<K> keySet();

  public abstract V put(K key, V value);

  public abstract void putAll(java.util.Map<? extends K, ? extends V> map);

  public abstract V remove(java.lang.Object key);

  public abstract int size();

  public abstract java.util.Collection<V> values();
}
