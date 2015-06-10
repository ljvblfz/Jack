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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.library.FileType;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;

import javax.annotation.Nonnull;

/**
 * Merge dexes respecting a filter
 */
@HasKeyId
@Description("Merge dexes respecting a filter which defaults to a filter accepting every type")
@Name("DexFileWriter")
public class DexFileWriter extends DexWriter
  implements RunnableSchedulable<JDefinedClassOrInterface> {

  /**
   * File name prefix of a {@code .dex} file automatically loaded in an archive.
   */
  @Nonnull
  static final String DEX_PREFIX = "classes";

  @Nonnull
  public static final String DEX_FILENAME = DEX_PREFIX + FileType.DEX.getFileExtension();

  @Nonnull
  private static final Filter<JDefinedClassOrInterface> filter =
    new Filter<JDefinedClassOrInterface>() {
      @Override
      public boolean accept(Class<? extends RunnableSchedulable<?>> runnableSchedulable,
          JDefinedClassOrInterface t) {
        return true;
      }

    };

  @Nonnull
  public static final ImplementationPropertyId<DexWritingTool> DEX_WRITING_POLICY =
      ImplementationPropertyId.create("jack.dex.output.policy",
          "Define which policy will be used to emit dex files", DexWritingTool.class)
          .addDefaultValue("single-dex");

  @Nonnull
  protected Filter<JDefinedClassOrInterface> getFilter() {
    return filter;
  }

  @Override
  public void run(JDefinedClassOrInterface type) throws Exception {
    if (!getFilter().accept(this.getClass(), type)) {
      return;
    }
    DexWritingTool writingTool = ThreadConfig.get(DexFileWriter.DEX_WRITING_POLICY);
    try {
      writingTool.merge(type);
    } catch (DexWritingException e) {
      Jack.getSession().getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }
  }

}
