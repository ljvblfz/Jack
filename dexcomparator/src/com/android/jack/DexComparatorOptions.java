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

package com.android.jack;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * Options used to configure a dex files comparator.
 */
class DexComparatorOptions {
  @Argument(usage = "candidate dex file", required = true)
  File candidateFile;

  @Option(name = "--referenceDexFile", aliases = "-ref", usage = "reference dex file",
      required = true)
  File referenceFile;

  @Option(
      name = "--strict",
      usage = "if false, the candidate Dex must at least contain all the structures of the "
          + "reference Dex; if true, the candidate Dex must exactly contain all the "
          + "structures of the reference Dex (default: false)")
  boolean strict = false;

  @Option(name = "--compareDebugInfo",
      usage = "enable comparison of debug infos (default: false)")
  boolean enableDebugInfoComparison = false;

  @Option(name = "--compareDebugInfoBinarily",
      usage = "enable binary comparison of debug infos, allowed only if "
          + "compareDebugInfo is enabled (default: false)")
  boolean enableBinaryDebugInfoComparison = false;

  @Option(name = "--compareCodeBinarily",
      usage = "enable binary comparison of code (default: false)")
  boolean enableBinaryCodeComparison = false;

  @Option(name = "--compareInstructionNumber",
      usage = "enable comparison of number of instructions, not allowed "
          + "with binary code comparison (default: false)")
  boolean compareInstructionNumber = false;

  @Option(name = "--instructionNumberTolerance",
      usage = "tolerance factor for comparison of number of instructions, allowed only if"
          + " compareInstructionNumber is enabled (default: 0f)")
  float instructionNumberTolerance = 0f;
}