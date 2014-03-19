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

package com.android.jack.shrob;

import com.android.jack.DifferenceFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This tool compares two list of seeds.
 */
public class SeedsComparator extends AbstractListingComparator {

  public static void compare(@Nonnull File reference, @Nonnull File candidate)
      throws IOException, DifferenceFoundException {
    new SeedsComparator().compareFiles(reference, candidate);
  }

  private void compareFiles(@Nonnull File reference, @Nonnull File candidate)
      throws IOException, DifferenceFoundException {
    {
      BufferedReader referenceReader = createStreamReader(reference);
      try {
        String currentReferenceLine = referenceReader.readLine();
        while (currentReferenceLine != null) {
          BufferedReader candidateReader = createStreamReader(candidate);
          try {
            assert currentReferenceLine != null; // FINDBUGS
            if (!findLine(currentReferenceLine, candidateReader)) {
              if (isTypeLine(currentReferenceLine)) {
                missingType(currentReferenceLine, false /* missingInReference */);
              } else {
                int index = currentReferenceLine.indexOf(':');
                String enclosingType = currentReferenceLine.substring(0, index).trim();
                String member = currentReferenceLine.substring(index + 1).trim();
                missingMember(member, enclosingType, false /* missingInReference */);
              }
            }
          } finally {
            candidateReader.close();
          }
          currentReferenceLine = referenceReader.readLine();
        }
      } finally {
        referenceReader.close();
      }
    }

    {
      BufferedReader candidateReader = createStreamReader(candidate);
      try {
        String currentCandidateLine = candidateReader.readLine();
        while (currentCandidateLine != null) {
          BufferedReader referenceReader = createStreamReader(reference);
          try {
            assert currentCandidateLine != null; // FINDBUGS
            if (!findLine(currentCandidateLine, referenceReader)) {
              if (isTypeLine(currentCandidateLine)) {
                missingType(currentCandidateLine, true /* missingInReference */);
              } else {
                int index = currentCandidateLine.indexOf(':');
                String enclosingType = currentCandidateLine.substring(0, index).trim();
                String member = currentCandidateLine.substring(index + 1).trim();
                missingMember(member, enclosingType, true /* missingInReference */);
              }
            }
          } finally {
            referenceReader.close();
          }
          currentCandidateLine = candidateReader.readLine();
        }
      } finally {
        candidateReader.close();
      }
    }

    if (differenceFound()) {
      throw new DifferenceFoundException();
    }
  }

  protected boolean isTypeLine(@Nonnull String line) {
    return !line.contains(":");
  }

  public static void main(String[] args) throws IOException, DifferenceFoundException {
    if (args.length != 2) {
      System.out.println("Wrong number of arguments: expecting <referenceFile> <candidateFile>");
    } else {
      File reference = new File(args[0]);
      File candidate = new File(args[1]);
      compare(reference, candidate);
    }
  }
}
