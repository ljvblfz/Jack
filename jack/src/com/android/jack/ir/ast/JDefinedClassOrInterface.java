/*
 * Copyright 2008 Google Inc.
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
package com.android.jack.ir.ast;


import com.android.jack.Jack;
import com.android.jack.ir.SourceInfo;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.JMethodIdLookupException;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.lookup.JMethodWithReturnLookupException;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.util.location.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any reference type.
 */
@Description("Declared type")
public abstract class JDefinedClassOrInterface extends JDefinedReferenceType
  implements JClassOrInterface, Annotable, CanBeAbstract, CanBeFinal {

  private static final long serialVersionUID = 1L;

  protected transient List<JField> fields = new ArrayList<JField>();

  protected transient List<JMethod> methods = new ArrayList<JMethod>();

  /**
   * The type which originally enclosed this type. Null if this class was a
   * top-level type. Note that all classes are converted to top-level types in
   * {@code com.android.gwt.dev.jjs.impl.GenerateJavaAST}; this information is
   * for tracking purposes.
   */
  private JClassOrInterface enclosingType;

  /**
   * List of inner types i.e. the types whose enclosed by this type.
   */
  @Nonnull
  private final List<JClassOrInterface> inners = new ArrayList<JClassOrInterface>();

  /**
   * True if this type is defined in the bootclasspath or the classpath but not in a sourcepath.
   */
  private boolean isExternal = true;

  /**
   * This type's modifier.
   */
  private int modifier;

  protected final AnnotationSet annotations = new AnnotationSet();

  @Nonnull
  private JPackage enclosingPackage;

  @Nonnull
  protected List<JMethodId> phantomMethods = new ArrayList<JMethodId>();

  @Nonnull
  protected List<JFieldId> phantomFields = new ArrayList<JFieldId>();

  @Nonnull
  protected final ClassOrInterfaceLoader loader;

  @Nonnull
  private final Location location;

  public JDefinedClassOrInterface(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
      @Nonnull JPackage enclosingPackage) {
    this(info, name, modifier, enclosingPackage, NopClassOrInterfaceLoader.INSTANCE);
    assert NamingTools.isIdentifier(name);

  }

  public JDefinedClassOrInterface(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
        @Nonnull JPackage enclosingPackage, @Nonnull ClassOrInterfaceLoader loader) {
    super(info, name);
    assert NamingTools.isIdentifier(name) || "package-info".equals(name);
    assert JModifier.isTypeModifier(modifier);
    assert JModifier.isValidTypeModifier(modifier);
    this.modifier = modifier;
    this.enclosingPackage = enclosingPackage;
    this.enclosingPackage.addType(this);
    this.loader = loader;
    location = loader.getLocation(this);
  }

  public void setModifier(int modifier) {
    this.modifier = modifier;
  }

  /**
   * Adds a field to this type.
   */
  public void addField(@Nonnull JField field) {
    assert field.getEnclosingType() == this;
    assert getPhantomField(field.getName(), field.getType(), field.getId().getKind()) == null;
    fields.add(field);
  }

  @Override
  @CheckForNull
  public <T extends Marker> T getMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.getMarker(cls);
  }

  @Override
  @Nonnull
  public Collection<Marker> getAllMarkers() {
    loader.ensureMarkers(this);
    return super.getAllMarkers();
  }

  @Override
  public <T extends Marker> boolean containsMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.containsMarker(cls);
  }

  @Override
  public <T extends Marker> T removeMarker(@Nonnull Class<T> cls) {
    loader.ensureMarker(this, cls);
    return super.removeMarker(cls);
  }

  /**
   * Adds an implemented interface to this type.
   */
  public void addImplements(JInterface superInterface) {
    superInterfaces.add(superInterface);
  }

  public void removeImplements(int index) {
    superInterfaces.remove(index);
  }

  public void setImplements(@Nonnull List<JInterface> superInterfaces) {
    this.superInterfaces = superInterfaces;
  }

  @Override
  @Nonnull
  public List<JInterface> getImplements() {
    loader.ensureHierarchy(this);
    return super.getImplements();
  }

  @Override
  public void setEnclosingPackage(@CheckForNull JPackage enclosingPackage) {
    assert enclosingPackage != null;
    this.enclosingPackage = enclosingPackage;
  }

  /**
   * Adds a method to this type.
   */
  public void addMethod(JMethod method) {
    assert method.getEnclosingType() == this;
    assert getPhantomMethod(method.getName(), method.getMethodId().getParamTypes(),
        method.getMethodId().getKind()) == null;
    methods.add(method);
  }

  /**
   * Returns the type which encloses this type.
   *
   * @return The enclosing type. May be {@code null}.
   */
  public JClassOrInterface getEnclosingType() {
    loader.ensureEnclosing(this);
    return enclosingType;
  }

  public JSession getSession() {
    return enclosingPackage.getSession();
  }

  /**
   * Returns this type's fields;does not include fields defined in a super type
   * unless they are overridden by this type.
   */
  public List<JField> getFields() {
    loader.ensureFields(this);
    return fields;
  }

  @Nonnull
  public List<JField> getFields(@Nonnull String fieldName) {
    loader.ensureFields(this, fieldName);
    List<JField> fieldsFound = new ArrayList<JField>();
    for (JField field : getFields()) {
      if (field.getName().equals(fieldName)) {
        fieldsFound.add(field);
      }
    }
    return fieldsFound;
  }

  @Nonnull
  @Override
  public Collection<JFieldId> getPhantomFields() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(phantomFields);
  }

  @Override
  @Nonnull
  public JPackage getEnclosingPackage() {
    return enclosingPackage;
  }

  /**
   * Returns this type's declared methods; does not include methods defined in a
   * super type unless they are overridden by this type.
   */
  public List<JMethod> getMethods() {
    loader.ensureMethods(this);
    return methods;
  }

  @Nonnull
  @Override
  public Collection<JMethodId> getPhantomMethods() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(phantomMethods);
  }

  /**
   * Returns the {@link JMethod} with the signature {@code signature} declared for this type.
   *
   * @return Returns the matching method if any, throws a {@link JMethodLookupException} otherwise.
   */
  @Nonnull
  public JMethod getMethod(@Nonnull String name, @Nonnull JType returnType,
      @Nonnull List<? extends JType> args) throws JMethodLookupException {
    loader.ensureMethod(this, name, args, returnType);
    for (JMethod m : methods) {
      if (m.getMethodId().equals(name, args) && (m.getType() == returnType)) {
        return m;
      }
    }
    throw new JMethodWithReturnLookupException(this, name, args, returnType);
  }

  /**
   * Returns the {@link JMethod} with the signature {@code signature} declared for this type.
   *
   * @return Returns the matching method if any, throws a {@link JMethodLookupException} otherwise.
   */
  public JMethod getMethod(@Nonnull String name, @Nonnull JType returnType,
      @Nonnull JType... args) throws JMethodLookupException {
    return (getMethod(name, returnType, Arrays.asList(args)));
  }

  /**
   * Returns this type's super class, or <code>null</code> if this type is
   * {@link Object} or an interface.
   */
  @CheckForNull
  public JClass getSuperClass() {
    return null;
  }

  @Override
  public boolean isExternal() {
    return isExternal;
  }

  /**
   * Removes the field at the specified index.
   */
  public void removeField(int i) {
    assert !isExternal() : "External types can not be modiified.";
    fields.remove(i);
  }

  /**
   * Removes the method at the specified index.
   */
  public void removeMethod(int i) {
    assert !isExternal() : "External types can not be modiified.";
    methods.remove(i);
  }

  /**
   * Sets the type which encloses this types.
   *
   * @param enclosingType May be {@code null}.
   */
  public void setEnclosingType(JClassOrInterface enclosingType) {
    this.enclosingType = enclosingType;
  }

  public void setExternal(boolean isExternal) {
    this.isExternal = isExternal;
  }

  public int getModifier() {
    loader.ensureModifier(this);
    return modifier;
  }

  public boolean isPublic() {
    return JModifier.isPublic(getModifier());
  }

  public boolean isProtected() {
    return JModifier.isProtected(getModifier());
  }

  public boolean isPrivate() {
    return JModifier.isPrivate(getModifier());
  }

  public boolean isStatic() {
    return JModifier.isStatic(getModifier());
  }

  public boolean isStrictfp() {
    return JModifier.isStrictfp(getModifier());
  }

  @Override
  public boolean isAbstract() {
    return JModifier.isAbstract(getModifier());
  }

  public void setAbstract() {
    modifier = getModifier() | JModifier.ABSTRACT;
  }

  @Override
  public boolean isFinal() {
    return JModifier.isFinal(getModifier());
  }

  public void setFinal() {
    modifier = getModifier() | JModifier.FINAL;
  }

  @Override
  public void addAnnotation(@Nonnull JAnnotationLiteral annotation) {
    annotations.addAnnotation(annotation);
  }

  @Override
  @CheckForNull
  public JAnnotationLiteral getAnnotation(@Nonnull JAnnotation annotationType) {
    loader.ensureAnnotation(this, annotationType);
    return annotations.getAnnotation(annotationType);
  }

  @Override
  @Nonnull
  public Collection<JAnnotationLiteral> getAnnotations() {
    loader.ensureAnnotations(this);
    return annotations.getAnnotations();
  }

  @Nonnull
  public List<JClassOrInterface> getMemberTypes() {
    loader.ensureInners(this);
    return Jack.getUnmodifiableCollections().getUnmodifiableList(inners);
  }

  public void addMemberType(@Nonnull JClassOrInterface jDeclaredType) {
    inners.add(jDeclaredType);
  }

  public void removeMemberType(@Nonnull JClassOrInterface jDeclaredType) {
    int index = inners.indexOf(jDeclaredType);
    if (index != -1) {
      inners.remove(index);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(inners, existingNode, (JClassOrInterface) newNode, transformation)) {
      if (!annotations.transform(existingNode, newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Nonnull
  @Override
  public JMethodId getMethodId(@Nonnull String name, @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind)
      throws JMethodLookupException {
    assert !(name.contains("(") || name.contains(")"));
    loader.ensureMethods(this);
    for (JMethod method : methods) {
      JMethodId id = method.getMethodId();
      if (id.equals(name, argsType)) {
        return id;
      }
    }

    for (JInterface jType : getImplements()) {
      try {
        return jType.getMethodId(name, argsType, kind);
      } catch (JMethodLookupException e) {
        // search next
      }
    }
    JClass superClass = getSuperClass();
    if (superClass != null) {
      try {
        return superClass.getMethodId(name, argsType, kind);
      } catch (JMethodLookupException e) {
        // let the following exception be thrown
      }
    }

    throw new JMethodIdLookupException(this, name, argsType);
  }

  @Nonnull
  @Override
  public JMethodId getOrCreateMethodId(@Nonnull String name,
      @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind) {
    try {
      return getMethodId(name, argsType, kind);
    } catch (JMethodLookupException e) {
      JMethodId id = getPhantomMethod(name, argsType, kind);

      if (id == null) {
        id = new JMethodId(name, argsType, kind);
        phantomMethods.add(id);
      }
      return id;
    }
  }

  @Override
  @Nonnull
  public JFieldId getOrCreateFieldId(@Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    try {
      return getFieldId(name, type, kind);
    } catch (JFieldLookupException e) {
      synchronized (phantomFields) {
        JFieldId id = getPhantomField(name, type, kind);
        if (id == null) {
          id = new JFieldId(name, type, kind);
          phantomFields.add(id);
        }
        return id;
      }
    }
  }

  @Override
  @Nonnull
  public JFieldId getFieldId(
      @Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    loader.ensureFields(this);
    for (JField field : fields) {
      JFieldId id = field.getId();
      if (id.equals(name, type, kind)) {
        return id;
      }
    }

    for (JInterface jType : getImplements()) {
      try {
        return jType.getFieldId(name, type, kind);
      } catch (JFieldLookupException e) {
        // search next
      }
    }
    JClass superClass = getSuperClass();
    if (superClass != null) {
      try {
        return superClass.getFieldId(name, type, kind);
      } catch (JFieldLookupException e) {
        // let the following exception be thrown
      }
    }

    throw new JFieldLookupException(this, name, type);
  }

  @CheckForNull
  private JMethodId getPhantomMethod(@Nonnull String name, @Nonnull List<? extends JType> argsType,
      @Nonnull MethodKind kind) {
    synchronized (phantomMethods) {
      for (JMethodId id : phantomMethods) {
        if (id.equals(name, argsType)) {
          assert id.getKind() == kind;
          return id;
        }
      }
    }
    return null;
  }

  @CheckForNull
  private JFieldId getPhantomField(@Nonnull String name, @Nonnull JType type,
      @Nonnull FieldKind kind) {
    synchronized (phantomFields) {
      for (JFieldId id : phantomFields) {
        if (id.equals(name, type, kind)) {
          return id;
        }
      }
    }
    return null;
  }

  @Nonnull
  public ClassOrInterfaceLoader getLoader() {
    return loader;
  }

  @Override
  @CheckForNull
  public JPrimitiveType getWrappedType() {
    return getWrappedType(this);
  }

  @Nonnull
  public Location getLocation() {
    return location;
  }

}
