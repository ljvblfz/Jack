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

package com.android.jack.jayce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jayce Header.
 */
public class JayceHeader {

  private static final char VERSION_SEPARATOR = '.';
  private static final char STRING_DELIMITER = '"';
  private static final char VALUE_SEPARATOR = ' ';
  private static final char LEFT_BRACKET = '(';
  private static final char RIGHT_BRACKET = ')';

  @Nonnull
  private static final String JAYCE_KEYWORD = "jayce";
  @Nonnull
  private static final Charset DEFAULT_CHARSET = Charset.forName("US-ASCII");
  @Nonnull
  private static final byte[] JAYCE_KEYWORD_BYTE_ARRAY = JAYCE_KEYWORD.getBytes(DEFAULT_CHARSET);
  @Nonnull
  private static final String STANDARD_ERROR_MESSAGE = "Invalid Jayce header";
  @Nonnegative
  private static final int INT_MAX_DIGITS = String.valueOf(Integer.MAX_VALUE).length();
  @Nonnegative
  private static final int CHARSET_NAME_MAX_LENGTH = 21;
  @Nonnegative
  private static final int EMITTER_ID_MAX_LENGTH = 1024;
  @Nonnull
  private static final String VERSION_FORMAT = "%04d";

  @Nonnegative
  private int majorVersion;
  @Nonnegative
  private int minorVersion;
  @CheckForNull
  private Charset encoding = null;
  @CheckForNull
  private String emitterId = null;

  private char previousChar;


  public JayceHeader(@Nonnegative int majorVersion, @Nonnegative int minorVersion,
      @CheckForNull Charset encoding, @CheckForNull String emitterId) throws JayceFormatException {
    if (emitterId != null) {
      if (encoding == null) {
        throw new JayceFormatException("If emitterId is set, encoding should also be.");
      }
    }
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.encoding = encoding;
    this.emitterId = emitterId;
  }

  public JayceHeader(@Nonnull InputStream is) throws IOException, JayceFormatException {
    readHeader(is);
  }

  private void readHeader(@Nonnull InputStream in) throws IOException, JayceFormatException {

    checkJayceKeyword(in);

    checkLeftBracket(readChar(in));

    majorVersion = readInt(in);

    checkVersionSeparator(getPreviousChar());

    minorVersion = readInt(in);

    if (checkIfRightBracket(getPreviousChar())) {
      return;
    }
    encoding = readEncoding(in);

    if (checkIfRightBracket(readChar(in))) {
      return;
    }
    emitterId = readString(in, EMITTER_ID_MAX_LENGTH);

    if (!checkIfRightBracket(readChar(in))) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    }
  }

  private void checkLeftBracket(char readChar) throws JayceFormatException {
    if (readChar != LEFT_BRACKET) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    }
  }

  private boolean checkIfRightBracket(char readChar) throws JayceFormatException {
    if (readChar == RIGHT_BRACKET) {
      return true;
    } else if (readChar != VALUE_SEPARATOR) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    } else {
      return false;
    }
  }

  private void checkVersionSeparator(char potentialSeparator) throws JayceFormatException {
    if (potentialSeparator != VERSION_SEPARATOR) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    }
  }

  private void checkJayceKeyword(@Nonnull InputStream in)
      throws IOException, JayceFormatException {
    byte[] byteArray = new byte[JAYCE_KEYWORD_BYTE_ARRAY.length];
    if (in.read(byteArray) != -1) {
      if (!Arrays.equals(byteArray, JAYCE_KEYWORD_BYTE_ARRAY)) {
        throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
      }
    } else {
      throw new JayceFormatException("No Jayce header found");
    }
  }

  private Charset readEncoding(@Nonnull InputStream in)
      throws IOException, JayceFormatException {
    try {
      return Charset.forName(readString(in, CHARSET_NAME_MAX_LENGTH));
    } catch (IllegalCharsetNameException e) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    }
  }

  private int readInt(@Nonnull InputStream in) throws IOException, JayceFormatException {
    StringBuffer buffer = new StringBuffer(2);
    char readChar = readChar(in);
    int numRead = 1;
    while (Character.isDigit(readChar)) {
      if (numRead > INT_MAX_DIGITS) {
        throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
      }
      buffer.append(readChar);
      readChar = readChar(in);
      numRead++;
    }
    try {
      return Integer.parseInt(buffer.toString());
    } catch (NumberFormatException e) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    }
  }

  @Nonnull
  private String readString(@Nonnull InputStream in, int upperLimit)
      throws IOException, JayceFormatException {
    char readChar = readChar(in);
    if (readChar != STRING_DELIMITER) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    }
    StringBuffer buffer = new StringBuffer(upperLimit);

    readChar = readChar(in);
    int numRead = 1;
    while (readChar != STRING_DELIMITER) {
      if (numRead > upperLimit) {
        throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
      }
      buffer.append(readChar);
      readChar = readChar(in);
      numRead++;
    }
    return buffer.toString();
  }

  public void writeHeader(@Nonnull OutputStream out) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(out, DEFAULT_CHARSET);
    writer.append(JAYCE_KEYWORD);
    writer.append(LEFT_BRACKET);
    writer.append(String.valueOf(majorVersion));
    writer.append(VERSION_SEPARATOR);
    writer.append(String.valueOf(minorVersion));
    if (encoding != null) {
      writer.append(VALUE_SEPARATOR);
      writer.append(STRING_DELIMITER);
      writer.append(encoding.displayName(Locale.US));
      writer.append(STRING_DELIMITER);
      if (emitterId != null) {
        writer.append(VALUE_SEPARATOR);
        writer.append(STRING_DELIMITER);
        writer.append(emitterId);
        writer.append(STRING_DELIMITER);
      }
    }
    writer.append(RIGHT_BRACKET);
    writer.flush();
  }

  @Nonnegative
  public int getMajorVersion() {
    return majorVersion;
  }

  @Nonnegative
  public int getMinorVersion() {
    return minorVersion;
  }

  @Nonnull
  public String getMajorVersionString() {
    return getVersionString(majorVersion);
  }

  @Nonnull
  public static String getVersionString(@Nonnegative int version) {
    return String.format(VERSION_FORMAT, Integer.valueOf(version));
  }

  @CheckForNull
  public String getEmitterId() {
    return emitterId;
  }

  @CheckForNull
  public Charset getEncoding() {
    return encoding;
  }

  private char readChar(@Nonnull InputStream in) throws IOException, JayceFormatException {
    int readChar = in.read();
    if (readChar == '\t') {
      readChar = VALUE_SEPARATOR;
    }

    // skip when several contiguous value separators (typically white spaces)
    if (previousChar == VALUE_SEPARATOR) {
      while (readChar == VALUE_SEPARATOR) {
        readChar = in.read();
      }
    }

    if (readChar == -1) {
      throw new JayceFormatException(STANDARD_ERROR_MESSAGE);
    } else {
      previousChar = (char) readChar;
      return previousChar;
    }
  }

  private char getPreviousChar() {
    return previousChar;
  }

}
