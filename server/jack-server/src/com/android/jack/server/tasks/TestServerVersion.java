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
import com.android.jack.server.TypeNotSupportedException;
import com.android.jack.server.VersionFinder;
import com.android.sched.util.codec.ParsingException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Test if a specific version of Jack is available.
 */
public class TestServerVersion extends SynchronousAdministrativeTask {

  @Nonnull
  private static final Logger logger = Logger.getLogger(TestServerVersion.class.getName());

  public TestServerVersion(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    VersionFinder versionFinder;
    try {
      response.setContentLength(0);
      versionFinder =
          this.jackServer.parseVersionFinder(request.getContentType(), request.getContent());
      HasVersion foundVersion =
          versionFinder.select(Collections.singletonList(jackServer));
      if (foundVersion != null) {
        response.setStatus(Status.OK);
      } else {
        response.setStatus(Status.NOT_FOUND);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read request", e);
      response.setStatus(Status.BAD_REQUEST);
    } catch (ParsingException e) {
      logger.log(Level.WARNING, "Failed to parse request", e);
      response.setStatus(Status.BAD_REQUEST);
    } catch (TypeNotSupportedException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.NOT_IMPLEMENTED);
    }
  }
}
