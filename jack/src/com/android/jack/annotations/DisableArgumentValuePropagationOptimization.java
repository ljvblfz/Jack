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

package com.android.jack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that the parameter (or all type/method parameters) shouldn't be considered
 * for argument value propagation optimization.
 *
 * If an annotation is specified on a parameter, this particular parameter
 * is not processed by the optimization.
 *
 * If an annotation is specified on a method, it is equal to having this annotation
 * is specified on all parameters of this method.
 *
 * If an annotation is specified on a class, it is equal to having this annotation
 * is specified on all methods defined in this class.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
public @interface DisableArgumentValuePropagationOptimization {
}
