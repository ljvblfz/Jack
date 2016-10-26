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
import com.android.jack.server.router.PartParserRouter.PartParser;
import com.android.jack.server.type.TextPlain;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;

import org.simpleframework.http.Part;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Parser for boolean {@link Part}
 */
public class TextPlainPartParser<T> implements PartParser<T> {

  @CheckForNull
  private final T defaultValue;

  private final boolean hasDefaultValue;

  @Nonnull
  private final StringCodec<T> codec;

  public TextPlainPartParser(@Nonnull StringCodec<T> codec) {
    this.codec = codec;
    this.defaultValue = null;
    hasDefaultValue = false;
  }

  public TextPlainPartParser(@Nonnull StringCodec<T> codec, @CheckForNull T defaultValue) {
    this.codec = codec;
    this.defaultValue = defaultValue;
    hasDefaultValue = true;
  }

  @Override
  @CheckForNull
  public T parse(@CheckForNull Part part) throws IOException,
    ParsingException,
    TypeNotSupportedException {
    if (part == null) {
      if (hasDefaultValue) {
        return defaultValue;
      } else {
        throw new ParsingException("Part is missing");
      }
    } else {
      String contentType = part.getContentType().getType();
      if (TextPlain.CONTENT_TYPE_NAME.equals(contentType)) {
        CodecContext context = new CodecContext();
        String partString = part.getContent();
        T value = codec.checkString(context, partString);
        if (value == null) {
          value = codec.parseString(context, partString);
        }
        return value;
      } else {
        throw new TypeNotSupportedException(contentType);
      }
    }
  }
}