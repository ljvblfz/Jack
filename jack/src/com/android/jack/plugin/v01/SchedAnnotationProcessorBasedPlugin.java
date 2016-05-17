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

package com.android.jack.plugin.v01;

import com.android.jack.plugin.PluginLocation;
import com.android.sched.build.SchedAnnotationProcessor;
import com.android.sched.reflections.AnnotationProcessorReflectionManager;
import com.android.sched.reflections.ReflectionManager;

import javax.annotation.Nonnull;

/**
 * A base implementation of a {@link Plugin} which uses the {@link SchedAnnotationProcessor} to
 * discover configuration.
 */
public abstract class SchedAnnotationProcessorBasedPlugin implements Plugin {
  @Override
  @Nonnull
  public final ReflectionManager getReflectionManager() {
    return new AnnotationProcessorReflectionManager(this.getClass().getClassLoader(),
        new PluginLocation(this));
  }
}
