/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.impl;


import com.google.common.annotations.VisibleForTesting;

import com.android.jack.Jack;
import com.android.jack.ir.StringInterner;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JNodeLookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Creates unresolved references to types, fields, and methods.
 */
public class ReferenceMapper {

  @Nonnull
  private final List<String> argNames = new ArrayList<String>();
  @Nonnull
  private final Map<SignatureKey, JField> fields = new HashMap<SignatureKey, JField>();
  @Nonnull
  private final Map<SignatureKey, JMethod> methods = new HashMap<SignatureKey, JMethod>();
  @Nonnull
  private static final StringInterner stringInterner = StringInterner.get();

  @Nonnull
  private final TypePackageAndMethodFormatter lookupFormater;

  @CheckForNull
  private JDefinedClass javaLangString;

  @Nonnull
  private final JNodeLookup lookup;
  @Nonnull
  private final LookupEnvironment lookupEnvironment;

  @Nonnull
  private final SourceInfoFactory sourceInfoFactory;

  @CheckForNull
  private ReferenceBinding ecjJlo = null;

  @CheckForNull
  private MethodBinding ecjJloCloneMth = null;

  public ReferenceMapper(@Nonnull JNodeLookup lookup,
      @Nonnull LookupEnvironment lookupEnvironment, @Nonnull SourceInfoFactory sourceInfoFactory) {
    this.lookup = lookup;
    this.lookupEnvironment = lookupEnvironment;
    this.sourceInfoFactory = sourceInfoFactory;
    this.lookupFormater = Jack.getLookupFormatter();
  }

  @Nonnull
  public LookupEnvironment getLookupEnvironment() {
    return lookupEnvironment;
  }

  @Nonnull
  public SourceInfoFactory getSourceInfoFactory() {
    return sourceInfoFactory;
  }

  @Nonnull
  public JLookup getLookup() {
    return lookup;
  }

  @Nonnull
  public JField get(@Nonnull FieldBinding binding) throws JTypeLookupException {
    binding = binding.original();
    SignatureKey key = new SignatureKey(binding);
    JField field = fields.get(key);
    if (field == null) {
      // Call createField on FieldBinding having a declaring class that is not a SourceTypeBinding
      // will result in NPE. If field is not already cached and the declaring class is a
      // SourceTypeBinding, no need to search the field since it does not exists, thus  create
      // it automatically. In other cases, for instance with BinaryTypeBinding, fields can be
      // created by the Jayce file loader, and the cache will not yet be filled, thus search
      // the field and fill the cache.
      if (binding.declaringClass instanceof SourceTypeBinding) {
        field = createField(binding);
      } else {
        JDefinedClassOrInterface enclosingType =
            (JDefinedClassOrInterface) get(binding.declaringClass);
        field = findField(binding, enclosingType);
        assert field != null;
      }
      cacheField(key, field);
    }
    return field;
  }

  private boolean isCloneOfArray(@Nonnull MethodBinding binding) {
    if (ecjJlo == null) {
      ecjJlo = lookupEnvironment.getType(TypeConstants.JAVA_LANG_OBJECT);
      assert ecjJlo != null;
      MethodBinding[] methods = ecjJlo.getMethods("clone".toCharArray());
      assert methods.length == 1;
      ecjJloCloneMth = methods[0];
    }

    return binding.declaringClass.equals(ecjJlo) && new String(binding.selector).equals("clone")
        && binding.returnType.isArrayType();
  }

