/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.tools.jacoco;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Defines constants for the reporter.
 */
public interface Constants {
  /**
   * The default report name.
   */
  @Nonnull
  public static final String DEFAULT_REPORT_NAME = "Report";

  /**
   * The default report type.
   */
  @Nonnull
  public static final ReportType DEFAULT_REPORT_TYPE = ReportType.HTML;

  /**
   * The default output encoding.
   */
  @Nonnull
  public static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";

  /**
   * The default input encoding of source files.
   */
  @Nonnull
  public static final String DEFAULT_INPUT_ENCODING = "UTF-8";

  /**
   * The default tab width.
   */
  @Nonnegative
  public static final int DEFAULT_TAB_WIDTH = 4;
}
