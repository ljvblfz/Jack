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

package com.android.jack.ecj.loader.jast;

import com.android.jack.ir.ast.HasEnclosingMethod;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.util.NamingTools;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@code IBinaryType} for jack.
 */
class JAstBinaryType implements IBinaryType {

  @Nonnull
  private static final char[] OBJECT = "java/lang/Object".toCharArray();

  @Nonnull
  private final JDefinedClassOrInterface jDeclaredType;
  @Nonnull
  private final JAstClasspath classpathLocation;

  JAstBinaryType(
      @Nonnull JDefinedClassOrInterface jDeclaredType, @Nonnull JAstClasspath classpathLocation) {
    this.jDeclaredType = jDeclaredType;
    this.classpathLocation = classpathLocation;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getModifiers() {
    int modifiers;
    JAnnotation enclosingAnnotation =
        AnnotationUtils.getAnnotation(jDeclaredType, AnnotationUtils.INNER_CLASS_ANNOTATION);
    if (enclosingAnnotation != null) {
      JNameValuePair pair =
          enclosingAnnotation.getNameValuePair(AnnotationUtils.INNERCLASS_ACCFLAGS_FIELD);
      assert pair != null;
      modifiers = ((JIntLiteral) pair.getValue()).getIntValue();
    } else {
      modifiers = jDeclaredType.getModifier();
    }
    modifiers = LoaderUtils.convertJAstModifiersToEcj(modifiers, jDeclaredType);
    modifiers &= ~JModifier.ANONYMOUS_TYPE;

    JClassOrInterface enclosingType = jDeclaredType.getEnclosingType();
    if (enclosingType != null && !isAnonymous()
        && enclosingType instanceof JDefinedClassOrInterface) {
      /* If the enclosing is not in the classpath, just skip. This should be with no bad consequence
       * since ECJ is refusing to compile a source referencing an inner class when its enclosing
       * class is missing.
       */
      JAstBinaryType enclosing =
          classpathLocation.findType((JDefinedClassOrInterface) enclosingType);
      if (enclosing != null) {
        if (LoaderUtils.isDeprecated(enclosing)) {
          modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
        }
      }
    }

    return modifiers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBinaryType() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getFileName() {
    return (classpathLocation.getPath() + "|" + getBinaryName()).toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryAnnotation[] getAnnotations() {
    return AnnotationUtils.convertJAstAnnotationToEcj(jDeclaredType, true);
  }

  public boolean hasEnclosingMethod() {
    return jDeclaredType instanceof JDefinedClass
        && ((JDefinedClass) jDeclaredType).getEnclosingMethod() != null;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] getEnclosingTypeName() {
    char[] enclosingTypeName = null;
    JClassOrInterface enclosingType = jDeclaredType.getEnclosingType();
    if (enclosingType == null && jDeclaredType instanceof JDefinedClass) {

      JMethod enclosingMethod = ((JDefinedClass) jDeclaredType).getEnclosingMethod();
      if (enclosingMethod != null) {
        enclosingType = enclosingMethod.getEnclosingType();
      }
    }
    if (enclosingType != null) {
      enclosingTypeName =
          LoaderUtils.getQualifiedNameFormatter().getName(enclosingType).toCharArray();
    }
    return enclosingTypeName;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryField[] getFields() {
    List<JField> fields = jDeclaredType.getFields();
    int totalFields = fields.size();

    if (totalFields == 0) {
      return null;
    }
    IBinaryField[] allFields = new IBinaryField[totalFields];
    int indexInAllFields = 0;
    for (JField field : fields) {
      JLiteral initialValue = null;
      initialValue = field.getLiteralInitializer();
      if (initialValue == null) {
        initialValue = field.getInitialValue();
      }
      allFields[indexInAllFields] = new JAstBinaryField(field, initialValue);
      indexInAllFields++;
    }

    return allFields;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] getGenericSignature() {
    return LoaderUtils.getGenericSignature(jDeclaredType);
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[][] getInterfaceNames() {
    List<JInterface> implemented = jDeclaredType.getImplements();
    char[][] names = null;
    int interfaceCount = implemented.size();
    if (interfaceCount > 0) {
      names = new char[interfaceCount][];
      TypeFormatter formatter = LoaderUtils.getQualifiedNameFormatter();
      for (int i = 0; i < interfaceCount; i++) {
        names[i] = formatter.getName(implemented.get(i)).toCharArray();
      }
    }
    return names;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryNestedType[] getMemberTypes() {
    List<JClassOrInterface> members = jDeclaredType.getMemberTypes();

    IBinaryNestedType[] nestedTypesArray = null;

    if (!members.isEmpty()) {
       int nestedTypeCount = members.size();
      List<IBinaryNestedType> nestedTypes = new ArrayList<IBinaryNestedType>(nestedTypeCount);

      for (JClassOrInterface jNested : members) {

        /* If the inner is not in the classpath, just skip it. This should not have consequence in
         * Jack context, since we are unable to present the missing class to ECJ anyway. */
        if (jNested instanceof JDefinedClassOrInterface) {
          /* According to a note in a comment on the interface, we have to filter out local types
           * found in the member types list. Tests have shown that, in this note, "local" means also
           * anonymous.
           */
          JAstBinaryType nested = classpathLocation.findType((JDefinedClassOrInterface) jNested);
          assert nested != null;
          if (!(nested.isAnonymous() || nested.isLocal())) {
            nestedTypes.add(new JAstBinaryNestedType(nested.jDeclaredType));
          }
        }
      }
      if (!nestedTypes.isEmpty()) {
        nestedTypesArray = nestedTypes.toArray(new IBinaryNestedType[nestedTypes.size()]);
      }
    }

    return nestedTypesArray;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryMethod[] getMethods() {
    List<JMethod> jMethods = jDeclaredType.getMethods();
    int total = jMethods.size();
    IBinaryMethod[] methods = null;
      if (total != 0) {
        methods = new IBinaryMethod[total];
        int indexInMethods = 0;
        for (JMethod method: jMethods) {
          methods[indexInMethods] = new JAstBinaryMethod(method);
          indexInMethods++;
        }
    }

    return methods;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[][][] getMissingTypeNames() {
    // N/A (Eclipse compiled classes with errors)
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getName() {
    return getBinaryName().toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] getSourceName() {

    SimpleName typeInfo = jDeclaredType.getMarker(SimpleName.class);
    if (typeInfo != null) {
      return typeInfo.getSimpleName().toCharArray();
    }

    char[] sourceNameArray;
    JAnnotation enclosingAnnotation =
        AnnotationUtils.getAnnotation(jDeclaredType, AnnotationUtils.INNER_CLASS_ANNOTATION);
    if (enclosingAnnotation != null) {

      /* if this class is an inner then return the source name given by the annotation */
      JNameValuePair pair =
          enclosingAnnotation.getNameValuePair(AnnotationUtils.INNERCLASS_NAME_FIELD);
      assert pair != null : "Invalid jayce file";
      JLiteral nameValue = pair.getValue();
      if (nameValue instanceof JNullLiteral) {
        /*
         * javadoc says anonymous must return null but ClassFileReader returns a name. Anyway
         * ClassFileReader returns weird things with anonymous in inner. Lets stick to the javadoc
         * for now. If we decide to stick to the reference instead we could just subtract the
         * enclosingTypeName to this type name.
         */
        sourceNameArray = null;
      } else {
        String sourceName = ((JAbstractStringLiteral) nameValue).getValue();
        sourceNameArray = sourceName.toCharArray();
      }

    } else {
      String binaryName = getBinaryName();
      String simpleName = NamingTools.getSimpleClassNameFromBinaryName(binaryName);
      sourceNameArray = simpleName.toCharArray();
    }

    return sourceNameArray;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] getSuperclassName() {
    JClass superType = jDeclaredType.getSuperClass();
    if (superType != null) {
      return LoaderUtils.getQualifiedNameFormatter().getName(superType).toCharArray();
    } else if (jDeclaredType instanceof JDefinedInterface) {
      return OBJECT;
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTagBits() {
    return AnnotationUtils.getTagBits(jDeclaredType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAnonymous() {
    boolean isAnonymous = false;
    SimpleName simpleNameInfo = jDeclaredType.getMarker(SimpleName.class);
    if (simpleNameInfo != null) {
      isAnonymous = simpleNameInfo.getSimpleName().isEmpty();
    } else {
      JAnnotation enclosingAnnotation =
          AnnotationUtils.getAnnotation(jDeclaredType, AnnotationUtils.INNER_CLASS_ANNOTATION);
      if (enclosingAnnotation != null) {
        JNameValuePair pair =
            enclosingAnnotation.getNameValuePair(AnnotationUtils.INNERCLASS_NAME_FIELD);
        assert pair != null;
        isAnonymous = (pair.getValue() instanceof JNullLiteral);
      }
    }

    return isAnonymous;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLocal() {
    return (!isAnonymous()) && hasEnclosingMethod();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isMember() {
    return (!isAnonymous())
        && (jDeclaredType.getEnclosingType() != null && !hasEnclosingMethod());
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] sourceFileName() {
    if (jDeclaredType.getSourceInfo() == SourceInfo.UNKNOWN) {
      return null;
    }
    String fileName = jDeclaredType.getSourceInfo().getFileName();
    int simpleNameIndex = fileName.lastIndexOf('/');
    simpleNameIndex = Math.max(simpleNameIndex, fileName.lastIndexOf('\\'));
    if (simpleNameIndex > 0) {
      fileName = fileName.substring(simpleNameIndex + 1);
    }

    return fileName.toCharArray();

  }

  @Override
  @Nonnull
  public String toString() {
    return jDeclaredType.toString();
  }

  @Nonnull
  String getBinaryName() {
    return LoaderUtils.getQualifiedNameFormatter().getName(jDeclaredType);
  }

  @Override
  public char[] getEnclosingMethod() {
    char[] enclosingMethodName = null;

    if (jDeclaredType instanceof HasEnclosingMethod) {
      JMethod enclosingMethod = ((HasEnclosingMethod) jDeclaredType).getEnclosingMethod();
      if (enclosingMethod != null) {
        enclosingMethodName =
            LoaderUtils.getSignatureFormatter().getName(enclosingMethod).toCharArray();
      }
    }

    return enclosingMethodName;
  }

  @Override
  public IBinaryTypeAnnotation[] getTypeAnnotations() {
    return null;
  }

  @Override
  public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker,
      Object member, LookupEnvironment environment) {
    // Jack does not support ecj external annotation file
    return walker;
  }
}
