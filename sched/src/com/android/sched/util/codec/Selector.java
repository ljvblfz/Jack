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

package com.android.sched.util.codec;

import com.google.common.base.Joiner;

import com.android.sched.reflections.ReflectionFactory;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.util.codec.Parser.ValueDescription;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.ReflectDefaultCtorFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This abstract parser is used to discover implementations annotated with
 * {@link ImplementationName}.
 *
 * @param <T> the base type of all implementations
 */
public abstract class Selector<T> {
  @Nonnull
  private final Class<T> type;
  @CheckForNull
  private Map<String, Class<? extends T>> propertyValues;
  @CheckForNull
  private List<ValueDescription> descriptions;

  public Selector(@Nonnull Class<T> type) {
    this.type = type;
  }

  @Nonnull
  public String getUsage() {
    ensureScan();
    assert propertyValues != null;
    List<String> values = new ArrayList<String>(propertyValues.keySet());

    Collections.sort(values, new Comparator<String>(){
      @Override
      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }});

    StringBuilder sb = new StringBuilder();
    return Joiner.on(',').appendTo(sb.append('{'), values).append('}').toString();
  }

  @Nonnull
  public String getVariableName() {
    VariableName variableName = type.getAnnotation(VariableName.class);
    if (variableName == null || variableName.value() == null) {
      throw new AssertionError(type.getCanonicalName() + " has no or a wrong @"
          + VariableName.class.getSimpleName());
    }

    return variableName.value();
  }

  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    if (descriptions == null) {
      ensureScan();
      assert propertyValues != null;

      descriptions = new ArrayList<ValueDescription>(propertyValues.size());

      for (Class<? extends T> subClass : propertyValues.values()) {
        ImplementationName value = subClass.getAnnotation(ImplementationName.class);
        assert value != null;

        if (!value.description().isEmpty()) {
          descriptions.add(new ValueDescription(value.name(), value.description()));
        }
      }

      Collections.sort(descriptions, new Comparator<ValueDescription>(){
        @Override
        public int compare(ValueDescription o1, ValueDescription o2) {
          return o1.getValue().compareToIgnoreCase(o2.getValue());
        }});

    }

    assert descriptions != null;
    return descriptions;
  }

  @Nonnull
  public Class<? extends T> getClass(@Nonnull String string) throws ParsingException {
    ensureScan();
    assert propertyValues != null;

    Class<? extends T> value = propertyValues.get(string);

    if (value == null) {
      throw new ParsingException(
          "The value must be " + getUsage() + " but is '" + string + "'");
    }

    return value;
  }

  @Nonnull
  public String getName(@Nonnull Class<? extends T> type) {
    ensureScan();
    assert propertyValues != null;

    for (Entry<String, Class<? extends T>> entry : propertyValues.entrySet()) {
      if (entry.getValue() == type) {
        return entry.getKey();
      }
    }

    throw new ConfigurationError("The class '" + type.getName() + "' does not have @"
        + ImplementationName.class.getSimpleName() + " annotation");
  }

  public boolean checkClass(@Nonnull Class<? extends T> type) {
    ensureScan();
    assert propertyValues != null;
    for (Entry<String, Class<? extends T>> entry : propertyValues.entrySet()) {
      if (entry.getValue() == type) {
        return true;
      }
    }

    return false;
  }

  @Nonnull
  public List<String> getNames(@Nonnull Class<? extends T> type) {
    List<String> list = new ArrayList<String>();

    ensureScan();
    assert propertyValues != null;
    for (Entry<String, Class<? extends T>> entry : propertyValues.entrySet()) {
      if (type.isAssignableFrom(entry.getValue())) {
        list.add(entry.getKey());
      }
    }

    if (list.isEmpty()) {
      throw new ConfigurationError("No sub-class of '" + type.getName() + "' have @"
        + ImplementationName.class.getSimpleName() + " annotation");
    }

    return list;
  }

  @Nonnull
  public Set<Class<? extends T>> getClasses() {
    ensureScan();
    assert propertyValues != null;

    Set<Class<? extends T>> set = new HashSet<Class<? extends T>>();
    set.addAll(propertyValues.values());

    return set;
  }

  private synchronized void ensureScan() {
    if (propertyValues == null) {
      propertyValues = new HashMap<String, Class<? extends T>>();

      ReflectionManager reflectionManager = ReflectionFactory.getManager();
      Set<Class<? extends T>> propertyValueClasses = reflectionManager.getSubTypesOf(type);
      propertyValueClasses.add(type);
      for (Class<? extends T> subClass : propertyValueClasses) {
        ImplementationName value = subClass.getAnnotation(ImplementationName.class);
        if (value != null) {
          if (propertyValues.containsKey(value.name())) {
            throw new ConfigurationError(
                "The same value " + value.name() + " is used for several classes.");
          }

          ImplementationFilter filter =
              new ReflectDefaultCtorFactory<ImplementationFilter>(value.filter(), false).create();
          if (filter.isValid()) {
            propertyValues.put(value.name(), subClass);
          }
        }
      }
    }
  }
}
