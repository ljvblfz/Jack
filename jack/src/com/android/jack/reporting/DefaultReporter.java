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

import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.location.ColumnAndLineLocation;
import com.android.sched.util.location.FileOrDirLocation;
import com.android.sched.util.location.Location;

import java.io.PrintWriter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * The default {@link Reporter}.
 */
@ImplementationName(iface = Reporter.class, name = "default")
public class DefaultReporter extends CommonReporter {

  @Override
  protected void printFilteredProblem(@Nonnull ProblemLevel problemLevel, @Nonnull String message,
      @CheckForNull Location location) {
    StringBuffer messageBuffer = new StringBuffer(problemLevel.toString());

    if (location != null) {

      String filePath = null;

      Location currentLocation = location;
      ColumnAndLineLocation call = null;

      if (currentLocation instanceof ColumnAndLineLocation) {
        call = (ColumnAndLineLocation) currentLocation;
        currentLocation = call.getParentLocation();
      }

      if (currentLocation instanceof FileOrDirLocation) {
        filePath = ((FileOrDirLocation) currentLocation).getPath();
      }

      if (filePath != null) {
        messageBuffer.append(": ");
        messageBuffer.append(filePath);
        if (call != null) {
          if (call.hasStartLine()) {
            messageBuffer.append(':');
            messageBuffer.append(call.getStartLine());

            if (call.hasStartColumn()) {
              messageBuffer.append('.');
              messageBuffer.append(call.getStartColumn());

              if (!call.hasEndLine() && call.hasEndColumn()) {
                messageBuffer.append('-');
                messageBuffer.append(call.getEndColumn());
              }
            }

            if (call.hasEndLine()) {
              messageBuffer.append('-');
              messageBuffer.append(call.getEndLine());

              if (call.hasStartColumn()) {
                if (call.hasEndColumn()) {
                  messageBuffer.append('.');
                  messageBuffer.append(call.getEndColumn());
                } else { // there's an "end line" but no "end column", use the "start column"
                  messageBuffer.append('.');
                  messageBuffer.append(call.getStartColumn());
                }
              }
            }
          }
        }
      }
    }

    messageBuffer.append(": ");
    messageBuffer.append(message);

    PrintWriter writer = writerByLevel.get(problemLevel);
    if (writer == null) {
      writer = writerByDefault;
    }

    writer.println(messageBuffer.toString());
  }
}
