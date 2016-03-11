/*
 * Copyright (C) 2016 The Android Open Source Project
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

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

import javax.annotation.Nonnull;

/**
 * A trampoline class for calling package or protected methods of {@link NameReference}.
 */
public class NameReferenceCaller {

  public static void checkEffectiveFinality(@Nonnull NameReference nameReference,
      @Nonnull LocalVariableBinding localBinding,
      @Nonnull Scope scope) {
    nameReference.checkEffectiveFinality(localBinding, scope);
  }
}
