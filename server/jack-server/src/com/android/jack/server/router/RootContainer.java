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

package com.android.jack.server.router;

import com.android.sched.util.log.LoggerFactory;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * container handling errors and runtime exceptions.
 */
public class RootContainer implements Container {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  private final Container guarded;

  public RootContainer(@Nonnull Container guarded) {
    this.guarded = guarded;
  }

  @Override
  public void handle(Request request, Response response) {
    logger.log(Level.INFO, "Route request for " + request.getMethod() + " " +
        request.getAddress().toString());
    try {
      guarded.handle(request, response);
    } catch (Error | RuntimeException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      // response may be closed but lets try anyway
      response.setContentLength(0);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      try {
        response.close();
      } catch (IOException ioe) {
        logger.log(Level.FINE, "Exception during close: ", ioe);
      }
    }
  }

}
