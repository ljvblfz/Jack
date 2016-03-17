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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A class describing version, release, build & code.
 */
public class Version {
  @Nonnegative
  private static final int VERSION_CODE = 2;

  @Nonnull
  private static final String VERSION_CODE_KEY = "version-file.version.code";
  @Nonnull
  private static final String VERSION_KEY = "version";
  @Nonnull
  private static final String RELEASE_NAME_KEY = "version.release.name";
  @Nonnull
  private static final String RELEASE_CODE_KEY = "version.release.code";
  @Nonnull
  private static final String SUB_RELEASE_CODE_KEY = "version.sub-release.code";
  @Nonnull
  private static final String SUB_RELEASE_KIND_KEY = "version.sub-release.kind";
  @Nonnull
  private static final String BUILD_ID_KEY = "version.buildid";
  @Nonnull
  private static final String SHA_KEY = "version.sha";
  @Nonnull
  private static final String RELEASER_KEY = "releaser";

  @Nonnull
  private static final String FILE_SUFFIX = "-version.properties";

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
  @CheckForNull
  private String releaser;

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
      initWithInputStream(resourceStream);
    } finally {
      try {
        resourceStream.close();
      } catch (IOException e) {
        //
      }
    }
  }

  // FINDBUGS Fields are initialized by initWithInputStream()
  @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public Version(@Nonnull InputStream is) throws IOException {
    initWithInputStream(is);
  }

  private void initWithInputStream(@Nonnull InputStream is) throws IOException {
    Properties prop = new Properties();
    prop.load(is);

    long versionFileVersion = Long.parseLong(prop.getProperty(VERSION_CODE_KEY));
    assert versionFileVersion >= 1;

    version = prop.getProperty(VERSION_KEY);
    assert version != null;

    releaseName = prop.getProperty(RELEASE_NAME_KEY);
    assert releaseName != null;

    releaseCode = Integer.parseInt(prop.getProperty(RELEASE_CODE_KEY));
    subReleaseCode = Integer.parseInt(prop.getProperty(SUB_RELEASE_CODE_KEY));
    subReleaseKind =
        SubReleaseKind.valueOf(SubReleaseKind.class, prop.getProperty(SUB_RELEASE_KIND_KEY));
    buildId = prop.getProperty(BUILD_ID_KEY);
    codeBase = prop.getProperty(SHA_KEY);
    releaser = prop.getProperty(RELEASER_KEY);

    if (versionFileVersion < VERSION_CODE) {
      adaptFromLegacy();
    } else {
      ensureValidity();
    }
  }

  public Version(@Nonnull String name, @Nonnull String version, int releaseCode,
      int subReleaseCode, @Nonnull SubReleaseKind subReleaseKind) {
    this(name, version, releaseCode, subReleaseCode, subReleaseKind, null, null, null);
  }

  public Version(@Nonnull String name, @Nonnull String version, int releaseCode,
      int subReleaseCode, @Nonnull SubReleaseKind subReleaseKind, @CheckForNull String releaser,
      @CheckForNull String buildId, @CheckForNull String codeBase) {
    this.version = version;
    this.releaseName = name;
    this.releaser = releaser;
    this.releaseCode = releaseCode;
    this.subReleaseCode = subReleaseCode;
    this.subReleaseKind = subReleaseKind;
    this.buildId = buildId;
    this.codeBase = codeBase;

    ensureValidity();
  }

  private void adaptFromLegacy() {
    if (buildId != null && buildId.isEmpty()) {
      buildId = null;
    }

    if (codeBase != null && codeBase.isEmpty()) {
      codeBase = null;
    }

    if (codeBase == null && buildId == null) {
      releaser = null;
    } else {
      releaser = "<unknown>";
    }

    if (subReleaseCode == 0 ||
        codeBase == null    ||
        buildId == null     ||
        subReleaseKind == SubReleaseKind.ENGINEERING) {
      subReleaseKind = SubReleaseKind.ENGINEERING;
      subReleaseCode = 0;

      // Cut the last part beginning from '-' if possible
      // (transform 1.2-a19 to 1.2)
      int idx = version.lastIndexOf('-');
      if (idx >= 0) {
        version = version.substring(0, idx);
      }

      releaser = null;
    }
  }

  private void ensureValidity() {
    if (releaser != null && releaser.isEmpty()) {
      releaser = null;
    }

    if (releaser != null) {
      if (buildId != null && buildId.isEmpty()) {
        buildId = null;
      }

      if (codeBase != null && codeBase.isEmpty()) {
        codeBase = null;
      }

      if (codeBase == null && buildId == null) {
        releaser = null;
      }
    } else {
      buildId = null;
      codeBase = null;
    }

    if (subReleaseCode == 0 ||
        codeBase == null    ||
        buildId == null     ||
        releaser == null    ||
        subReleaseKind == SubReleaseKind.ENGINEERING) {
      subReleaseKind = SubReleaseKind.ENGINEERING;
      subReleaseCode = 0;
    }
  }

  @Nonnull
  public String getVersion() {
    return version + ((subReleaseKind == SubReleaseKind.ENGINEERING) ? "-eng" : "");
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

  @CheckForNull
  public String getReleaser() {
    return releaser;
  }

  @Nonnull
  public String getVerboseVersion() {
    String str;

    str = getVersion() + " '" + releaseName + "'";

    if (releaser != null) {
      str += " (";
      if (buildId != null) {
        str += buildId;
        if (codeBase != null) {
          str += " ";
        }
      }
      if (codeBase != null) {
        str += codeBase;
      }
      str += " by " + releaser + ')';
    }

    return str;
  }

  public boolean isOlderThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) < 0;
  }

  public boolean isOlderThan(int releaseCode, int subReleaseCode) throws UncomparableVersion {
    return compareTo(releaseCode, subReleaseCode) < 0;
  }

  public boolean isOlderOrEqualThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) <= 0;
  }

  public boolean isOlderOrEqualThan(int releaseCode, int subReleaseCode)
      throws UncomparableVersion {
    return compareTo(releaseCode, subReleaseCode) <= 0;
  }

  public boolean isNewerThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) > 0;
  }

  public boolean isNewerThan(int releaseCode, int subReleaseCode) throws UncomparableVersion {
    return compareTo(releaseCode, subReleaseCode) > 0;
  }

  public boolean isNewerOrEqualThan(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) >= 0;
  }

  public boolean isNewerOrEqualThan(int releaseCode, int subReleaseCode)
      throws UncomparableVersion {
    return compareTo(releaseCode, subReleaseCode) >= 0;
  }

  public boolean isSame(@Nonnull Version other) throws UncomparableVersion {
    return compareTo(other) == 0;
  }

  public boolean isSame(int releaseCode, int subReleaseCode) throws UncomparableVersion {
    return compareTo(releaseCode, subReleaseCode) == 0;
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

  private boolean isComparable(int releaseCode, int subReleaseCode) {
    return !(releaseCode <= 0 || subReleaseCode <= 0);
  }

  int compareTo(int releaseCode, int subReleaseCode) throws UncomparableVersion {
    if (!isComparable(releaseCode, subReleaseCode)) {
      throw new UncomparableVersion(
          "Version " + releaseCode + "." + subReleaseCode + " is not comparable");
    }

    if (!isComparable()) {
      throw new UncomparableVersion(
          getVerboseVersion() + " is not comparable");
    }

    if (this.releaseCode > releaseCode || (
        this.releaseCode == releaseCode
        && this.subReleaseCode > subReleaseCode)) {
      return 1;
    }


    if (this.releaseCode < releaseCode || (
        this.releaseCode == releaseCode
        && this.subReleaseCode < subReleaseCode)) {
      return -1;
    }

    return 0;
  }

  int compareTo(@Nonnull Version other) throws UncomparableVersion {
    if (!other.isComparable()) {
      throw new UncomparableVersion(
          getVerboseVersion() + " is not comparable");
    }

    return compareTo(other.getReleaseCode(), other.getSubReleaseCode());
  }

  @Override
  @Nonnull
  public String toString() {
    return releaseCode + "." + subReleaseCode + "-" + subReleaseKind;
  }

  public void store(@Nonnull OutputStream out) throws IOException {
    Properties prop = new Properties();

    prop.put(VERSION_CODE_KEY, Integer.toString(VERSION_CODE));
    prop.put(VERSION_KEY, version);
    prop.put(RELEASE_NAME_KEY, releaseName);
    prop.put(RELEASE_CODE_KEY, Integer.toString(releaseCode));
    prop.put(SUB_RELEASE_CODE_KEY, Integer.toString(subReleaseCode));
    prop.put(SUB_RELEASE_KIND_KEY, subReleaseKind.toString());
    if (buildId != null) {
      prop.put(BUILD_ID_KEY, buildId);
    }
    if (codeBase != null) {
      prop.put(SHA_KEY, codeBase);
    }
    if (releaser != null) {
      prop.put(RELEASER_KEY, releaser);
    }

    prop.store(out, "Version description");
  }
}
