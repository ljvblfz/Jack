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

package com.android.sched.vfs;

import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.HasLocation;

import java.io.InputStream;

import javax.annotation.Nonnull;

/**
 * Provider of {@link InputStream}.
 */
public interface InputStreamProvider extends HasLocation {

  @Nonnull
  InputStream getInputStream() throws WrongPermissionException;
}
