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
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.core.Container;

import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Route based on content type of a part. ContentType of the request must be "multipart/form-data".
 */
public class PartContentTypeRouter extends ContentTypeRouter {

  @Nonnull
  private static Logger logger = Logger.getLogger(PartContentTypeRouter.class.getName());

  @Nonnull
  private final String partName;

  public PartContentTypeRouter(@Nonnull String partName) {
    super();
    this.partName = partName;
  }

  public PartContentTypeRouter(@Nonnull String partName, @Nonnull Container primary) {
    super(primary);
    this.partName = partName;
  }

  @Override
  @CheckForNull
  protected ContentType getContentType(@Nonnull Request request) {
    Part part = request.getPart(partName);
    return part == null ? null : part.getContentType();
  }
}