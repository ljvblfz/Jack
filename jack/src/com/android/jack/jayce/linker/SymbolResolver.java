/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.jayce.linker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Tool for resolving symbols.
 * @param <T>
 */
public class SymbolResolver<T> {

  @Nonnull
  private final Map<String, List<Linker<T>>> pendingSymbols =
    new HashMap<String, List<Linker<T>>>();

  @Nonnull
  private final Map<String, T> resolvedSymbols = new HashMap<String, T>();

  public void addLink(@Nonnull String symbol, @Nonnull Linker<T> link) {
    T resolved = resolvedSymbols.get(symbol);
    if (resolved != null) {
      link.link(resolved);
    } else {
      List<Linker<T>> linkList = pendingSymbols.get(symbol);
      if (linkList == null) {
        linkList = new LinkedList<Linker<T>>();
        pendingSymbols.put(symbol, linkList);
      }
      linkList.add(link);
    }
  }

  public void addTarget(@Nonnull String symbol, @Nonnull T target) {
    assert !resolvedSymbols.containsKey(symbol);
    resolvedSymbols.put(symbol, target);
    List<Linker<T>> pendings = pendingSymbols.remove(symbol);
    if (pendings != null) {
      for (Linker<T> link : pendings) {
        link.link(target);
      }
    }
  }

  public void clear() {
    assert pendingSymbols.isEmpty();
    resolvedSymbols.clear();
  }

}
