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

  @Nonnull
  private RuntimeVersion runtimeVersion;

  private static class ToolchainFilter extends Filter {

    @Nonnull
    private IToolchain candidate;

    @Nonnull
    private IToolchain reference;

    private boolean dumpTests = false;

    @Nonnull
    private RuntimeVersion runtimeVersion;

    public ToolchainFilter(@Nonnull IToolchain candidate, @Nonnull IToolchain reference,
        boolean dumpTest, @Nonnull RuntimeVersion runtimeVersion) {
      this.candidate = candidate;
      this.reference = reference;
      this.dumpTests = dumpTest;
      this.runtimeVersion = runtimeVersion;
    }

    @Override
    @Nonnull
    public String describe() {
      return "Filters excluded toolchains";
    }

    @Override
    public boolean shouldRun(@Nonnull Description description) {
      boolean knownIssue = false;
      boolean notApplicable = false;

      KnownIssue knownIssueAnnot = description.getAnnotation(KnownIssue.class);
      Runtime runtimeAnnot = description.getAnnotation(Runtime.class);

      // Special case of ecj tests that use JUnit3
      boolean isEcjTestPostM = false;
      if (description.getClassName().contains("Ecj")) {
        isEcjTestPostM =
            (description.getClassName().contains("PostM"))
                || description.getClassName().contains("EcjInterfaceMethodsTest");

        notApplicable = isEcjTestPostM && runtimeVersion.compareTo(RuntimeVersion.N) < 0;

      } else {
        // General case
        knownIssue = (knownIssueAnnot != null)
            && ((knownIssueAnnot.candidate().length == 0)
                    || !isValidToolchain(candidate, knownIssueAnnot.candidate())
                && (knownIssueAnnot.reference().length == 0
                    || !isValidToolchain(reference, knownIssueAnnot.reference())));

        notApplicable = runtimeAnnot != null && runtimeAnnot.from().compareTo(runtimeVersion) > 0;
      }

      if (dumpTests) {
        if (description.getMethodName() != null) {
          System.out.println(
              "  \"" + description.getClassName() + '#' + description.getMethodName() + "\": {");
          System.out.println("    \"notApplicable\":" + notApplicable);
          System.out.print("    \"knownIssue\":" + knownIssue);
          if (runtimeAnnot != null) {
            System.out.println(",");
            System.out.println(
                "    \"runtimePostM\":"
                    + (runtimeAnnot.from().ordinal() > RuntimeVersion.M.ordinal()));
          } else if (description.getClassName().contains("Ecj")) {
            // Special case for Ecj tests that use JUnit3
            System.out.println(",");
            System.out.println("    \"runtimePostM\":" + isEcjTestPostM);
          } else {
            System.out.println();
          }
          System.out.println("  },");

          // Don't run test, dump only
          return false;

        } else {

          // Visit types
          return true;
        }
      }

      return !knownIssue && !notApplicable;
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

    runtimeVersion =
        RuntimeVersion.valueOf(System.getProperty("runtime.version", "M").toUpperCase());

    try {
      ToolchainFilter filter = new ToolchainFilter(AbstractTestTools.getCandidateToolchain(),
          AbstractTestTools.getReferenceToolchain(), dumpTests, runtimeVersion);
      filter(filter);
    } catch (NoTestsRemainException e) {
      if (!dumpTests) {
        throw new InitializationError(e);
      }
    }
  }
}
