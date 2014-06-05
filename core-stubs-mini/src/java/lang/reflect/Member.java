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

package java.lang.reflect;

public interface Member {
  @java.lang.SuppressWarnings(value = {"unchecked"})
  public abstract java.lang.Class<?> getDeclaringClass();

  public abstract int getModifiers();

  public abstract java.lang.String getName();

  public abstract boolean isSynthetic();

  public static final int PUBLIC = 0;
  public static final int DECLARED = 1;
}
