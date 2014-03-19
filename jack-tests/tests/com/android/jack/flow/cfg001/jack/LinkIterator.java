/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.flow.cfg001.jack;


public class LinkIterator<ET> {
  int pos;
  int link;

  public LinkIterator(int size, int location) {
      if (location >= 0) {
      } else {
          throw new IndexOutOfBoundsException();
      }
  }

  public LinkIterator(int size, int location, int unused) {
    if (location >= 0 && location <= size) {
      if(size == 0) {
        location = 3;
      } else {
        location = 5;
      }
    } else {
        throw new IndexOutOfBoundsException();
    }
}

  public LinkIterator(int size, int location, byte unused) {
    if (location >= 0) {
      if (location < size / 2) {
        link = 0;
        for (pos = -1; pos + 1 < location; pos++) {
            link ++;
        }
    } else {
        for (pos = size; pos >= location; pos--) {
            link --;
        }
    }
    } else {
        throw new IndexOutOfBoundsException();
    }
}

}
