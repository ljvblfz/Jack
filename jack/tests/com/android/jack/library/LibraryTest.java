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

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputZipVFS;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

public class LibraryTest {
    @BeforeClass
    public static void setUpClass() {
      LibraryTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }

    @Test
    public void testEmptyLibraryInClassPath() throws Exception {
      File emptyLib = createEmptyLibrary();

      TestTools.compileSourceToDex(new Options(),
          TestTools.getJackTestsWithJackFolder("library/test001"),
          TestTools.getDefaultBootclasspathString() + File.pathSeparator + emptyLib.getPath(),
          TestTools.createTempFile("library001", ".zip"), /* zip = */ true);
    }

    @Test
    public void testRscLibraryInClassPath() throws Exception {
      File emptyLib = createRscLibrary();

      TestTools.compileSourceToDex(new Options(),
          TestTools.getJackTestsWithJackFolder("library/test001"),
          TestTools.getDefaultBootclasspathString() + File.pathSeparator + emptyLib.getPath(),
          TestTools.createTempFile("library001", ".zip"), /* zip = */ true);
    }

    @Test
    public void testImportEmptyLibrary() throws Exception {
      File lib = createEmptyLibrary();
      Options options = new Options();
      options.addJayceImport(lib);
      TestTools.compileSourceToDex(options,
          TestTools.getJackTestsWithJackFolder("library/test001"),
          TestTools.getDefaultBootclasspathString(),
          TestTools.createTempFile("library001", ".zip"), /* zip = */ true);
    }

    @Test
    public void testImportRscLibrary() throws Exception {
      File lib = createRscLibrary();
      Options options = new Options();
      options.addJayceImport(lib);
      File out = TestTools.createTempFile("library001", ".jack");
      TestTools.compileSourceToJack(options,
          TestTools.getJackTestsWithJackFolder("library/test001"),
          TestTools.getDefaultBootclasspathString(),
          out, /* zip = */ true);

      RunnableHooks hooks = new RunnableHooks();
      InputVFS vfs = new InputZipVFS(new InputZipFile(out.getPath(), hooks, Existence.MUST_EXIST,
          ChangePermission.NOCHANGE));
      try {
        InputJackLibrary inputJackLibrary = JackLibraryFactory.getInputLibrary(vfs);
        Assert.assertTrue(inputJackLibrary.containsFileType(FileType.RSC));
      } finally {
        vfs.close();
      }

    }

    @Nonnull
    private File createEmptyLibrary() throws IOException, Exception {
      File emptyLib = TestTools.createTempFile("empty", ".jack");
      Options options = new Options();
      options.setJayceOutputZip(emptyLib);
      TestTools.runCompilation(options);
      return emptyLib;
    }

    @Nonnull
    private File createRscLibrary() throws IOException, Exception {
      File emptyLib = TestTools.createTempFile("rsc", ".jack");
      Options options = new Options();
      options.addResource(TestTools.getJackTestLibFolder("library/test001"));
      options.setJayceOutputZip(emptyLib);
      TestTools.runCompilation(options);
      return emptyLib;
    }
}
