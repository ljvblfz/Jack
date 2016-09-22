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
import com.android.jack.server.api.v01.NotInstalledException;
import com.android.jack.server.api.v01.ServerException;
import com.android.sched.util.UncomparableVersion;
import com.android.sched.util.Version;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.Files;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.StringLocation;
import com.android.sched.util.stream.LocationByteStreamSucker;

import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Administrative task: Install new server.
 */
public class InstallServer extends SynchronousAdministrativeTask {

  @Nonnull
  private static final String VERSION_FILE_SUFFIX = "-version.properties";

  @Nonnull
  private static final Logger logger = Logger.getLogger(InstallServer.class.getName());
  public InstallServer(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    response.setContentLength(0);

    Part jarPart = request.getPart("jar");
    assert jarPart != null && jarPart.getContentType() != null
        && jarPart.getContentType().getType().equals("application/octet-stream");
    boolean force = ((Boolean) request.getAttribute("force")).booleanValue();

    InputStream jarIn = null;
    try {
      jarIn = jarPart.getInputStream();
      if (!force) {
        File tmpServer = Files.createTempFile("jackserver-");
        try (FileOutputStream out = new FileOutputStream(tmpServer)) {
          new LocationByteStreamSucker(jarIn, out, new StringLocation("Request input"),
              new FileLocation(tmpServer)).suck();
        }

        try (ZipFile zip = new ZipFile(tmpServer)) {
          ZipEntry entry = zip.getEntry(JackHttpServer.VERSION_CODE + VERSION_FILE_SUFFIX);
          if (entry == null) {
            throw new IOException("Given server jar is invalid, it is missing a version file");
          }
          try (InputStream versionInput = zip.getInputStream(entry);) {
            Version candidateVersion = new Version(versionInput);
            Version currentVerion = jackServer.getVersion();

            try {
              if (!candidateVersion.isNewerThan(currentVerion)) {
                if (candidateVersion.equals(currentVerion)) {
                  logger.log(Level.INFO, "Server version "
                      + currentVerion.getVerboseVersion() + " was already installed");
                  return;
                } else {
                  throw new NotInstalledException("Not installing server "
                      + candidateVersion.getVerboseVersion()
                      + " since it is not newer than current server "
                      + currentVerion.getVerboseVersion());
                }
              }
            } catch (UncomparableVersion e) {
              if (!candidateVersion.isComparable()) {
                throw new NotInstalledException("Not installing server '"
                    + candidateVersion.getVerboseVersion() + "' without force request");
              }
              // else: current is experimental or eng, candidate is not, lets proceed
            }
          }
        }

        // replace jarIn, the server jar is no longer available from the request
        jarIn.close();
        jarIn = new FileInputStream(tmpServer);
      }

      jackServer.shutdownServerOnly();
      jackServer.getLauncherHandle().replaceServer(
          jarIn,
          jackServer.getServerParameters().asMap(),
          force);
    } catch (ServerException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    } catch (CannotChangePermissionException | CannotReadException | CannotCreateFileException
           | CannotWriteException | IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    } catch (NotInstalledException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      response.setStatus(Status.BAD_REQUEST);
      return;
    } finally {
      if (jarIn != null) {
        try {
          jarIn.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Exception during close", e);
        }
      }
    }

    response.setStatus(Status.OK);
  }
}
