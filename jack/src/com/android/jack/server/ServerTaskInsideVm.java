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

package com.android.jack.server;

import com.android.jack.CommandLine;
import com.android.jack.Main;
import com.android.jack.Options;
import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;

import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

/**
 * {@link ServerTask} implementation.
 */
public class ServerTaskInsideVm extends CommandLine implements ServerTask {

  @Override
  public int run(@Nonnull PrintStream out, @Nonnull PrintStream err, @Nonnull File pwd,
      @Nonnull TokenIterator args) {
    List<String> list = new ArrayList<String>();
    Options options;
    try {
      while (args.hasNext()) {
        list.add(args.next());
      }
      options = Main.parseCommandLine(list);
      options.setWorkingDirectory(pwd);
      options.setStandardError(err);
      options.setStandardOutput(out);
    } catch (CmdLineException e) {
      if (e.getMessage() != null) {
        err.println(e.getMessage());
      }
      return ServerExitStatus.FAILURE_USAGE;
    } catch (NoSuchElementException e) {
      return ServerExitStatus.FAILURE_USAGE;
    } catch (WrongPermissionException e) {
      return ServerExitStatus.FAILURE_USAGE;
    } catch (NoSuchFileException e) {
      return ServerExitStatus.FAILURE_USAGE;
    } catch (NotFileOrDirectoryException e) {
      return ServerExitStatus.FAILURE_USAGE;
    } catch (CannotReadException e) {
      return ServerExitStatus.FAILURE_USAGE;
    }

    return runJack(err, options);
  }
}
