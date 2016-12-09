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

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Route based on content type
 */
public class ContentTypeRouter implements Container {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Map<String, Container> registry = new HashMap<String, Container>();

  @Nonnull
  private final Container primaryContainer;

  public ContentTypeRouter() {
    this(new ErrorContainer(Status.UNSUPPORTED_MEDIA_TYPE));
  }

  public ContentTypeRouter(@Nonnull Container primary) {
    this.primaryContainer = primary;
  }

  /**
   * @param type the content type string without parameter, see {@link ContentType#getType()}.
   */
  public <T extends Container> ContentTypeRouter add(@Nonnull String type,
      @Nonnull T container) {
    registry.put(type, container);
    return this;
  }

  @Override
  public void handle(@Nonnull Request request, @Nonnull Response response) {
    ContentType contentType = getContentType(request);
    Container container;
    if (contentType == null) {
      logger.log(Level.INFO, "Using primary route for " + getDescription()
        + " without content type");
      container = primaryContainer;
    } else {
      String type = contentType.getType();

      logger.log(Level.FINE, "Route request for '" + type + "'");

      container = registry.get(type);
      if (container == null) {
        logger.log(Level.INFO, "Using primary route for " + getDescription()
          + " with content type '" + type + "'");
        container = primaryContainer;
      }
    }
    container.handle(request, response);
  }

  @Nonnull
  protected String getDescription() {
    return "request";
  }

  @CheckForNull
  protected ContentType getContentType(@Nonnull Request request) {
    return request.getContentType();
  }
}