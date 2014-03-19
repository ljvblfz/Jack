/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.trycatch.test004.jack;

public class TryCatch {
  Object mCurApp;

  private class Drawable {
  }

  private String loadLabel(Object p) {
    return null;
  }

  private Drawable loadIcon(Object p) {
    return null;
  }

  private void setDrawable(int iconId, Drawable d) {}

  private void setText(int actionId, String text) {}

  private String getString(int actionStr, CharSequence appName) {
    return null;
  }

  // The generated dex should not contain useless 'mov' instructions into empty catch.
  void setIconAndText(int iconId, int actionId, int descriptionId, String packageName,
      int actionStr, int descriptionStr) {
    CharSequence appName = "";
    Drawable appIcon = null;

    try {
      appName = loadLabel(null);
      appIcon = loadIcon(null);
    } catch (Exception e) {
    }


    setDrawable(iconId, appIcon);
    setText(actionId, getString(actionStr, appName));
  }
}
