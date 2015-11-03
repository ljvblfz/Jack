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

import com.android.jack.api.JackProvider;
import com.android.jack.server.JackHttpServer;
import com.android.jack.server.JackHttpServer.Program;
import com.android.jack.server.NoSuchVersionException;
import com.android.jack.server.type.ExactCodeVersionFinder;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.stream.ByteStreamSucker;

import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Install new Jack.
 */
public class InstallJack extends SynchronousAdministrativeTask {

  @Nonnull
  private static Logger logger = Logger.getLogger(InstallJack.class.getName());

  public InstallJack(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  // The ClassLoader does not need privileged handling
  @SuppressFBWarnings("DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    response.setContentLength(0);

    String programName = request.getPath().getName();
    Part jarPart = request.getPart("jar");
    assert jarPart != null && jarPart.getContentType() != null
        && jarPart.getContentType().getType().equals("application/octet-stream");

    boolean force = ((Boolean) request.getAttribute("force")).booleanValue();
    if (force) {
      logger.log(Level.WARNING, "Forced update is not supported when updating 'jack'");
      response.setStatus(Status.BAD_REQUEST);
      return;
    }

    InputStream jarIn = null;
    FileOutputStream out = null;
    File tmpJack = null;
    File jackDir = new File(jackServer.getServerDir(), programName);
    try {
      jarIn = jarPart.getInputStream();
      tmpJack = File.createTempFile("jackserver-", ".tmp", jackDir);
      out = new FileOutputStream(tmpJack);
      new ByteStreamSucker(jarIn, out).suck();
      out.close();
      out = null;

      URLClassLoader tmpLoader;
      try {
        tmpLoader = new URLClassLoader(new URL[] {tmpJack.toURI().toURL()},
            this.getClass().getClassLoader());
      } catch (MalformedURLException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw new AssertionError();
      }

      Version version = new Version("jack", tmpLoader);
      try {
        if (version.getSubReleaseKind() != SubReleaseKind.ENGINEERING) {
          jackServer.selectJack(
              new ExactCodeVersionFinder(version.getReleaseCode(), version.getSubReleaseCode(),
                  SubReleaseKind.ENGINEERING));
          logger.log(Level.INFO,
              "Jack version " + version.getVerboseVersion() + " was already installed");
          response.setStatus(Status.OK);
          return;
        }
      } catch (NoSuchVersionException e) {
        // expected
      }
      File newInstalledJack = File.createTempFile("jack-", ".jar", jackDir);
      if (!tmpJack.renameTo(newInstalledJack)) {
        logger.log(Level.SEVERE, "Failed to rename '" + tmpJack
            + "' to '" + newInstalledJack + "'");
        if (!newInstalledJack.delete()) {
          logger.log(Level.WARNING, "Failed to delete empty file '" + newInstalledJack + "'");
        }
        response.setStatus(Status.INTERNAL_SERVER_ERROR);
        return;
      }
      jackServer.addInstalledJack(
          new Program<JackProvider>(version, newInstalledJack, null));
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    } finally {
      if (jarIn != null) {
        try {
          jarIn.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Exception during close", e);
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Exception during close", e);
        }
      }
      if (tmpJack != null) {
        if (!tmpJack.delete()) {
          if (tmpJack.exists()) {
            logger.log(Level.SEVERE,
                "Failed to delete temp file '" + tmpJack + "'");
          }
        }
      }
    }

    response.setStatus(Status.OK);
  }
}
