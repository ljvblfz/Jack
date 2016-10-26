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
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.log.LoggerFactory;

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
  private static Logger logger = LoggerFactory.getLogger();

  public ReloadConfig(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    logger.log(Level.INFO, "Reloading configuration");
    response.setContentLength(0);
    try {
      jackServer.reloadConfig();
      response.setStatus(Status.OK);
    } catch (NotFileException | ServerException | WrongPermissionException | IOException
        | CannotCreateFileException e) {
      logger.log(Level.SEVERE, "Failed to reload configuration: " + e.getMessage());
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
    }
  }
}