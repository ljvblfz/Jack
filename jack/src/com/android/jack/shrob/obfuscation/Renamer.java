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


package com.android.jack.shrob.obfuscation;

import com.android.jack.Jack;
import com.android.jack.ir.ast.CanBeRenamed;
import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.shrob.obfuscation.nameprovider.NameProvider;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.transformations.request.ChangeEnclosingPackage;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.jack.util.PackageCodec;
import com.android.sched.item.Description;
import com.android.sched.marker.MarkerManager;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.codec.PathCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Visitor that renames {@code JNode}s
 */
@HasKeyId
@Description("Visitor that renames JNodes")
@Constraint(need = {KeepNameMarker.class, OriginalNames.class}, no = FinalNames.class)
@Transform(remove = OriginalNames.class,
    add = {OriginalNameMarker.class, OriginalPackageMarker.class, FinalNames.class})
@Use(MappingApplier.class)
public class Renamer implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final
      BooleanPropertyId USE_PACKAGE_OBFUSCATION_DICTIONARY = BooleanPropertyId.create(
          "jack.obfuscation.packagedictionary",
          "Use obfuscation dictionary for packages").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final PropertyId<File> PACKAGE_OBFUSCATION_DICTIONARY = PropertyId.create(
      "jack.obfuscation.packagedictionary.file", "Obfuscation dictionary for packages",
      new PathCodec()).requiredIf(Renamer.USE_PACKAGE_OBFUSCATION_DICTIONARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId USE_CLASS_OBFUSCATION_DICTIONARY = BooleanPropertyId.create(
      "jack.obfuscation.classdictionary", "Use obfuscation dictionary for classes")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final PropertyId<File> CLASS_OBFUSCATION_DICTIONARY = PropertyId.create(
      "jack.obfuscation.classdictionary.file", "Obfuscation dictionary for classes",
      new PathCodec()).requiredIf(USE_CLASS_OBFUSCATION_DICTIONARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId USE_OBFUSCATION_DICTIONARY = BooleanPropertyId.create(
      "jack.obfuscation.dictionary", "Use obfuscation dictionary for members")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final PropertyId<File> OBFUSCATION_DICTIONARY = PropertyId.create(
      "jack.obfuscation.dictionary.file", "Obfuscation dictionary for members",
      new PathCodec()).requiredIf(USE_OBFUSCATION_DICTIONARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId USE_MAPPING = BooleanPropertyId.create(
      "jack.obfuscation.mapping", "Use mapping for types and members")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final PropertyId<File> MAPPING_FILE = PropertyId.create(
      "jack.obfuscation.mapping.file",
      "File containing the mapping of all types and members", new PathCodec())
      .addDefaultValue("mapping.txt");

  @Nonnull
  public static final BooleanPropertyId REPACKAGE_CLASSES = BooleanPropertyId.create(
      "jack.obfuscation.repackageclasses",
      "Change package for all renamed classes")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final PropertyId<String> PACKAGE_FOR_RENAMED_CLASSES = PropertyId.create(
      "jack.obfuscation.repackageclasses.package",
      "Enclosing package for all renamed classes", new PackageCodec())
      .requiredIf(REPACKAGE_CLASSES.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId FLATTEN_PACKAGE = BooleanPropertyId.create(
      "jack.obfuscation.flattenpackage",
      "Change package for all renamed packages")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final PropertyId<String> PACKAGE_FOR_RENAMED_PACKAGES = PropertyId.create(
      "jack.obfuscation.flattenpackage.package",
      "Enclosing package for all renamed packages", new PackageCodec())
      .requiredIf(FLATTEN_PACKAGE.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId USE_UNIQUE_CLASSMEMBERNAMES = BooleanPropertyId.create(
      "jack.obfuscation.uniqueclassmembernames",
      "All members with the same name must have the same obfuscated name")
      .addDefaultValue(Boolean.FALSE);

  public static boolean mustBeRenamed(@Nonnull MarkerManager node) {
    return !node.containsMarker(KeepNameMarker.class)
        && !node.containsMarker(OriginalNameMarker.class);
  }

  @Nonnull
  static String getFieldKey(@Nonnull JFieldId fieldId) {
    return fieldId.getName() + ':'
        + GrammarActions.getSignatureFormatter().getName(fieldId.getType());
  }

  @Nonnull
  static String getKey(@Nonnull HasName namedElement) {
    if (namedElement instanceof JFieldId) {
      return Renamer.getFieldKey((JFieldId) namedElement);
    } else if (namedElement instanceof JMethodId) {
      JMethodId mid = (JMethodId) namedElement;
      return GrammarActions.getSignatureFormatter().getNameWithoutReturnType(mid);
    } else {
      return namedElement.getName();
    }
  }

  private static void rename(@Nonnull CanBeRenamed node, @Nonnull NameProvider nameProvider) {
    if (mustBeRenamed((MarkerManager) node)) {
      String newName = nameProvider.getNewName(getKey(node));
      ((MarkerManager) node).addMarker(new OriginalNameMarker(node.getName()));
      node.setName(newName);
    }
  }

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JPackage pack) {
      List<JPackage> subPackages = pack.getSubPackages();
      NameProvider packageNameProvider = nameProviderFactory.getPackageNameProvider(subPackages);
      for (JPackage subPack : subPackages) {
        rename(subPack, packageNameProvider);
      }

      List<JDefinedClassOrInterface> types = pack.getTypes();
      NameProvider classNameProvider = nameProviderFactory.getClassNameProvider(types);
      for (JClassOrInterface type : types) {
        if (type instanceof JDefinedClassOrInterface) {
          rename((JDefinedClassOrInterface) type, classNameProvider);
        }
      }
      return super.visit(pack);
    }

    @Nonnull
    private Collection<JFieldId> collectAllFieldIdsInHierarchy(
        @Nonnull JDefinedClassOrInterface referenceType) {
      List<JFieldId> collectedFields = new ArrayList<JFieldId>();
      assert allTypes != null;
      for (JDefinedClassOrInterface type : allTypes) {
        if (referenceType.canBeSafelyUpcast(type) || type.canBeSafelyUpcast(referenceType)) {
          for (JField field : type.getFields()) {
            collectedFields.add(field.getId());
          }
        }
      }
      return collectedFields;
    }

    @Nonnull
    private Collection<JMethodId> collectAllMethodIdsInHierarchy(
        @Nonnull JDefinedClassOrInterface referenceType) {
      Set<JMethodId> collectedMethods = new HashSet<JMethodId>();
      assert allTypes != null;
      for (JDefinedClassOrInterface type : allTypes) {
        if (referenceType.canBeSafelyUpcast(type) || type.canBeSafelyUpcast(referenceType)) {
          for (JMethod method : type.getMethods()) {
            collectedMethods.add(method.getMethodId());
          }
        }
      }
      return collectedMethods;
    }

    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface type) {
      if (!type.isExternal()) {
        Collection<JFieldId> allFieldsInHierarchy = collectAllFieldIdsInHierarchy(type);
        NameProvider fieldNameProvider =
            nameProviderFactory.getFieldNameProvider(allFieldsInHierarchy);
        for (JField field : type.getFields()) {
          rename(field.getId(), fieldNameProvider);
        }

        Collection<JMethodId> allMethodsInHierarchy = collectAllMethodIdsInHierarchy(type);
        NameProvider methodNameProvider =
            nameProviderFactory.getMethodNameProvider(allMethodsInHierarchy);
        for (JMethod method : type.getMethods()) {
          rename(method.getMethodId(), methodNameProvider);
        }
      }

      return false;
    }
  }

  private class RepackagerVisitor extends Visitor {

    @Nonnull
    private final TransformationRequest request;

    @Nonnull
    private final String packageNameForRenamedClasses =
        NamingTools.getBinaryName(ThreadConfig.get(PACKAGE_FOR_RENAMED_CLASSES));

    @Nonnull
    private final JPackage packageForRenamedClasses
      = Jack.getSession().getLookup().getOrCreatePackage(packageNameForRenamedClasses);

    @Nonnull
    private final NameProvider classNameProvider =
          nameProviderFactory.getClassNameProvider(packageForRenamedClasses.getTypes());

    private RepackagerVisitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public boolean visit(@Nonnull JPackage pack) {

      for (JClassOrInterface type : pack.getTypes()) {
        if (mustBeRenamed((MarkerManager) type)) {
          JPackage oldEnclosingPackage = type.getEnclosingPackage();
          assert oldEnclosingPackage != null;
          ((MarkerManager) type).addMarker(
              new OriginalPackageMarker(oldEnclosingPackage));
          request.append(new ChangeEnclosingPackage(type, packageForRenamedClasses));
          rename((JDefinedClassOrInterface) type, classNameProvider);
        }
      }

      return super.visit(pack);
    }
  }

  private class FlattenerVisitor extends Visitor {

    @Nonnull
    private final TransformationRequest request;

    @Nonnull
    private final String packageNameForRenamedPackages =
        NamingTools.getBinaryName(ThreadConfig.get(PACKAGE_FOR_RENAMED_PACKAGES));

    @Nonnull
    private final JPackage packageForRenamedPackages
      = Jack.getSession().getLookup().getOrCreatePackage(packageNameForRenamedPackages);

    @Nonnull
    private final NameProvider packageNameProvider =
        nameProviderFactory.getPackageNameProvider(packageForRenamedPackages.getSubPackages());

    private FlattenerVisitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public boolean visit(@Nonnull JPackage pack) {
      List<JPackage> subPackages = pack.getSubPackages();
      for (JPackage subPack : subPackages) {
        if (mustBeRenamed(subPack) && !subPack.equals(packageForRenamedPackages)) {
          request.append(new ChangeEnclosingPackage(subPack, packageForRenamedPackages));
          subPack.addMarker(new OriginalPackageMarker(pack));
          rename(subPack, packageNameProvider);
        }
      }

      List<JDefinedClassOrInterface> types = pack.getTypes();
      NameProvider classNameProvider = nameProviderFactory.getClassNameProvider(types);
      for (JClassOrInterface type : types) {
        if (type instanceof JDefinedClassOrInterface) {
          rename((JDefinedClassOrInterface) type, classNameProvider);
        }
      }

      return true;
    }
  }

  @Nonnull
  private final NameProviderFactory nameProviderFactory;

  @CheckForNull
  private Collection<JDefinedClassOrInterface> allTypes;

  public Renamer() {
    File dictionary = null;
    if (ThreadConfig.get(USE_OBFUSCATION_DICTIONARY).booleanValue()) {
      dictionary = ThreadConfig.get(OBFUSCATION_DICTIONARY);
    }
    File classDictionary = null;
    if (ThreadConfig.get(USE_CLASS_OBFUSCATION_DICTIONARY).booleanValue()) {
      classDictionary = ThreadConfig.get(CLASS_OBFUSCATION_DICTIONARY);
    }
    File packageDictionary = null;
    if (ThreadConfig.get(USE_PACKAGE_OBFUSCATION_DICTIONARY).booleanValue()) {
      packageDictionary = ThreadConfig.get(PACKAGE_OBFUSCATION_DICTIONARY);
    }

    this.nameProviderFactory = new NameProviderFactory(
        dictionary,
        classDictionary,
        packageDictionary);
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    allTypes = session.getTypesToEmit();
    Map<String, String> fieldNames = new HashMap<String, String>();
    Map<String, String> methodNames = new HashMap<String, String>();
    boolean useUniqueClassMemberNames =
        ThreadConfig.get(USE_UNIQUE_CLASSMEMBERNAMES).booleanValue();
    if (ThreadConfig.get(USE_MAPPING).booleanValue()) {
      TransformationRequest request = new TransformationRequest(session);
      MappingApplier mappingApplier;
      if (useUniqueClassMemberNames) {
        mappingApplier = new CollectingMappingApplier(request);
        fieldNames = ((CollectingMappingApplier) mappingApplier).getFieldNames();
        methodNames = ((CollectingMappingApplier) mappingApplier).getMethodNames();
      } else {
        mappingApplier = new MappingApplier(request);
      }
      mappingApplier.applyMapping(ThreadConfig.get(MAPPING_FILE), session);
      request.commit();
    }

    if (useUniqueClassMemberNames) {
      Set<JFieldId> allFieldIds = new HashSet<JFieldId>();
      Set<JMethodId> allMethodIds = new HashSet<JMethodId>();
      for (JDefinedClassOrInterface type : allTypes) {
        for (JField field : type.getFields()) {
          allFieldIds.add(field.getId());
        }
        for (JMethod method : type.getMethods()) {
          allMethodIds.add(method.getMethodId());
        }
      }
      nameProviderFactory.createGlobalFieldNameProvider(fieldNames, allFieldIds);
      nameProviderFactory.createGlobalMethodNameProvider(methodNames, allMethodIds);
    }

    if (ThreadConfig.get(REPACKAGE_CLASSES).booleanValue()) {
      TransformationRequest request = new TransformationRequest(session);
      Visitor visitor = new RepackagerVisitor(request);
      visitor.accept(session);
      request.commit();
    } else if (ThreadConfig.get(FLATTEN_PACKAGE).booleanValue()) {
      TransformationRequest request = new TransformationRequest(session);
      Visitor visitor = new FlattenerVisitor(request);
      visitor.accept(session);
      request.commit();
    } else {
      Visitor visitor = new Visitor();
      visitor.accept(session);
    }
  }
}
