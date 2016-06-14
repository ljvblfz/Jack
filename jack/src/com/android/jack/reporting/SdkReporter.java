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

import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.location.ColumnAndLineLocation;
import com.android.sched.util.location.FileOrDirLocation;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.PrintWriter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * A {@link Reporter} for the SDK.
 */
@ImplementationName(iface = Reporter.class, name = "sdk")
public class SdkReporter extends CommonReporter {

  private static final int SDK_UNKNOWN_VALUE = -1;

  @Override
  protected void printFilteredProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message, @CheckForNull Location location) {
    String escapedMessage = convertString(message);

    StringBuffer messageBuffer = new StringBuffer("MESSAGE:{");

    messageBuffer.append("\"kind\":\"").append(convertLevelName(problemLevel)).append("\",");
    messageBuffer.append("\"text\":\"").append(escapedMessage).append("\",");
    messageBuffer.append("\"sources\":[{");

    if (location != null) {

      String filePath = null;
      ColumnAndLineLocation call = null;

      Location currentLocation = location;

      if (currentLocation instanceof ColumnAndLineLocation) {
        call = (ColumnAndLineLocation) currentLocation;
        currentLocation = call.getParentLocation();
      }

      if (currentLocation instanceof FileOrDirLocation) {
        filePath = ((FileOrDirLocation) currentLocation).getPath();
      }

      if (filePath != null) {

        String fileName = new File(filePath).getAbsolutePath();
        String escapedFileName = convertString(fileName);

        messageBuffer.append("\"file\":\"").append(escapedFileName).append("\",");
        messageBuffer.append("\"position\":{");

        if (call != null) {
          // Convert unknown values to match sdk expectations
          int sdkStartLine = SDK_UNKNOWN_VALUE;
          int sdkStartColumn = SDK_UNKNOWN_VALUE;
          int sdkEndLine = SDK_UNKNOWN_VALUE;
          int sdkEndColumn = SDK_UNKNOWN_VALUE;

          if (call.hasStartLine()) {
            sdkStartLine = call.getStartLine();
          }

          if (call.hasEndLine()) {
            sdkEndLine = call.getEndLine();
          } else {
            sdkEndLine = sdkStartLine;
          }

          if (call.hasStartColumn()) {
            sdkStartColumn = call.getStartColumn();
          }

          if (call.hasEndColumn()) {
            sdkEndColumn = call.getEndColumn();
          } else {
            sdkEndColumn = sdkStartColumn;
          }

          messageBuffer.append("\"startLine\":").append(sdkStartLine).append(',');
          messageBuffer.append("\"startColumn\":").append(sdkStartColumn).append(',');
          messageBuffer.append("\"startOffset\":").append(SDK_UNKNOWN_VALUE).append(',');
          messageBuffer.append("\"endLine\":").append(sdkEndLine).append(',');
          messageBuffer.append("\"endColumn\":").append(sdkEndColumn).append(',');
          messageBuffer.append("\"endOffset\":").append(SDK_UNKNOWN_VALUE);
        }

        messageBuffer.append('}');
      }
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
