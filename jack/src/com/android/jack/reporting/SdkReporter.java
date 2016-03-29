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

import com.google.common.base.Strings;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.codec.ImplementationName;

import java.io.File;
import java.io.PrintWriter;

import javax.annotation.Nonnull;


/**
 * A {@link Reporter} for the SDK.
 */
@ImplementationName(iface = Reporter.class, name = "sdk")
public class SdkReporter extends CommonReporter {

  @Override
  protected void printFilteredProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message, @Nonnull SourceInfo sourceInfo) {
    String escapedMessage = convertString(message);

    StringBuffer messageBuffer = new StringBuffer("MESSAGE:{");

    messageBuffer.append("\"kind\":\"").append(convertLevelName(problemLevel)).append("\",");
    messageBuffer.append("\"text\":\"").append(escapedMessage).append("\",");
    messageBuffer.append("\"sources\":[{");

    if (sourceInfo != SourceInfo.UNKNOWN) {
      String fileName = new File(sourceInfo.getFileName()).getAbsolutePath();
      String escapedFileName = convertString(fileName);

      messageBuffer.append("\"file\":\"").append(escapedFileName).append("\",");
      messageBuffer.append("\"position\":{");

      // Convert unknown values to match sdk expectations
      int startLine = sourceInfo.getStartLine() == SourceInfo.UNKNOWN_LINE_NUMBER ? -1
          : sourceInfo.getStartLine();
      int startColumn = sourceInfo.getStartColumn() == SourceInfo.UNKNOWN_COLUMN_NUMBER ? -1
          : sourceInfo.getStartColumn();
      int endLine = sourceInfo.getEndLine() == SourceInfo.UNKNOWN_LINE_NUMBER ? startLine
          : sourceInfo.getEndLine();
      int endColumn = sourceInfo.getEndColumn() == SourceInfo.UNKNOWN_COLUMN_NUMBER ? startColumn
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

    PrintWriter writer = writerByLevel.get(problemLevel);
    if (writer == null) {
      writer = writerByDefault;
    }

    writer.println(messageBuffer.toString());
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

  // http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf
  @Nonnull
  private static String convertString(@Nonnull String s) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
          buffer.append("\\\"");
          break;
        case '\\':
          buffer.append("\\\\");
          break;
        case '/':
          buffer.append("\\/");
          break;
        case '\b':
          buffer.append("\\b");
          break;
        case '\f':
          buffer.append("\\f");
          break;
        case '\n':
          buffer.append("\\n");
          break;
        case '\r':
          buffer.append("\\r");
          break;
        case '\t':
          buffer.append("\\t");
          break;
        default:
          if (Character.isISOControl(c)) {
            buffer.append("\\u");
            String cAsHex = Integer.toHexString(c);
            buffer.append(Strings.repeat("0", 4 - cAsHex.length()));
            buffer.append(cAsHex.toUpperCase());
          } else {
            buffer.append(c);
          }
      }
    }
    return buffer.toString();
  }

}
