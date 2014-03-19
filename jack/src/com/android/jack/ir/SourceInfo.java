/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir;

import java.io.Serializable;

/**
 * Tracks file and line information for AST nodes.
 */
public interface SourceInfo extends Serializable {

  int getEndColumn();

  String getFileName();

  SourceOrigin getOrigin();

  int getStartLine();

  int getEndLine();

  int getStartColumn();

  /**
   * Create a child node of the same type and Origin as this node. If data
   * accumulation is enabled, the derived node will inherit its Correlations
   * from this node.
   */
  SourceInfo makeChild();

  /**
   * Create a child node of the same type as this node, but with a new Origin.
   * If data accumulation is enabled, the derived node will inherit its
   * Correlations from this node.
   */
  SourceInfo makeChild(SourceOrigin origin);
}
