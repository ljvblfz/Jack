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

package com.android.sched.util.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider.Service;

import javax.annotation.Nonnull;

/**
 * An object capable of providing instances of {@link MessageDigest}.
 */
public class MessageDigestFactory implements DefaultFactory<MessageDigest> {
  @Nonnull
  private final Service service;

  public MessageDigestFactory(@Nonnull Service service) {
    this.service = service;
  }

  @Override
  @Nonnull
  public MessageDigest create() {
    try {
      return MessageDigest.getInstance(service.getAlgorithm(), service.getProvider());
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  @Nonnull
  public Class<MessageDigest> getInstantiatedClass() {
    return MessageDigest.class;
  }

  @Nonnull
  public Service getService() {
    return service;
  }
}