  @Nonnull
  public JMethod get(@Nonnull MethodBinding binding) throws JTypeLookupException {
    if (isCloneOfArray(binding)) {
      // ECJ has replaced the clone prototype "jlo clone()" by "int[] clone()", thus replace the
      // binding by the binding of clone from jlo to be able to lookup the method.
      binding = ecjJloCloneMth;
      assert binding != null;
    }

    binding = binding.original();
    SignatureKey key = new SignatureKey(binding);
    JMethod method = methods.get(key);
    if (method == null) {
      if (binding.declaringClass instanceof SourceTypeBinding) {
        method = createMethod(binding);
      } else {
        JDefinedClassOrInterface enclosingType =
            (JDefinedClassOrInterface) get(binding.declaringClass);
        method = findMethod(binding, enclosingType);
        if (method == null) {
          // because synthetic bindings are not automatically loaded but may be requested by
          // createSyntheticMethodFromBinding
          assert binding instanceof SyntheticMethodBinding;
          method = createMethod(binding);
        }
      }
      cacheMethod(key, method);
    }
    return method;
  }

  @Nonnull
  public List<JType> getBounds(@Nonnull IntersectionTypeBinding18 binding)
      throws JTypeLookupException {
    List<JType> bounds = new ArrayList<JType>(binding.intersectingTypes.length);

    for (ReferenceBinding refBinding : binding.intersectingTypes) {
      bounds.add(get(refBinding));
    }
    return bounds;
  }

  @Nonnull
  public JType get(@Nonnull TypeBinding binding) throws JTypeLookupException {
    binding = binding.erasure();
    assert !(binding instanceof IntersectionTypeBinding18);
    return get(new String(binding.signature()));
  }

  @Nonnull
  static String intern(@Nonnull char[] cs) {
    return intern(String.valueOf(cs));
  }

  @Nonnull
  static String intern(@Nonnull String s) {
    return stringInterner.intern(s);
  }

  @Nonnull
  public JType get(@Nonnull String signature) throws JTypeLookupException {
    return lookup.getType(signature);
  }

  void setField(@Nonnull FieldBinding binding, @Nonnull JField field) {
    cacheField(new SignatureKey(binding), field);
  }

  @Nonnull
  private JMethod createMethod(@Nonnull MethodBinding b) throws JTypeLookupException {
    AbstractMethodDeclaration declaration = getDeclaration(b);
    CudInfo cuInfo;
    SourceInfo info;
    if (declaration != null) {
      cuInfo = new CudInfo(declaration.scope.referenceCompilationUnit());
      b = declaration.binding;
      info = makeSourceInfo(cuInfo, declaration, sourceInfoFactory);
    } else {
      cuInfo = null;
      info = SourceInfo.UNKNOWN;
    }

    ReferenceBinding declaringClass = (ReferenceBinding) b.declaringClass.erasure();
    Set<String> alreadyNamedVariables = new HashSet<String>();
    JDefinedClassOrInterface enclosingType = (JDefinedClassOrInterface) get(declaringClass);
    JMethod method;
    boolean isNested = JackIrBuilder.isNested(declaringClass);
    int flags = b.getAccessFlags();

    // No need to add the extra 'default' modifier into Jack
    flags = flags & ~ExtraCompilerModifiers.AccDefaultMethod;

    if (b.isDeprecated()) {
      flags |= JModifier.DEPRECATED;
    }
    if (b.isConstructor()) {
      method = new JConstructor(info, (JDefinedClass) enclosingType, flags);
      if (declaringClass.isEnum()) {
        // Enums have hidden arguments for name and value
        createParameter(info, method, "enum$name",
            lookupEnvironment.getType(TypeConstants.JAVA_LANG_STRING),
            JModifier.SYNTHETIC | JModifier.NAME_PRESENT);
        createParameter(info, method, "enum$ordinal", TypeBinding.INT,
            JModifier.SYNTHETIC | JModifier.NAME_PRESENT);
      }
      // add synthetic args for outer this
      if (isNested) {
        NestedTypeBinding nestedBinding = (NestedTypeBinding) declaringClass;
        createParameters(nestedBinding.enclosingInstances, info, method, alreadyNamedVariables,
            /* forceToImplicit= */ !nestedBinding.isAnonymousType()
                || !nestedBinding.superclass().isLocalType());
      }
    } else {
      JType returnType = get(b.returnType);
      if (declaringClass.isAnnotationType()) {
        method =
            new JAnnotationMethod(info,
                new JMethodId(
                    new JMethodIdWide(intern(b.selector), MethodKind.INSTANCE_VIRTUAL), returnType),
                enclosingType,
                ReferenceMapper.removeSynchronizedOnBridge(flags));
      } else {
        method = new JMethod(info, new JMethodId(new JMethodIdWide(intern(b.selector),
              ReferenceMapper.getMethodKind(flags)), returnType),
            enclosingType,
            ReferenceMapper.removeSynchronizedOnBridge(flags));
      }
    }

    // User args.
    if (declaration != null) {
      assert cuInfo != null;
      createParameters(method, declaration, cuInfo);
    } else {
      mapParameters(info, method, b, 0);
    }

    if (b.isConstructor()) {
      if (isNested) {
        // add synthetic args for locals
        NestedTypeBinding nestedBinding = (NestedTypeBinding) declaringClass;
        // add synthetic args for outer this and locals
        createParameters(nestedBinding.outerLocalVariables, info, method, alreadyNamedVariables,
            /* forceToImplicit= */ false);
      }
    }

    mapExceptions(method, b);

    if (b.isSynthetic()) {
      method.setSynthetic();
    }
    enclosingType.addMethod(method);
    char[] genSignature = b.genericSignature();
    if (genSignature != null) {
      method.addMarker(new GenericSignature(intern(genSignature)));
    }

    method.updateParents(enclosingType);
    return method;
  }

