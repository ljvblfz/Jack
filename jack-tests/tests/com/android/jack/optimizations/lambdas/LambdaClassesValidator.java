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

package com.android.jack.optimizations.lambdas;

import com.android.jack.comparator.DifferenceFoundException;
import com.android.jack.test.comparator.ComparatorDiff;
import com.android.jack.test.dex.DexField;
import com.android.jack.test.dex.DexMethod;
import com.android.jack.test.dex.DexType;
import com.android.jack.test.dex.DexValidator;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.transformations.lambda.LambdaCollection;

import junit.framework.Assert;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.Nonnull;

class LambdaClassesValidator extends DexValidator<DexFile> {
  @Nonnull
  private final String expected;

  LambdaClassesValidator(@Nonnull String expected) {
    this.expected = expected;
  }

  private void validateString(@Nonnull String actual) {
    try {
      File actualFile = AbstractTestTools.createTempFile("actual", ".out");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(actualFile))) {
        writer.write(actual.trim());
      }

      File expectedFile = AbstractTestTools.createTempFile("expected", ".out");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(expectedFile))) {
        writer.write(expected.trim());
      }

      new ComparatorDiff(actualFile, expectedFile).compare();

    } catch (DifferenceFoundException e) {
      Assert.fail("Actual text differs from expected, see STDOUT");

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Unexpected exception caught");
    }
  }


  private static void dump(@Nonnull DexType type, @Nonnull StringBuilder sb) {
    String descriptor = type.getTypeId().getTypeDescriptor();
    if (!descriptor.contains(LambdaCollection.LAMBDA_GROUP_CLASS_NAME_PREFIX) &&
        !descriptor.contains("$Lambda$")) {
      return;
    }

    // Name
    sb.append(descriptor).append('\n');

    // Interfaces
    List<String> items = new ArrayList<>(type.getInterfaceNames());
    print(sb, "implements", items);

    // Fields
    items.clear();
    for (DexField field : type.getFields()) {
      items.add(field.getId());
    }
    print(sb, "fields", items);

    // Methods
    items.clear();
    for (DexMethod method : type.getMethods()) {
      items.add(method.getId());
    }
    print(sb, "methods", items);
  }

  private static void print(
      @Nonnull StringBuilder sb, @Nonnull String header, @Nonnull List<String> items) {
    if (items.size() > 0) {
      sb.append("  - ").append(header).append(":\n");
      Collections.sort(items);
      for (String item : items) {
        sb.append("    ").append(item).append('\n');
      }
    }
  }

  @Override
  protected void validateImpl(@Nonnull DexFile dexFile) {
    TreeMap<String, DexType> name2type = new TreeMap<String, DexType>();
    for (ClassDefItem item : dexFile.ClassDefsSection.getItems()) {
      DexType clazz = new DexType(item);
      name2type.put(clazz.getTypeId().getTypeDescriptor(), clazz);
    }

    StringBuilder sb = new StringBuilder();
    for (DexType type : name2type.values()) {
      dump(type, sb);
    }
    String actual = sb.toString();

    validateString(actual);
  }
}
