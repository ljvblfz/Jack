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

package com.android.jack.test.comparator;

import com.android.jack.comparator.DifferenceFoundException;
import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link Comparator} uses the {@code diff} shell command to compare 2 files.
 */
public class ComparatorDiff extends ComparatorFile {

  public ComparatorDiff(@Nonnull File reference, @Nonnull File candidate) {
    super(reference, candidate);
  }

  @Override
  public void compare() throws DifferenceFoundException, ComparatorException {
    try {
      ExecuteFile ef = new ExecuteFile(
          "diff " + candidate.getAbsolutePath() + " " + reference.getAbsolutePath());
      ef.inheritEnvironment();
      ef.setOut(System.out);
      ef.setErr(System.err);

      int exitStatus = ef.run();
      switch (exitStatus) {
        case 0:
          break;
        case 1:
          throw new DifferenceFoundException();
        default:
          throw new ComparatorException("Exit status: " + exitStatus);
      }
    } catch (ExecFileException e) {
      throw new ComparatorException(e);
    } catch (IOException e) {
      throw new ComparatorException(e);
    }
  }
}
