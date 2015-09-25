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

package com.android.jack.server.type;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Request;
import org.simpleframework.http.parse.ContentTypeParser;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Tools for handling text/plain content type.
 */
public class TextPlain {

  @Nonnull
  private static final Logger logger = Logger.getLogger(TextPlain.class.getName());

  @Nonnull
  public static final String CONTENT_TYPE_NAME = "text/plain";

  // See RFC 2046 4.1.1 and RFC 2616 3.7.1
  @Nonnull
  public static final String EOL = "\r\n";

  @Nonnull
  private static final Charset DEFAULT_CHARSET = StandardCharsets.US_ASCII;

  @Nonnull
  public static Charset getPreferredTextPlainCharset(@Nonnull Request request) {
    UnsupportedCharsetException pending = null;
    for (String accepted : request.getValues("accept")) {
      ContentType expectedContentType = new ContentTypeParser(accepted);
      if (CONTENT_TYPE_NAME.equals(expectedContentType.getType())) {
        try {
          return getCharset(expectedContentType);
        } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
          logger.log(Level.FINE, "Failed to parse charset from " + expectedContentType.getCharset(),
              e);
          if (pending == null) {
            pending = new UnsupportedCharsetException(e.getMessage());
          } else {
            pending.addSuppressed(e);
          }
        }
      }
    }
    assert pending != null;
    throw pending;
  }

  @Nonnull
  public static Charset getCharset(@Nonnull ContentType contentType)
      throws IllegalCharsetNameException, UnsupportedCharsetException {
    assert CONTENT_TYPE_NAME.equals(contentType.getType()) : contentType.getType();

    String charsetName = contentType.getCharset();
    Charset charset;
    if (charsetName == null) {
      charset = DEFAULT_CHARSET;
    } else {
      charset = Charset.forName(charsetName);
    }

    return charset;
  }

}
