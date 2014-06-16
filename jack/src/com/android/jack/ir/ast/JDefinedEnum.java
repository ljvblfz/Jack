/*
 * Copyright 2007 Google Inc.
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
package com.android.jack.ir.ast;


import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.sched.item.Description;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Java enum type reference expression.
 */
@Description("Java enum type reference expression")
public class JDefinedEnum extends JDefinedClass implements JEnum {

  /*
   * TODO(gwt): implement traverse?
   */

  @Nonnull
  private final List<JEnumField> enumList = new ArrayList<JEnumField>();

  private boolean hasOrdinal = true;

  public JDefinedEnum(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
      @Nonnull JPackage enclosingPackage, @Nonnull ClassOrInterfaceLoader loader) {
    super(info, name, modifier, enclosingPackage, loader);
    assert JModifier.isEnum(modifier);
  }

  @Override
  public void addField(@Nonnull JField field) {
    if (field instanceof JEnumField) {
      if (hasOrdinal) {
        JEnumField enumField = (JEnumField) field;
        int ordinal = enumField.ordinal();
        if (ordinal == JEnumField.ORDINAL_UNKNOWN) {
          hasOrdinal = false;
          enumList.clear();
        } else {
          while (ordinal >= enumList.size()) {
            enumList.add(null);
          }
          enumList.set(ordinal, enumField);
        }
      }
    }
    super.addField(field);
  }

  /**
   * Returns the list of enum fields in this enum.
   */
  @Nonnull
  public List<JEnumField> getEnumList() {
    loader.ensureFields(this);
    assert hasOrdinal;
    return enumList;
  }
}
