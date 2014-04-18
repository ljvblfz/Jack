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

package com.android.jack.backend;

import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.Resource;
import com.android.jack.scheduling.feature.Resources;
import com.android.jack.util.BytesStreamSucker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Writer of resources.
 */
@Description("Writer of resources")
@Name("ResourceWriter")
@Support(Resources.class)
@Synchronized
public class ResourceWriter implements RunnableSchedulable<JPackage> {

  @Override
  public synchronized void run(@Nonnull JPackage pack) throws Exception {
    OutputVDir outputVDir = pack.getSession().getOutputVDir();
    assert outputVDir != null;
    VDirPathFormatter formatter = new VDirPathFormatter(outputVDir);
    List<Resource> resources = pack.getResources();
    for (Resource resource : resources) {
      InputVFile inputFile = resource.getVFile();
      String path = formatter.getName(pack, inputFile.getName());
      OutputVFile outputFile = outputVDir.createOutputVFile(path);
      InputStream is = inputFile.openRead();
      OutputStream os = outputFile.openWrite();
      try {
        BytesStreamSucker sucker = new BytesStreamSucker(is, os);
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
