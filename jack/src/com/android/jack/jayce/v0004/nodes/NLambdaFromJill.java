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

package com.android.jack.jayce.v0004.nodes;

import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.transformations.lambda.LambdaFromJillMarker;

import javax.annotation.Nonnull;


/**
 * This {@link NMarker} means that the lambda was generated by Jill.
 */
public class NLambdaFromJill extends NMarker {

  @Nonnull
  public static final Token TOKEN = Token.LAMBDA_FROM_JILL;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    assert node instanceof LambdaFromJillMarker;
  }

  @Override
  @Nonnull
  public LambdaFromJillMarker exportAsJast(@Nonnull ExportSession exportSession) {
    return LambdaFromJillMarker.INSTANCE;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) {
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) {
  }

  @SuppressWarnings("unused")
  public static void skipContent(@Nonnull JayceInternalReaderImpl in) {
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
