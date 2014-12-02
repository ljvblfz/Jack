package com.android.jack.multidex;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
    MultiDexTests.class,
    MultiDexOverflowTests.class})
public class MultiDexAllTests {
}
