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
package com.android.jack.test.toolchain;

import com.android.sched.util.log.LogFormatter;
import com.android.sched.util.log.LoggerConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Logger configurations
 */
public enum LogLevel implements LoggerConfiguration {
  ERROR {
    @Override
    public List<PackageLevel> getLevels() {
      List<PackageLevel> setup = new ArrayList<PackageLevel>();
      setup.add(new PackageLevel("", Level.SEVERE));
      return setup;
    }
  },
  WARNING {
    @Override
    public List<PackageLevel> getLevels() {
      List<PackageLevel> setup = new ArrayList<PackageLevel>();
      setup.add(new PackageLevel("", Level.WARNING));
      return setup;
    }
  },
  DEBUG {
    @Override
    public List<PackageLevel> getLevels() {
      List<PackageLevel> setup = new ArrayList<PackageLevel>();
      setup.add(new PackageLevel("", Level.FINE));
      setup.add(new PackageLevel("com.android.sched", Level.WARNING));
      return setup;
    }
  };

  @Override
  public Collection<Handler> getHandlers() {
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(new LogFormatter());
    handler.setLevel(Level.FINEST);
    return Collections.<Handler>singletonList(handler);
  }
}