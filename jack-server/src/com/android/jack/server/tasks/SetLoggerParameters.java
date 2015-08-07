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
import com.android.jack.server.ServerLogConfiguration;
import com.android.sched.util.codec.ParsingException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;


/**
 * Administrative task: Parameter server log.
 */
public class SetLoggerParameters extends SynchronousAdministrativeTask {

  @Nonnull
  private static Logger logger = Logger.getLogger(ReloadConfig.class.getName());

  public SetLoggerParameters(JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, Request request, Response response) {
    logger.log(Level.INFO, "Updating logger parameters");

    ServerLogConfiguration logConfig = jackServer.getLogConfiguration();
    try {
      logConfig.setLevel(request.getPart("level").getContent());
      int limit = Integer.parseInt(request.getPart("limit").getContent());
      if (limit < 0) {
        logger.log(Level.WARNING, "Invalid limit value " + limit);
        response.setStatus(Status.BAD_REQUEST);
        return;
      }
      logConfig.setMaxLogFileSize(limit);
      int count = Integer.parseInt(request.getPart("count").getContent());
      if (count < 1) {
        logger.log(Level.WARNING, "Invalid count value " + count);
        response.setStatus(Status.BAD_REQUEST);
        return;
      }
      logConfig.setLogFileCount(count);
    } catch (ParsingException | NumberFormatException e) {
      logger.log(Level.WARNING, "Failed to parse request", e);
      response.setStatus(Status.BAD_REQUEST);
      return;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read request", e);
      response.setStatus(Status.BAD_REQUEST);
      return;
    }

    try {
      jackServer.setLogConfiguration(logConfig);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to set logger configuration with pattern '"
          + logConfig.getLogFilePattern() + "'", e);
      response.setStatus(Status.BAD_REQUEST);
      return;
    }

  }

}
