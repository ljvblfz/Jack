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

package com.android.jack.digest;

import com.android.jack.util.TextOutput;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link TextOutput} which computes a digest of the textual representation
 */
public class DigestOutput implements TextOutput {
  @Nonnull
  private final MessageDigest digest;

  protected DigestOutput(@Nonnull MessageDigest digest) {
    this.digest = digest;
  }

  @Nonnull
  public MessageDigest getMessageDigest() {
    return digest;
  }

  @Override
  public void indentIn() {
  }

  @Override
  public void indentOut() {
  }

  @Override
  public void newline() {
  }

  @Override
  public void newlineOpt() {
  }

  @Override
  public void print(char c) {
    digest.update((byte) (c >> 8));
    digest.update((byte) (c & 0xFF));
  }

  @Override
  public void print(@Nonnull char[] s) {
    digest.update(new String(s).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void print(@Nonnull String s) {
    digest.update(s.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void printOpt(char c) {
  }

  @Override
  public void printOpt(@Nonnull char[] s) {
  }

  @Override
  public void printOpt(@Nonnull String s) {
  }

  @Override
  public int getPosition() {
    throw new AssertionError();
  }
}
