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

package com.android.sched.scheduler;

import com.android.sched.filter.NoFilter;
import com.android.sched.item.Component;
import com.android.sched.item.Feature;
import com.android.sched.item.MarkerOrComponent;
import com.android.sched.item.NoFeature;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarker;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.ComponentFilter;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.ProcessorSchedulable;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.schedulable.With;
import com.android.sched.util.Reflect;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a {@link ProcessorSchedulable} with all annotations and signatures extracted.
 */
public class ManagedRunnable extends ManagedSchedulable {
  @Nonnull
  private final Scheduler scheduler;

  // Public runnable
  @Nonnull
  private final Class<? extends ProcessorSchedulable<? extends Component>> runnable;

  // @Support
  @Nonnull
  private final FeatureSet supportedFeatures;

  // @Constraint and @ToSupport
  @Nonnull
  private final Map<FeatureSet, TagOrMarkerOrComponentSet> neededTags =
      new HashMap<FeatureSet, TagOrMarkerOrComponentSet>();
  @Nonnull
  private final Map<FeatureSet, TagOrMarkerOrComponentSet> unsupportedTags =
      new HashMap<FeatureSet, TagOrMarkerOrComponentSet>();

  // @Transform
  @Nonnull
  private final TagOrMarkerOrComponentSet addedTags;
  @Nonnull
  private final TagOrMarkerOrComponentSet removedTags;
  @Nonnull
  private final TagOrMarkerOrComponentSet modifiedTags;

  // @Protect
  @Nonnull
  private final TagOrMarkerOrComponentSet protectAddingTags;
  @Nonnull
  private final TagOrMarkerOrComponentSet protectRemovingTags;
  @Nonnull
  private final TagOrMarkerOrComponentSet protectModifyingTags;
  @Nonnull
  private final TagOrMarkerOrComponentSet unprotectByAddingTags;
  @Nonnull
  private final TagOrMarkerOrComponentSet unprotectByRemovingTags;

  // @Produce
  @Nonnull
  private final ProductionSet productions;

  // @Filter
  @Nonnull
  private final ComponentFilterSet neededFilters;
  @Nonnull
  private final FeatureSet filtersIfAll;
  @Nonnull
  private final FeatureSet filtersUnlessOne;

  // @ExclusiveAccess
  @Nonnull
  private Class<? extends Component> exclusiveAccess;
  // @Access
  @Nonnull
  private Class<? extends Component> access;

  // @Use
  @Nonnull
  private final List<Class<?>> useTools = new ArrayList<Class<?>>();

  // Class ... implements RunnableSchedulable<T>
  @Nonnull
  private Class<? extends Component> schedulableOn;

  // Nonnull field is actually initialized during construction, in a private method
  @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public ManagedRunnable(@Nonnull Scheduler scheduler,
      @Nonnull Class<? extends ProcessorSchedulable<? extends Component>> runnable)
      throws SchedulableNotConformException {
    super(runnable);

    this.scheduler = scheduler;
    this.runnable  = runnable;

    addedTags = scheduler.createTagOrMarkerOrComponentSet();
    removedTags = scheduler.createTagOrMarkerOrComponentSet();
    modifiedTags = scheduler.createTagOrMarkerOrComponentSet();
    productions = scheduler.createProductionSet();
    protectAddingTags = scheduler.createTagOrMarkerOrComponentSet();
    protectRemovingTags = scheduler.createTagOrMarkerOrComponentSet();
    protectModifyingTags = scheduler.createTagOrMarkerOrComponentSet();
    unprotectByAddingTags = scheduler.createTagOrMarkerOrComponentSet();
    unprotectByRemovingTags = scheduler.createTagOrMarkerOrComponentSet();
    neededFilters = scheduler.createComponentFilterSet();
    filtersIfAll = scheduler.createFeatureSet();
    filtersUnlessOne = scheduler.createFeatureSet();
    supportedFeatures = scheduler.createFeatureSet();

    extractUse(runnable);
    extractSchedulableOn(runnable);
    extractAccesses(runnable);

    extractTransform(runnable);
    extractProduce(runnable);
    extractProtect(runnable);
    extractSupport(runnable);
    extractFilters(runnable);

    for (Class<?> tool : useTools) {
      extractTransform(tool);
      extractProduce(tool);
      extractProtect(tool);
      extractSupport(tool);
    }

    // Extract constraint and optional MUST be extracted after support

    extractConstraint(runnable);
    extractOptional(runnable);

    for (Class<?> tool : useTools) {
      extractConstraint(tool);
      extractOptional(tool);
    }

    checkValidity();

    LoggerFactory.getLogger().log(Level.CONFIG, "{0}", this);
  }

