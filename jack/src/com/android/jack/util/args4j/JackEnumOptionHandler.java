/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.util.args4j;

import com.google.common.base.Joiner;

import com.android.sched.util.log.LoggerFactory;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.EnumOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An args4J enum handler that don't ignore the metavar attribute.
 */
public class JackEnumOptionHandler<T extends Enum<T>> extends OptionHandler<T> {

  @Nonnull
  private final Class<T> enumType;

  @Nonnull
  private final EnumOptionHandler<T> delegate;

  @SuppressWarnings("unchecked")
  public JackEnumOptionHandler(
      @Nonnull CmdLineParser parser, @Nonnull OptionDef option, @Nonnull Setter<? super T> setter) {
    super(parser, option, setter);
    enumType = ((Class<T>) setter.getType());

    this.delegate = new EnumOptionHandler<T>(parser, option, setter, enumType);
  }

  @Override
  public int parseArguments(@Nonnull Parameters params) throws CmdLineException {
    return delegate.parseArguments(params);
  }

  @Override
  @CheckForNull
  public String getDefaultMetaVariable() {
    T[] enumConstants = enumType.getEnumConstants();
    List<String> items = new ArrayList<>(enumConstants.length);
    for (Enum<T> item : enumConstants) {
      try {
        if (enumType.getField(item.name()).getAnnotation(Deprecated.class) == null) {
          items.add(item.name().toLowerCase());
        }
      } catch (NoSuchFieldException e) {
        throw new AssertionError();
      } catch (SecurityException e) {
        // Best effort
        items.add(item.name());
        LoggerFactory.getLogger()
            .info(
                "A SecurityException occured while getting field '"
                    + item.name()
                    + "' from enum '"
                    + enumType.getName()
                    + "': "
                    + e.getMessage());
      }
    }
    return new StringBuffer()
        .append('[')
        .append(Joiner.on(" | ").join(items))
        .append(']')
        .toString();
  }

}