  @CheckForNull
  private AbstractMethodDeclaration getDeclaration(@Nonnull MethodBinding b) {
    // Lambda method does not have declaration but they are not SyntheticMethodBinding, thus use
    // also b.isSynthetic(). isSynthetic method can not always be used because some
    // SyntheticMethodBinding does not have synthetic modifier, it is the case for valueOf of
    // enumeration
    if (b instanceof SyntheticMethodBinding || b.isSynthetic()) {
      return null;
    }

    AbstractMethodDeclaration declaration = b.sourceMethod();
    if (declaration == null) { // happens at least for clone() of arrays, see UpdatedMethodBinding
      SourceTypeBinding sourceType = (SourceTypeBinding) b.declaringClass;
      for (AbstractMethodDeclaration candidate : sourceType.scope.referenceContext.methods) {
        if (CharOperation.equals(candidate.selector, b.selector)
            && CharOperation.equals(candidate.binding.signature(), b.signature())) {
          declaration = candidate;
          break;
        }
      }
      assert declaration != null;
    }
    return declaration;
  }

  private void createParameters(@CheckForNull SyntheticArgumentBinding[] sab,
      @Nonnull SourceInfo info, @Nonnull JMethod method,
      @Nonnull Set<String> alreadyNamedVariables,
      boolean forceToImplicit) {
    if (sab != null) {
      for (int i = 0; i < sab.length; ++i) {
        SyntheticArgumentBinding arg = sab[i];
        String argName = String.valueOf(arg.name);
        if (alreadyNamedVariables.contains(argName)) {
          argName += "_" + i;
        }
        createParameter(info, method, argName, arg.type,
            getFinalModifier(arg) | (forceToImplicit ? JModifier.IMPLICIT : JModifier.SYNTHETIC)
                | JModifier.NAME_PRESENT);
        alreadyNamedVariables.add(argName);
      }
    }
  }

  private void createParameters(@Nonnull JMethod method, @Nonnull AbstractMethodDeclaration x,
      @Nonnull CudInfo cuInfo) throws JTypeLookupException {
    if (x.arguments != null) {
      for (Argument argument : x.arguments) {
        SourceInfo info = makeSourceInfo(cuInfo, argument, sourceInfoFactory);
        LocalVariableBinding binding = argument.binding;
        createParameter(info, method, intern(binding.name), binding.type,
            getFinalModifier(binding) | JModifier.NAME_PRESENT);
      }
    }
  }

