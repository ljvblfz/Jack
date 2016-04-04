package com.android.jack.experimental.incremental;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {DependenciesTest001.class, DependenciesTest002.class,
    DependenciesTest003.class, DependenciesTest004.class, DependenciesTest005.class,
    DependenciesTest006.class, DependenciesTest007.class, DependenciesTest008.class,
    DependenciesTest009.class, DependenciesTest010.class, DependenciesTest011.class,
    DependenciesTest012.class, DependenciesTest013.class, DependenciesTest014.class,
    DependenciesTest015.class, IncrementalTests.class})
public class DependencyAllTests {
}