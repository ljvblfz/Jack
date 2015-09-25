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

package com.android.jack.server.tasks;

import com.android.jack.server.HasVersion;
import com.android.jack.server.JackHttpServer;
import com.android.sched.util.Version;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Returns launcher version.
 */
public class GetLauncherVersion extends GetVersions {

  @Nonnull
  private static final Logger logger = Logger.getLogger(GetLauncherVersion.class.getName());

  public GetLauncherVersion(@Nonnull JackHttpServer jackServer) {
    super("server", jackServer);
  }

  @Override
  protected Collection<? extends HasVersion> getVersionnedElements() {
    final Version version;
    try {
      version = new Version("jack-launcher",
          jackServer.getLauncherHandle().getLauncherClassLoader());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read Jack-launcher version properties", e);
      throw new AssertionError();
    }
    return Collections.singleton(new HasVersion() {
      @Override
      @Nonnull
      public Version getVersion() {
          return version;
      }
    });
  }

}
