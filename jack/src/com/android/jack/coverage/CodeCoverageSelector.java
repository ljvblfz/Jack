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

package com.android.jack.coverage;

import com.android.jack.Options;
import com.android.jack.digest.OriginDigestMarker;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.library.DumpInLibrary;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

/**
 * A schedulable that selects classes that needs to be instrumented based on a given filter.
 * All classes selected for code coverage are marked with a {@link CodeCoverageMarker} marker.
 */
@HasKeyId
@Description("Filters classes for code coverage")
@Support(CodeCoverage.class)
@Constraint(need = OriginalNames.class)
@Transform(add = CodeCoverageMarker.Initialized.class)
@Protect(add = JDefinedClassOrInterface.class)
public class CodeCoverageSelector implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  public static final PropertyId<CoverageFilterSet> COVERAGE_JACOCO_INCLUDES =
      PropertyId
          .create(
              "jack.coverage.jacoco.include",
              "Class names included in the code coverage instrumentation",
              new CoverageFilterSetCodec())
          .addDefaultValue(new CoverageFilterSet())
          .requiredIf(Options.CODE_COVERVAGE.getValue().isTrue())
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final PropertyId<CoverageFilterSet> COVERAGE_JACOCO_EXCLUDES =
      PropertyId
          .create(
              "jack.coverage.jacoco.exclude",
              "Class names excluded from the code coverage instrumentation",
              new CoverageFilterSetCodec())
          .addDefaultValue(new CoverageFilterSet())
          .requiredIf(Options.CODE_COVERVAGE.getValue().isTrue())
          .addCategory(DumpInLibrary.class);

  @Nonnull
  private static final SourceFormatter formatter = SourceFormatter.getFormatter();

  /**
   * A {@link CoverageFilter} singleton used to cache include and exclude properties.
   */
  private static CoverageFilter singleton = null;

  private static CoverageFilter getFilterInstance() {
    if (singleton == null) {
      singleton = new CoverageFilter(
          ThreadConfig.get(COVERAGE_JACOCO_INCLUDES), ThreadConfig.get(COVERAGE_JACOCO_EXCLUDES));
    }
    return singleton;
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface t) throws Exception {
    if (needsCoverage(t)) {
      long classId = computeClassID(t);
      t.addMarker(new CodeCoverageMarker(classId));
    }
  }

  private static boolean needsCoverage(@Nonnull JDefinedClassOrInterface declaredType) {
    if (declaredType.isExternal()) {
      // Do not instrument classes that will no be part of the output.
      return false;
    }
    if (declaredType instanceof JDefinedInterface) {
      // Interface are not covered.
      return false;
    }
    // Manage class filtering.
    CoverageFilter filter = getFilterInstance();
    String typeName = formatter.getName(declaredType);
    return filter.matches(typeName);
  }

  @Nonnull
  private static byte[] computeClassDigest(@Nonnull JDefinedClassOrInterface type)
      throws NoSuchAlgorithmException {
    OriginDigestMarker marker = type.getMarker(OriginDigestMarker.class);
    if (marker != null) {
      // Use the digest that has been already computed.
      return marker.getDigest();
    }
    // Fallback to compute digest based on the class name.
    // Note: this will cause conflicts in Jacoco if multiple classes with the same name are
    // instrumented at the same time.
    String className = formatter.getName(type);
    byte[] classNameAsBytes = className.getBytes(StandardCharsets.UTF_8);
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    messageDigest.update(classNameAsBytes);
    byte[] digest = messageDigest.digest();
    assert digest != null;
    return digest;
  }

  private static long computeClassID(@Nonnull JDefinedClassOrInterface type)
      throws NoSuchAlgorithmException {
    // Compute the digest of the class and convert it to a long.
    byte[] digest = computeClassDigest(type);
    BigInteger bigInteger = new BigInteger(digest);
    return bigInteger.longValue();
  }
}
