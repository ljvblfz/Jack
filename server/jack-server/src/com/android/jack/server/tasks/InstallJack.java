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
import com.android.jack.server.UnsupportedProgramException;
import com.android.jack.server.type.ExactCodeVersionFinder;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
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
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;
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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Administrative task: Install new Jack.
 */
public class InstallJack extends SynchronousAdministrativeTask {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

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
    try {
      Directory jackDir = new Directory(new File(jackServer.getServerDir(), programName).getPath(),
          null,
          Existence.MUST_EXIST,
          Permission.READ | Permission.WRITE,
          ChangePermission.NOCHANGE);
      jarIn = jarPart.getInputStream();
      tmpJack = com.android.sched.util.file.Files.createTempFile("jackserver-", ".tmp", jackDir);
      out = new FileOutputStream(tmpJack);
      new ByteStreamSucker(jarIn, out).suck();
      out.close();
      out = null;

      Version version;
      try {
        final URL[] path = new URL[] {tmpJack.toURI().toURL()};
        URLClassLoader tmpLoader = new URLClassLoader(path,
            this.getClass().getClassLoader());
        JackProvider tmpProvider = JackHttpServer.getJackProvider(tmpLoader, path);
        version = JackHttpServer.getJackVersion(tmpProvider);
      } catch (MalformedURLException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw new AssertionError();
      } catch (UnsupportedProgramException e) {
        logger.log(Level.WARNING, "Uploaded jar does not contain a supported Jack");
        response.setStatus(Status.BAD_REQUEST);
        return;
      }

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
      File newInstalledJack = com.android.sched.util.file.Files.createTempFile("jack-", ".jar",
          jackDir);

      try {
        try {
          Files.move(
              tmpJack.toPath(),
              newInstalledJack.toPath(),
              StandardCopyOption.REPLACE_EXISTING,
              StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
          logger.log(Level.WARNING, "Atomic move not supported for renaming '" + tmpJack.getPath()
            + "' to '" + newInstalledJack.getPath() + "'");
          Files.move(
              tmpJack.toPath(),
              newInstalledJack.toPath(),
              StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to rename '" + tmpJack
            + "' to '" + newInstalledJack + "'", e);
        if (!newInstalledJack.delete()) {
          logger.log(Level.WARNING, "Failed to delete empty file '" + newInstalledJack + "'");
        }
        response.setStatus(Status.INTERNAL_SERVER_ERROR);
        return;
      }
      jackServer.addInstalledJack(
          new Program<JackProvider>(version, newInstalledJack, null));
    } catch (IOException | CannotCreateFileException | CannotChangePermissionException
        | NotDirectoryException | WrongPermissionException | NoSuchFileException
        | FileAlreadyExistsException e) {
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
