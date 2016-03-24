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

package com.android.jack.util.collect;

import com.android.jack.Jack;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.codec.DefaultFactorySelector;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.DefaultFactory;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.category.Private;
import com.android.sched.util.config.id.PropertyId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A wrapper around java.utils.Collections with a behavior controlled by the configuration.
 */
@HasKeyId
@VariableName("policy")
public interface UnmodifiableCollections {

  /**
   * {@link UnmodifiableCollections} with checks enabled.
   */
  @ImplementationName(iface = UnmodifiableCollections.class, name = "always-check")
  public class ChecksEnabledUnmodifiableCollection implements UnmodifiableCollections {
    /**
     * @see Collections#unmodifiableList(List)
     */
    @Override
    @Nonnull
    public <T> List<T> getUnmodifiableList(@Nonnull List<T> list) {
      return Collections.unmodifiableList(list);
    }

    /**
     * @see Collections#unmodifiableSet(Set)
     */
    @Override
    @Nonnull
    public <T> Set<T> getUnmodifiableSet(@Nonnull Set<T> set) {
      return Collections.unmodifiableSet(set);
    }

    /**
     * @see Collections#unmodifiableCollection(Collection)
     */
    @Override
    @Nonnull
    public <T> Collection<T> getUnmodifiableCollection(@Nonnull Collection<T> collection) {
      return Collections.unmodifiableCollection(collection);
    }
  }

  /**
   * {@link UnmodifiableCollections} with checks disabled.
   */
  @ImplementationName(iface = UnmodifiableCollections.class, name = "never-check")
  public class ChecksdisabledUnmodifiableCollection implements UnmodifiableCollections {
    @Override
    @Nonnull
    public <T> List<T> getUnmodifiableList(@Nonnull List<T> list) {
      return list;
    }

    @Override
    @Nonnull
    public <T> Set<T> getUnmodifiableSet(@Nonnull Set<T> set) {
      return set;
    }

    @Override
    @Nonnull
    public <T> Collection<T> getUnmodifiableCollection(@Nonnull Collection<T> collection) {
      return collection;
    }
  }

  @Nonnull
  public static final PropertyId<DefaultFactory<UnmodifiableCollections>> UNMODIFIABLE_COLLECTION =
      PropertyId.create("jack.collections.unmodifiable",
          "Policy when using an unmodifiable collection",
          new DefaultFactorySelector<UnmodifiableCollections>(UnmodifiableCollections.class))
      .addDefaultValue((Jack.getVersion().getSubReleaseKind() == SubReleaseKind.ENGINEERING)
          ? "always-check" : "never-check")
      .addCategory(Private.class);

  /**
   * @see Collections#unmodifiableList(List)
   */
  @Nonnull
  public <T> List<T> getUnmodifiableList(@Nonnull List<T> list);

  /**
   * @see Collections#unmodifiableSet(Set)
   */
  @Nonnull
  public <T> Set<T> getUnmodifiableSet(@Nonnull Set<T> set);

  /**
   * @see Collections#unmodifiableList(List)
   */
  @Nonnull
  public <T> Collection<T> getUnmodifiableCollection(@Nonnull Collection<T> collection);

}
