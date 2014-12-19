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

package com.android.jack.analysis.dependency.file;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.JackUserException;
import com.android.jack.analysis.dependency.DependencyInLibraryProduct;
import com.android.jack.incremental.IncrementalException;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.file.CannotCreateFileException;

import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.Nonnull;

/**
 * This {@code RunnableSchedulable} write file dependencies in a library.
 */
@Description("Write file dependencies in a library")
@Constraint(need = FileDependencies.Collected.class)
@Produce(DependencyInLibraryProduct.class)
public class FileDependenciesInLibraryWriter implements RunnableSchedulable<JSession>{

  @Override
  public void run(@Nonnull JSession session) throws JackUserException {
    write(session.getJackOutputLibrary(), Jack.getSession().getFileDependencies());
  }

  public static void write(@Nonnull OutputJackLibrary ojl,
      @Nonnull FileDependencies fileDependencies) {
    PrintStream ps = null;
    try {
      ps = new PrintStream(
          ojl.createFile(FileType.DEPENDENCIES, FileDependencies.vpath).openWrite());
      fileDependencies.write(ps);
    } catch (CannotCreateFileException e) {
      IncrementalException incrementalException = new IncrementalException(e);
      Jack.getSession().getReporter().report(Severity.FATAL, incrementalException);
      throw new JackAbortException(incrementalException);
    } catch (IOException e) {
      IncrementalException incrementalException = new IncrementalException(e);
      Jack.getSession().getReporter().report(Severity.FATAL, incrementalException);
      throw new JackAbortException(incrementalException);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }
}