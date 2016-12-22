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

package com.android.sched.util.codec;

import com.android.sched.util.LineSeparator;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.StandardOutputKind;
import com.android.sched.util.file.WriterFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.NoLocation;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class WriterFileCodecTest {

  @Test
  public void testFormatValueStdout() {
    String string =
        new WriterFileCodec(Existence.MAY_EXIST)
            .formatValue(new WriterFile(StandardOutputKind.STANDARD_OUTPUT));
    Assert.assertEquals(FileCodec.STANDARD_IO_NAME, string);
  }

  @Test
  public void testFormatValueStderr() {
    String string =
        new WriterFileCodec(Existence.MAY_EXIST)
            .formatValue(new WriterFile(StandardOutputKind.STANDARD_ERROR));
    Assert.assertEquals(FileCodec.STANDARD_ERROR_NAME, string);
  }

  @Test
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public void testFormatValueFile()
      throws NotFileException, FileAlreadyExistsException, CannotCreateFileException,
          CannotChangePermissionException, WrongPermissionException, NoSuchFileException,
          IOException {
    File file = File.createTempFile("sched-test", "tmp");
    try {
      String string =
          new WriterFileCodec(Existence.MAY_EXIST)
              .formatValue(new WriterFile(/* workingDirectory = */ null,
                  file.getPath(),
                  Charset.forName("UTF-8"),
                  LineSeparator.SYSTEM,
                  /* bufferSize = */ 4 * 1024,
                  /* hooks = */ null,
                  Existence.MAY_EXIST,
                  ChangePermission.NOCHANGE,
                  /* append = */ false));
      Assert.assertEquals(file.getPath(), string);
    } finally {
      file.delete();
    }
  }

  @Test
  @Ignore // Known Schedlib bug
  public void testFormatValueStreamOnly() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String string =
        new WriterFileCodec(Existence.MAY_EXIST)
            .formatValue(new WriterFile(out, new NoLocation()));
    Assert.assertEquals("TBD", string);
  }
}
