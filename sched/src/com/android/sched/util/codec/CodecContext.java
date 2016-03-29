/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.codec;

import com.android.sched.util.RunnableHooks;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.WrongPermissionException;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Object containing context local to a {@code Config} for {@link StringCodec} based parser.
 */
public class CodecContext {
  private boolean debug = false;

  @CheckForNull
  private RunnableHooks hooks;
  @Nonnull
  private Charset defaultCharset = Charset.defaultCharset();

  @Nonnull
  private InputStream standardInput = System.in;
  @Nonnull
  private Charset standardInputCharset = Charset.defaultCharset();

  @Nonnull
  private PrintStream standardOutput = System.out;
  @Nonnull
  private Charset standardOutputCharset = Charset.defaultCharset();

  @Nonnull
  private PrintStream standardError = System.err;
  @Nonnull
  private Charset standardErrorCharset = Charset.defaultCharset();

  @CheckForNull
  private Directory workingDirectory;

  @Nonnull
  public CodecContext setDebug() {
    this.debug = true;

    return this;
  }

  public boolean isDebug() {
    return debug;
  }

  @CheckForNull
  public RunnableHooks getRunnableHooks() {
    return hooks;
  }

  @Nonnull
  public CodecContext setHooks(@Nonnull RunnableHooks hooks) {
    this.hooks = hooks;

    return this;
  }

  @Nonnull
  public Charset getDefaultCharset() {
    return defaultCharset;
  }

  public void setDefaultCharset(@Nonnull Charset charset) {
    this.defaultCharset = charset;
  }

  @Nonnull
  public InputStream getStandardInput() {
    return standardInput;
  }

  public void setStandardInput(@Nonnull InputStream standardInput) {
    this.standardInput = standardInput;
  }

  @Nonnull
  public Charset getStandardInputCharset() {
    return standardInputCharset;
  }

  public void setStandardInputCharset(@Nonnull Charset charset) {
    this.standardInputCharset = charset;
  }

  @Nonnull
  public PrintStream getStandardOutput() {
    return standardOutput;
  }

  public void setStandardOutput(@Nonnull PrintStream standardOutput) {
    this.standardOutput = standardOutput;
  }

  @Nonnull
  public Charset getStandardOutputCharset() {
    return standardOutputCharset;
  }

  public void setStandardOutputCharset(@Nonnull Charset charset) {
    this.standardOutputCharset = charset;
  }

  @Nonnull
  public PrintStream getStandardError() {
    return standardError;
  }

  public void setStandardError(@Nonnull PrintStream standardError) {
    this.standardError = standardError;
  }

  @Nonnull
  public Charset getStandardErrorCharset() {
    return standardErrorCharset;
  }

  public void setStandardErrorCharset(@Nonnull Charset charset) {
    this.standardErrorCharset = charset;
  }

  @CheckForNull
  public Directory getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(@Nonnull File workingDirectory) throws NotDirectoryException,
      WrongPermissionException, NoSuchFileException {
    try {
      this.workingDirectory = new Directory(workingDirectory.getPath(), null, Existence.MUST_EXIST,
          Permission.EXECUTE, ChangePermission.NOCHANGE);
    } catch (CannotChangePermissionException e) {
      // we're not changing the permissions
      throw new AssertionError(e);
    } catch (FileAlreadyExistsException e) {
      // we're not creating the directory
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      // we're not creating the directory
      throw new AssertionError(e);
    }
  }
}
