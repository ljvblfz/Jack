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

package com.android.jack.analysis.dependency.file;

import com.google.common.io.LineReader;

import com.android.jack.Jack;
import com.android.jack.analysis.dependency.Dependency;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing bidirectional dependencies between java files and their types.
 */
public class FileDependencies extends Dependency {

  @Nonnull
  public static final VPath vpath = new VPath("files", '/');

  /**
   * This tag means that {@link FileDependencies} are collected.
   */
  @Description("File dependencies are collected")
  @Name("FileDependencies.Collected")
  public static final class Collected implements Tag{
  }

  @Nonnull
  private Map<String, Set<String>> javaFileToTypes = new HashMap<String, Set<String>>();

  public void addMappingBetweenJavaFileAndType(@Nonnull String javaFileName,
      @Nonnull JType type) {
    String typeFqn = BinaryQualifiedNameFormatter.getFormatter().getName(type);
    Set<String> types = javaFileToTypes.get(javaFileName);

    if (types == null) {
      types = new HashSet<String>();
      javaFileToTypes.put(javaFileName, types);
    }

    types.add(typeFqn);
  }

  public void write(@Nonnull PrintStream ps) {
    writeMapOne2Many(ps, javaFileToTypes);
    ps.print(Dependency.MAP_SEPARATOR);
    ps.println();
  }

  @Nonnull
  public void read(@Nonnull Readable reader) throws IOException {
    javaFileToTypes = readMapOne2Many(new LineReader(reader));
  }

  @Nonnull
  public Set<String> getTypeNames(@Nonnull String javaFileName) {
    Set<String> typeNames = javaFileToTypes.get(javaFileName);
    if (typeNames == null) {
      typeNames = Collections.emptySet();
    }
    return Jack.getUnmodifiableCollections().getUnmodifiableSet(typeNames);
  }

  @CheckForNull
  public String getJavaFileName(@Nonnull String typeName) {
    for (Map.Entry<String, Set<String>> entry : javaFileToTypes.entrySet()) {
      if (entry.getValue().contains(typeName)) {
        return entry.getKey();
      }
    }

    return null;
  }

  @Nonnull
  public Set<String> getCompiledJavaFiles() {
    return Jack.getUnmodifiableCollections().getUnmodifiableSet(javaFileToTypes.keySet());
  }
}
