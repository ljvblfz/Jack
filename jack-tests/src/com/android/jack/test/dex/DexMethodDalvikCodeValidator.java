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

package com.android.jack.test.dex;

import com.android.jack.comparator.DifferenceFoundException;
import com.android.jack.test.comparator.ComparatorDiff;
import com.android.jack.test.toolchain.AbstractTestTools;

import junit.framework.Assert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.annotation.Nonnull;

/** Method validator checking method's dex */
public class DexMethodDalvikCodeValidator extends DexValidator<DexMethod> {
  @Nonnull
  private final File expected;

  public DexMethodDalvikCodeValidator(@Nonnull File expected) {
    this.expected = expected;
  }

  @Override
  protected void validateImpl(@Nonnull DexMethod method) {
    try {
      File actual = AbstractTestTools.createTempFile("method-actual", ".dalvik");
      BufferedWriter writer = new BufferedWriter(new FileWriter(actual));
      writer.write(method.getSource());
      writer.close();

      new ComparatorDiff(actual, expected).compare();

    } catch (DifferenceFoundException e) {
      Assert.fail("Method body IR does not match");

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Unexpected exception caught");
    }
  }
}
