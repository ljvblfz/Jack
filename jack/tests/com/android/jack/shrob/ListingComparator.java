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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This tool compares two list of types and their members.
 */
public class ListingComparator extends AbstractListingComparator {

  public static void compare(@Nonnull File reference, @Nonnull File candidate)
      throws IOException, DifferenceFoundException {
    new ListingComparator().compareReadables(new FileReadable(reference),
        new FileReadable(candidate));
  }

  public static void compare(@Nonnull File reference, @Nonnull String candidate)
      throws IOException, DifferenceFoundException {
    new ListingComparator().compareReadables(new FileReadable(reference),
        new StringReadable(candidate));
  }

  private void compareReadables(@Nonnull Readable reference, @Nonnull Readable candidate)
      throws IOException, DifferenceFoundException {
    List<String> candidateTypesList;
    {
      BufferedReader candidateReader = candidate.openReader();
      try {
        candidateTypesList = getTypeList(candidateReader);
      } finally {
        candidateReader.close();
      }
    }

    BufferedReader referenceReader = reference.openReader();
    try {
      String currentReferenceLine = referenceReader.readLine();
      while (currentReferenceLine != null) {
        if (!isTypeLine(currentReferenceLine)) {
          throw new ParseException("Type expected");
        }
        String currentType = getTypeNameFromLine(currentReferenceLine);
        int typeIndex = candidateTypesList.indexOf(currentReferenceLine);
        if (typeIndex != -1) {
          candidateTypesList.remove(currentReferenceLine);
          BufferedReader candidateReader = candidate.openReader();
          try {
            if (findLine(currentReferenceLine, candidateReader)) {
              currentReferenceLine = checkMembers(referenceReader, candidateReader, currentType);
            } else {
              throw new AssertionError();
            }
          } finally {
            candidateReader.close();
          }
        } else {
          missingType(currentType, false /* missingInReference */);
          // Skip members
          do {
            currentReferenceLine = referenceReader.readLine();
          } while (currentReferenceLine != null && !isTypeLine(currentReferenceLine));
        }
      }
    } finally {
      referenceReader.close();
    }

    if (!candidateTypesList.isEmpty()) {
      Iterator<String> iterator = candidateTypesList.iterator();
      while (iterator.hasNext()) {
        missingType(getTypeNameFromLine(iterator.next()), true /* missingInReference */);
      }
    }

    if (differenceFound()) {
      throw new DifferenceFoundException();
    }
  }

  @Nonnull
  private static List<String> getTypeList(@Nonnull BufferedReader reader) throws IOException {
    List<String> typesFound = new ArrayList<String>();
    String line = reader.readLine();
    while (line != null) {
      if (isTypeLine(line)) {
        typesFound.add(line);
      }
      line = reader.readLine();
    }
    return typesFound;
  }

  @CheckForNull
  private String checkMembers(@Nonnull BufferedReader referenceReader,
      @Nonnull BufferedReader candidateReader, @Nonnull String currentType)
      throws IOException {
    List<String> candidateMembers = getMemberList(candidateReader);
    String currentReferenceLine = referenceReader.readLine();
    while (currentReferenceLine != null && !isTypeLine(currentReferenceLine)) {
      int index = candidateMembers.indexOf(currentReferenceLine);
      if (index == -1) {
        missingMember(currentReferenceLine, currentType, false /* missingInReference*/);
      } else {
        candidateMembers.remove(index);
      }
      currentReferenceLine = referenceReader.readLine();
    }

    if (!candidateMembers.isEmpty()) {
      Iterator<String> iterator = candidateMembers.iterator();
      while (iterator.hasNext()) {
        missingMember(iterator.next(), currentType, true /* missingInReference*/);
      }
    }

    return currentReferenceLine;
  }

  @Nonnull
  private static List<String> getMemberList(@Nonnull BufferedReader referenceReader)
      throws IOException {
    String line = referenceReader.readLine();
    List<String> referenceMembers = new ArrayList<String>();
    while (line != null && !isTypeLine(line)) {
      referenceMembers.add(line);
      line = referenceReader.readLine();
    }
    return referenceMembers;
  }

  private static boolean isTypeLine(@Nonnull String line) {
    return line.endsWith(":");
  }

  @Nonnull
  private static String getTypeNameFromLine(@Nonnull String line) {
    assert isTypeLine(line);
    return line.substring(0, line.length() - 1);
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
