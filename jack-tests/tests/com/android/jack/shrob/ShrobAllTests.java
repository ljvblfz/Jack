package com.android.jack.shrob;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
//    AnnotationsTest.class,
    FlattenPackageTests.class,
//    ObfuscationWithAttributesTests.class,
//    ObfuscationWithDebugInfoTests.class,
    ObfuscationWithMappingTests.class,
    ObfuscationWithoutMappingTests.class,
    RepackagingTest.class,
    SeedTests.class,
    ShrinkMultiDexTests.class,
    ShrinkTests.class,
    ShrobRuntimeTests.class
    })
public class ShrobAllTests {
}