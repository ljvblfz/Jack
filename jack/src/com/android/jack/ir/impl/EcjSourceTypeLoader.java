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

package com.android.jack.ir.impl;

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.StringInterner;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.marker.OriginalTypeInfo;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.jack.lookup.JLookup;
import com.android.jack.util.NamingTools;
import com.android.sched.marker.Marker;
import com.android.sched.util.location.Location;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class or interface loader implementation for source types parsed by ecj frontend.
 */
public class EcjSourceTypeLoader implements ClassOrInterfaceLoader {

  private static enum Scope {
    HIERARCHY,
    FIELDS,
    METHODS,
    MARKERS,
    RETENTION,
    MODIFIER,
    ENCLOSING,
    INNERS;

    private final int mask;

    private Scope() {
      this.mask = 1 << ordinal();
    }

    public int getMask() {
      return mask;
    }
  }

  @Nonnull
  private final WeakReference<SourceTypeBinding> bindingRef;

  @CheckForNull
  private final WeakReference<TypeDeclaration> declarationRef;

  @Nonnull
  private final WeakReference<ReferenceMapper> refMap;

  private int loadStatus = 0;

  @Nonnull
  private final Location location;

  @Nonnull
  public static JDefinedClassOrInterface createType(@Nonnull ReferenceMapper refMap,
      @Nonnull JPackage enclosingPackage, @Nonnull SourceTypeBinding binding,
      @CheckForNull TypeDeclaration typeDeclaration, Location location) {
    EcjSourceTypeLoader loader = new EcjSourceTypeLoader(refMap, binding, typeDeclaration,
        location);
    CudInfo cuInfo = new CudInfo(binding.scope.referenceCompilationUnit());
    SourceInfo info = ReferenceMapper.makeSourceInfo(cuInfo, binding.scope.referenceContext);
    String name;
    if (binding instanceof LocalTypeBinding) {
      name = NamingTools.getSimpleClassNameFromBinaryName(new String(binding.constantPoolName()));
    } else {
      name = new String(binding.compoundName[binding.compoundName.length - 1]);
    }
    name = intern(name);
    JDefinedClassOrInterface type;
    int accessFlags = binding.getAccessFlags();
    if (binding.isClass()) {
      type = new JDefinedClass(info, name, accessFlags, enclosingPackage, loader);
      if (binding.isNestedType()
          && !binding.isMemberType()
          && ((LocalTypeBinding) binding).enclosingMethod != null) {
        MethodBinding methodBinding = ((LocalTypeBinding) binding).enclosingMethod;
        JMethod jMethod = refMap.get(methodBinding);
        ((JDefinedClass) type).setEnclosingMethod(jMethod);
      }
    } else if (binding.isInterface()) {
      if (binding.isAnnotationType()) {
        assert JModifier.isAnnotation(accessFlags);
        type = new JDefinedAnnotation(info, name, accessFlags,
            enclosingPackage, loader);
      } else {
        type = new JDefinedInterface(info, name, accessFlags, enclosingPackage, loader);
      }
    } else if (binding.isEnum()) {
      if (binding.isAnonymousType()) {
        // Don't model an enum subclass as a JEnumType.
        assert JModifier.isEnum(accessFlags);
        type = new JDefinedClass(info, name, accessFlags, enclosingPackage, loader);
      } else {
        type = new JDefinedEnum(info, name, accessFlags, enclosingPackage, loader);
      }
    } else {
      throw new AssertionError("ReferenceBinding is not a class, interface, or enum.");
    }
    type.updateParents(enclosingPackage.getSession());
    return type;
  }


  EcjSourceTypeLoader(@Nonnull ReferenceMapper refMap, @Nonnull SourceTypeBinding binding,
      @CheckForNull TypeDeclaration typeDeclaration, @Nonnull Location location) {
    this.refMap = new WeakReference<ReferenceMapper>(refMap);
    this.bindingRef = new WeakReference<SourceTypeBinding>(binding);
    if (typeDeclaration != null) {
      this.declarationRef = new WeakReference<TypeDeclaration>(typeDeclaration);
    } else {
      this.declarationRef = null;
    }
    this.location = location;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JDefinedClassOrInterface loaded) {
    return location;
  }


  private void load(@Nonnull JLookup lookup, @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull FieldBinding binding) {
    getRefMap().createField(binding);
  }

  @Nonnull
  private ReferenceMapper getRefMap() {
    ReferenceMapper refMap = this.refMap.get();
    assert refMap != null;
    return refMap;
  }


  private void load(@Nonnull JLookup lookup, @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull MethodBinding binding) {
    getRefMap().createMethod(binding, null);
  }

  @Nonnull
  private SourceTypeBinding getBinding(@Nonnull JDefinedClassOrInterface loaded) {
    SourceTypeBinding binding = bindingRef.get();
    assert binding != null;
    return binding;
  }

  @Nonnull
  private static String intern(@Nonnull String name) {
    return StringInterner.get().intern(name);
  }

  static boolean isNested(@Nonnull ReferenceBinding binding) {
    return binding.isNestedType() && !binding.isStatic();
  }

