/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.signature.GenericSignatureParser;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/**
 * Split field generic signature of {@code OriginalTypeInfo} into more specific string literals.
 */
@Description("Split field generic signature into more specific string literals.")
@Name("FieldGenericSignatureSplitter")
@Transform(modify = GenericSignature.class)
@Use(GenericSignatureRefiner.class)
public class FieldGenericSignatureSplitter implements RunnableSchedulable<JField> {

  @Override
  public void run(@Nonnull JField field) throws Exception {
    GenericSignature marker = field.getMarker(GenericSignature.class);
    if (marker != null) {
      String oldSignature = marker.getGenericSignature();
      GenericSignatureRefiner parserActions = new GenericSignatureRefiner();
      GenericSignatureParser<JType> parser = new GenericSignatureParser<JType>(parserActions);
      parser.parseFieldSignature(oldSignature);
      assert parserActions.getNewSignature().toString().equals(oldSignature);
      marker.setGenericSignature(parserActions.getNewSignature());
    }
  }

}
