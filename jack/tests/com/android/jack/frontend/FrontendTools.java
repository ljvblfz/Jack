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

package com.android.jack.frontend;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JProgram;

import junit.framework.Assert;

public class FrontendTools {

  public static JMethod parseMethod(
      String classSignature, String methodSignature, Options options)
      throws Exception {
    JProgram jprogram = TestTools.buildJAst(options);
    Assert.assertNotNull(jprogram);

    JDefinedClassOrInterface type = (JDefinedClassOrInterface) jprogram.getLookup().getType(classSignature);
    Assert.assertNotNull(type);

    JMethod foundMethod = TestTools.getMethod(type, methodSignature);
    Assert.assertNotNull(foundMethod);

    return foundMethod;
  }
}