  private int getFinalModifier(@Nonnull LocalVariableBinding lvBinding) {
    return lvBinding.isFinal() ? JModifier.FINAL : JModifier.DEFAULT;
  }

  @Nonnull
  public JParameter createParameter(@Nonnull SourceInfo info, @Nonnull JMethod method,
      @Nonnull String name, @Nonnull TypeBinding typeBinding, int modifier) {
    return createParameter(info, method, name, typeBinding, modifier, method.getParams().size());
  }

  @Nonnull
  public JParameter createParameter(@Nonnull SourceInfo info, @Nonnull JMethod method,
      @Nonnull String name, @Nonnull TypeBinding typeBinding, int modifier, int paramIndex)
      throws JTypeLookupException {
    JType type = get(typeBinding);
    JParameter param =
        new JParameter(info, name, type, modifier, method);

    method.getParams().add(paramIndex, param);
    method.getMethodIdWide().getParamTypes().add(paramIndex, type);

    addGenericSignatureMarker(typeBinding, param);

    assert method.getParams().size() == method.getMethodIdWide().getParamTypes().size();

    return param;
  }

  public void addGenericSignatureMarker(@Nonnull TypeBinding typeBinding,
      @Nonnull JVariable variable) {
    char[] genericTypeSignature = typeBinding.genericTypeSignature();
    if (genericTypeSignature != null) {
      char[] signature = typeBinding.signature();
      char[] genericSignature = typeBinding.genericTypeSignature();
      // Check if the generic signature really contains generic types i.e. is different from the
      // non-generic signature
      if (!CharOperation.equals(signature, genericSignature)) {
        variable.addMarker(new GenericSignature(intern(genericSignature)));
      }
    }
  }

  @Nonnull
  private JField createField(@Nonnull FieldBinding binding) throws JTypeLookupException {
    FieldDeclaration sourceField = binding.sourceField();

    CudInfo cuInfo =
        new CudInfo(((SourceTypeBinding) binding.declaringClass).scope.referenceCompilationUnit());
    SourceInfo info = makeSourceInfo(cuInfo, sourceField, sourceInfoFactory);
    JType type = get(binding.type);
    JDefinedClassOrInterface enclosingType = (JDefinedClassOrInterface) get(binding.declaringClass);

    JField field;
    if (sourceField.initialization != null
        && sourceField.initialization instanceof AllocationExpression
        && ((AllocationExpression) sourceField.initialization).enumConstant != null) {
      field =
          new JEnumField(info, intern(binding.name), binding.original().id,
              (JDefinedEnum) enclosingType, (JDefinedClass) type);
    } else {
      int flags = binding.getAccessFlags();
      if (binding.isDeprecated()) {
        flags |= JModifier.DEPRECATED;
      }
      if (isCompileTimeConstant(binding)) {
        flags |= JModifier.COMPILE_TIME_CONSTANT;
      }
      field = new JField(info, intern(binding.name), enclosingType, type, flags);
    }
    enclosingType.addField(field);
    char [] genSignature = binding.genericSignature();
    if (genSignature != null) {
      field.addMarker(new GenericSignature(intern(genSignature)));
    }

    field.updateParents(enclosingType);

    return field;
  }

  @Nonnull
  static JRetentionPolicy getRetentionPolicy(long tagBits) {
    JRetentionPolicy result;
    long annotBits = tagBits & TagBits.AnnotationRetentionMASK;

    if ((annotBits ^ TagBits.AnnotationSourceRetention) == 0) {
      result = JRetentionPolicy.SOURCE;
    } else if ((annotBits ^ TagBits.AnnotationRuntimeRetention) == 0) {
      result = JRetentionPolicy.RUNTIME;
    } else  {
      result = JRetentionPolicy.CLASS;
    }
    return result;
  }

  private void ensureArgNames(int required) {
    for (int i = argNames.size(); i <= required; ++i) {
      argNames.add(intern("arg" + i));
    }
  }