  @Override
  public void ensureHierarchy(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.HIERARCHY)) {
        return;
      }
      SourceTypeBinding binding = getBinding(loaded);
      JLookup lookup = loaded.getEnclosingPackage().getSession().getPhantomLookup();
      if (loaded instanceof JDefinedClass) {
        ReferenceBinding superclass = binding.superclass();
        if (superclass != null) {
          ((JDefinedClass) loaded).setSuperClass(lookup.getClass(
              new String(superclass.signature())));
        }
      }
      ReferenceBinding[] superInterfaces = binding.superInterfaces();
      if (superInterfaces != null) {
        for (ReferenceBinding intf : superInterfaces) {
          loaded.addImplements(lookup.getInterface(new String(intf.signature())));
        }
      }
     markLoaded(Scope.HIERARCHY);
   }
  }

  @Override
  public void ensureEnclosing(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.ENCLOSING)) {
        return;
      }
      ReferenceBinding enclosingBinding = getBinding(loaded).enclosingType();
      if (enclosingBinding != null) {
        JDefinedClassOrInterface enclosing =
            (JDefinedClassOrInterface) getRefMap().get(enclosingBinding);
        loaded.setEnclosingType(enclosing);
        enclosing.addMemberType(loaded);
      }
      markLoaded(Scope.ENCLOSING);
    }
  }

  @Override
  public void ensureMarkers(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.MARKERS)) {
        return;
      }
      SourceTypeBinding binding = getBinding(loaded);
      OriginalTypeInfo marker = new OriginalTypeInfo();
      char [] genSignature = binding.genericSignature();
      if (genSignature != null) {
        if (CharOperation.contains('<', genSignature)) {
          assert CharOperation.contains('>', genSignature);
          marker.setGenericSignature(GwtAstBuilder.getStringLiteral(SourceOrigin.UNKNOWN,
              genSignature));
        }
      }
      marker.setSourceName(new String(binding.sourceName));
      loaded.addMarker(marker);
      markLoaded(Scope.MARKERS);
    }
  }

  @Override
  public void ensureMarker(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull Class<? extends Marker> cls) {
    if (cls == OriginalTypeInfo.class) {
      ensureMarkers(loaded);
    }
  }

  @Override
  public void ensureAnnotations(@Nonnull JDefinedClassOrInterface loaded) {
    // Not yet supported here, only done by GwtAstBuilder in full pass
  }

  @Override
  public void ensureAnnotation(
      @Nonnull JDefinedClassOrInterface loaded, @Nonnull JAnnotation annotation) {
    ensureAnnotations(loaded);
  }

  @Override
  public void ensureInners(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.INNERS)) {
        return;
      }
      if (declarationRef != null) {
        TypeDeclaration declaration = declarationRef.get();
        assert declaration != null;
        if (declaration.memberTypes != null) {
          ReferenceMapper referenceMapper = refMap.get();
          for (TypeDeclaration memberType : declaration.memberTypes) {
            ((JDefinedClassOrInterface) referenceMapper.get(memberType.binding)).getEnclosingType();
          }
        }
      }
      markLoaded(Scope.INNERS);
    }
  }

  public void loadFully(@Nonnull JDefinedClassOrInterface loaded) {
    ensureHierarchy(loaded);
    ensureMarkers(loaded);
    ensureModifier(loaded);
    ensureAnnotations(loaded);
    ensureMethods(loaded);
    ensureFields(loaded);
    ensureEnclosing(loaded);
    ensureInners(loaded);
    if (loaded instanceof JDefinedAnnotation) {
      ensureRetentionPolicy((JDefinedAnnotation) loaded);
    }
  }

  @Override
  public void ensureMethods(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.METHODS)) {
        return;
      }
      SourceTypeBinding binding = getBinding(loaded);
      JLookup lookup = loaded.getEnclosingPackage().getSession().getPhantomLookup();
      for (MethodBinding methodBinding : binding.methods()) {
        load(lookup, loaded, methodBinding);
      }
      markLoaded(Scope.METHODS);
    }
  }

  @Override
  public void ensureMethod(@Nonnull JDefinedClassOrInterface loaded, @Nonnull String name,
      @Nonnull List<? extends JType> args, @Nonnull JType returnType) {
    ensureMethods(loaded);
  }

  @Override
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.FIELDS)) {
        return;
      }
      SourceTypeBinding binding = getBinding(loaded);
      JLookup lookup = loaded.getEnclosingPackage().getSession().getPhantomLookup();
      for (FieldBinding fieldBinding : binding.fields()) {
        load(lookup, loaded, fieldBinding);
      }
      markLoaded(Scope.FIELDS);
    }
  }

  @Override
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded, @Nonnull String fieldName) {
    ensureFields(loaded);
  }

  @Override
  public void ensureRetentionPolicy(@Nonnull JDefinedAnnotation loaded) {
    synchronized (this) {
      if (isLoaded(Scope.RETENTION)) {
        return;
      }
      loaded.setRetentionPolicy(ReferenceMapper.getRetentionPolicy(
          getBinding(loaded).getAnnotationTagBits()));
      markLoaded(Scope.RETENTION);
    }
  }


  @Override
  public void ensureModifier(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (isLoaded(Scope.MODIFIER)) {
        return;
      }

      SourceTypeBinding binding = getBinding(loaded);
      int accessFlags = binding.getAccessFlags();
      if (binding.isAnonymousType()) {
        accessFlags |= JModifier.ANONYMOUS_TYPE;
        // add missing static flag on static anonymous classes
        if (GwtAstBuilder.isNested(binding)) {
          NestedTypeBinding nestedBinding = (NestedTypeBinding) binding;
          accessFlags |= JModifier.STATIC;
          if (nestedBinding.enclosingInstances != null) {
            accessFlags &= ~JModifier.STATIC;
          }
        }
      }
      if (binding.isDeprecated()) {
        accessFlags |= JModifier.DEPRECATED;
      }
      loaded.setModifier(accessFlags);
      markLoaded(Scope.MODIFIER);
    }
  }

  private boolean isLoaded(@Nonnull Scope range) {
    return (loadStatus & range.getMask()) != 0;
  }

  private void markLoaded(@Nonnull Scope range) {
    loadStatus |= range.getMask();
  }
}
