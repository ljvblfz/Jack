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

import com.google.common.base.Splitter;

import com.android.jack.api.ConfigNotSupportedException;
import com.android.jack.api.JackProvider;
import com.android.jack.api.v01.Cli01Config;
import com.android.jack.api.v01.ConfigurationException;
import com.android.jack.api.v01.UnrecoverableException;
import com.android.jack.server.JackHttpServer;
import com.android.jack.server.JackHttpServer.Program;
import com.android.jack.server.NoSuchVersionException;
import com.android.jack.server.TypeNotSupportedException;
import com.android.jack.server.UnsupportedProgramException;
import com.android.jack.server.VersionFinder;
import com.android.jack.server.type.CommandOut;
import com.android.sched.util.codec.ParsingException;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.parse.ContentTypeParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Service task: Execute one Jack command.
 */
public class JackTask extends SynchronousServiceTask {

  @Nonnull
  private static Logger logger = Logger.getLogger(JackTask.class.getName());

  public JackTask(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  private static final int JACK_STATUS_ERROR = 47;

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {

    ContentType accept = new ContentTypeParser(request.getValue("accept"));
    String charsetName = accept.getCharset();
    Charset outCharset;
    if (charsetName == null) {
      outCharset = CommandOut.DEFAULT_COMMAND_OUT_CHARSET;
    } else {
      outCharset = Charset.forName(charsetName);
    }

    String cli;
    VersionFinder versionFinder;
    File pwd;
    try {
      Part cliPart = request.getPart("cli");
      assert cliPart != null;
      cli = cliPart.getContent();
      Part versionPart = request.getPart("version");
      assert versionPart != null;
      ContentType versionType = versionPart.getContentType();
      assert versionType != null;
      versionFinder = jackServer.parseVersionFinder(versionType, versionPart.getContent());
      Part pwdPart = request.getPart("pwd");
      assert pwdPart != null;
      pwd = new File(pwdPart.getContent());

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read request", e);
      response.setContentLength(0);
      response.setStatus(Status.BAD_REQUEST);
      return;
    } catch (ParsingException e) {
      logger.log(Level.WARNING, "Failed to parse request", e);
      response.setContentLength(0);
      response.setStatus(Status.BAD_REQUEST);
      return;
    } catch (TypeNotSupportedException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setContentLength(0);
      response.setStatus(Status.NOT_IMPLEMENTED);
      return;
    }

    Cli01Config jack;
    Program<JackProvider> program;
    try {
      program = jackServer.selectJack(versionFinder);
    } catch (NoSuchVersionException e) {
      logger.log(Level.SEVERE, "Failed to load Jack", e);
      response.setContentLength(0);
      response.setStatus(Status.BAD_REQUEST);
      return;
    }
    try {
      JackProvider jackProvider = jackServer.getProvider(program);
      jack = jackProvider.createConfig(Cli01Config.class);
    } catch (ConfigNotSupportedException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setContentLength(0);
      response.setStatus(Status.BAD_REQUEST);
      return;
    } catch (UnsupportedProgramException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      response.setContentLength(0);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    }

    // split is only skipping trailing empty string not heading
    String[] command = Splitter.on(" ").trimResults().omitEmptyStrings().splitToList(cli)
        .toArray(new String[0]);

    logger.log(Level.INFO, "Read command '" + cli + "', pwd: '" + pwd.getPath() + "'");

    response.setContentType(CommandOut.JACK_COMMAND_OUT_CONTENT_TYPE + "; version=1");
    int commandStatus = JACK_STATUS_ERROR;
    CommandOut commandOut;
    try {
      commandOut = new CommandOut(response.getByteChannel(), outCharset);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Exception while opening response: ", e);
      response.setContentLength(0);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      PrintStream err = commandOut.getErr();
      long start = System.currentTimeMillis();

      try {
        logger.log(Level.INFO, "Run Compilation #" + taskId);
        jack.setStandardError(err);
        jack.setStandardOutput(commandOut.getOut());
        jack.setWorkingDirectory(pwd);
        start = System.currentTimeMillis();
        commandStatus = jack.getTask(command).run();
      } catch (ConfigurationException e) {
        err.println("ERROR: Configuration: " + e.getMessage());
      } catch (IllegalStateException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        err.println("ERROR: Internal server error: " + e.getMessage() +
            ". Please see server logs");
      } catch (UnrecoverableException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        err.println("ERROR: Compiler error: " + e.getMessage() + ". Please see server logs");
      } finally {
        long stop = System.currentTimeMillis();
        logger.log(Level.INFO, "Compilation #" + taskId + " return exit code " + commandStatus);
        logger.log(Level.INFO, "Compilation #" + taskId + " run in " + (stop - start) + " ms");
      }
    } finally {
      try {
        commandOut.close(commandStatus);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Exception while writing response: ", e);
        response.setContentLength(0);
        response.setStatus(Status.INTERNAL_SERVER_ERROR);
        return;
      }
    }
    response.setStatus(Status.OK);
  }
}