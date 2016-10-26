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
import com.android.sched.util.log.LoggerFactory;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

abstract class QueryVersion extends SynchronousAdministrativeTask {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  public QueryVersion(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    VersionFinder versionFinder;
    response.setContentLength(0);
    try {
      versionFinder =
          jackServer.parseVersionFinder(request.getContentType(), request.getContent());
      HasVersion found = versionFinder.select(getVersionedElements());
      if (found != null) {
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

  @Nonnull
  protected abstract Collection<? extends HasVersion> getVersionedElements();
}
