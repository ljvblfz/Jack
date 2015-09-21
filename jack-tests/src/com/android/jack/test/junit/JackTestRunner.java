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

package com.android.jack.test.junit;

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.sched.util.collect.Lists;

import org.junit.experimental.categories.Categories;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import javax.annotation.Nonnull;

/**
 * This runner handles {@link KnownIssue} annotation.
 * It filters out tests if current toolchains match those listed in the annotation.
 */
public class JackTestRunner extends Categories {

  private boolean dumpTests = false;

  private static class ToolchainFilter extends Filter {

    @Nonnull
    private IToolchain candidate;

    @Nonnull
    private IToolchain reference;

    private boolean dumpTests = false;

    public ToolchainFilter(
        @Nonnull IToolchain candidate, @Nonnull IToolchain reference, boolean dumpTest) {
      this.candidate = candidate;
      this.reference = reference;
      this.dumpTests = dumpTest;
    }

    @Override
    @Nonnull
    public String describe() {
      return "Filters excluded toolchains";
    }

    @Override
    public boolean shouldRun(@Nonnull Description description) {

      boolean shouldRun = false;

      KnownIssue knownIssueAnnot = description.getAnnotation(KnownIssue.class);

      if (knownIssueAnnot == null) {
        shouldRun = true;
      } else {
        shouldRun = (knownIssueAnnot.candidate().length > 0
                     || knownIssueAnnot.reference().length > 0)
                   && (isValidToolchain(candidate, knownIssueAnnot.candidate())
                       && isValidToolchain(reference, knownIssueAnnot.reference()));
      }

      if (dumpTests && description.getMethodName() != null) {
        System.out.println(
            "  \"" + description.getClassName() + '#' + description.getMethodName() + "\": {");
        System.out.println("    \"ignored\":" + !shouldRun);
        System.out.println("  },");
        return false;
      }

      return shouldRun;
    }

    private boolean isValidToolchain(
        @Nonnull IToolchain currentToolchain,
        @Nonnull Class<? extends IToolchain>[] excludedToolchains) {
      for (Class<? extends IToolchain> c : Lists.create(excludedToolchains)) {
        if (c.isAssignableFrom(currentToolchain.getClass())) {
          return false;
        }
      }
      return true;
    }
  }

  public JackTestRunner(@Nonnull Class<?> klass, @Nonnull RunnerBuilder builder)
      throws InitializationError {
    super(klass, builder);

    dumpTests = Boolean.parseBoolean(System.getProperty("tests.dump", "false"));

    ToolchainFilter filter = new ToolchainFilter(AbstractTestTools.getCandidateToolchain(),
        AbstractTestTools.getReferenceToolchain(), dumpTests);

    try {
      filter(filter);
    } catch (NoTestsRemainException e) {
      if (!dumpTests) {
        throw new InitializationError(e);
      }
    }
  }
}
