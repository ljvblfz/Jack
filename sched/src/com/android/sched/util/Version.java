/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.util;

import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A class describing version, release, build & code.
 */
public class Version {
  @Nonnull
  private static final String FILE_SUFFIX = "-version.properties";
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private String version;
  @Nonnull
  private String releaseName;
  private int    releaseCode;
  @Nonnull
  private SubReleaseKind subReleaseKind;
  private int    subReleaseCode;
  @CheckForNull
  private String buildId;
  @CheckForNull
  private String codeBase;

  // FINDBUGS Fields are initialized by init()
  @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public Version(@Nonnull String name, @Nonnull ClassLoader loader)
      throws IOException {
    String resourceName = name + FILE_SUFFIX;
    InputStream resourceStream = loader.getResourceAsStream(resourceName);
    if (resourceStream == null) {
      throw new FileNotFoundException(resourceName);
    }
    try {
      init(resourceStream);
    } finally {
      try {
        resourceStream.close();
      } catch (IOException e) {
        //
      }
    }
  }

  // FINDBUGS Fields are initialized by init()
  @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public Version(@Nonnull InputStream is) throws IOException {
    init(is);
  }

  public Version(int releaseCode, int subReleaseCode, @Nonnull SubReleaseKind subReleaseKind) {
    this.version = "Unknown";
    this.releaseName = "Unknown";
    this.releaseCode = releaseCode;
    this.subReleaseCode = subReleaseCode;
    this.subReleaseKind = subReleaseKind;
  }

  private void init(InputStream is) throws IOException {
    Properties prop = new Properties();
    prop.load(is);

    long versionFileVersion = Long.parseLong(prop.getProperty("version-file.version.code"));
    assert versionFileVersion >= 1;

    version = prop.getProperty("version");
    assert version != null;

    releaseName = prop.getProperty("version.release.name");
    assert releaseName != null;

    releaseCode = Integer.parseInt(prop.getProperty("version.release.code"));

    subReleaseCode = Integer.parseInt(prop.getProperty("version.sub-release.code"));

    subReleaseKind =
        SubReleaseKind.valueOf(SubReleaseKind.class, prop.getProperty("version.sub-release.kind"));

    buildId = prop.getProperty("version.buildid");
    if (buildId != null && buildId.isEmpty()) {
      buildId = null;
    }
    codeBase = prop.getProperty("version.sha");
    if (codeBase != null && codeBase.isEmpty()) {
      codeBase = null;
    }

    if (codeBase == null || buildId == null) {
      subReleaseKind = SubReleaseKind.ENGINEERING;
    }
  }

  @Nonnull
  public String getVersion() {
    return version;
  }

  @Nonnull
  public String getReleaseName() {
    return releaseName;
  }

  public int getReleaseCode() {
    return releaseCode;
  }

  @Nonnull
  public SubReleaseKind getSubReleaseKind() {
    return subReleaseKind;
  }

  public int getSubReleaseCode() {
    return subReleaseCode;
  }

  @CheckForNull
  public String getBuildId() {
    return buildId;
  }

  @CheckForNull
  public String getCodeBase() {
    return codeBase;
  }

  @Nonnull
  public String getVerboseVersion() {
    return version + " '" + releaseName + "' ("
                   + (buildId != null ? buildId : "engineering")
                   + (codeBase != null ? (' ' + codeBase) : "") + ")";
  }

  public boolean isOlderThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) < 0;
  }

  public boolean isOlderOrEqualsThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) <= 0;
  }

  public boolean isNewerThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) > 0;
  }

  public boolean isNewerOrEqualsThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) >= 0;
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Version) {
      Version other = (Version) obj;
      return version.equals(other.version)
          && releaseName.equals(other.releaseName)
          && releaseCode == other.releaseCode
          && subReleaseCode == other.subReleaseCode
          && subReleaseKind == other.subReleaseKind
          && ((buildId != null && buildId.equals(other.buildId))
              || (buildId == null && other.buildId == null))
          && ((codeBase != null && codeBase.equals(other.codeBase))
              || (codeBase == null && other.codeBase == null));
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return version.hashCode() ^ releaseName.hashCode() ^ (releaseCode * 7) ^ (subReleaseCode * 17)
        ^ subReleaseKind.hashCode() ^ (buildId != null ? buildId.hashCode() : 0)
        ^ (codeBase != null ? codeBase.hashCode() : 0);
  }

  public boolean isComparable() {
    return !(subReleaseKind == SubReleaseKind.ENGINEERING
        || releaseCode <= 0
        || subReleaseCode <= 0);
  }

  int compareTo(@Nonnull Version other) throws UncomparableVersion {
    if (!isComparable() || !other.isComparable()) {
      throw new UncomparableVersion(
          getVerboseVersion() + " is not comparable with " + other.getVerboseVersion());
    }

    if (this.releaseCode > other.getReleaseCode() || (
        this.releaseCode == other.getReleaseCode()
        && this.subReleaseCode > other.getSubReleaseCode())) {
      return 1;
    }


    if (this.releaseCode < other.getReleaseCode() || (
        this.releaseCode == other.getReleaseCode()
        && this.subReleaseCode < other.getSubReleaseCode())) {
      return -1;
    }

    return 0;
  }
}