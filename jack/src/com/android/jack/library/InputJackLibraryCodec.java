/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.jack.LibraryException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.MessageDigestCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.file.ZipException;
import com.android.sched.vfs.BadVFSFormatException;
import com.android.sched.vfs.CaseInsensitiveFS;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;

import java.io.File;
import java.security.Provider.Service;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputJackLibrary}.
 */
public class InputJackLibraryCodec implements StringCodec<InputJackLibrary> {

  @Nonnull
  private final MessageDigestCodec messageDigestCodec = new MessageDigestCodec();

  @CheckForNull
  private String infoString;

  @Override
  @Nonnull
  public InputJackLibrary parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public InputJackLibrary checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      VFS vfs;
      Directory workingDirectory = context.getWorkingDirectory();
      File dirOrZip = FileOrDirectory.getFileFromWorkingDirectory(workingDirectory, string);
      if (dirOrZip.isDirectory()) {
        DirectFS directFS = new DirectFS(new Directory(workingDirectory,
            string,
            context.getRunnableHooks(),
            Existence.MUST_EXIST,
            Permission.READ | Permission.WRITE,
            ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);
        directFS.setInfoString(infoString);
        try {
          Service service = messageDigestCodec.checkString(context, "SHA");
          vfs = new CaseInsensitiveFS(directFS, /* numGroups = */ JackLibrary.NUM_GROUPS_FOR_DIRS,
              /* groupSize = */ JackLibrary.GROUP_SIZE_FOR_DIRS,
              new MessageDigestFactory(service), /* debug = */ false);
        } catch (BadVFSFormatException e) {
          vfs = directFS;
        }
      } else {
        @SuppressWarnings("resource")
        ReadZipFS rzFS = new ReadZipFS(new InputZipFile(workingDirectory, string));
        rzFS.setInfoString(infoString);
        vfs = rzFS;
      }

      return JackLibraryFactory.getInputLibrary(vfs);
    } catch (LibraryException e) {
      throw new ParsingException(e.getMessage(), e);
    } catch (NotFileOrDirectoryException e) {
      // we already checked it this was a dir or a file
      throw new AssertionError(e);
    } catch (FileAlreadyExistsException e) {
      // the file or dir already exists
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      // the file or dir already exists
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      // we're not changing the permissions
      throw new AssertionError(e);
    } catch (WrongPermissionException e) {
      throw new ParsingException(e.getMessage(), e);
    } catch (NoSuchFileException e) {
      throw new ParsingException(e.getMessage(), e);
    } catch (ZipException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a jack library";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "jack";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputJackLibrary data) {
    return data.getPath();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputJackLibrary data) {
  }

  @Nonnull
  public InputJackLibraryCodec setInfoString(@CheckForNull String infoString) {
    this.infoString = infoString;
    return this;
  }
}
