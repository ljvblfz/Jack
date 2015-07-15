/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.enums.opt;

import com.google.common.collect.Maps;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.transformations.enums.OptimizationUtil;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
/**
 * This class is used to manage the synthetic class, as multiple synthetic switch map
 * classes are created. The idea behind the scene is that if it is decided that creating
 * synthetic class is worth of, then create it. Otherwise follow {@link SwitchEnumSupport}'s
 * solution.
 */
public class SyntheticClassManager {
  // the prefix of synthetic switch map class
  public static final String SyntheticSwitchmapClassNamePrefix = "SyntheticSwitchmapClass-";

  // the package name of synthetic switch map class
  public static final String SyntheticSwitchmapClassPkgName = "com/android/jack/enums/synthetic/";

  // the statistic counting the number of synthetic switch map class created during current
  // compilation.
  @Nonnull
  public static final StatisticId<Counter> SYNTHETIC_SWITCHMAP_CLASS = new StatisticId<Counter>(
      "jack.optimization.enum.switch.synthetic.class.increase",
      "Total number of synthetic class created",
      CounterImpl.class, Counter.class);

  // this map represents relationship from the prefix to synthetic class. There should only be
  // one synthetic class at most per prefix. Synthetic class cannot be located anywhere because
  // the enum field may not be visible to it
  private final Map<String, JDefinedClass> syntheticClassMap = Maps.newHashMap();

  // support utility provides basic APIs to use
  private final OptimizationUtil supportUtil;

  /**
   * Minimal number of user classes to enable enum optimization. If it is less or equals than 0,
   * use the default algorithm {@link #isOptimizationWorthwhile(JDefinedEnum)}
   */
  private final int optimizeThreshold;

  // statistic tracer. It will be used to collect statistic measurement
  @Nonnull
  private final Tracer statisticTracer = TracerFactory.getTracer();

  // total number of classes using public enum
  private int publicEnumUsedClasses = -1; // initialize it with meaningless value

  /**
   * Determine if the given class is synthetic switch map class.
   * @param cls The class to check
   *
   * @return true if given class is synthetic class
   */
  public static boolean isSyntheticSwitchMapClass(@Nonnull JDefinedClass cls) {
    String shortName = cls.getName();
    return shortName.contains(SyntheticClassManager.SyntheticSwitchmapClassNamePrefix);
  }

  /**
   * Constructor.
   * @param supportUtil The instance of {@link OptimizationUtil}
   * @param optimizeThreshold Number of classes triggering strategies, details is given at
   * {@link #isOptimizationWorthwhile(JDefinedEnum)}
   */
  public SyntheticClassManager(@Nonnull OptimizationUtil supportUtil, int optimizeThreshold) {
    this.supportUtil = supportUtil;
    this.optimizeThreshold = optimizeThreshold;
  }

