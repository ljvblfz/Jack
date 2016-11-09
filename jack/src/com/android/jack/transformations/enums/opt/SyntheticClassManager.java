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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.Options.SwitchEnumOptStrategy;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * This class is used to manage the synthetic class. The idea behind the scene is that if
 * it is decided that creating synthetic class is worth, then create it. Otherwise inject
 * switch map initializer inside of user class, which follows {@link SwitchEnumSupport}'s
 * solution.
 */
public class SyntheticClassManager {
  // the prefix of synthetic switch map class
  @Nonnull
  public static final String SyntheticSwitchmapClassNamePrefix = "SyntheticSwitchmapClass-";

  // the package name of synthetic switch map class for the public enums
  @Nonnull
  public static final String PublicSyntheticSwitchmapClassPkgName =
      "com/android/jack/enums/synthetic";

  // the statistic counting the number of synthetic switch map class created in total
  @Nonnull
  public static final StatisticId<Counter> SYNTHETIC_SWITCHMAP_CLASS =
    new StatisticId<Counter>("jack.optimization.enum.switch.synthetic.class.increase",
      "Total number of synthetic class created", CounterImpl.class, Counter.class);

  @Nonnull
  private final SwitchEnumOptStrategy optimizationStrategy =
    ThreadConfig.get(Options.OPTIMIZED_ENUM_SWITCH);

  // this map represents relationship from the package to synthetic class. There should only be
  // one synthetic class at most per package. Synthetic class cannot be located anywhere because
  // the enum field may not be visible to it
  @Nonnull
  private final Map<JPackage, JDefinedClass> syntheticClassMap = Maps.newHashMap();

  @Nonnull
  private final Tracer statisticTracer = TracerFactory.getTracer();

  @Nonnull
  private final JSession session = Jack.getSession();

  @Nonnull
  private final OptimizationUtil supportUtil;

  // temporarily deleted library synthetic class during the step while merging synthetic classes
  @Nonnull
  private final Map<String, JDefinedClass> deletedLibSyntheticClasses = Maps.newHashMap();

  /**
   * Constructor.
   * @param supportUtil the instance of {@link OptimizationUtil}
   */
  public SyntheticClassManager(@Nonnull OptimizationUtil supportUtil) {
    this.supportUtil = supportUtil;
  }

