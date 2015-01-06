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

package com.android.jack.analysis.dependency.type;

import com.google.common.io.LineReader;

import com.android.jack.analysis.dependency.Dependency;
import com.android.jack.analysis.dependency.file.FileDependencies;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing dependencies between types. Dependencies are categorized into 3 categories:
 * - hierarchy dependency represents inheritance and implementation
 * - constant dependency represents constant usage dependency
 * - code dependency represents type used by code
 */
public class TypeDependencies extends Dependency {

  @Nonnull
  public static final VPath vpath = new VPath("types", '/');

  /**
   * This tag means that {@link TypeDependencies} are collected.
   */
  @Description("Type dependencies are collected")
  @Name("TypeDependencies.Collected")
  public static final class Collected implements Tag{
  }

  @Nonnull
  private Map<String, Set<String>> codeDependencies = new HashMap<String, Set<String>>();

  @Nonnull
  private Map<String, Set<String>> hierarchyDependencies = new HashMap<String, Set<String>>();

  @Nonnull
  private Map<String, Set<String>> constantDependencies = new HashMap<String, Set<String>>();

  public void addHierarchyDependency(@Nonnull JType depender, @Nonnull JType dependee) {
    addDependency(hierarchyDependencies, depender, dependee);
  }

  public void addConstantDependency(@Nonnull JType depender, @Nonnull JType dependee) {
    addDependency(constantDependencies, depender, dependee);
  }

  public void addCodeDependency(@Nonnull JType depender, @Nonnull JType dependee) {
    addDependency(codeDependencies, depender, dependee);
  }

  public void write(@Nonnull PrintStream ps) {
    writeMapOne2Many(ps, hierarchyDependencies);
    ps.print(Dependency.END_OF_MAP);
    ps.println();
    writeMapOne2Many(ps, constantDependencies);
    ps.print(Dependency.END_OF_MAP);
    ps.println();
    writeMapOne2Many(ps, codeDependencies);
    ps.print(Dependency.END_OF_MAP);
    ps.println();
  }

  @Nonnull
  public Map<String, Set<String>> getRecompileDependencies() {
    Map<String, Set<String>> recompileDependencies = new HashMap<String, Set<String>>();

    Set<String> allKeys = new HashSet<String>(codeDependencies.keySet());
    allKeys.addAll(constantDependencies.keySet());
    allKeys.addAll(hierarchyDependencies.keySet());

    for (String typeToRecompile : allKeys) {
      Set<String> typesToRecompile = recompileDependencies.get(typeToRecompile);
      if (typesToRecompile == null) {
        typesToRecompile = new HashSet<String>();
        recompileDependencies.put(typeToRecompile, typesToRecompile);
      }
      computeCodeRecompileDependencies(recompileDependencies, codeDependencies.get(typeToRecompile),
          typeToRecompile);
      computeConstantRecompileDependencies(recompileDependencies,
          constantDependencies.get(typeToRecompile), typeToRecompile, new HashSet<String>());
      computeHierarchyRecompileDependencies(recompileDependencies,
          hierarchyDependencies.get(typeToRecompile), typeToRecompile);
    }

    return recompileDependencies;
  }

  private void addDependency(@Nonnull Map<String, Set<String>> typeDependencies,
      @Nonnull JType depender, @Nonnull JType dependee) {
    String typeFqn = BinaryQualifiedNameFormatter.getFormatter().getName(depender);
    String dependsOnTypeFqn = BinaryQualifiedNameFormatter.getFormatter().getName(dependee);

    if (!typeFqn.equals(dependsOnTypeFqn)) {
      Set<String> dependencies = typeDependencies.get(typeFqn);

      if (dependencies == null) {
        dependencies = new HashSet<String>();
        typeDependencies.put(typeFqn, dependencies);
      }

      dependencies.add(dependsOnTypeFqn);
    }
  }

