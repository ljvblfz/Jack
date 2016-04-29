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
 * Route based on path
 */
public class PathRouter implements Container {

  @Nonnull
  private static Logger logger = Logger.getLogger(PathRouter.class.getName());

  @Nonnull
  private final Map<String, Container> registry = new HashMap<String, Container>();
  @Nonnull
  private final Container primary;

  public PathRouter() {
    this(new ErrorContainer(Status.NOT_FOUND));
  }

  public PathRouter(@Nonnull Container primary) {
    this.primary = primary;
  }

  public <T extends Container> PathRouter add(@Nonnull String path, @Nonnull T container) {
    assert !registry.containsKey(path);
    registry.put(path, container);
    return this;
  }

  @Override
  public void handle(@Nonnull Request request, @Nonnull Response response) {
    String normalizedPath = request.getPath().getPath();

    logger.log(Level.FINE, "Route request for path '" + normalizedPath + "'");

    Container container = registry.get(normalizedPath);
    if (container != null) {
      container.handle(request, response);
    } else {
      primary.handle(request, response);
    }
  }
}