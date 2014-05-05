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

package com.android.jack.jayce.v0002.nodes;

import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.lookup.JMethodLookupException;

import javax.annotation.Nonnull;

/**
 * Base class for any storage location.
 */
public abstract class NVariable extends NNode implements HasSourceInfo {

  @Override
  @Nonnull
  public abstract JVariable exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException;

}
