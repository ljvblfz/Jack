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

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Request;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.parse.ContentTypeParser;

import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Route based on Accept content type
 */
public class AcceptContentTypeRouter extends ContentTypeRouter {

  @Nonnull
  private static Logger logger = Logger.getLogger(AcceptContentTypeRouter.class.getName());

  public AcceptContentTypeRouter() {
    this(new ErrorContainer(Status.NOT_ACCEPTABLE));
  }

  public AcceptContentTypeRouter(@Nonnull Container primary) {
    super(primary);
  }

  @Override
  @Nonnull
  protected String getDescription() {
    return "Accept";
  }

  @Override
  @CheckForNull
  protected ContentType getContentType(@Nonnull Request request) {
    String accept = request.getValue("accept");
    return accept != null ? new ContentTypeParser(accept) : null;
  }
}