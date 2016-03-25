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

package com.android.jack.backend.jayce;

import com.android.jack.ir.JackFormatIr;
import com.android.jack.ir.NonJackFormatIr;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;

/**
 * A {@link JayceInLibraryWriter} processing all types.
 */
@Description("Writer of Jayce files in a library organized according to package names")
@Constraint(need = {JackFormatIr.class}, no = {NonJackFormatIr.class})
@Produce(JayceInLibraryProduct.class)
public class JayceInLibraryWriterAll extends JayceInLibraryWriter {
}

