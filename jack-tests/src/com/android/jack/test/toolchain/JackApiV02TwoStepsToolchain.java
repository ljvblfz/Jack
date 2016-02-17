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

package com.android.jack.test.toolchain;

import com.android.jack.Options;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link Toolchain} uses Jack through v02 API and perform two steps
 * compilation. Compilation from sources to to executables are thus
 * performed twice, by using an intermediate library.
 */
public class JackApiV02TwoStepsToolchain extends JackApiV02Toolchain implements TwoStepsToolchain {

  @Nonnull
  private File jackPrebuilt;

  @CheckForNull
  private Map<String, String> properties;

  JackApiV02TwoStepsToolchain(File jackPrebuilt) {
    super(jackPrebuilt);
    this.jackPrebuilt = jackPrebuilt;
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {
    File tmpFile = AbstractTestTools.createTempFile("lib", "intermediate");
    srcToLib(tmpFile, true, sources);

    JackApiV02Toolchain secondStep = new JackApiV02Toolchain(jackPrebuilt);
    secondStep.addProperty(Options.USE_PREBUILT_FROM_LIBRARY.getName(), "false");
    secondStep.setSourceLevel(sourceLevel);
    secondStep.setVerbose(isVerbose);
    secondStep.setOutputStream(outRedirectStream);
    secondStep.setErrorStream(errRedirectStream);
    secondStep.setWithDebugInfos(withDebugInfos);
    // STOPSHIP: getSingleAbstractMethod need to do a lookup
    secondStep.addToClasspath(classpath.toArray(new File[classpath.size()]));

    for (Entry<String, String> entry : getProperties().entrySet()) {
      secondStep.addProperty(entry.getKey(), entry.getValue());
    }

    secondStep.libToExe(tmpFile, out, zipFile);

  }

  @Override
  @Nonnull
  public JackApiV02TwoStepsToolchain addProperty(
      @Nonnull String propertyName, @Nonnull String propertyValue) {
    super.addProperty(propertyName, propertyValue);
    getProperties().put(propertyName, propertyValue);
    return this;
  }

  @Nonnull
  private Map<String, String> getProperties() {
    if (properties == null) {
      properties = new HashMap<String, String>();
    }
    return properties;
  }

}