  private void mapExceptions(JMethod method, MethodBinding binding) throws JTypeLookupException {
    ReferenceBinding[] thrownExceptions = binding.thrownExceptions;
    int length = thrownExceptions.length;
    if (length != 0) {
      List<JClass> thrownException = new ArrayList<JClass>(length);
      for (ReferenceBinding thrownBinding : thrownExceptions) {
        JDefinedClass type = (JDefinedClass) get(thrownBinding);
        thrownException.add(type);
      }
      method.addMarker(new ThrownExceptionMarker(thrownException));
    }
  }

  private int mapParameters(SourceInfo info, JMethod method, MethodBinding binding, int argPosition)
      throws JTypeLookupException {
    if (binding.parameters != null) {
      ensureArgNames(argPosition + binding.parameters.length);
      for (TypeBinding argType : binding.parameters) {
        createParameter(info, method, argNames.get(argPosition++), argType, JModifier.DEFAULT);
      }
    }
    return argPosition;
  }

  @Nonnull
  private static String getTypeConstantPoolName(@Nonnull String typeName) {
    assert typeName.charAt(0) == 'L' : typeName + " is not well formed.";
    assert typeName.charAt(typeName.length() - 1) == ';' : typeName + " is not well formed.";
    return typeName.substring(1, typeName.length() - 1);
  }

  @Nonnull
  public static ReferenceBinding getEcjType(@Nonnull String typeName,
      @Nonnull LookupEnvironment lookupEnvironment) throws JTypeLookupException {
    String typeNameWithDot = getTypeConstantPoolName(typeName);
    char[][] compoundName = CharOperation.splitOn('/', typeNameWithDot.toCharArray());
    ReferenceBinding refBinding = lookupEnvironment.getType(compoundName);

    if (refBinding instanceof ProblemReferenceBinding) {
      ProblemReferenceBinding problem = (ProblemReferenceBinding) refBinding;
      ReferenceBinding closestMatch = problem.closestReferenceMatch();
      if (closestMatch != null && typeName.equals(new String(closestMatch.signature()))) {
        assert closestMatch.isNestedType();
        refBinding = closestMatch;
      } else {
        refBinding = null;
      }
    }

    if (refBinding == null) {
      throw new MissingJTypeLookupException(typeName);
    }

    return (refBinding);
  }

  static int removeSynchronizedOnBridge(int accessFlags) {
    if (JModifier.isBridge(accessFlags)) {
      accessFlags &= ~JModifier.SYNCHRONIZED;
    }
    return accessFlags;
  }


  /**
   * Get the {@link MethodKind} of a non constructor method from its access flags.
   */
  @Nonnull
  static MethodKind getMethodKind(int accessFlags) {
    if (JModifier.isStatic(accessFlags)) {
        return MethodKind.STATIC;
    } else if (JModifier.isPrivate(accessFlags)) {
      return MethodKind.INSTANCE_NON_VIRTUAL;
    } else {
      return MethodKind.INSTANCE_VIRTUAL;
    }
  }

  @CheckForNull
  private JField findField(@Nonnull FieldBinding binding,
      @Nonnull JDefinedClassOrInterface enclosingType) {
    JField field = null;
    String name = new String(binding.name);
    String typeSignature = new String(binding.type.signature());
    for (JField existing: enclosingType.getFields()) {
      if (name.equals(existing.getName()) &&
          typeSignature.equals(Jack.getLookupFormatter().getName(existing.getType()))) {
        field = existing;
        break;
      }
    }
    return field;
  }

  @CheckForNull
  private JMethod findMethod(
      @Nonnull MethodBinding binding, @Nonnull JDefinedClassOrInterface enclosingType) {
    JMethod method = null;
    String paramsSignature = new String(binding.signature());
    String searchedSignature = new String(binding.selector) + paramsSignature;
    int paramsCount = countParams(paramsSignature);
    for (JMethod existing : enclosingType.getMethods()) {
      if (equals(paramsCount, searchedSignature, existing)) {
        method = existing;
        break;
      }
    }
    return method;
  }

