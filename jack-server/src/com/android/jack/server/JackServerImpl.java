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

package com.android.jack.server;

import com.android.jack.server.ServerLogConfiguration.ServerLogConfigurationException;
import com.android.jack.server.api.v01.JackServer;
import com.android.jack.server.api.v01.LauncherHandle;
import com.android.jack.server.api.v01.ServerException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Service starting Jack server.
 */
public class JackServerImpl implements JackServer {

  @Nonnull
  private static Logger logger = Logger.getLogger(JackServerImpl.class.getName());

  @CheckForNull
  private LauncherHandle launcherHandle;
  @CheckForNull
  private JackHttpServer jackServer;

  @Override
  public void setHandle(@Nonnull LauncherHandle handle) {
    this.launcherHandle = handle;
  }

  @Override
  public void onSystemStart() throws ServerException {
    assert launcherHandle != null;
    assert jackServer == null;
    try {
      JackHttpServer.cleanUp(launcherHandle);
    } catch (IOException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public void run(@Nonnull Map<String, Object> parameters)
      throws ServerException, InterruptedException {
    assert launcherHandle != null;
    assert jackServer == null;
    try {
      jackServer = new JackHttpServer(launcherHandle);
      jackServer.start(parameters);
      jackServer.waitServerShutdown();
    } catch (IOException | ServerLogConfigurationException e) {
      throw new ServerException(e.getMessage(), e);
    } catch (InterruptedException e) {
      jackServer.shutdown();
      try {
        jackServer.waitServerShutdown();
      } catch (InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        throw new ServerException("Server failed to shutdown properly", interruptedException);
      }
      throw e;
    } finally {
      if (jackServer != null) {
        try {
          jackServer.shutdown();
        } catch (Throwable t) {
          logger.log(Level.SEVERE, "Exception during final shutdown", t);
        }
      }
    }
  }
}