  private void computeCodeRecompileDependencies(
      @Nonnull Map<String, Set<String>> recompileDependencies,
      @CheckForNull Set<String> codeDependencies, @Nonnull String typeToRecompile) {
    if (codeDependencies != null) {
      for (String codeDependency : codeDependencies) {
        Set<String> typesToRecompile = recompileDependencies.get(codeDependency);
        if (typesToRecompile == null) {
          typesToRecompile = new HashSet<String>();
          recompileDependencies.put(codeDependency, typesToRecompile);
        }
        typesToRecompile.add(typeToRecompile);

        Set<String> hierarchyDeps = hierarchyDependencies.get(codeDependency);
        if (hierarchyDeps != null) {
          computeCodeRecompileDependencies(recompileDependencies, hierarchyDeps, typeToRecompile);
        }
      }
    }
  }

  private void computeConstantRecompileDependencies(
      @Nonnull Map<String, Set<String>> recompileDependencies,
      @CheckForNull Set<String> constantDependencies, @Nonnull String typeToRecompile,
      @Nonnull Set<String> alreadyVisited) {
    if (constantDependencies != null) {
      for (String constantDependency : constantDependencies) {
        Set<String> typesToRecompile = recompileDependencies.get(constantDependency);
        if (typesToRecompile == null) {
          typesToRecompile = new HashSet<String>();
          recompileDependencies.put(constantDependency, typesToRecompile);
        }
        typesToRecompile.add(typeToRecompile);

        Set<String> constantDeps = this.constantDependencies.get(constantDependency);
        if (!alreadyVisited.contains(constantDependency) && constantDeps != null) {
          alreadyVisited.add(constantDependency);
          computeConstantRecompileDependencies(recompileDependencies, constantDeps, typeToRecompile,
              alreadyVisited);
        }
      }
    }
  }

  private void computeHierarchyRecompileDependencies(
      @Nonnull Map<String, Set<String>> recompileDependencies,
      @CheckForNull Set<String> hierarchyDependencies, @Nonnull String typeToRecompile) {
    if (hierarchyDependencies != null) {
      for (String hierarchyDependency : hierarchyDependencies) {
        Set<String> typesToRecompile = recompileDependencies.get(hierarchyDependency);
        if (typesToRecompile == null) {
          typesToRecompile = new HashSet<String>();
          recompileDependencies.put(hierarchyDependency, typesToRecompile);
        }
        typesToRecompile.add(typeToRecompile);

        Set<String> newHierarchyDependencies = this.hierarchyDependencies.get(hierarchyDependency);
        if (newHierarchyDependencies != null) {
          computeHierarchyRecompileDependencies(recompileDependencies, newHierarchyDependencies,
              typeToRecompile);
        }
      }
    }
  }

  @Override
  @Nonnull
  public void read(@Nonnull Readable readable) throws IOException {
    LineReader lr = new LineReader(readable);
    hierarchyDependencies = readMapOne2Many(lr);
    constantDependencies = readMapOne2Many(lr);
    codeDependencies = readMapOne2Many(lr);
  }

  public void update(@Nonnull FileDependencies fileDependencies,
      @Nonnull Set<String> deleteFileNames, @Nonnull Set<String> modifiedFileNames) {
    for (String deletedJavaFileName : deleteFileNames) {
      for (String deleteTypeName : fileDependencies.getTypeNames(deletedJavaFileName)) {
        codeDependencies.remove(deleteTypeName);
        constantDependencies.remove(deleteTypeName);
        hierarchyDependencies.remove(deleteTypeName);
      }
    }
    for (String modifiedJavaFileName : modifiedFileNames) {
      for (String deleteTypeName : fileDependencies.getTypeNames(modifiedJavaFileName)) {
        codeDependencies.remove(deleteTypeName);
        constantDependencies.remove(deleteTypeName);
        hierarchyDependencies.remove(deleteTypeName);
      }
    }
  }
}