  /**
   * The logic behind the scene is that different strategies are needed based on
   * the profiling of given enum:
   * <li> enumType is public </li>
   * In this case, only a synthetic class is created at SyntheticSwitchmapClassPkgName if
   * the optimizing condition is met {@link #isOptimizationWorthwhile(JDefinedEnum)}.
   *
   * <li> enumType is private </li>
   * In this case, synthetic class is created at the same level of user class only when
   * it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * <li> enumType is protected </li>
   * In this case, synthetic class is created at the same level of user class only when
   * it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * <li> enumType is package private </li>
   * In this case, synthetic class is created at the same level of user class only when
   * it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * @param enumType The related enum for which synthetic class will be generated
   * @param createIfNotExist True means create class is not found, otherwise throw exception
   *
   * @return The synthetic class to create
   * @throws AssertionError
   */
  @Nullable
  public JDefinedClass getOrCreateRelatedSyntheticClass(@Nonnull JDefinedEnum enumType,
      boolean createIfNotExist) {
    String syntheticClassFullNamePrefix;
    if (!isOptimizationWorthwhile(enumType)) {
      // if optimization is not worth, don't optimize it because of class overhead
      return null;
    } else if (enumType.isPublic()) {
      // create synthetic class at a specific package if the enum is public. We can access
      // it from anywhere
      syntheticClassFullNamePrefix = "L" + SyntheticSwitchmapClassPkgName;
    } else {
      syntheticClassFullNamePrefix = Jack.getLookupFormatter().getName(enumType);
      // search for class that can access enum. The class should be at the same package
      // level or inner class level as enum does. Search '$' or '/' whichever comes last
      int index = syntheticClassFullNamePrefix.length() - 1;
      while (index >= 0) {
        char charAtPos = syntheticClassFullNamePrefix.charAt(index);
        if (charAtPos == '/' || charAtPos == '$') {
          break;
        }
        index--;
      }
      // create synthetic class at this level could access to the enum constant fields
      syntheticClassFullNamePrefix = syntheticClassFullNamePrefix.substring(0, index + 1);
    }
    // make sure only one synthetic class is created every prefix
    JDefinedClass syntheticClass = syntheticClassMap.get(syntheticClassFullNamePrefix);
    if (syntheticClass != null) {
      return syntheticClass;
    } else if (!createIfNotExist) {
      // if synthetic class is not found and createIfNotExist is set to false
      throw new AssertionError("Not found synthetic class under prefix: "
          + syntheticClassFullNamePrefix);
    }
    // if there is no synthetic class existed, create one. Note each synthetic switch map
    // class will have a unique name
    String syntheticClassFullName = syntheticClassFullNamePrefix +
         SyntheticSwitchmapClassNamePrefix + UUID.randomUUID().toString() + ";";
    syntheticClass = checkAndInitializeSyntheticClass(syntheticClassFullName);
    syntheticClassMap.put(syntheticClassFullNamePrefix, syntheticClass);
    // increase the statistic measurement for synthetic class created
    statisticTracer.getStatistic(SYNTHETIC_SWITCHMAP_CLASS).incValue();
    return syntheticClass;
  }

  /**
   * Check and create synthetic switch map class which never exists before. The class is
   * added to the session.
   * @param syntheticClassFullName The full class name of synthetic class
   *
   * @return The created synthetic switch map class
   */
  private JDefinedClass checkAndInitializeSyntheticClass(String syntheticClassFullName) {
    JDefinedClass syntheticClass;
    try {
      syntheticClass = supportUtil.getLookup().getClass(syntheticClassFullName);
      throw new AssertionError("Duplicated initialize synthetic switch map class: "
          + syntheticClass);
    } catch (JPackageLookupException e) {
      // if the package cannot be found, create it and the synthetic switch map class
      syntheticClass = initializeSyntheticClass(syntheticClassFullName);
    } catch (JTypeLookupException e){
      // if the synthetic switch map class cannot be found, create it
      syntheticClass = initializeSyntheticClass(syntheticClassFullName);
    }
    return syntheticClass;
  }

  /**
   * Initialize synthetic class and corresponding package. Both synthetic class as well as
   * Corresponding package are added into session.
   * @param syntheticClassFullName Synthetic switch map class full name which includes package
   *
   * @return The created synthetic switch map class
   */
  private JDefinedClass initializeSyntheticClass(@Nonnull String syntheticClassFullName) {
    JDefinedClass syntheticSwitchmapClass = createSyntheticClass(syntheticClassFullName);
    // set super class since it is required by {@link ClassDefItemBuilder}
    syntheticSwitchmapClass.setSuperClass(supportUtil.getObjectType());
    // it is not external class
    syntheticSwitchmapClass.setExternal(false);
    // make sure the future instrumentation knows this class
    Jack.getSession().addTypeToEmit(syntheticSwitchmapClass);

    // make sure this class exists
    if (supportUtil.getLookup().getClass(syntheticClassFullName) != syntheticSwitchmapClass) {
      throw new AssertionError("Existed synthetic switch map class doesn't match with: "
          + syntheticSwitchmapClass);
    }
    return syntheticSwitchmapClass;
  }