  // TODO(jplesot) Add more checks here
  private void checkValidity() throws SchedulableNotConformException {
    if (addedTags.containsOne(removedTags)) {
      throw new SchedulableNotConformException("RunnableSchedulable '" + getName()
          + "' can not have same tags " + addedTags.getIntersection(removedTags).toString()
          + " in added and removed");
    }

    if (getAllPossibleNeededTags().containsOne(getAllPossibleUnsupportedTags())) {
      throw new SchedulableNotConformException("RunnableSchedulable '" + getName()
          + "' can not have same tags "
          + getAllPossibleNeededTags().getIntersection(getAllPossibleUnsupportedTags()).toString()
          + " in needed and unsupported");
    }

    if (removedTags.containsOne(getAllPossibleUnsupportedTags())) {
      throw new SchedulableNotConformException("RunnableSchedulable '" + getName()
          + "' can not have same tags "
          + removedTags.getIntersection(getAllPossibleUnsupportedTags()).toString()
          + " in removed and unsupported");
    }
  }

  /**
   * @return the {@link ProcessorSchedulable} associated with the {@link ManagedRunnable}
   */
  @Nonnull
  public Class<? extends ProcessorSchedulable<? extends Component>> getRunnableSchedulable() {
    return this.runnable;
  }

  /**
   * Get a set of needed tags, markers or components when the given set of features are
   * present.
   *
   * @param features the set of features present
   * @return the set of needed tags, markers or components
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getNeededTags(@Nonnull FeatureSet features) {
    TagOrMarkerOrComponentSet set = new TagOrMarkerOrComponentSet(getDefaultNeededTags());

    for (Map.Entry<FeatureSet, TagOrMarkerOrComponentSet> entry : neededTags.entrySet()) {
      if (features.equals(entry.getKey())) {
        set.addAll(entry.getValue());
      }
    }

    return set;
  }

  /**
   * Get a set of unsupported tags, markers or components when the given set of features are
   * present.
   *
   * @param features the set of features present
   * @return the set of unsupported tags, markers or components
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getUnsupportedTags(@Nonnull FeatureSet features) {
    TagOrMarkerOrComponentSet set = new TagOrMarkerOrComponentSet(getDefaultUnsupportedTags());

    for (Map.Entry<FeatureSet, TagOrMarkerOrComponentSet> entry : unsupportedTags.entrySet()) {
      if (features.equals(entry.getKey())) {
        set.addAll(entry.getValue());
      }
    }

    return set;
  }

  /**
   * @return the set of needed tags, markers or components when all features are present
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getAllPossibleNeededTags() {
    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();

    for (TagOrMarkerOrComponentSet ts : neededTags.values()) {
      set.addAll(ts);
    }

    return set;
  }

  /**
   * @return the set of unsupported tags, markers or components when all features are present
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getAllPossibleUnsupportedTags() {
    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();

    for (TagOrMarkerOrComponentSet ts : unsupportedTags.values()) {
      set.addAll(ts);
    }

    return set;
  }

  /**
   * @return the set of needed tags, markers or components when {@code getSupportedFeatures} are
   * present
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getDefaultNeededTags() {
    TagOrMarkerOrComponentSet set =
        new TagOrMarkerOrComponentSet(neededTags.get(supportedFeatures));

    return set;
  }

  /**
   * @return the set of unsupported tags, markers or components when {@code getSupportedFeatures}
   * are present
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getDefaultUnsupportedTags() {
    TagOrMarkerOrComponentSet set =
        new TagOrMarkerOrComponentSet(unsupportedTags.get(supportedFeatures));

    return set;
  }

  /**
   * Given a set of features present and a set of tags, markers or components presents, return
   * if the {@link ManagedRunnable} is allowed to run.
   *
   * @param features the set of features
   * @param tags the set of tags, markers or component
   * @return true if the {@link ManagedRunnable} is allowed to run, false otherwise
   */
  public boolean isCompatible(
      @Nonnull FeatureSet features, @Nonnull TagOrMarkerOrComponentSet tags) {
    return tags.containsAll(getNeededTags(features))
        && tags.containsNone(getUnsupportedTags(features));
  }

