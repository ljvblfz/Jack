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

  private static class ToolchainFilter extends Filter {

    @Nonnull
    private IToolchain candidate;

    @Nonnull
    private IToolchain reference;

    public ToolchainFilter(@Nonnull IToolchain candidate, @Nonnull IToolchain reference) {
      this.candidate = candidate;
      this.reference = reference;
    }

    @Override
    @Nonnull
    public String describe() {
      return "Filters excluded toolchains";
    }

    @Override
    public boolean shouldRun(@Nonnull Description description) {
      KnownIssue knownIssueAnnot = description.getAnnotation(KnownIssue.class);

      if (knownIssueAnnot == null) {
        return true;
      }

      return (knownIssueAnnot.candidate().length > 0 || knownIssueAnnot.reference().length > 0)
          && (isValidToolchain(candidate, knownIssueAnnot.candidate())
                 && isValidToolchain(reference, knownIssueAnnot.reference()));
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
    try {
      filter(new ToolchainFilter(AbstractTestTools.getCandidateToolchain(),
          AbstractTestTools.getReferenceToolchain()));
    } catch (NoTestsRemainException e) {
      throw new InitializationError(e);
    }
  }

}
