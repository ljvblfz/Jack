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

import com.android.jack.server.JackHttpServer;
import com.android.jack.server.api.v01.ServerException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Reload server configuration.
 */
public class ReloadConfig extends SynchronousAdministrativeTask {

  @Nonnull
  private static Logger logger = Logger.getLogger(ReloadConfig.class.getName());

  public ReloadConfig(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    logger.log(Level.INFO, "Reloading configuration");
    try {
      jackServer.reloadConfig();
      response.setStatus(Status.OK);
    } catch (IOException | ServerException e) {
      logger.log(Level.SEVERE, "Failed to reload configuration: " + e.getMessage());
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
    }
  }
}