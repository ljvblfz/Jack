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

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: initiate server shutdown and returns after the last compilation.
 */
public class Shutdown implements Container {

  @Nonnull
  private static Logger logger = Logger.getLogger(Shutdown.class.getName());
  @Nonnull
  private final JackHttpServer jackServer;

  public Shutdown(@Nonnull JackHttpServer jackServer) {
    this.jackServer = jackServer;
  }

  @Override
  public void handle(@Nonnull Request request, @Nonnull Response response) {
    try {
      logger.log(Level.INFO, "Force shutdown");
      jackServer.shutdown();
      jackServer.waitServerShutdown();
      response.setStatus(Status.OK);
    } catch (InterruptedException e) {
      logger.log(Level.SEVERE, "Shutdown thread was interrupted before server shutdown completion");
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      Thread.currentThread().interrupt();
    } catch (Error | RuntimeException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Exception during close of request: " + request.toString(), e);
      }
    }
  }
}