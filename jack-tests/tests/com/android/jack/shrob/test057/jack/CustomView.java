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

package com.android.jack.shrob.test057.jack;

public class CustomView {

    public int getInt() { return 42; }
    public void setInt(int i) {}

    public String getString() { return null; }
    public void setString(String s) {}

    public void setTwoFloats(float f1, float f2) {}

    public void setFourFloats(float f1, float f2, float f3, float f4) {}

    public long[] getLongArray() { return null; }
    public void setLongArray(long[] array) {}

    public String[] getStringArray() { return null; }
    public void setStringArray(String[] strings) {}

    public CustomListener getCustomListener() { return null; }
    public void setCustomListener(CustomListener cl) {}

    public void otherMethod() {}
    public void otherMethod2(int i) {}
    public void otherMethod3(Object o) {}
}