  /**
   * The logic behind the scene is that different strategies are needed based on
   * the profiling of given enum:
   * <li> enumType is public </li>
   * In this case, only a synthetic class is created at SyntheticSwitchmapClassPkgName if
   * the optimizing condition is met {@link #isOptimizationWorthwhile(int, Set)}.
   * Otherwise switch map initializer will be created inside of user class like what is
   * done in {@link SwitchEnumSupport}.
   *
   * <li> enumType is private/protected/package private </li>
   * In this case, synthetic class is created at the same package where user class is declared
   * only when it detects it is worth to do that. Otherwise switch map initializer will be created
   * inside of user class like what is done in {@link SwitchEnumSupport}.
   *
   * @param enumType the related enum for which synthetic class will be generated
   * @param createIfNotExist true means create class is not found, otherwise throw exception
   *
   * @return the synthetic class to create
   */
  @CheckForNull
  public JDefinedClass getOrCreateSyntheticClass(
      @Nonnull JDefinedEnum enumType, boolean createIfNotExist) {
    JPackage syntheticClassPackage;
    if (enumType.isPublic()) {
      // create synthetic class at a specific package if the enum is public. We can access
      // it from anywhere
      syntheticClassPackage =
          session.getLookup().getOrCreatePackage(PublicSyntheticSwitchmapClassPkgName);
    } else {
      syntheticClassPackage = enumType.getEnclosingPackage();
    }

    JDefinedClass syntheticClass = syntheticClassMap.get(syntheticClassPackage);
    if (syntheticClass == null) {
      // check if there exists a source synthetic class under specific package
      syntheticClass = getSyntheticClassUnderPackage(syntheticClassPackage);
    }
    assert syntheticClass != null || createIfNotExist;

    SwitchEnumUsageMarker enumUsageMarker =
        syntheticClassPackage.getMarker(SwitchEnumUsageMarker.class);

    boolean isOptWorth = false;
    Set<JDefinedEnum> usedEnumsType = Sets.newHashSet();
    if (optimizationStrategy == SwitchEnumOptStrategy.FEEDBACK) {
      assert enumUsageMarker != null;
      usedEnumsType.addAll(enumUsageMarker.getUsedEnumsType());
    } else {
      // no feedback usage knowledge exists, we only know current enum is used
      usedEnumsType.add(enumType);
    }
    if (syntheticClass != null) {
      isOptWorth = usedEnumsType.addAll(getPredefinedEnumsType(syntheticClass));
    }

    initEnumFieldMarkers(usedEnumsType);
    if (!isOptWorth) {
      if (optimizationStrategy == SwitchEnumOptStrategy.ALWAYS) {
        // under always strategy, optimization is always enabled
        isOptWorth = true;
      } else {
        isOptWorth = isOptimizationWorthwhile(enumUsageMarker.getUses(), usedEnumsType);
      }
    }

    if (!isOptWorth) {
      // if optimization is not worth, don't optimize it because of class overhead
      return null;
    }
    // if there is no synthetic class existed, create one. The name of synthetic switch map
    // class will depend on the related enums inside it
    String uuid = getSyntheticClassUUID(usedEnumsType);
    String syntheticClassName = SyntheticSwitchmapClassNamePrefix + uuid;
    // check to see if there exists a duplicated synthetic class already, because we don't want to
    // generate multiple source classes under a package.
    // the ideal approach is to merge all synthetic classes under a package, but due to the fact
    // that multiple libraries may be imported to a source, there may be multiple synthetic classes
    // under a package. Merging them together requires re-compile the library code because the int
    // array switch map may be updated. This is a future work and please be advised that it may
    // increase compilation time
    try {
      String syntheticClassFullName =
          "L" + BinaryQualifiedNameFormatter.getFormatter().getName(syntheticClassPackage) + "/"
              + syntheticClassName + ";";
      JDefinedClass existSyntheticClass = session.getLookup().getClass(syntheticClassFullName);
      if (existSyntheticClass.getLocation() instanceof TypeInInputLibraryLocation
          && session.getTypesToEmit().contains(existSyntheticClass)) {
        // there exists a class inside of library which has the same name, and this class
        // will be emit as well. In this situation, we want to remove it first for avoiding
        // generating duplicated classes with the same name
        session.removeTypeToEmit(existSyntheticClass);
        deletedLibSyntheticClasses.put(syntheticClassFullName, existSyntheticClass);
      }
    } catch (JTypeLookupException e) {}

    if (syntheticClass != null && !syntheticClassName.equals(syntheticClass.getName())) {
      // update the class name because it is different
      String syntheticClassFullName =
          "L" + BinaryQualifiedNameFormatter.getFormatter().getName(syntheticClass) + ";";
      if (deletedLibSyntheticClasses.containsKey(syntheticClassFullName)) {
        // add the lib synthetic class back to emit list because the name of current
        // synthetic class will be changed
        session.addTypeToEmit(deletedLibSyntheticClasses.remove(syntheticClassFullName));
      }
      syntheticClass.setName(syntheticClassName);
    } else if (syntheticClass == null) {
      syntheticClass =
          checkAndInitializeSyntheticClass(syntheticClassPackage, syntheticClassName);
      syntheticClassMap.put(syntheticClassPackage, syntheticClass);
      // increase the statistic measurement for synthetic class created
      statisticTracer.getStatistic(SYNTHETIC_SWITCHMAP_CLASS).incValue();
    }
    return syntheticClass;
  }

