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

package com.android.jack.compile.androidtree.services;

import com.android.jack.TestTools;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
public class ServicesCompilationTest {

  private static final File[] CLASSPATH = new File[] {
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/android.policy_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core-junit_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/telephony-common_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.core_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.accessibility_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.appwidget_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.backup_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.devicepolicy_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.print_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.restrictions_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.usage_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.usb_intermediates/classes.jack"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.voiceinteraction_intermediates/classes.jack"),

};

private static final File[] REF_CLASSPATH = new File[] {
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/android.policy_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core-junit_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jar"),
    TestTools
        .getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/telephony-common_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.core_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.accessibility_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.appwidget_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.backup_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.devicepolicy_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.print_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.restrictions_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.usage_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.usb_intermediates/classes.jar"),
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/services.voiceinteraction_intermediates/classes.jar"),};

private static final File S_SOURCELIST = TestTools.getTargetLibSourcelist("services");

@Test
@Category(RedundantTests.class)
public void compileServices() throws Exception {
  AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
  toolchain.addToClasspath(CLASSPATH)
  .srcToExe(
      AbstractTestTools.createTempDir(),
      /* zipFile = */ false,
      S_SOURCELIST);
}

@Test
@Category(SlowTests.class)
public void compareServicesStructure() throws Exception {
  SourceToDexComparisonTestHelper helper = new CheckDexStructureTestHelper(S_SOURCELIST);
  helper.setCandidateClasspath(CLASSPATH);
  helper.setReferenceClasspath(REF_CLASSPATH);
  helper.setSourceLevel(SourceLevel.JAVA_7);

  ComparatorDex comparator = helper.createDexFileComparator();
  comparator.setCompareDebugInfoBinary(false);
  comparator.setCompareInstructionNumber(true);
  comparator.setInstructionNumberTolerance(0.45f);

  helper.runTest(comparator);
}

}
