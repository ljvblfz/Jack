package com.android.jack.frontend;

import com.android.jack.test.junit.JackTestRunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite only related to ECJ.
 * This test suite must be executed each time ECJ is updated.
 */
@RunWith(JackTestRunner.class)
@SuiteClasses(value = {ECJTest.class, ParserTest.class})
public class ECJUpdateAllTests {}
