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

package com.android.jack.ir.ast;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A visitor for iterating through an AST that support concurrent modification.
 */
public class JVisitorWithConcurrentModification extends JVisitor {

  @Override
  public <T extends JNode> void accept(@Nonnull ArrayList<T> array) {
    accept((Collection<T>)array);
  }

  @Override
  public <T extends JNode> void accept(@Nonnull Collection<T> collection) {
    // Duplicate the visited collection since commit of transformation request can add items into
    // the input collection.
    ArrayList<T> copiedCollection = new ArrayList<T>(collection);
    for (int i = 0, len = copiedCollection.size(); i < len; ++i) {
      copiedCollection.get(i).traverse(this);
    }
  }

}
