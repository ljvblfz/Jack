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

import java.io.File;
import java.io.PrintStream;

import javax.annotation.Nonnull;


/**
 * A {@link Reporter} for the SDK.
 */
@ImplementationName(iface = Reporter.class, name = "sdk")
public class SdkReporter extends CommonReporter {

  @Override
  protected void printFilteredProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message, @Nonnull SourceInfo sourceInfo) {
    StringBuffer messageBuffer = new StringBuffer("MESSAGE:{");

    messageBuffer.append("\"kind\":\"").append(convertLevelName(problemLevel)).append("\",");
    messageBuffer.append("\"text\":\"").append(message).append("\",");
    messageBuffer.append("\"sources\":[{");

    if (sourceInfo != SourceInfo.UNKNOWN) {
      String fileName = new File(sourceInfo.getFileName()).getAbsolutePath();

      messageBuffer.append("\"file\":\"").append(fileName).append("\",");
      messageBuffer.append("\"position\":{");

      // Convert unknown values to match sdk expectations
      int startLine = sourceInfo.getStartLine() == SourceInfo.UNKNOWN_LINE_NUMBER ? -1
          : sourceInfo.getStartLine();
      int startColumn = sourceInfo.getStartColumn() == SourceInfo.UNKNOWN_COLUMN_NUMBER ? -1
          : sourceInfo.getStartColumn();
      int endLine =
          sourceInfo.getEndLine() == SourceInfo.UNKNOWN_LINE_NUMBER ? -1 : sourceInfo.getEndLine();
      int endColumn = sourceInfo.getEndColumn() == SourceInfo.UNKNOWN_COLUMN_NUMBER ? -1
          : sourceInfo.getEndColumn();

      messageBuffer.append("\"startLine\":").append(startLine).append(',');
      messageBuffer.append("\"startColumn\":").append(startColumn).append(',');
      messageBuffer.append("\"startOffset\":-1,");
      messageBuffer.append("\"endLine\":").append(endLine).append(',');
      messageBuffer.append("\"endColumn\":").append(endColumn).append(',');
      messageBuffer.append("\"endOffset\":-1");

      messageBuffer.append('}');
    }

    messageBuffer.append("}]}");

    PrintStream printer = streamByLevel.get(problemLevel);
    if (printer == null) {
      printer = streamByDefault;
    }

    printer.println(messageBuffer.toString());
  }

  private String convertLevelName(@Nonnull ProblemLevel problemLevel) {
    switch (problemLevel) {
      case ERROR:
        return "ERROR";
      case WARNING:
        return "WARNING";
      case INFO:
        return "INFO";
      default:
        throw new AssertionError("Unkown problem level: '" + problemLevel.name() + "'");
    }
  }

}
