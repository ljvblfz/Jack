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

package com.android.jack.server.tasks;

import com.android.jack.api.cli02.Cli02Config;
import com.android.jack.api.v01.Cli01Config;
import com.android.jack.server.JackHttpServer;
import com.android.jack.server.type.CommandOutRaw;

import org.simpleframework.http.Response;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

/**
 * Service task: Execute one Jack command.
 */
public class JackTaskRawOut extends JackTask<CommandOutRaw> {

  public JackTaskRawOut(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void installJackOutErr(Cli01Config jack, CommandOutRaw commandOut) {
    jack.setStandardError(commandOut.getErrPrintStream());
    jack.setStandardOutput(commandOut.getOutPrintStream());
  }

  @Override
  @Nonnull
  protected CommandOutRaw createCommandOut(Response response, Charset outCharset)
      throws IOException {
    return new CommandOutRaw(response.getByteChannel(), Charset.defaultCharset(), outCharset);
  }

  @Override
  protected void installJackOutErr(Cli02Config jack, CommandOutRaw commandOut) {
    jack.setStandardError(commandOut.getErrPrintStream());
    jack.setStandardOutput(commandOut.getOutPrintStream());
  }

}
