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

package com.android.jack;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {

  @Test
  public void testVersion() {
    Version version = Jack.getVersion();

    assertNotNull(version);
    assertNotNull(version.getVersion());
    assertNotNull(version.getVerboseVersion());
    assertNotNull(version.getReleaseName());
    assertTrue(version.getReleaseCode() > 0);
    assertTrue(version.getSubReleaseCode() > 0);

    if (version.getBuildId() == null || version.getCodeBase() == null) {
      assertTrue(version.getSubReleaseKind() == SubReleaseKind.ENGINEERING);
    }
  }

}
