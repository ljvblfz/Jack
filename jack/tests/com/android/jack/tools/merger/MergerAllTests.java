/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.jack.tools.merger;

import com.android.jack.category.KnownBugs;
import com.android.jack.tools.merger.test001.MergerTest001;
import com.android.jack.tools.merger.test002.MergerTest002;
import com.android.jack.tools.merger.test003.MergerTest003;
import com.android.jack.tools.merger.test004.MergerTest004;
import com.android.jack.tools.merger.test005.MergerTest005;
import com.android.jack.tools.merger.test006.MergerTest006;
import com.android.jack.tools.merger.test007.MergerTest007;
import com.android.jack.tools.merger.test008.MergerTest008;
import com.android.jack.tools.merger.test009.MergerTest009;
import com.android.jack.tools.merger.test010.MergerTest010;
import com.android.jack.tools.merger.test011.MergerTest011;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Categories.class)
@SuiteClasses(
value = {MergerTest001.class, MergerTest002.class, MergerTest003.class,
    MergerTest004.class, MergerTest005.class, MergerTest006.class, MergerTest007.class,
    MergerTest008.class, MergerTest009.class, MergerTest010.class, MergerTest011.class})
@ExcludeCategory(KnownBugs.class)
public class MergerAllTests {
}