  /**
   * @return the set of tags, markers or components after running this runnable
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getAfterTags(@Nonnull TagOrMarkerOrComponentSet beforeTags) {
    TagOrMarkerOrComponentSet afterTags = new TagOrMarkerOrComponentSet(beforeTags);

    afterTags.addAll(getAddedTags());
    afterTags.removeAll(getRemovedTags());

    return afterTags;
  }

  /**
   * @return the set of missing tags, markers or components to run this runnable
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getMissingTags(
      @Nonnull FeatureSet features, @Nonnull TagOrMarkerOrComponentSet tags) {
    TagOrMarkerOrComponentSet needed = new TagOrMarkerOrComponentSet(getNeededTags(features));

    needed.removeAll(tags);

    return needed;
  }

  /**
   * @return the set of forbidden tags, markers or components to run this runnable
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getForbiddenTags(
      @Nonnull FeatureSet features, @Nonnull TagOrMarkerOrComponentSet tags) {
    TagOrMarkerOrComponentSet unsupported =
        new TagOrMarkerOrComponentSet(getUnsupportedTags(features));

    return unsupported.getIntersection(tags);
  }

  /**
   * @return the number of constraints to run this runnable
   */
  @Nonnegative
  public int getConstraintCount(@Nonnull FeatureSet features) {
    return getNeededTags(features).getSize() + getUnsupportedTags(features).getSize();
  }

  /**
   * @return the number of unsatisfied constraint to run this runnable
   */
  @Nonnegative
  public int getUnsatisfiedConstraintCount(
      @Nonnull FeatureSet features, @Nonnull TagOrMarkerOrComponentSet tags) {
    return getMissingTags(features, tags).getSize() + getForbiddenTags(features, tags).getSize();
  }

  /**
   * @return a copy of the set of {@link Production} produced
   */
  @Nonnull
  public ProductionSet getProductions() {
    return productions.clone();
  }


  /**
   * @return a copy of the set of {@link TagOrMarkerOrComponent} added
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getAddedTags() {
    return addedTags.clone();
  }

  /**
   * @return a copy of the set of {@link TagOrMarkerOrComponent} removed
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getRemovedTags() {
    return removedTags.clone();
  }

  /**
   * @return a copy of the set of {@link TagOrMarkerOrComponent} modified
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getModifiedTags() {
    return modifiedTags.clone();
  }

  /**
   * @return a copy of the set of tags protected from adding
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getProtectAddingTags() {
    return protectAddingTags.clone();
  }

  /**
   * @return a copy of the set of tags protected from removing
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getProtectRemovingTags() {
    return protectRemovingTags.clone();
  }

  /**
   * @return a copy of the set of tags protected from modifying
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getProtectModifyingTags() {
    return protectModifyingTags.clone();
  }

  /**
   * @return a copy of the set of tags added when removing the protection
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getUnprotectByAddingTags() {
    return unprotectByAddingTags.clone();
  }

  /**
   * @return a copy of the set of tags removed when removing the protection
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getUnprotectByRemovingTags() {
    return unprotectByRemovingTags.clone();
  }

  /**
   * @return the {@link Component} on which it runs
   */
  @Nonnull
  @Override
  public Class<? extends Component> getRunOn() {
    return schedulableOn;
  }

  /**
   * @return the {@link Component} needed as access
   */
  @Nonnull
  public Class<? extends Component> getAccess() {
    return access;
  }

  /**
   * @return the {@link Component} needed as exclusive access
   */
  @Nonnull
  public Class<? extends Component> getExclusiveAccess() {
    return exclusiveAccess;
  }

