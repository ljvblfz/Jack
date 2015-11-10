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

package com.android.jack.resource;

import com.android.jack.Options;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.jack.library.FileType;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.ResourceInInputLibraryLocation;
import com.android.jack.scheduling.feature.Resources;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.stream.ByteStreamSucker;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Writer of resources.
 */
@Description("Writer of resources")
@Name("LibraryResourceWriter")
@Support(Resources.class)
public class LibraryResourceWriter implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    OutputJackLibrary ojl = session.getJackOutputLibrary();
    List<Resource> resources = session.getResources();
    boolean generateLibFromIncremental =
        ThreadConfig.get(Options.GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER).booleanValue();
    for (Resource resource : resources) {
      if (!(resource.getLocation() instanceof ResourceInInputLibraryLocation)
          || !generateLibFromIncremental) {
        InputVFile inputFile = resource.getVFile();
        VPath path = resource.getPath();
        OutputVFile outputFile = ojl.createFile(FileType.RSC, path);
        InputStream is = inputFile.getInputStream();
        OutputStream os = outputFile.getOutputStream();
        try {
          ByteStreamSucker sucker = new ByteStreamSucker(is, os);
          sucker.suck();
        } finally {
          if (is != null) {
            is.close();
          }
          if (os != null) {
            os.close();
          }
        }
      }
    }
  }
}
