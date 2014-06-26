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

package com.android.jack.util.filter;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * A {@link Filter} that filters {@link JMethod}s according to their signature.
 */
@HasKeyId
@ImplementationName(iface = Filter.class, name = "method-with-signature")
public class SignatureMethodFilter implements Filter<JMethod> {

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Nonnull
  public static final PropertyId<String> METHOD_SIGNATURE_FILTER = PropertyId.create(
      "jack.internal.filter.method.signature",
      "Method signature that will be accepted by the filter",
      new SignatureCodec()).requiredIf(
      ((ImplementationPropertyId<Filter>) (Object) Options.METHOD_FILTER).getClazz()
          .isImplementedBy(SignatureMethodFilter.class));

  @Nonnull
  private final String methodSignature;

  public SignatureMethodFilter() {
    this.methodSignature = ThreadConfig.get(METHOD_SIGNATURE_FILTER);
  }

  @Override
  public boolean accept(
      @Nonnull Class<? extends RunnableSchedulable<?>> runnableSchedulable,
      @Nonnull JMethod method) {
    return (Jack.getLookupFormatter().getName(method).equals(methodSignature));
  }
}