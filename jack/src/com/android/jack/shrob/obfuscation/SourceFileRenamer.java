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

package com.android.jack.shrob.obfuscation;

import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.sourceinfo.FileSourceInfo;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.PathCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;

import java.io.File;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A visitor that rename source files.
 */
@HasKeyId
@Description("Rename source file")
public class SourceFileRenamer implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final BooleanPropertyId RENAME_SOURCEFILE = BooleanPropertyId.create(
      "jack.obfuscation.renamesourcefile",
      "Rename source file").addDefaultValue("false");

  @Nonnull
  public static final PropertyId<File> NEW_SOURCEFILE_NAME = PropertyId.create(
      "jack.obfuscation.renamesourcefile.newname",
      "New source file name", new PathCodec()).addDefaultValue("")
      .requiredIf(RENAME_SOURCEFILE.getValue().isTrue());

  @Nonnull
  private final File newSourceFileName = ThreadConfig.get(NEW_SOURCEFILE_NAME);

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    Set<FileSourceInfo> infos = session.getSourceInfoFactory().getFileSourceInfos();
    for (FileSourceInfo info : infos) {
      info.setFileName(newSourceFileName.getPath());
    }
  }

}
