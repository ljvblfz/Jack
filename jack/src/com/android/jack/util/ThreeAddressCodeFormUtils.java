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

package com.android.jack.util;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.schedulable.Constraint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tools related to the three address code form.
 */
@Constraint(need = ThreeAddressCodeForm.class)
public class ThreeAddressCodeFormUtils {

  @CheckForNull
  public static DefinitionMarker getDefinitionMarker(@Nonnull JStatement stmt) {
    JExpression asg;
    if (stmt instanceof JExpressionStatement
        && ((asg = ((JExpressionStatement) stmt).getExpr()) instanceof JAsgOperation)) {
      return (asg.getMarker(DefinitionMarker.class));
    }

    return null;
  }

}
