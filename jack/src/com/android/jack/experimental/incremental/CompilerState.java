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

package com.android.jack.experimental.incremental;

import com.android.jack.JackIOException;
import com.android.jack.util.TextUtils;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.util.file.Directory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A compiler state of a compilation. It could be reused later during another compilation.
 */
public final class CompilerState {

  @Nonnull
  private static final String COMPILER_STATE_FILENAME = "compilerState.ser";

  /**
   * This tag means that {@link CompilerState} is filled and could be write for later compilation.
   */
  @Description("Compiler state is filled and ready to be written")
  @Name("CompilerState.Filled")
  public static final class Filled implements Tag{
  }

  @Nonnull
  private Map<String, Set<String>> codeFileToUsedFiles = new HashMap<String, Set<String>>();

  @Nonnull
  private Map<String, Set<String>> structFileToUsedFiles = new HashMap<String, Set<String>>();

  @Nonnull
  private Map<String, Set<String>> cstFileToUsedFiles = new HashMap<String, Set<String>>();

  @Nonnull
  private Map<String, Set<String>> javaFileToJackFile = new HashMap<String, Set<String>>();

  @Nonnull
  private final File compilerStateFile;

  public CompilerState(@Nonnull Directory directory) {
    compilerStateFile = new File(directory.getFile(), COMPILER_STATE_FILENAME);
  }

  @Nonnull
  public File getCompilerStateFile() {
    return compilerStateFile;
  }

  public void updateCompilerState(@Nonnull Set<String> filesToRecompile) {
    for (String javaFileToRecompile : filesToRecompile) {
      javaFileToJackFile.remove(javaFileToRecompile);
      codeFileToUsedFiles.remove(javaFileToRecompile);
      structFileToUsedFiles.remove(javaFileToRecompile);
      cstFileToUsedFiles.remove(javaFileToRecompile);
    }
  }

  @Nonnull
  public Set<String> getJavaFilename() {
    return (javaFileToJackFile.keySet());
  }

  @Nonnull
  public Set<String> getJacksFileNameFromJavaFileName(@Nonnull String javaFileName) {
    assert javaFileToJackFile.containsKey(javaFileName);
    return (javaFileToJackFile.get(javaFileName));
  }

  public synchronized void addMappingBetweenJavaAndJackFile(@Nonnull String javaFileName,
      @Nonnull String jackFileName) {
    getOrCreate(javaFileToJackFile, javaFileName).add(jackFileName);
  }

  public void addStructUsage(@Nonnull String filename, @CheckForNull String nameOfUsedFile) {
    addUsage(structFileToUsedFiles, filename, nameOfUsedFile);
  }

  public void addCstUsage(@Nonnull String filename, @CheckForNull String nameOfUsedFile) {
    addUsage(cstFileToUsedFiles, filename, nameOfUsedFile);
  }

  public void addCodeUsage(@Nonnull String filename, @CheckForNull String nameOfUsedFile) {
    addUsage(codeFileToUsedFiles, filename, nameOfUsedFile);
  }

  public boolean exists() {
    return compilerStateFile.exists();
  }

