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

package com.android.jack.transformations.ast.string;

import com.android.jack.signature.GenericSignatureAction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Dummy actions for testing purposes.
 *
 * It only concatenates the parsed strings.
 */
public class DummyAction implements GenericSignatureAction<Object> {

  @Nonnull
  private StringBuilder strBuf = new StringBuilder();

  @Override
  public void parsedSymbol(char symbol) {
    strBuf.append(symbol);
  }

  @Override
  public void parsedIdentifier(@Nonnull String identifier) {
    strBuf.append(identifier);
  }

  @Override
  @CheckForNull
  public Object parsedTypeName(@Nonnull String name) {
    strBuf.append(name);
    return null;
  }

  @Override
  @CheckForNull
  public Object parsedInnerTypeName(@CheckForNull Object enclosingTypeName, @Nonnull String name) {
    strBuf.append(name);
    return null;
  }

  @Override
  public void start() {
    strBuf = new StringBuilder();
  }

  @Override
  public void stop() {
  }

  @Nonnull
  public String getNewSignature() {
    return strBuf.toString();
  }

}