  /**
   * @return a copy of the set of filters
   */
  @Nonnull
  public ComponentFilterSet getFilters(@Nonnull FeatureSet features) {
    if (features.containsAll(filtersIfAll) &&
        features.containsNone(filtersUnlessOne)) {
      return neededFilters.clone();
    } else {
      return scheduler.createComponentFilterSet();
    }
  }

  /**
   * @return true if it represents a visitor, false otherwise
   */
  @Override
  public boolean isVisitor() {
    return false;
  }

  /**
   * @return true if it represents a runner, false otherwise
   */
  @Override
  public boolean isRunnable() {
    return true;
  }

  /**
   * @return a copy of the set of {@link Feature} supported by default (not optional)
   */
  @Nonnull
  public FeatureSet getSupportedFeatures() {
    return supportedFeatures.clone();
  }

  /**
   * @return the set of {@link Feature} optionally supported
   */
  @Nonnull
  public List<FeatureSet> getOptionalFeatures() {
    List<FeatureSet> list = new ArrayList<FeatureSet>();

    list.addAll(neededTags.keySet());
    list.remove(supportedFeatures);

    return list;
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Runnable '");
    sb.append(getName());
    sb.append('\'');

    return new String(sb);
  }

  //
  // Extract info from annotations and signatures
  //

  private void extractUse(@Nonnull Class<?> cls) {
    Use uses = cls.getAnnotation(Use.class);

    if (uses != null) {
      if (uses.value() != null) {
        for (Class<?> use : uses.value()) {
          if (!useTools.contains(use)) {
            useTools.add(use);
            // Recursive behavior
            extractUse(use);
          }
        }
      }
    }
  }

  private void extractTransform (@Nonnull Class<?> cls) {
    Transform transform = cls.getAnnotation(Transform.class);

    if (transform != null) {
      if (transform.add() != null) {
        for (Class<? extends TagOrMarkerOrComponent> item : transform.add()) {
          addedTags.add(item);
        }
      }

      if (transform.remove() != null) {
        for (Class<? extends TagOrMarkerOrComponent> item : transform.remove()) {
          removedTags.add(item);
        }
      }

      if (transform.modify() != null) {
        for (Class<? extends MarkerOrComponent> item : transform.modify()) {
          modifiedTags.add(item);
        }
      }
    }
  }

  private void extractProduce(@Nonnull Class<?> cls) {
    Produce produce = cls.getAnnotation(Produce.class);

    if (produce != null) {
      if (produce.value() != null) {
        for (Class<? extends Production> production : produce.value()) {
          productions.add(production);
        }
      }
    }
  }

  private void extractAccesses(@Nonnull Class<?> cls) {
    ExclusiveAccess exclusiveAccessAnnotation = cls.getAnnotation(ExclusiveAccess.class);

    if (exclusiveAccessAnnotation != null) {
      exclusiveAccess = exclusiveAccessAnnotation.value();
    } else {
      exclusiveAccess = getRunOn();
    }

    Access accessAnnotation = cls.getAnnotation(Access.class);

    if (accessAnnotation != null) {
      access = accessAnnotation.value();
    } else {
      access = getRunOn();
    }
  }

  private void extractSupport(@Nonnull Class<?> cls) {
    Support support = cls.getAnnotation(Support.class);

    if (support != null) {
      if (support.value() != null) {
        for (Class<? extends Feature> feature : support.value()) {
          supportedFeatures.add(feature);
        }
      }
    }
  }

  private void extractFilters(@Nonnull Class<?> cls) {
    Filter filters = cls.getAnnotation(Filter.class);

    if (filters != null) {
      if (filters.value() != null) {
        for (Class<? extends ComponentFilter<? extends Component>> filter : filters.value()) {
          neededFilters.add(filter);
        }
        for (Class<? extends Feature> feature : filters.ifAll()) {
          if (feature != NoFeature.class) {
            filtersIfAll.add(feature);
          }
        }
        for (Class<? extends Feature> feature : filters.unlessOne()) {
          if (feature != NoFeature.class) {
            filtersUnlessOne.add(feature);
          }
        }
      } else {
        neededFilters.add(NoFilter.class);
      }
    } else {
      neededFilters.add(NoFilter.class);
    }
  }

