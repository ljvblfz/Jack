/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.dexcomparator.test;

import com.android.jack.util.ExecuteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class TestTools {

  @Nonnull
  private static final File jackJar = new File("../jack/dist/jack.jar");
  @Nonnull
  private static final File coreStubsMini = new File("../jack/libs/core-stubs-mini.jar");

  public static void compileToDexWithJack(@Nonnull List<File> sources, @Nonnull File dex) {
    int size = 8 + sources.size();
    List<String> argList = new ArrayList<String>(size);
    argList.add("java");
    argList.add("-jar");
    argList.add(jackJar.getAbsolutePath());
    argList.add("-cp");
    argList.add(coreStubsMini.getAbsolutePath());
    argList.add("-o");
    argList.add(dex.getAbsolutePath());
    argList.add("--ecj");
    for (File source : sources) {
      argList.add(source.getAbsolutePath());
    }

    ExecuteFile execFile = new ExecuteFile(argList.toArray(new String[size]));
    if (!execFile.run()) {
      throw new RuntimeException("Jack exited with an error");
    }
  }

  public static void compileToDexWithJack(@Nonnull File source, @Nonnull File dex) {
    compileToDexWithJack(Collections.singletonList(source), dex);
  }

}