  /**
   * Check and create synthetic switch map class which never exists before.
   *
   * @param enclosingPackage the package of synthetic class
   * @param syntheticClassShortName The short class name of synthetic class
   *
   * @return The created synthetic switch map class
   */
  @Nonnull
  private JDefinedClass checkAndInitializeSyntheticClass(
      @Nonnull JPackage enclosingPackage, @Nonnull String syntheticClassShortName) {
    JDefinedClass syntheticClass = null;
    boolean needSyntheticClass = false;
    try {
      syntheticClass = (JDefinedClass) enclosingPackage.getType(syntheticClassShortName);
      if (syntheticClass.getLocation() instanceof TypeInInputLibraryLocation
          && !session.getTypesToEmit().contains(syntheticClass)) {
        // if the synthetic class is inside of library and it will not be emit, we have to
        // create it inside of source because we want to avoid the dependency of library
        needSyntheticClass = true;
      }
    } catch (JTypeLookupException e){
      needSyntheticClass = true;
    }
    if (needSyntheticClass) {
      syntheticClass = initializeSyntheticClass(enclosingPackage, syntheticClassShortName);
    }
    assert syntheticClass != null;
    return syntheticClass;
  }

  /**
   * Initialize synthetic class and corresponding package.
   *
   * @param enclosingPackage the package of synthetic class
   * @param syntheticClassShortName Synthetic switch map class short name
   *
   * @return The created synthetic switch map class
   */
  @Nonnull
  private JDefinedClass initializeSyntheticClass(
      @Nonnull JPackage enclosingPackage, @Nonnull String syntheticClassShortName) {
    JDefinedClass syntheticSwitchmapClass = new JDefinedClass(SourceInfo.UNKNOWN,
        syntheticClassShortName, JModifier.PUBLIC | JModifier.FINAL | JModifier.SYNTHETIC,
        enclosingPackage, NopClassOrInterfaceLoader.INSTANCE);

    syntheticSwitchmapClass.setSuperClass(
        session.getLookup().getClass(CommonTypes.JAVA_LANG_OBJECT));
    // make sure the future instrumentation knows this class
    session.addTypeToEmit(syntheticSwitchmapClass);

    return syntheticSwitchmapClass;
  }

