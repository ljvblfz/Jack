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

import org.simpleframework.http.Method;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Route based on {@link Method}
 */
public class MethodRouter implements Container {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Map<String, Container> registry = new HashMap<String, Container>();
  @Nonnull
  private final Container primary;

  public MethodRouter() {
    this(new ErrorContainer(Status.METHOD_NOT_ALLOWED));
  }

  public MethodRouter(@Nonnull Container primary) {
    this.primary = primary;
  }

  public <T extends Container> MethodRouter add(@Nonnull String method,
      @Nonnull T container) {
    registry.put(method, container);
    return this;
  }

  @Override
  public void handle(@Nonnull Request request, @Nonnull Response response) {
    String method = request.getMethod();

    logger.log(Level.FINE, "Route request for method '" + method + "'");

    Container container = registry.get(method);
    if (container != null) {
      container.handle(request, response);
    } else {
      logger.log(Level.INFO, "Using primary route for method '" + method + "'");
      primary.handle(request, response);
    }
  }
}