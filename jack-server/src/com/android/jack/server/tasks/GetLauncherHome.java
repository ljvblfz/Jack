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

package com.android.jack.server.tasks;

import com.android.jack.server.JackHttpServer;
import com.android.jack.server.type.TextPlain;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Get installation directory.
 */
public class GetLauncherHome extends SynchronousServiceTask {

  @Nonnull
  private static Logger logger = Logger.getLogger(GetLauncherHome.class.getName());

  public GetLauncherHome(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    PrintStream out = null;
    try {
      response.setContentType(TextPlain.CONTENT_TYPE_NAME + "; Charset="
          + TextPlain.getPreferredTextPlainCharset(request).name());
      out = response.getPrintStream();
      out.append(jackServer.getLauncherHandle().getServerDir().getAbsolutePath());
      out.append('\n');
      response.setStatus(Status.OK);
    } catch (UnsupportedCharsetException e) {
      logger.log(Level.SEVERE, "Unsupported charset", e);
      response.setContentLength(0);
      response.setStatus(Status.NOT_ACCEPTABLE);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write response", e);
      response.setContentLength(0);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
}