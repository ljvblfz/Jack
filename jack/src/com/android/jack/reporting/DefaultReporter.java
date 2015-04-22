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

package com.android.jack.reporting;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.codec.ImplementationName;

import java.io.PrintStream;

import javax.annotation.Nonnull;


/**
 * The default {@link Reporter}.
 */
@ImplementationName(iface = Reporter.class, name = "default")
public class DefaultReporter extends CommonReporter {

  @Override
  protected void printFilteredProblem(@Nonnull ProblemLevel problemLevel, @Nonnull String message,
      @Nonnull SourceInfo sourceInfo) {
    StringBuffer messageBuffer = new StringBuffer(problemLevel.toString());
    if (sourceInfo != SourceInfo.UNKNOWN) {
      messageBuffer.append(": ");
      messageBuffer.append(sourceInfo.getFileName());
      if (sourceInfo.getStartLine() >= 0) {
        messageBuffer.append(":");
        messageBuffer.append(sourceInfo.getStartLine());
      }
    }

    messageBuffer.append(": ");
    messageBuffer.append(message);

    PrintStream printer = streamByLevel.get(problemLevel);
    if (printer == null) {
      printer = streamByDefault;
    }

    printer.println(messageBuffer.toString());
  }
}
