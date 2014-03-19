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
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Symbol manager.
 * @param <T>
 */
public class SymbolManager<T> {

  @Nonnull
  private final Map<T, String> ids = new HashMap<T, String>();

  private int nextId;

  public SymbolManager() {
  }

  @Nonnull
  public String getId(T node) {
    String id = ids.get(node);
    if (id == null) {
      id = Integer.toString(nextId++);
      ids.put(node, id);
    }

    return id;
  }
}
