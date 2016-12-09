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

import com.android.jack.server.TypeNotSupportedException;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.log.LoggerFactory;

import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Router parsing and checking {@link Part}.
 */
public class PartParserRouter<T> implements Container {

  /**
   * Parser for {@link Part}
   */
  public static interface PartParser<T>{
    @CheckForNull
    T parse(@CheckForNull Part part) throws IOException, ParsingException,
      TypeNotSupportedException;
  }

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Container errorContainer;

  @Nonnull
  private final Container mainContainer;

  @Nonnull
  private final String partName;

  @Nonnull
  private final PartParser<T> parser;

  @Nonnull
  private final Object key;

  public PartParserRouter(@Nonnull String partName, @Nonnull PartParser<T> parser,
      @Nonnull Container mainContainer) {
    this(partName,
        parser,
        partName,
        mainContainer,
        new ErrorContainer(Status.BAD_REQUEST));
  }

  public PartParserRouter(@Nonnull String partName,
      @Nonnull PartParser<T> parser,
      @Nonnull Object key,
      @Nonnull Container mainContainer,
      @Nonnull Container errorContainer) {
    this.errorContainer = errorContainer;
    this.partName = partName;
    this.parser = parser;
    this.key = key;
    this.mainContainer = mainContainer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handle(@Nonnull Request request, @Nonnull Response response) {
    Part part = request.getPart(partName);
    try {
      request.getAttributes().put(key, parser.parse(part));
      mainContainer.handle(request, response);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read part '" + partName + "' of the request", e);
      response.setContentLength(0);
      response.setStatus(Status.BAD_REQUEST);
      try {
        response.close();
      } catch (IOException c) {
        logger.log(Level.SEVERE, "Exception during close: ", c);
      }
    } catch (ParsingException e) {
      logger.log(Level.WARNING, "Failed to parse part '" + partName + "' of the request", e);
      response.setContentLength(0);
      response.setStatus(Status.BAD_REQUEST);
      try {
        response.close();
      } catch (IOException c) {
        logger.log(Level.SEVERE, "Exception during close: ", c);
      }
    } catch (TypeNotSupportedException e) {
      logger.log(Level.FINE, "Part '" + partName + "' has an unsupported type '" + e.getType()
      + "'");
      errorContainer.handle(request, response);
    }
  }
}