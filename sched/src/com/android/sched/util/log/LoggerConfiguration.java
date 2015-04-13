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

package com.android.sched.util.log;

import java.util.Collection;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A logger configuration.
 */
public interface LoggerConfiguration {
  /**
   * Definition of the log level of a package.
   */
  public static class PackageLevel {
    @Nonnull
    private final String packageName;
    @Nonnull
    private final Level level;

    public PackageLevel(@Nonnull String packageName, @Nonnull Level level) {
      this.packageName = packageName;
      this.level = level;
    }

    @Nonnull
    public String getPackageName() {
      return packageName;
    }

    @Nonnull
    public Level getLevel() {
      return level;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
      if (obj instanceof PackageLevel) {
        return packageName.equals(((PackageLevel) obj).packageName)
            && level.equals(((PackageLevel) obj).level);
      }
      return super.equals(obj);
    }

    @Override
    public final int hashCode() {
      return packageName.hashCode() ^ level.hashCode();
    }
  }


  @Nonnull
  Collection<Handler> getHandlers();

  /**
   * Returns a list of {@link PackageLevel} sorted parent first, then children.
   */
  @Nonnull
  List<PackageLevel> getLevels();
}