  /**
   * Create the class and fill its content later.
   * @param syntheticClassFullName The full class name for synthetic switch map class
   *
   * @return The created synthetic switch map class
   */
  private JDefinedClass createSyntheticClass(@Nonnull String syntheticClassFullName) {
    // check if it is illegal class name
    if (!syntheticClassFullName.startsWith("L") || !syntheticClassFullName.endsWith(";")) {
      throw new AssertionError("Invalid full class name: " + syntheticClassFullName);
    }

    // get the package name of full class name
    String classShortName;
    JPackage currentPackage;
    int lastIndex = syntheticClassFullName.lastIndexOf('/');
    if (lastIndex >= 0) {
      String pkgName = syntheticClassFullName.substring(1, lastIndex);
      classShortName = syntheticClassFullName.substring(lastIndex + 1,
          syntheticClassFullName.length() - 1);

      // create the package object
      currentPackage = supportUtil.getLookup().getOrCreatePackage(pkgName);
    } else {
      // if there is no "/", then classShortName is classFullName
      classShortName = syntheticClassFullName.substring(1, syntheticClassFullName.length()
          - 1);
      currentPackage = supportUtil.getLookup().getTopLevelPackage();
    }

    // create the class under current package
    JDefinedClass syntheticSwitchmapClass = new JDefinedClass(SourceInfo.UNKNOWN,
        classShortName, JModifier.PUBLIC, currentPackage, NopClassOrInterfaceLoader.INSTANCE);

    return syntheticSwitchmapClass;
  }

  /**
   * This function to determine if optimization is worth is based on experiment. The metric
   * may vary slightly across different versions of Jack.
   * @param enumType Enum to be optimized
   *
   * @return true if it is worth optimization
   */
  private boolean isOptimizationWorthwhile(JDefinedEnum enumType) {
    SwitchEnumUsageMarker usageMarker = enumType.getMarker(SwitchEnumUsageMarker.class);
    if (usageMarker == null) {
      throw new AssertionError("No EnumUsageMarker is attached to enum: " + enumType);
    }
    int uses = 0;
    if (enumType.isPublic()) {
      // for public enum we count the cumulative number of uses
      uses = getPublicEnumUsesInApp();
    } else {
      uses = usageMarker.getUses();
    }
    if (enumType.isExternal()) {
      // if the enum is external, e.g., library, we should count its own uses
      uses += usageMarker.getUses();
    }

    if (optimizeThreshold > 0) {
      // simply metric could be used here like number of user classes is more than 2.
      return uses >= optimizeThreshold;
    } else {
      EnumFieldMarker enumFieldMarker = enumType.getMarker(EnumFieldMarker.class);
      if (enumFieldMarker == null) {
        throw new AssertionError();
      }
      int enumFields = enumFieldMarker.getEnumLiterals();
      // try a little complicated algorithm
      // the observation is that if an enum has
      // 1 constant enum fields, 3 user classes is enough to observe space reduction
      // 4 constant enum fields, 2 user classes is enough to observe space reduction
      if (enumFields >= 1 && enumFields < 4 && uses >= 3) {
        return true;
      } else if (enumFields >= 4 && uses >= 2) {
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Compute the total number of classes using public enum in switch statements across the
   * whole compilation.
   *
   * @return The total number of classes using public enum in switch statements
   */
  private int getPublicEnumUsesInApp() {
    if (publicEnumUsedClasses >= 0) {
      return publicEnumUsedClasses;
    }
    int counts = 0;
    for (JDefinedClassOrInterface classOrInterface : Jack.getSession().getTypesToEmit()) {
      if (classOrInterface instanceof JDefinedEnum && classOrInterface.isPublic()) {
        JDefinedEnum enumType = (JDefinedEnum) classOrInterface;
        SwitchEnumUsageMarker usageMarker = enumType.getMarker(SwitchEnumUsageMarker.class);
        if (usageMarker == null) {
          // if the enum is not reachable during shrinking
          continue;
        }
        counts += usageMarker.getUses();
      }
    }
    publicEnumUsedClasses = counts;
    return counts;
  }
}