  @Nonnegative
  @VisibleForTesting
  static int countParams(@Nonnull String signature) {
    int result = 0;
    int pos = 1; // skip '('
    while (pos < signature.length() && signature.charAt(pos) != ')') {
      switch (signature.charAt(pos)) {
        case 'L':
          do {
            pos++;
          } while (pos < signature.length() && signature.charAt(pos) != ';');
          assert pos < signature.length() && signature.charAt(pos) == ';';
          // Fall-through.
        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I':
        case 'J':
        case 'F':
        case 'D':
          result++;
          break;
        case '[':
          // ignore
          break;
        default:
          throw new AssertionError();
      }
      pos++;
    }
    return result;
  }

  private boolean equals(@Nonnegative int paramsCount,
      @Nonnull String bindingSignature, @Nonnull JMethod method) {
    if (paramsCount != method.getParams().size() ||
        !bindingSignature.startsWith(method.getName())) {
      return false;
    }
    return bindingSignature.equals(lookupFormater.getName(method));
  }

  static SourceInfo makeSourceInfo(@Nonnull CudInfo cuInfo, @Nonnull AbstractMethodDeclaration x,
      @Nonnull SourceInfoFactory factory) {
    return JackIrBuilder.makeSourceInfo(cuInfo, x.declarationSourceStart, x.declarationSourceEnd,
        factory);
  }

  static SourceInfo makeSourceInfo(@Nonnull CudInfo cuInfo, @Nonnull ASTNode x,
      @Nonnull SourceInfoFactory factory) {
    return JackIrBuilder.makeSourceInfo(cuInfo, x.sourceStart, x.sourceEnd, factory);
  }

  static boolean isCompileTimeConstant(@Nonnull FieldBinding binding) {
    assert !binding.isFinal() || !binding.isVolatile();
    boolean isCompileTimeConstant =
        binding.isStatic() && binding.isFinal() && (binding.constant() != Constant.NotAConstant);
    if (isCompileTimeConstant) {
      assert binding.type.isBaseType() || (binding.type.id == TypeIds.T_JavaLangString);
    }
    return isCompileTimeConstant;
  }

  private void cacheMethod(@Nonnull SignatureKey key, @Nonnull JMethod method) {
    assert !methods.containsKey(key);
    methods.put(key, method);
  }

  private void cacheField(@Nonnull SignatureKey key, @Nonnull JField field) {
    assert !fields.containsKey(key);
    fields.put(key, field);
  }

  private static class SignatureKey {
    private static final int PRIME = 277;
    @Nonnull
    private final char[] declaringClass;
    @Nonnull
    private final char[] name;
    @Nonnull
    private final char[] signature;
    @Nonnegative
    private final int hashCode;

    public SignatureKey(@Nonnull char[] declaringClass, @Nonnull char[] name,
        @Nonnull char[] signature) {
      this.declaringClass = declaringClass;
      this.name = name;
      this.signature = signature;
      hashCode = hash(declaringClass) ^ hash(name) ^ hash(signature);
    }

    private static int hash(@Nonnull char[] data) {
      int hash = 0;
      for (int i = 0; i < data.length; ++i) {
         hash = hash * PRIME + data[i];
      }
      return hash;
    }

    public SignatureKey(@Nonnull MethodBinding binding) {
      this(binding.declaringClass.constantPoolName(), binding.selector, binding.signature());
    }

    public SignatureKey(@Nonnull FieldBinding binding) {
      this(binding.declaringClass.constantPoolName(), binding.name, binding.type.signature());
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
      if (!(obj instanceof SignatureKey)) {
        return false;
      }
      SignatureKey key = (SignatureKey) obj;
      return Arrays.equals(declaringClass, key.declaringClass) && Arrays.equals(name, key.name) &&
          Arrays.equals(signature, key.signature);
    }

    @Override
    public final int hashCode() {
      return hashCode;
    }
  }
}
