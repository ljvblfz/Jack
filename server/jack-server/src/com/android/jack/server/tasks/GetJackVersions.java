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

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Return versions of available Jack.
 */
public class GetJackVersions extends GetVersions {

  @Nonnull
  private static final Logger logger = Logger.getLogger(GetJackVersions.class.getName());

  public GetJackVersions(@Nonnull JackHttpServer jackServer) {
    super("Jack", jackServer);
  }

  @Nonnull
  @Override
  protected Collection<? extends HasVersion> getVersionedElements() {
    return jackServer.getInstalledJacks();
  }

}