  public void write(@Nonnull Directory directory) throws JackIOException {
    File compilerStateFile = new File(directory.getFile(), COMPILER_STATE_FILENAME);
    PrintStream ps = null;

    try {
      StringBuffer sb = new StringBuffer();

      writeMap(sb, javaFileToJackFile);
      writeMap(sb, codeFileToUsedFiles);
      writeMap(sb, cstFileToUsedFiles);
      writeMap(sb, structFileToUsedFiles);

      ps = new PrintStream(compilerStateFile);
      ps.print(sb.toString());

    } catch (FileNotFoundException e) {
      throw new JackIOException("Could not write compiler state file to output '"
          + compilerStateFile.getAbsolutePath() + "'", e);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  @Nonnull
  public void read() throws JackIOException {
    BufferedReader br = null;
    File csf = getCompilerStateFile();
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(csf)));
      javaFileToJackFile = readMap(br);
      codeFileToUsedFiles = readMap(br);
      cstFileToUsedFiles = readMap(br);
      structFileToUsedFiles = readMap(br);
    } catch (IOException e) {
      throw new JackIOException(
          "Could not read compiler state file '" + csf.getAbsolutePath() + "'", e);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException e) {
        throw new JackIOException(
            "Could not read compiler state file '" + csf.getAbsolutePath() + "'", e);
      }
    }
  }

  @Nonnull
  public Map<String, Set<String>> computeDependencies() {
    Map<String, Set<String>> fileDependencies = new HashMap<String, Set<String>>();

    for (String fileName : codeFileToUsedFiles.keySet()) {
      getOrCreate(fileDependencies, fileName);

      computeStructDependencies(fileDependencies, structFileToUsedFiles.get(fileName), fileName);
      computeCstDependencies(fileDependencies, cstFileToUsedFiles.get(fileName), fileName,
          new HashSet<String>());
      computeCodeDependencies(fileDependencies, codeFileToUsedFiles.get(fileName), fileName);
    }

    return fileDependencies;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Compiler State");
    builder.append(TextUtils.LINE_SEPARATOR);

    builder.append("*File mapping*");
    builder.append(TextUtils.LINE_SEPARATOR);
    for (String javaFileName : javaFileToJackFile.keySet()) {
      builder.append(javaFileName);
      builder.append("->");
      builder.append(javaFileToJackFile.get(javaFileName));
      builder.append(TextUtils.LINE_SEPARATOR);
    }

    builder.append("*Code usage list*");
    builder.append(TextUtils.LINE_SEPARATOR);
    for (String fileName : codeFileToUsedFiles.keySet()) {
      builder.append(fileName);
      builder.append("->");
      builder.append(codeFileToUsedFiles.get(fileName));
      builder.append(TextUtils.LINE_SEPARATOR);
    }

    builder.append("*Constant usage list*");
    builder.append(TextUtils.LINE_SEPARATOR);
    for (String fileName : cstFileToUsedFiles.keySet()) {
      builder.append(fileName);
      builder.append("->");
      builder.append(cstFileToUsedFiles.get(fileName));
      builder.append(TextUtils.LINE_SEPARATOR);
    }

    builder.append("*Struct usage list*");
    builder.append(TextUtils.LINE_SEPARATOR);
    for (String fileName : structFileToUsedFiles.keySet()) {
      builder.append(fileName);
      builder.append("->");
      builder.append(structFileToUsedFiles.get(fileName));
      builder.append(TextUtils.LINE_SEPARATOR);
    }

    return builder.toString();
  }

  private void computeCodeDependencies(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull Set<String> codeDependencies, @Nonnull String dependencyToAdd) {
    for (String codeDependency : codeDependencies) {
      Set<String> usedByFiles = getOrCreate(fileDependencies, codeDependency);
      usedByFiles.add(dependencyToAdd);

      if (structFileToUsedFiles.get(codeDependency) != null) {
        computeStructDependencies(fileDependencies, structFileToUsedFiles.get(codeDependency),
            dependencyToAdd);
      }
    }
  }

  private void computeCstDependencies(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull Set<String> cstDependencies, @Nonnull String dependencyToAdd,
      @Nonnull Set<String> alreadyVisited) {
    for (String cstDependency : cstDependencies) {
      Set<String> usedByFiles = getOrCreate(fileDependencies, cstDependency);
      usedByFiles.add(dependencyToAdd);

      if (!alreadyVisited.contains(cstDependency) &&
          cstFileToUsedFiles.get(cstDependency) != null) {
        alreadyVisited.add(cstDependency);
        computeCstDependencies(fileDependencies, cstFileToUsedFiles.get(cstDependency),
            dependencyToAdd, alreadyVisited);
      }
    }
  }

  private void computeStructDependencies(@Nonnull Map<String, Set<String>> fileDependencies,
      @Nonnull Set<String> structDependencies, @Nonnull String dependencyToAdd) {
    for (String structDependency : structDependencies) {
      Set<String> usedByFiles = getOrCreate(fileDependencies, structDependency);
      usedByFiles.add(dependencyToAdd);

      Set<String> newStructDependencies = structFileToUsedFiles.get(structDependency);
      if (newStructDependencies != null) {
        computeStructDependencies(fileDependencies, newStructDependencies, dependencyToAdd);
      }
    }
  }

  private synchronized void addUsage(@Nonnull Map<String, Set<String>> str2UsageSet,
      @Nonnull String filename, @CheckForNull String nameOfUsedFile) {
    if (!filename.equals(nameOfUsedFile)) {
      Set<String> usages = getOrCreate(str2UsageSet, filename);
      if (nameOfUsedFile != null) {
        usages.add(nameOfUsedFile);
      }
    }
  }

  @Nonnull
  private Set<String> getOrCreate(@Nonnull Map<String, Set<String>> str2Set,
      @Nonnull String filenameUsingCst) {
    Set<String> filenameHavingCst = str2Set.get(filenameUsingCst);

    if (filenameHavingCst == null) {
      filenameHavingCst = new HashSet<String>();
      str2Set.put(filenameUsingCst, filenameHavingCst);
    }

    return (filenameHavingCst);
  }

  private void writeMap(@Nonnull StringBuffer sb, @Nonnull Map<String, Set<String>> str2Set) {
    for (Map.Entry<String, Set<String>> entry : str2Set.entrySet()) {
      sb.append(entry.getKey());
      Iterator<String> itValues = entry.getValue().iterator();
      while (itValues.hasNext()) {
        sb.append(",");
        sb.append(itValues.next());
      }
      sb.append("\n");
    }
    sb.append("#\n");
  }

  @Nonnull
  private static Map<String, Set<String>> readMap(@Nonnull BufferedReader br) throws IOException {
    Map<String, Set<String>> str2Set = new HashMap<String, Set<String>>();
    String line;

    while ((line = br.readLine()) != null && !line.equals("#")) {
      Set<String> values = new HashSet<String>();
      StringTokenizer strTok = new StringTokenizer(line, ",");
      String key = strTok.nextToken();
      while (strTok.hasMoreTokens()) {
        values.add(strTok.nextToken());
      }
      str2Set.put(key, values);
    }

    return str2Set;
  }
}
