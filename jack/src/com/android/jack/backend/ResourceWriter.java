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

import com.android.jack.Options;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.jack.scheduling.feature.Resources;
import com.android.jack.util.BytesStreamSucker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
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
@Name("ResourceWriter")
@Support(Resources.class)
public class ResourceWriter implements RunnableSchedulable<JSession> {

  @Nonnull
  private final OutputVDir outputVDir;

  {
    if (ThreadConfig.get(Options.GENERATE_JACK_FILE).booleanValue()) {
      Container containerType = ThreadConfig.get(Options.JACK_OUTPUT_CONTAINER_TYPE);
      if (containerType == Container.DIR) {
        outputVDir = ThreadConfig.get(Options.JACK_FILE_OUTPUT_DIR);
      } else {
        outputVDir = ThreadConfig.get(Options.JACK_FILE_OUTPUT_ZIP);
      }
    } else {
      assert ThreadConfig.get(Options.GENERATE_DEX_FILE).booleanValue();
      Container containerType = ThreadConfig.get(Options.DEX_OUTPUT_CONTAINER_TYPE);
      assert containerType == Container.ZIP;
      outputVDir = ThreadConfig.get(Options.DEX_ZIP_OUTPUT);
    }
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    assert outputVDir != null;
    List<Resource> resources = session.getResources();
    for (Resource resource : resources) {
      InputVFile inputFile = resource.getVFile();
      VPath path = resource.getPath();
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
