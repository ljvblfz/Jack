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
import com.android.jack.server.api.v01.NotInstalledException;
import com.android.jack.server.api.v01.ServerException;

import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Install new server.
 */
public class InstallServer extends SynchronousAdministrativeTask {

  @Nonnull
  private static final Logger logger = Logger.getLogger(InstallServer.class.getName());

  public InstallServer(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    Part jarPart = request.getPart("jar");
    assert jarPart.getContentType().getType().equals("application/octet-stream");
    Part forcePart = request.getPart("force");
    assert forcePart != null;

    InputStream jarIn = null;
    try {
      boolean force = "true".equals(forcePart.getContent());
      jarIn = jarPart.getInputStream();
      jackServer.shutdown();
      jackServer.getLauncherHandle().replaceServer(jarIn, force);
    } catch (ServerException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    } catch (NotInstalledException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      response.setStatus(Status.BAD_REQUEST);
      return;
    } finally {
      if (jarIn != null) {
        try {
          jarIn.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Exception during close", e);
        }
      }
    }

    response.setStatus(Status.OK);
  }
}
