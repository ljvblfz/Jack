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

package com.android.jack.ir;

import com.android.jack.ir.ast.JAsgAddOperation;
import com.android.jack.ir.ast.JAsgBitAndOperation;
import com.android.jack.ir.ast.JAsgBitOrOperation;
import com.android.jack.ir.ast.JAsgBitXorOperation;
import com.android.jack.ir.ast.JAsgConcatOperation;
import com.android.jack.ir.ast.JAsgDivOperation;
import com.android.jack.ir.ast.JAsgModOperation;
import com.android.jack.ir.ast.JAsgMulOperation;
import com.android.jack.ir.ast.JAsgShlOperation;
import com.android.jack.ir.ast.JAsgShrOperation;
import com.android.jack.ir.ast.JAsgShruOperation;
import com.android.jack.ir.ast.JAsgSubOperation;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;

/**
 * Represents binary assignment operators compound with another operator.
 */
@ComposedOf({JAsgAddOperation.class, JAsgBitAndOperation.class, JAsgBitOrOperation.class,
  JAsgBitXorOperation.class, JAsgDivOperation.class, JAsgMulOperation.class,
  JAsgModOperation.class, JAsgSubOperation.class, JAsgShlOperation.class,
  JAsgShrOperation.class, JAsgShruOperation.class, JAsgConcatOperation.class})
@Description("CompoundAssignment represents binary assignment operators compound with another" +
    " operator.")
public class CompoundAssignment implements AbstractComponent {
}