  private void extractOptional(@Nonnull Class<?> cls) {
    Optional optional = cls.getAnnotation(Optional.class);

    if (optional != null) {
      if (optional.value() != null) {
        for (ToSupport toSupport : optional.value()) {
          FeatureSet features = scheduler.createFeatureSet();

          for (Class<? extends Feature> feature : toSupport.feature()) {
            features.add(feature);
          }

          TagOrMarkerOrComponentSet needed = neededTags.get(features);
          if (needed == null) {
            needed = scheduler.createTagOrMarkerOrComponentSet();
            neededTags.put(features, needed);
          }

          TagOrMarkerOrComponentSet unsupport = unsupportedTags.get(features);
          if (unsupport == null) {
            unsupport = scheduler.createTagOrMarkerOrComponentSet();
            unsupportedTags.put(features, unsupport);
          }

          if (toSupport.add() != null) {
            for (Constraint constraint : toSupport.add()) {
              if (constraint.need() != null) {
                for (Class<? extends TagOrMarkerOrComponent> tag : constraint.need()) {
                  needed.add(tag);
                }
              }

              if (constraint.no() != null) {
                for (Class<? extends TagOrMarkerOrComponent> tag : constraint.no()) {
                  unsupport.add(tag);
                }
              }
            }
          }
        }
      }
    }
  }

  private void extractConstraint(@Nonnull Class<?> cls) {
    TagOrMarkerOrComponentSet needed = neededTags.get(supportedFeatures);
    if (needed == null) {
        needed = scheduler.createTagOrMarkerOrComponentSet();
        neededTags.put(supportedFeatures, needed);
    }

    TagOrMarkerOrComponentSet unsupport = unsupportedTags.get(supportedFeatures);
    if (unsupport == null) {
      unsupport = scheduler.createTagOrMarkerOrComponentSet();
      unsupportedTags.put(supportedFeatures, unsupport);
    }

    Constraint constraint = cls.getAnnotation(Constraint.class);

    if (constraint != null) {
      if (constraint.need() != null) {
        for (Class<? extends TagOrMarkerOrComponent> tag : constraint.need()) {
          needed.add(tag);
        }
      }

      if (constraint.no() != null) {
        for (Class<? extends TagOrMarkerOrComponent> tag : constraint.no()) {
          unsupport.add(tag);
        }
      }
    }
  }

  private void extractProtect(@Nonnull Class<?> cls) {
    Protect protect = cls.getAnnotation(Protect.class);

    if (protect != null) {
      if (protect.add() != null) {
        for (Class<? extends TagOrMarkerOrComponent> item : protect.add()) {
          protectAddingTags.add(item);
        }
      }

      if (protect.remove() != null) {
        for (Class<? extends TagOrMarkerOrComponent> item : protect.remove()) {
          protectRemovingTags.add(item);
        }
      }

      if (protect.modify() != null) {
        for (Class<? extends TagOrMarkerOrComponent> item : protect.modify()) {
          protectModifyingTags.add(item);
        }
      }

      With[] withs = protect.unprotect();

      for (With with : withs) {
        if (with.add() != null) {
          for (Class<? extends TagOrMarker> item : with.add()) {
            unprotectByAddingTags.add(item);
          }
        }

        if (with.remove() != null) {
          for (Class<? extends TagOrMarker> item : with.remove()) {
            unprotectByRemovingTags.add(item);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void extractSchedulableOn(
      @Nonnull Class<? extends ProcessorSchedulable<? extends Component>> cls) {
    for (Type intf : Reflect.getAllGenericInSuperClassOrInterface(cls)) {
      if (intf instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) intf;

        if (ProcessorSchedulable.class.isAssignableFrom((Class<?>) (pt.getRawType()))) {
          schedulableOn = (Class<? extends Component>) pt.getActualTypeArguments()[0];
          return;
        }
      }
    }

    throw new AssertionError();
  }
}
