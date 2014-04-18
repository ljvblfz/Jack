package com.android.jack.jayce;
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



import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A reader of Jayce streams.
 */
public class JayceReader extends JayceProcessor {

  @Nonnull
  private final InputStream in;
  @CheckForNull
  private JayceInternalReader jayceInternalReader;
  @CheckForNull
  private NodeLevel nodeLevel;
  @Nonnull
  private static final String UNKNOWN_VERSION_STRING = "UNKNOWN";

  private static final int UNKNOWN_VERSION = -1;
  @Nonnull
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private int majorVersion = UNKNOWN_VERSION;

  private int minorVersion = UNKNOWN_VERSION;
  @Nonnull
  private final String readerClassName;
  @CheckForNull
  private final String emitterId;
  @Nonnull
  private Charset encoding = DEFAULT_CHARSET;

  public JayceReader(@Nonnull InputStream in)
      throws IOException, JayceFormatException {
    this.in = in;
    JayceHeader jayceHeader = new JayceHeader(in);
    String majorVersionString = jayceHeader.getMajorVersionString();
    majorVersion = jayceHeader.getMajorVersion();
    minorVersion = jayceHeader.getMinorVersion();
    emitterId = jayceHeader.getEmitterId();
    readerClassName =
        "com.android.jack.jayce.v" + majorVersionString + ".io.JayceInternalReaderImpl";
    Charset headerEncoding = jayceHeader.getEncoding();
    if (headerEncoding != null) {
      encoding = headerEncoding;
    }
  }

  /**
   * Reads type from a Jayce stream as a {@link Node}.
   *
   * @param nodeLevel the level of the wanted node
   * @return the node representing the types found in the jayce stream.
   * @throws IOException thrown when there is an issue reading the stream
   * @throws JayceFormatException thrown if the Jayce stream has an invalid format
   * @throws JayceVersionException thrown if the version of the Jayce stream is not supported
   */
  public DeclaredTypeNode readType(@Nonnull NodeLevel nodeLevel)
      throws IOException, JayceFormatException, JayceVersionException {
    if (jayceInternalReader == null) {
      initialize(nodeLevel);
    }
    assert jayceInternalReader != null;
    assert this.nodeLevel == nodeLevel;
    return jayceInternalReader.readType(nodeLevel);

  }
  private void initialize(@Nonnull NodeLevel nodeLevel) throws JayceVersionException {

    jayceInternalReader = (JayceInternalReader) instantiateConstructorWithParameters(
        readerClassName, new Class[] {InputStream.class},
        new Object[] {in}, majorVersion + "." + minorVersion);
    int minorMin = jayceInternalReader.getMinorMin();
    int currentMinor = jayceInternalReader.getCurrentMinor();
    if (minorVersion < minorMin) {
      throw new JayceVersionException("The version of the jack file is not supported anymore."
          + "File version: " + majorVersion + "." + minorVersion + " - Current version: "
          + majorVersion + "." + currentMinor +
          " - Minimum compatible version: " + majorVersion + "." + minorMin);
    } else if (minorVersion > currentMinor) {
      throw new JayceVersionException("The version of the jack file is too recent."
          + "File version: " + majorVersion + "." + minorVersion + " - Current version: "
          + majorVersion + "." + currentMinor);
    } else if (minorVersion < currentMinor) {
      LoggerFactory.getLogger().log(Level.WARNING,
          "The version of the jack file is older than the current version but should be supported. "
          + "File version: {0}.{1} - Current version: {2}.{3}", new Object[] {
          Integer.valueOf(majorVersion), Integer.valueOf(minorVersion),
          Integer.valueOf(majorVersion), Integer.valueOf(currentMinor)});
    }
    this.nodeLevel = nodeLevel;
  }

  @Nonnull
  public String getVersionString() {
    if (majorVersion != UNKNOWN_VERSION && minorVersion != UNKNOWN_VERSION) {
      return majorVersion + "." + minorVersion;
    } else {
      return UNKNOWN_VERSION_STRING;
    }
  }

  @Nonnull
  public String getEmitterId() {
    if (emitterId != null) {
      return emitterId;
    } else {
      return UNKNOWN_VERSION_STRING;
    }
  }

}
