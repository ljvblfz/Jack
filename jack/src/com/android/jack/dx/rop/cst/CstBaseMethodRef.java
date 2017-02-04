/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.jack.dx.rop.cst;

import com.android.jack.dx.rop.type.Prototype;
import com.android.jack.dx.rop.type.Type;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for constants of "methodish" type.
 *
 * <p><b>Note:</b> As a {@code TypeBearer}, this class bears the return type
 * of the method.</p>
 */
public abstract class CstBaseMethodRef extends CstMemberRef {
  /** {@code non-null;} the raw prototype for this method */
  @Nonnull
  private final Prototype prototype;

  /**
   * {@code null-ok;} the prototype for this method taken to be an instance
   * method, or {@code null} if not yet calculated
   */
  @CheckForNull
  private Prototype instancePrototype;

  /**
   * Constructs an instance.
   *
   * @param definingClass {@code non-null;} the type of the defining class
   * @param name {@code non-null;} the member reference name
   * @param prototype {@code non-null;} the member reference prototype
   */
  /* package */ CstBaseMethodRef(@Nonnull Type definingClass, @Nonnull CstString name,
      @Nonnull Prototype prototype) {
    super(definingClass, name);
    this.prototype = prototype;
    this.instancePrototype = null;
  }

  /**
   * Gets the raw prototype of this method. This doesn't include a
   * {@code this} argument.
   *
   * @return {@code non-null;} the method prototype
   */
  @Nonnull
  public final Prototype getPrototype() {
    return prototype;
  }

  /**
   * Gets the prototype of this method as either a
   * {@code static} or instance method. In the case of a
   * {@code static} method, this is the same as the raw
   * prototype. In the case of an instance method, this has an
   * appropriately-typed {@code this} argument as the first
   * one.
   *
   * @param isStatic whether the method should be considered static
   * @return {@code non-null;} the method prototype
   */
  @Nonnull
  public final Prototype getPrototype(boolean isStatic) {
    if (isStatic) {
      return prototype;
    } else {
      if (instancePrototype == null) {
        Type thisType = getDefiningClass();
        instancePrototype = prototype.withFirstParameter(thisType);
      }
      return instancePrototype;
    }
  }

  /** {@inheritDoc} */
  @Override
  protected final int compareTo0(Constant other) {
    int cmp = super.compareTo0(other);

    if (cmp != 0) {
      return cmp;
    }

    CstBaseMethodRef otherMethod = (CstBaseMethodRef) other;
    return prototype.compareTo(otherMethod.prototype);
  }

  /**
   * {@inheritDoc}
   *
   * In this case, this method returns the <i>return type</i> of this method.
   *
   * @return {@code non-null;} the method's return type
   */
  @Override
  @Nonnull
  public final Type getType() {
    return prototype.getReturnType();
  }

  /**
   * Gets the number of words of parameters required by this
   * method's descriptor. Since instances of this class have no way
   * to know if they will be used in a {@code static} or
   * instance context, one has to indicate this explicitly as an
   * argument. This method is just a convenient shorthand for
   * {@code getPrototype().getParameterTypes().getWordCount()},
   * plus {@code 1} if the method is to be treated as an
   * instance method.
   *
   * @param isStatic whether the method should be considered static
   * @return {@code >= 0;} the argument word count
   */
  public final int getParameterWordCount(boolean isStatic) {
    return getPrototype(isStatic).getParameterTypes().getWordCount();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(Object other) {
    if ((other == null) || (getClass() != other.getClass())) {
      return false;
    }

    CstBaseMethodRef otherRef = (CstBaseMethodRef) other;
    return getDefiningClass().equals(otherRef.getDefiningClass())
        && getName().equals(otherRef.getName()) && prototype.equals(otherRef.prototype);
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return ((getDefiningClass().hashCode() * 31) + getName().hashCode() * 31)
        + getType().hashCode();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public final String toHuman() {
    return getDefiningClass().toHuman() + '.' + getName().toHuman() + ':' + prototype.toString();
  }
}
