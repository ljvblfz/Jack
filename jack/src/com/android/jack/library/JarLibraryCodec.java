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

package com.android.jack.library;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.FileOrDirCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.file.ZipException;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} is used to create an instance of a {@link JarLibrary}.
 */
public class JarLibraryCodec extends FileOrDirCodec<JarLibrary> {

  public JarLibraryCodec() {
    super(Existence.MUST_EXIST, Permission.READ);
  }

  @Override
  @Nonnull
  public JarLibrary parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public JarLibrary checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    InputZipFile inputZipFile;
    try {
      inputZipFile = new InputZipFile(context.getWorkingDirectory(), string);
    } catch (NotFileException e) {
      throw new ParsingException(e.getMessage(), e);
    } catch (NoSuchFileException e) {
      throw new ParsingException(e.getMessage(), e);
    } catch (WrongPermissionException e) {
      throw new ParsingException(e.getMessage(), e);
    } catch (ZipException e) {
      throw new ParsingException(e.getMessage(), e);
    }
    return new JarLibrary(inputZipFile);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a jar file (" + getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "jar";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull JarLibrary data) {
    return data.getPath();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull JarLibrary data) {
  }
}
