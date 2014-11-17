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

package com.android.jack.multidex.test003.jack;


public class TestAnnotationResolution {

    public void testAnnotation() throws Exception {
        Class<?> clazz = Class.forName("com.google.annotationresolution.test." + "Annotated");
        clazz.getAnnotations();
        clazz = Class.forName("com.google.annotationresolution.test." + "Annotated2");
        clazz.getAnnotations();
        clazz = Class.forName("com.google.annotationresolution.test." + "Annotated3");
        clazz.getAnnotations();
   }

}
