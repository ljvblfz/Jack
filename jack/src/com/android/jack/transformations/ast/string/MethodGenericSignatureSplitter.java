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

import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.marker.OriginalTypeInfo;
import com.android.jack.signature.GenericSignatureParser;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/**
 * Split method generic signature of {@code OriginalTypeInfo} into more specific string literals.
 */
@Description("Split method generic signature into more specific string literals.")
@Name("MethodGenericSignatureSplitter")
@Constraint(need = {OriginalTypeInfo.class})
@Use(GenericSignatureRefiner.class)
public class MethodGenericSignatureSplitter implements RunnableSchedulable<JMethod> {

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    OriginalTypeInfo marker = method.getMarker(OriginalTypeInfo.class);
    if (marker != null) {
      JAbstractStringLiteral oldSignature = marker.getGenericSignature();
      if (oldSignature != null) {
        GenericSignatureRefiner parserActions = new GenericSignatureRefiner();
        GenericSignatureParser parser = new GenericSignatureParser(parserActions);
        String strOldSignature = oldSignature.getValue();
        parser.parseMethodSignature(strOldSignature);
        assert parserActions.getNewSignature().getValue().equals(strOldSignature);
        marker.setGenericSignature(parserActions.getNewSignature());
      }
    }
  }

}
