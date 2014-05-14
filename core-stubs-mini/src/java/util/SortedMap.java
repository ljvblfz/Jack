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

public interface SortedMap<K, V> extends java.util.Map<K, V> {
  public abstract java.util.Comparator<? super K> comparator();

  public abstract K firstKey();

  public abstract java.util.SortedMap<K, V> headMap(K endKey);

  public abstract K lastKey();

  public abstract java.util.SortedMap<K, V> subMap(K startKey, K endKey);

  public abstract java.util.SortedMap<K, V> tailMap(K startKey);
}
