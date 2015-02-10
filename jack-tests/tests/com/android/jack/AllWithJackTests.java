package com.android.jack;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
    AllUnitTests.class,
    AllTests.class
  })
public class AllWithJackTests {}
