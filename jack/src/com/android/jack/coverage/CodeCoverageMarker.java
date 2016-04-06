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

package com.android.jack.coverage;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A marker holding coverage information for a class.
 */
@Description("A marker containing code coverage information.")
@ValidOn(JDefinedClassOrInterface.class)
public class CodeCoverageMarker implements Marker {

  /**
   * This tag means that the {@link CodeCoverageMarker} is just initialized but no probe
   * has been computed yet.
   */
  @Description("The CodeCoverageMarker is initialized.")
  @Name("CodeCoverageMarker.Initialized")
  public static final class Initialized implements Tag {
  }

  /**
   * This tag means that the analysis filled the {@link CodeCoverageMarker} with probes and
   * that the class is ready to be instrumented.
   */
  @Description("The CodeCoverageMarker is analyzed.")
  @Name("CodeCoverageMarker.Analyzed")
  public static final class Analyzed implements Tag {
  }

  /**
   * This tag means that the class has been instrumented and that the {@link CodeCoverageMarker}
   * is completely initialized with all required information for code coverage.
   */
  @Description("The CodeCoverageMarker is complete.")
  @Name("CodeCoverageMarker.Complete")
  public static final class Complete implements Tag {
  }

  public static final long INVALID_CLASS_ID = -1L;

  // Access to this list must be thread-safe.
  @Nonnull
  private final List<ProbeDescription> probes;

  /**
   * The unique identifier of a class for code coverage. It must distinguish different classes
   * with the same name (mainly for class loader).
   */
  private long classId;

  /**
   * The coverage initialization method added to the class. This is used to exclude this method
   * from the coverage metadata file.
   */
  @CheckForNull
  private JMethod initMethod;

  /**
   * Creates a new probe for the given {@link JMethod}.
   *
   * This method is synchronized to support multithreading during code coverage analysis.
   *
   * @param method
   *        the method the created probe belongs to.
   * @return a new {@link ProbeDescription} instance
   */
  @Nonnull
  public synchronized ProbeDescription createProbe(@Nonnull JMethod method) {
    ProbeDescription p = new ProbeDescription(probes.size(), method);
    probes.add(p);
    return p;
  }

  public CodeCoverageMarker() {
    this(INVALID_CLASS_ID, new ArrayList<ProbeDescription>());
  }

  private CodeCoverageMarker(long classId, @Nonnull List<ProbeDescription> probes) {
    this.classId = classId;
    this.probes = probes;
  }

  public long getClassId() {
    return classId;
  }

  public void setClassId(long classId) {
    this.classId = classId;
  }

  @Nonnull
  public List<ProbeDescription> getProbes() {
    return probes;
  }

  @Nonnegative
  public int getNumberOfProbes() {
    return probes.size();
  }

  @CheckForNull
  public JMethod getInitMethod() {
    return initMethod;
  }

  public void setInitMethod(@Nonnull JMethod initMethod) {
    this.initMethod = initMethod;
  }

  @Override
  public Marker cloneIfNeeded() {
    CodeCoverageMarker marker = new CodeCoverageMarker(classId, probes);
    if (initMethod != null) {
      marker.setInitMethod(initMethod);
    }
    return marker;
  }
}
