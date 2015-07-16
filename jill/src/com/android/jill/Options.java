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

package com.android.jill;

import com.android.jill.utils.FileUtils;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Jill command line options.
 */
public class Options {

  @CheckForNull
  @Argument(usage = "read command line from file", metaVar = "@<FILE>")
  private File binaryFile;

  @Option(name = "--verbose", usage = "enable verbosity (default: false)")
  private  boolean verbose = false;

  @Option(name = "-h", aliases = "--help", usage = "display help")
  protected boolean help;

  @CheckForNull
  @Option(name = "--output", usage = "output file", metaVar = "FILE")
  protected File output;

  @Option(name = "--version", usage = "display version")
  protected boolean version;

  @Option(name = "--tolerant", usage = "be tolerant to malformed input (default: false)")
  protected boolean tolerant = false;

  private final ContainerType outputContainer = ContainerType.ZIP;

  @Option(name = "--no-debug", usage = "disable debug info emission")
  protected boolean disableEmitDebugInfo = false;

  @CheckForNull
  private PrintStream err;
  @CheckForNull
  private File workingDir;

  public void checkValidity() throws IllegalOptionsException {
    if (askForVersion() || askForHelp()) {
      return;
    }

    if (binaryFile != null) {
      checkBinaryFileValidity();
    } else {
      throw new IllegalOptionsException("Input file not provided");
    }
    if (output != null) {
      if (outputContainer == ContainerType.DIR) {
        checkOutputDir();
      }
    } else {
      throw new IllegalOptionsException("Output directory not provided");
    }
  }

  public void setBinaryFile(@Nonnull File binaryFile) {
    this.binaryFile = binaryFile;
  }

  public void setOutput(@Nonnull File output) {
    this.output = output;
  }

  @Nonnull
  public File getOutput() {
    assert output != null;
    if (workingDir != null && !output.isAbsolute()) {
      return new File(workingDir, output.getPath());
    } else {
      return output;
    }
  }

  @Nonnull
  public File getBinaryFile() {
    assert binaryFile != null;
    if (workingDir != null && !binaryFile.isAbsolute()) {
      return new File(workingDir, binaryFile.getPath());
    } else {
      return binaryFile;
    }
  }

  public boolean askForVersion() {
    return version;
  }

  public boolean askForHelp() {
    return help;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }


  public boolean isVerbose() {
    return verbose;
  }

  public void setTolerant(boolean tolerant) {
    this.tolerant = tolerant;
  }

  public boolean isTolerant() {
    return tolerant;
  }

  public boolean isEmitDebugInfo() {
    return !disableEmitDebugInfo;
  }

  public void setEmitDebugInfo(boolean emitDebugInfo) {
    disableEmitDebugInfo = !emitDebugInfo;
  }

  @Nonnull
  public ContainerType getOutputContainer() {
    return outputContainer;
  }


  public void setStandardError(@Nonnull PrintStream standardError) {
    err = standardError;
  }

  @CheckForNull
  public PrintStream getStandardError() {
    return err;
  }

  public void setWorkingDirectory(@Nonnull File workingDir) {
    this.workingDir = workingDir;
  }

  private void checkBinaryFileValidity() throws IllegalOptionsException {
    assert binaryFile != null;

    File binaryFile = getBinaryFile();

    if (!binaryFile.exists()) {
      throw new IllegalOptionsException(binaryFile.getName() + " does not exists.");
    }

    if (binaryFile.isFile() && FileUtils.isJarFile(binaryFile)) {
      return;
    }

    if (binaryFile.isFile() && !FileUtils.isJavaBinaryFile(binaryFile)
        && !FileUtils.isJarFile(binaryFile)) {
      throw new IllegalOptionsException(binaryFile.getName() + " is not a supported binary file.");
    }

    List<File> binaryFiles = new ArrayList<File>();
    FileUtils.getJavaBinaryFiles(binaryFile, binaryFiles);
    if (binaryFiles.isEmpty()) {
      System.err.println("Warning: Folder " + binaryFile.getName()
          + " does not contains class files.");
    }
  }

  private void checkOutputDir() throws IllegalOptionsException {
    assert output != null;

    File output = getOutput();

    if (!output.exists()) {
      throw new IllegalOptionsException(output.getName() + " does not exist.");
    }

    if (!output.canRead() || !output.canWrite()) {
      throw new IllegalOptionsException("The specified output folder '"
          + output.getAbsolutePath()
          + "' for jack files cannot be written to or read from.");
    }
  }
}