  /**
   * This function is used to determine if optimization is worth is based on experiment. The
   * criteria is based on the observation.
   *
   * @param uses the number of uses under the related package
   * @param enumsType the set of enum inside of synthetic class under a specific package
   *
   * @return true if it is worth optimization
   */
  private boolean isOptimizationWorthwhile(int uses, @Nonnull Set<JDefinedEnum> enumsType) {
    // calculate the average of enum fields
    int enumFields = 0;
    for (JDefinedEnum enumType : enumsType) {
      EnumFieldMarker enumFieldMarker = enumType.getMarker(EnumFieldMarker.class);
      assert enumFieldMarker != null;
      enumFields += enumFieldMarker.getEnumFields().size();
    }
    enumFields = enumFields / enumsType.size();
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

  /**
   * Get the enums type which are related to the given synthetic class.
   * @param syntheticClass The input synthetic class
   *
   * @return the set of enums type related with the given synthetic class
   */
  @Nonnull
  private Set<JDefinedEnum> getPredefinedEnumsType(
      @Nonnull JDefinedClassOrInterface syntheticClass) {
    Set<JDefinedEnum> enumsType = Sets.newHashSet();
    for (JField definedField : syntheticClass.getFields()) {
      if (!supportUtil.isSyntheticSwitchMapField(definedField)) {
        continue;
      }
      String enumName = OptimizationUtil.getEnumNameFromSyntheticField(definedField);
      JDefinedClass enumType = null;
      try {
        enumType = session.getLookup().getClass(enumName);
      } catch (JTypeLookupException e) {
        continue;
      }
      assert enumType instanceof JDefinedEnum;
      enumsType.add((JDefinedEnum) enumType);
    }
    return enumsType;
  }

  /**
   * Generate the UUID based on the given set of enum types. The basic idea is
   * to concatenate all the enum's full class name and enum field together,
   * e.g., $FULL_CLASS_OF_ENUM1$:Field1,Field2,Field3.$FULL_CLASS_OF_ENUM2$:Field1,
   * Field2,Field3.
   *
   * Please keep in mind that both the enums type and enum fields are first sorted in
   * alphabetic order, then concatenate them together. Finally based on this string,
   * generate SHA-256 and use it as UUID.
   *
   * @param enumsType the set of enums type
   *
   * @return UUID
   */
  @Nonnull
  private String getSyntheticClassUUID(@Nonnull Set<JDefinedEnum> enumsType) {
    List<JDefinedEnum> sortedEnumsType = Lists.newArrayList(enumsType);
    Collections.sort(sortedEnumsType, new Comparator<JDefinedEnum> () {
      @Override
      public int compare(JDefinedEnum enum1, JDefinedEnum enum2) {
        String enumName1 = BinaryQualifiedNameFormatter.getFormatter().getName(enum1);
        String enumName2 = BinaryQualifiedNameFormatter.getFormatter().getName(enum2);
        return enumName1.compareTo(enumName2);
      }
    });

    StringBuilder sb = new StringBuilder();
    for (JDefinedEnum sortedEnumType : sortedEnumsType) {
      EnumFieldMarker enumFieldMarker =
          sortedEnumType.getMarker(EnumFieldMarker.class);
      assert enumFieldMarker != null;
      sb.append(Jack.getLookupFormatter().getName(sortedEnumType));
      sb.append(":");
      enumFieldMarker.sortEnumFields();
      List<JEnumField> sortedEnumFields = enumFieldMarker.getEnumFields();
      for (JEnumField enumField : sortedEnumFields) {
        sb.append(enumField.getName());
        sb.append(",");
      }
      sb.append(".");
    }
    String sig = sb.toString();
    sb.delete(0, sb.length());
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError();
    }
    byte[] uuid = md.digest(sig.getBytes());
    for (int i = 0; i < uuid.length; i++) {
      String hexString = Integer.toHexString(0xff & uuid[i]);
      if (hexString.length() == 1) {
        sb.append('0');
      }
      sb.append(hexString);
    }
    return sb.toString();
  }

  /**
   * Get the synthetic switch map class under given package.
   *
   * @param syntheticClassPackage The synthetic package to check
   * @return the synthetic class if exists
   */
  @CheckForNull
  private JDefinedClass getSyntheticClassUnderPackage(@Nonnull JPackage syntheticClassPackage) {
    for (JDefinedClassOrInterface classOrInterface : syntheticClassPackage.getTypes()) {
      String className = classOrInterface.getName();
      if (className.startsWith(SyntheticSwitchmapClassNamePrefix)) {
        assert classOrInterface instanceof JDefinedClass;
        if (!(classOrInterface.getLocation() instanceof TypeInInputLibraryLocation)) {
          // only return the source synthetic class
          return (JDefinedClass) classOrInterface;
        }
      }
    }
    return null;
  }

  /**
   * Initialize the EnumFieldMarker for all given enum types.
   * @param enumsType the set of enums
   */
  private void initEnumFieldMarkers(@Nonnull Set<JDefinedEnum> enumsType) {
    for (JDefinedEnum enumType : enumsType) {
      if (enumType.containsMarker(EnumFieldMarker.class)) {
        continue;
      }
      // count the total number of enum literals defined inside
      EnumFieldMarker enumFieldMarker = new EnumFieldMarker();
      enumType.addMarker(enumFieldMarker);
      for (JField enumField : enumType.getFields()) {
        if (!(enumField instanceof JEnumField)) {
          continue;
        }
        enumFieldMarker.addEnumField((JEnumField) enumField);
      }
    }
  }
}
