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

package com.android.jack.util;

import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.codec.LongCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigurationError;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to return a {@link AndroidApiLevel}
 */
public class AndroidApiLevelCodec implements StringCodec<AndroidApiLevel> {
  @Nonnull
  private final LongCodec releasedCodec = new LongCodec();
  @CheckForNull
  private EnumCodec<AndroidApiLevel.ProvisionalLevel> provisionalCodec =
      new EnumCodec<>(AndroidApiLevel.ProvisionalLevel.class);

  public AndroidApiLevelCodec() {
    releasedCodec.setMin(1);
    provisionalCodec.ignoreCase();
  }

  public void setMinReleasedApiLevel(@Nonnegative int level) {
    releasedCodec.setMin(level);
    forbidProvisionalLevel();
  }

  public void setMinReleasedApiLevel(@Nonnull AndroidApiLevel.ReleasedLevel level) {
    setMinReleasedApiLevel(level.getLevel());
  }

  public void setMaxReleasedApiLevel(@Nonnegative int level) {
    releasedCodec.setMax(level);
    forbidProvisionalLevel();
  }

  public void setMaxReleasedApiLevel(@Nonnull AndroidApiLevel.ReleasedLevel level) {
    setMinReleasedApiLevel(level.getLevel());
  }

  public void forbidProvisionalLevel() {
    provisionalCodec = null;
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "level";
  }

  @Override
  @Nonnull
  public AndroidApiLevel parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public AndroidApiLevel checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      return new AndroidApiLevel(releasedCodec.checkString(context, string).intValue());
    } catch (ParsingException e1) {
      if (provisionalCodec != null) {
        try {
          return new AndroidApiLevel(provisionalCodec.checkString(context, string));
        } catch (ParsingException e2) {
          throw new ParsingException(
              "The value must be " + getUsage() + " but is '" + string + "'");
        }
      } else {
        throw new ParsingException(
            "The value must be " + getUsage() + " but is '" + string + "'");
      }
    }
  }

  @Override
  @Nonnull
  public String getUsage() {
    String usage = "a released API level as " + releasedCodec.getUsage();

    if (provisionalCodec != null && provisionalCodec.hasPublicEntries()) {
      usage += " or a provisional API level as " + provisionalCodec.getUsage();
    }

    return usage;
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    List<ValueDescription> descriptions = new ArrayList<>();

    descriptions.addAll(releasedCodec.getValueDescriptions());
    if (provisionalCodec != null) {
      descriptions.addAll(provisionalCodec.getValueDescriptions());
    }

    return descriptions;

  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull AndroidApiLevel data) {
    if (data.isReleasedLevel()) {
      return releasedCodec.formatValue(Long.valueOf(data.getReleasedLevel()));
    }

    assert provisionalCodec != null;
    return provisionalCodec.formatValue(data.getProvisionalLevel());
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull AndroidApiLevel data)
      throws CheckingException {
    if (data.isReleasedLevel()) {
      releasedCodec.checkValue(context, Long.valueOf(data.getReleasedLevel()));
      return;
    }

    if (provisionalCodec == null) {
      throw new CheckingException("Provisional API level not allowed");
    }

    provisionalCodec.checkValue(context, data.getProvisionalLevel());
  }
}
